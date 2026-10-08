package com.banfico.banking.controller;

import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.OpenBankingDataResponse;
import com.banfico.banking.dto.response.TransactionResponse;
import com.banfico.banking.entity.Consent;
import com.banfico.banking.entity.ConsentAuditLog;
import com.banfico.banking.exception.ConsentAccessDeniedException;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.repository.ConsentAuditLogRepository;
import com.banfico.banking.service.AccountService;
import com.banfico.banking.service.ConsentService;
import com.banfico.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/open-banking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Open Banking Data Access (Data Consumer / Provider)", description = "Regulated AIS Data Sharing endpoint strictly validated against active Open Banking Consents")
public class OpenBankingDataController {

    private final ConsentService consentService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ConsentAuditLogRepository auditLogRepository;

    @GetMapping("/consents/{consentReference}/data")
    @Operation(summary = "Access Regulated Financial Data via Consent", 
               description = "Data Consumer (TPP) fetches customer financial data. Bank validates active consent, scope, and expiry before release.")
    public ResponseEntity<ApiResponse<OpenBankingDataResponse>> getOpenBankingData(
            @PathVariable String consentReference,
            @RequestParam(required = false, defaultValue = "ALL") String resource) {
        
        log.info("Data access attempt by TPP for consent reference: {}, resource: {}", consentReference, resource);
        
        // 1. Validate Consent State & Expiry via Consent Manager Engine
        Consent consent = consentService.getActiveConsentEntityByReference(consentReference);
        String scopes = consent.getScopes() != null ? consent.getScopes().toUpperCase() : "";
        Long customerId = consent.getCustomer().getId();

        List<AccountResponse> accounts = new ArrayList<>();
        List<TransactionResponse> transactions = new ArrayList<>();

        // 2. Enforce Resource Scopes
        boolean canReadAccounts = scopes.contains("READ_ACCOUNTS") || scopes.contains("ALL");
        boolean canReadBalances = scopes.contains("READ_BALANCES") || scopes.contains("ALL");
        boolean canReadTransactions = scopes.contains("READ_TRANSACTIONS") || scopes.contains("ALL");

        if (!canReadAccounts && !canReadBalances && !canReadTransactions) {
            logScopeViolation(consentReference, customerId, consent.getTppClientId(), "NONE", scopes);
            throw new ConsentAccessDeniedException("Scope violation: Consent does not grant permission to read any financial resources. Authorized scopes: [" + scopes + "]");
        }

        // Fetch Accounts / Balances if scoped
        if (canReadAccounts || canReadBalances) {
            if (consent.getBankAccount() != null) {
                AccountResponse acc = accountService.getAccountById(consent.getBankAccount().getId());
                if (!canReadBalances) {
                    acc.setBalance(null); // Mask balance if READ_BALANCES is not granted
                }
                accounts.add(acc);
            } else {
                List<AccountResponse> customerAccounts = accountService.getAccountsByCustomerId(customerId);
                if (!canReadBalances) {
                    customerAccounts.forEach(a -> a.setBalance(null));
                }
                accounts.addAll(customerAccounts);
            }
        }

        // Fetch Transactions if scoped
        if (canReadTransactions) {
            if (consent.getBankAccount() != null) {
                transactions.addAll(transactionService.getTransactionsByAccountId(consent.getBankAccount().getId()));
            } else {
                for (AccountResponse acc : accountService.getAccountsByCustomerId(customerId)) {
                    transactions.addAll(transactionService.getTransactionsByAccountId(acc.getId()));
                }
            }
        }

        // 3. Log Data Access Event into Audit Trail
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(consentReference)
                .customerId(customerId)
                .action("DATA_ACCESS_PERMITTED")
                .actorRole("TPP")
                .actorIdentifier(consent.getTppClientId())
                .details("TPP accessed customer data under authorized scopes [" + scopes + "]. Released: " + accounts.size() + " accounts, " + transactions.size() + " transactions.")
                .build());

