package com.banfico.banking.mapper;

import com.banfico.banking.dto.response.*;
import com.banfico.banking.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .accountCount(customer.getAccounts() != null ? customer.getAccounts().size() : 0)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public AccountResponse toAccountResponse(BankAccount account) {
        if (account == null) return null;
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .customerId(account.getCustomer() != null ? account.getCustomer().getId() : null)
                .customerName(account.getCustomer() != null ? account.getCustomer().getName() : null)
                .customerEmail(account.getCustomer() != null ? account.getCustomer().getEmail() : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        if (transaction == null) return null;
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .currency(transaction.getCurrency() != null ? transaction.getCurrency() : "INR")
                .initiatedBy(transaction.getInitiatedBy())
                .approvedBy(transaction.getApprovedBy())
                .rejectionReason(transaction.getRejectionReason())
                .sourceAccountId(transaction.getSourceAccount() != null ? transaction.getSourceAccount().getId() : null)
                .sourceAccountNumber(transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountNumber() : null)
                .destinationAccountId(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getId() : null)
                .destinationAccountNumber(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountNumber() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public BeneficiaryResponse toBeneficiaryResponse(Beneficiary beneficiary) {
        if (beneficiary == null) return null;
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .accountNumber(beneficiary.getAccountNumber())
                .bankName(beneficiary.getBankName())
                .customerId(beneficiary.getCustomer() != null ? beneficiary.getCustomer().getId() : null)
                .customerName(beneficiary.getCustomer() != null ? beneficiary.getCustomer().getName() : null)
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }

    public ConsentResponse toConsentResponse(Consent consent) {
        if (consent == null) return null;
        return ConsentResponse.builder()
                .id(consent.getId())
                .consentReference(consent.getConsentReference())
                .customerId(consent.getCustomer() != null ? consent.getCustomer().getId() : null)
                .customerName(consent.getCustomer() != null ? consent.getCustomer().getName() : null)
                .customerEmail(consent.getCustomer() != null ? consent.getCustomer().getEmail() : null)
                .accountId(consent.getBankAccount() != null ? consent.getBankAccount().getId() : null)
                .accountNumber(consent.getBankAccount() != null ? consent.getBankAccount().getAccountNumber() : null)
                .status(consent.getStatus())
                .purpose(consent.getPurpose())
                .scopes(consent.getScopes())
                .tppClientId(consent.getTppClientId())
                .validUntil(consent.getValidUntil())
                .revokedAt(consent.getRevokedAt())
                .revocationReason(consent.getRevocationReason())
                .createdAt(consent.getCreatedAt())
                .updatedAt(consent.getUpdatedAt())
                .build();
    }

    public ConsentAuditLogResponse toConsentAuditLogResponse(ConsentAuditLog log) {
        if (log == null) return null;
        return ConsentAuditLogResponse.builder()
                .id(log.getId())
                .consentReference(log.getConsentReference())
                .customerId(log.getCustomerId())
                .action(log.getAction())
                .actorRole(log.getActorRole())
                .actorIdentifier(log.getActorIdentifier())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
