package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.TransactionResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.Transaction;
import com.banfico.banking.entity.enums.AccountStatus;
import com.banfico.banking.entity.enums.TransactionStatus;
import com.banfico.banking.entity.enums.TransactionType;
import com.banfico.banking.exception.InsufficientBalanceException;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
import com.banfico.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request) {
        log.info("Processing {} transaction of amount {} for account id: {}",
                request.getTransactionType(), request.getAmount(), accountId);

        BankAccount primaryAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (primaryAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account " + primaryAccount.getAccountNumber() + " is not ACTIVE");
        }

        BigDecimal amount = request.getAmount();
        String ref = generateTransactionReference();
        BankAccount sourceAccount = null;
        BankAccount destinationAccount = null;

        switch (request.getTransactionType()) {
            case DEPOSIT -> {
                destinationAccount = primaryAccount;
                primaryAccount.setBalance(primaryAccount.getBalance().add(amount));
                accountRepository.save(primaryAccount);
                log.info("Deposited {} into account {}. New balance: {}", amount, primaryAccount.getAccountNumber(), primaryAccount.getBalance());
            }
            case WITHDRAWAL -> {
                sourceAccount = primaryAccount;
                if (primaryAccount.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient funds. Current balance: " + primaryAccount.getBalance() + ", Requested: " + amount);
                }
                primaryAccount.setBalance(primaryAccount.getBalance().subtract(amount));
                accountRepository.save(primaryAccount);
                log.info("Withdrew {} from account {}. New balance: {}", amount, primaryAccount.getAccountNumber(), primaryAccount.getBalance());
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
                    throw new InsufficientBalanceException("Insufficient funds. Current balance: " + sourceAccount.getBalance() + ", Transfer amount: " + amount);
                }

                // Atomic balance debit & credit
                sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
                destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
                accountRepository.save(sourceAccount);
                accountRepository.save(destinationAccount);
                log.info("Transferred {} from {} to {}", amount, sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber());
            }
        }

        Transaction transaction = Transaction.builder()
                .transactionReference(ref)
                .transactionType(request.getTransactionType())
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription() != null ? request.getDescription() : request.getTransactionType().name() + " of " + amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return dtoMapper.toTransactionResponse(saved);
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
