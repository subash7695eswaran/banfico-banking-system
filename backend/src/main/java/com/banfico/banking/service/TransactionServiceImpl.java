package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.TransactionResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.ConsentAuditLog;
import com.banfico.banking.entity.Transaction;
import com.banfico.banking.entity.enums.AccountStatus;
import com.banfico.banking.entity.enums.TransactionStatus;
import com.banfico.banking.entity.enums.TransactionType;
import com.banfico.banking.exception.ConsentAccessDeniedException;
import com.banfico.banking.exception.InsufficientBalanceException;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
import com.banfico.banking.repository.ConsentAuditLogRepository;
import com.banfico.banking.repository.TransactionRepository;
import com.banfico.banking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final ConsentAuditLogRepository auditLogRepository;
    private final DtoMapper dtoMapper;

    @Value("${banking.maker-checker.approval-threshold-inr:50000.00}")
    private BigDecimal approvalThresholdInr;

    @Override
    @Transactional
    public TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request) {
        String currentUser = SecurityUtils.getCurrentUser();
        log.info("Processing {} transaction of amount ₹{} for account id: {} by user: {}",
                request.getTransactionType(), request.getAmount(), accountId, currentUser);

        BankAccount primaryAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (primaryAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account " + primaryAccount.getAccountNumber() + " is not ACTIVE");
        }

        BigDecimal amount = request.getAmount();
        String ref = generateTransactionReference();
        BankAccount sourceAccount = null;
        BankAccount destinationAccount = null;

        // Check if high value operation requiring Checker authorization (Threshold in INR)
        boolean requiresCheckerApproval = (request.getTransactionType() == TransactionType.TRANSFER || 
                                           request.getTransactionType() == TransactionType.WITHDRAWAL) 
                                           && amount.compareTo(approvalThresholdInr) >= 0;

        if (requiresCheckerApproval) {
            if (request.getTransactionType() == TransactionType.TRANSFER) {
                if (request.getDestinationAccountId() == null) {
                    throw new InvalidOperationException("Destination account ID is required for TRANSFER transaction");
                }
                destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Destination bank account not found with id: " + request.getDestinationAccountId()));
                sourceAccount = primaryAccount;
            } else {
                sourceAccount = primaryAccount;
            }

            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient funds for pending authorization. Current balance: ₹" + sourceAccount.getBalance() + ", Requested: ₹" + amount);
            }

            Transaction transaction = Transaction.builder()
                    .transactionReference(ref)
                    .transactionType(request.getTransactionType())
                    .amount(amount)
                    .currency("INR")
                    .initiatedBy(currentUser)
                    .status(TransactionStatus.PENDING_APPROVAL)
                    .description((request.getDescription() != null ? request.getDescription() : "High-value " + request.getTransactionType().name()) + " [Maker: " + currentUser + " - Awaiting Checker Approval]")
                    .sourceAccount(sourceAccount)
                    .destinationAccount(destinationAccount)
                    .build();

            Transaction saved = transactionRepository.save(transaction);
            log.info("Transaction {} marked as PENDING_APPROVAL for Checker review (Threshold: ₹{})", ref, approvalThresholdInr);

            // Record audit event
            auditLogRepository.save(ConsentAuditLog.builder()
                    .consentReference(ref)
                    .customerId(primaryAccount.getCustomer().getId())
                    .action("TRANSACTION_PENDING_APPROVAL")
                    .actorRole(SecurityUtils.hasRole("MAKER") ? "MAKER" : "CUSTOMER")
                    .actorIdentifier(currentUser)
                    .details("Initiated high-value " + request.getTransactionType() + " of ₹" + amount + " requiring Checker dual-authorization")
                    .build());

            return dtoMapper.toTransactionResponse(saved);
        }

        // Standard instant transaction below threshold
        switch (request.getTransactionType()) {
            case DEPOSIT -> {
                destinationAccount = primaryAccount;
                primaryAccount.setBalance(primaryAccount.getBalance().add(amount));
                accountRepository.save(primaryAccount);
                log.info("Deposited ₹{} into account {}. New balance: ₹{}", amount, primaryAccount.getAccountNumber(), primaryAccount.getBalance());
            }
            case WITHDRAWAL -> {
                sourceAccount = primaryAccount;
                if (primaryAccount.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient funds. Current balance: ₹" + primaryAccount.getBalance() + ", Requested: ₹" + amount);
                }
                primaryAccount.setBalance(primaryAccount.getBalance().subtract(amount));
                accountRepository.save(primaryAccount);
                log.info("Withdrew ₹{} from account {}. New balance: ₹{}", amount, primaryAccount.getAccountNumber(), primaryAccount.getBalance());
            }
            case TRANSFER -> {
                sourceAccount = primaryAccount;
                if (request.getDestinationAccountId() == null) {
                    throw new InvalidOperationException("Destination account ID is required for TRANSFER transaction");
                }
                if (request.getDestinationAccountId().equals(accountId)) {
                    throw new InvalidOperationException("Cannot transfer funds to the same bank account");
                }

                destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Destination bank account not found with id: " + request.getDestinationAccountId()));

                if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
                    throw new InvalidOperationException("Destination account " + destinationAccount.getAccountNumber() + " is not ACTIVE");
                }

                if (sourceAccount.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient funds. Current balance: ₹" + sourceAccount.getBalance() + ", Transfer amount: ₹" + amount);
                }

                // Atomic balance debit & credit
                sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
                destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
                accountRepository.save(sourceAccount);
                accountRepository.save(destinationAccount);
                log.info("Transferred ₹{} from {} to {}", amount, sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber());
            }
        }

        Transaction transaction = Transaction.builder()
                .transactionReference(ref)
                .transactionType(request.getTransactionType())
                .amount(amount)
                .currency("INR")
                .initiatedBy(currentUser)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription() != null ? request.getDescription() : request.getTransactionType().name() + " of ₹" + amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // Record audit event
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(ref)
                .customerId(primaryAccount.getCustomer().getId())
                .action("TRANSACTION_COMPLETED")
                .actorRole(SecurityUtils.hasRole("MAKER") ? "MAKER" : "CUSTOMER")
                .actorIdentifier(currentUser)
                .details("Completed " + request.getTransactionType() + " of ₹" + amount)
                .build());

        return dtoMapper.toTransactionResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse approveTransaction(Long id, String checkerName) {
        String effectiveChecker = (checkerName != null && !checkerName.trim().isEmpty()) 
                ? checkerName.trim() 
                : SecurityUtils.getCurrentUser();

        log.info("Checker {} approving transaction id: {}", effectiveChecker, id);
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // 1. Role Authorization Check: Maker is forbidden from approving
        if (SecurityUtils.hasRole("MAKER") && !SecurityUtils.isChecker()) {
            throw new ConsentAccessDeniedException("Access Denied: Users with MAKER role are forbidden from approving transactions. CHECKER role required.");
        }

        // 2. Maker/Checker Separation of Duties Check
        if (tx.getInitiatedBy() != null && tx.getInitiatedBy().equalsIgnoreCase(effectiveChecker)) {
            throw new ConsentAccessDeniedException("Maker/Checker separation of duties violation: Maker [" + tx.getInitiatedBy() + "] cannot approve their own transaction.");
        }

        // 3. State Check
        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new InvalidOperationException("Transaction " + tx.getTransactionReference() + " is already in status: " + tx.getStatus() + " and cannot be re-approved.");
        }

        BankAccount source = tx.getSourceAccount();
        BankAccount destination = tx.getDestinationAccount();
        BigDecimal amount = tx.getAmount();

        // 3. Atomic Balance Updates
        if (source != null) {
            if (source.getBalance().compareTo(amount) < 0) {
                tx.setStatus(TransactionStatus.FAILED);
                tx.setRejectionReason("Insufficient funds at time of approval");
                transactionRepository.save(tx);
                throw new InsufficientBalanceException("Insufficient funds at time of approval. Balance: ₹" + source.getBalance() + ", Amount: ₹" + amount);
            }
            source.setBalance(source.getBalance().subtract(amount));
            accountRepository.save(source);
        }

        if (destination != null) {
            destination.setBalance(destination.getBalance().add(amount));
            accountRepository.save(destination);
        }

        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setApprovedBy(effectiveChecker);
        tx.setDescription(tx.getDescription() + " [Approved by Checker: " + effectiveChecker + "]");
        Transaction updated = transactionRepository.save(tx);

        // 4. Audit Log
        Long customerId = (source != null && source.getCustomer() != null) ? source.getCustomer().getId() : 1L;
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getTransactionReference())
                .customerId(customerId)
                .action("TRANSACTION_APPROVED")
                .actorRole("CHECKER")
                .actorIdentifier(effectiveChecker)
                .details("Approved and settled high-value transfer of ₹" + amount + " initiated by " + tx.getInitiatedBy())
                .build());

        return dtoMapper.toTransactionResponse(updated);
    }

    @Override
    @Transactional
    public TransactionResponse rejectTransaction(Long id, String reason) {
        String effectiveChecker = SecurityUtils.getCurrentUser();
        log.info("Checker {} rejecting transaction id: {}, reason: {}", effectiveChecker, id, reason);
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new InvalidOperationException("Transaction " + tx.getTransactionReference() + " is already in status: " + tx.getStatus() + " and cannot be rejected.");
        }

        tx.setStatus(TransactionStatus.REJECTED);
        tx.setApprovedBy(effectiveChecker);
        tx.setRejectionReason(reason != null ? reason : "Checker declined operation");
        tx.setDescription(tx.getDescription() + " [Rejected by Checker: " + tx.getRejectionReason() + "]");
        Transaction updated = transactionRepository.save(tx);

        Long customerId = (tx.getSourceAccount() != null && tx.getSourceAccount().getCustomer() != null) 
                ? tx.getSourceAccount().getCustomer().getId() 
                : 1L;
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getTransactionReference())
                .customerId(customerId)
                .action("TRANSACTION_REJECTED")
                .actorRole("CHECKER")
                .actorIdentifier(effectiveChecker)
                .details("Rejected transaction of ₹" + tx.getAmount() + ". Reason: " + tx.getRejectionReason())
                .build());

        return dtoMapper.toTransactionResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getPendingTransactions() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING_APPROVAL).stream()
                .map(dtoMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        log.info("Fetching transaction history for account id: {}", accountId);
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Bank account not found with id: " + accountId);
        }
        return transactionRepository.findByAccountId(accountId).stream()
                .map(dtoMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(dtoMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference) {
        log.info("Fetching transaction with reference: {}", reference);
        Transaction tx = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));
        return dtoMapper.toTransactionResponse(tx);
    }

    private String generateTransactionReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + datePart + "-" + uuidPart;
    }
}
