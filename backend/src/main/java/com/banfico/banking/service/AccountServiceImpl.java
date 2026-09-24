package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateAccountRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.entity.enums.AccountStatus;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
import com.banfico.banking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final BankAccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final DtoMapper dtoMapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating {} account for customer id: {}", request.getAccountType(), request.getCustomerId());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        String generatedAccountNumber = generateUniqueAccountNumber();
        BigDecimal initialBalance = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;

        BankAccount account = BankAccount.builder()
                .accountNumber(generatedAccountNumber)
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        BankAccount saved = accountRepository.save(account);
        log.info("Bank account successfully created with id: {} and number: {}", saved.getId(), saved.getAccountNumber());
        return dtoMapper.toAccountResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.info("Fetching all bank accounts");
        return accountRepository.findAll().stream()
                .map(dtoMapper::toAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        log.info("Fetching bank account with id: {}", accountId);
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));
        return dtoMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Fetching bank account with number: {}", accountNumber);
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with account number: " + accountNumber));
        return dtoMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        log.info("Fetching accounts for customer id: {}", customerId);
        return accountRepository.findByCustomerId(customerId).stream()
                .map(dtoMapper::toAccountResponse)
                .collect(Collectors.toList());
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long numberPart = 10000000000000L + (long) (RANDOM.nextDouble() * 89999999999999L);
            accountNumber = "GB29BNFC" + numberPart;
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