        OpenBankingDataResponse response = OpenBankingDataResponse.builder()
                .consentReference(consentReference)
                .tppClientId(consent.getTppClientId())
                .purpose(consent.getPurpose())
                .authorizedScopes(consent.getScopes())
                .customerId(customerId)
                .customerName(consent.getCustomer().getName())
                .validUntil(consent.getValidUntil())
                .accessTimestamp(LocalDateTime.now())
                .accounts(accounts)
                .transactions(transactions)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Financial data retrieved successfully under authorized consent", response));
    }

    @GetMapping("/consents/{consentReference}/accounts")
    @Operation(summary = "Get Accounts under Consent (Scope: READ_ACCOUNTS)")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByConsent(@PathVariable String consentReference) {
        Consent consent = consentService.getActiveConsentEntityByReference(consentReference);
        String scopes = consent.getScopes() != null ? consent.getScopes().toUpperCase() : "";

        if (!scopes.contains("READ_ACCOUNTS") && !scopes.contains("ALL")) {
            logScopeViolation(consentReference, consent.getCustomer().getId(), consent.getTppClientId(), "READ_ACCOUNTS", scopes);
            throw new ConsentAccessDeniedException("Scope violation: Consent " + consentReference + " does not grant READ_ACCOUNTS permission. Granted: [" + scopes + "]");
        }

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(consent.getCustomer().getId());
        if (!scopes.contains("READ_BALANCES") && !scopes.contains("ALL")) {
            accounts.forEach(a -> a.setBalance(null));
        }

        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(consentReference)
                .customerId(consent.getCustomer().getId())
                .action("DATA_ACCESS_PERMITTED")
                .actorRole("TPP")
                .actorIdentifier(consent.getTppClientId())
                .details("TPP read accounts under scope READ_ACCOUNTS")
                .build());

        return ResponseEntity.ok(ApiResponse.ok("Accounts retrieved successfully", accounts));
    }

    @GetMapping("/consents/{consentReference}/balances")
    @Operation(summary = "Get Balances under Consent (Scope: READ_BALANCES)")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getBalancesByConsent(@PathVariable String consentReference) {
        Consent consent = consentService.getActiveConsentEntityByReference(consentReference);
        String scopes = consent.getScopes() != null ? consent.getScopes().toUpperCase() : "";

        if (!scopes.contains("READ_BALANCES") && !scopes.contains("ALL")) {
            logScopeViolation(consentReference, consent.getCustomer().getId(), consent.getTppClientId(), "READ_BALANCES", scopes);
            throw new ConsentAccessDeniedException("Scope violation: Consent " + consentReference + " does not grant READ_BALANCES permission. Granted: [" + scopes + "]");
        }

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(consent.getCustomer().getId());

        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(consentReference)
                .customerId(consent.getCustomer().getId())
                .action("DATA_ACCESS_PERMITTED")
                .actorRole("TPP")
                .actorIdentifier(consent.getTppClientId())
                .details("TPP read balances under scope READ_BALANCES")
                .build());

        return ResponseEntity.ok(ApiResponse.ok("Balances retrieved successfully", accounts));
    }

    @GetMapping("/consents/{consentReference}/transactions")
    @Operation(summary = "Get Transactions under Consent (Scope: READ_TRANSACTIONS)")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByConsent(@PathVariable String consentReference) {
        Consent consent = consentService.getActiveConsentEntityByReference(consentReference);
        String scopes = consent.getScopes() != null ? consent.getScopes().toUpperCase() : "";

        if (!scopes.contains("READ_TRANSACTIONS") && !scopes.contains("ALL")) {
            logScopeViolation(consentReference, consent.getCustomer().getId(), consent.getTppClientId(), "READ_TRANSACTIONS", scopes);
            throw new ConsentAccessDeniedException("Scope violation: Consent " + consentReference + " does not grant READ_TRANSACTIONS permission. Granted: [" + scopes + "]");
        }

        List<TransactionResponse> txs = new ArrayList<>();
        if (consent.getBankAccount() != null) {
            txs.addAll(transactionService.getTransactionsByAccountId(consent.getBankAccount().getId()));
        } else {
            for (AccountResponse acc : accountService.getAccountsByCustomerId(consent.getCustomer().getId())) {
                txs.addAll(transactionService.getTransactionsByAccountId(acc.getId()));
            }
        }

        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(consentReference)
                .customerId(consent.getCustomer().getId())
                .action("DATA_ACCESS_PERMITTED")
                .actorRole("TPP")
                .actorIdentifier(consent.getTppClientId())
                .details("TPP read transactions under scope READ_TRANSACTIONS")
                .build());

        return ResponseEntity.ok(ApiResponse.ok("Transactions retrieved successfully", txs));
    }

    private void logScopeViolation(String consentReference, Long customerId, String tppId, String requestedScope, String grantedScopes) {
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(consentReference)
                .customerId(customerId)
                .action("DATA_ACCESS_DENIED")
                .actorRole("TPP")
                .actorIdentifier(tppId)
                .details("Access denied: Scope violation. Requested [" + requestedScope + "] but consent only authorizes [" + grantedScopes + "]")
                .build());
    }
}
