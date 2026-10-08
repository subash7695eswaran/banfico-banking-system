package com.banfico.banking.config;

import com.banfico.banking.entity.*;
import com.banfico.banking.entity.enums.AccountStatus;
import com.banfico.banking.entity.enums.AccountType;
import com.banfico.banking.entity.enums.ConsentStatus;
import com.banfico.banking.entity.enums.TransactionStatus;
import com.banfico.banking.entity.enums.TransactionType;
import com.banfico.banking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    private final ConsentRepository consentRepository;
    private final ConsentAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Database already contains data, skipping DataInitializer seed.");
            return;
        }

        log.info("Seeding initial Open Banking & Core Banking demo data...");

        // 1. Seed Customers
        Customer subash = Customer.builder()
                .name("Subash Murugan")
                .email("subash@banfico.com")
                .phone("+91 9876543210")
                .build();

        Customer alice = Customer.builder()
                .name("Alice Johnson")
                .email("alice.j@example.com")
                .phone("+44 7700900077")
                .build();

        Customer bob = Customer.builder()
                .name("Bob Smith")
                .email("bob.smith@example.com")
                .phone("+44 7700900088")
                .build();

        customerRepository.saveAll(List.of(subash, alice, bob));

        // 2. Seed Bank Accounts
        BankAccount subashSavings = BankAccount.builder()
                .accountNumber("GB29BNFC10000000000001")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("25000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(subash)
                .build();

        BankAccount subashCurrent = BankAccount.builder()
                .accountNumber("GB29BNFC10000000000002")
                .accountType(AccountType.CURRENT)
                .balance(new BigDecimal("14250.50"))
                .status(AccountStatus.ACTIVE)
                .customer(subash)
                .build();

        BankAccount aliceSavings = BankAccount.builder()
                .accountNumber("GB29BNFC10000000000003")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("8900.00"))
                .status(AccountStatus.ACTIVE)
                .customer(alice)
                .build();

        BankAccount bobCurrent = BankAccount.builder()
                .accountNumber("GB29BNFC10000000000004")
                .accountType(AccountType.CURRENT)
                .balance(new BigDecimal("3500.75"))
                .status(AccountStatus.ACTIVE)
                .customer(bob)
                .build();

        accountRepository.saveAll(List.of(subashSavings, subashCurrent, aliceSavings, bobCurrent));

        // 3. Seed Beneficiaries
        Beneficiary b1 = Beneficiary.builder()
                .beneficiaryName("Alice Johnson")
                .accountNumber("GB29BNFC10000000000003")
                .bankName("Banfico Bank")
                .customer(subash)
                .build();

        Beneficiary b2 = Beneficiary.builder()
                .beneficiaryName("Tech Retailer UK")
                .accountNumber("GB12BARC20202020202020")
                .bankName("Barclays Bank")
                .customer(subash)
                .build();

        Beneficiary b3 = Beneficiary.builder()
                .beneficiaryName("Utility Services")
                .accountNumber("GB99HSBC40404040404040")
                .bankName("HSBC UK")
                .customer(alice)
                .build();

        beneficiaryRepository.saveAll(List.of(b1, b2, b3));

        // 4. Seed Transactions
        Transaction t1 = Transaction.builder()
                .transactionReference("TXN-20260901-0001")
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("25000.00"))
                .status(TransactionStatus.COMPLETED)
                .description("Initial opening balance")
                .destinationAccount(subashSavings)
                .build();

        Transaction t2 = Transaction.builder()
                .transactionReference("TXN-20260905-0002")
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("500.00"))
                .status(TransactionStatus.COMPLETED)
                .description("Transfer to Alice")
                .sourceAccount(subashSavings)
                .destinationAccount(aliceSavings)
                .build();

        Transaction t3 = Transaction.builder()
                .transactionReference("TXN-20260910-0003")
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("15000.00"))
                .status(TransactionStatus.COMPLETED)
                .description("Salary credited")
                .destinationAccount(subashCurrent)
                .build();

        Transaction t4 = Transaction.builder()
                .transactionReference("TXN-20260912-0004")
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("749.50"))
                .status(TransactionStatus.COMPLETED)
                .description("ATM Cash Withdrawal")
                .sourceAccount(subashCurrent)
                .build();

        transactionRepository.saveAll(List.of(t1, t2, t3, t4));

        // 5. Seed Open Banking Consents
        Consent c1 = Consent.builder()
                .consentReference("CONSENT-20260915-001")
                .customer(subash)
                .bankAccount(subashSavings)
                .status(ConsentStatus.AUTHORISED)
                .purpose("Account Information Service - Aggregator App")
                .scopes("READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS")
                .tppClientId("fintech-aggregator-tpp")
                .validUntil(LocalDateTime.now().plusDays(90))
                .build();

        Consent c2 = Consent.builder()
                .consentReference("CONSENT-20260920-002")
                .customer(subash)
                .bankAccount(subashCurrent)
                .status(ConsentStatus.PENDING)
                .purpose("Payment Initiation Service - E-Commerce Checkout")
                .scopes("READ_ACCOUNTS,READ_BALANCES")
                .tppClientId("amazon-pay-tpp")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();

        Consent c3 = Consent.builder()
                .consentReference("CONSENT-20260922-003")
                .customer(alice)
                .bankAccount(aliceSavings)
                .status(ConsentStatus.REJECTED)
                .purpose("Credit Scoring Analysis")
                .scopes("READ_ACCOUNTS,READ_TRANSACTIONS")
                .tppClientId("quick-loan-tpp")
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();

        consentRepository.saveAll(List.of(c1, c2, c3));

        // 6. Seed Consent Audit Logs
        ConsentAuditLog a1 = ConsentAuditLog.builder()
                .consentReference("CONSENT-20260915-001")
                .customerId(subash.getId())
                .action("CONSENT_CREATED")
                .actorRole("CONSENT_REQUESTER")
                .actorIdentifier("fintech-aggregator-tpp")
                .details("Requested scopes: READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS for purpose: Account Information Service - Aggregator App")
                .build();

        ConsentAuditLog a2 = ConsentAuditLog.builder()
                .consentReference("CONSENT-20260915-001")
                .customerId(subash.getId())
                .action("CONSENT_AUTHORISED")
                .actorRole("CUSTOMER")
                .actorIdentifier("subash@banfico.com")
                .details("Customer granted explicit consent for 90 days")
                .build();

        ConsentAuditLog a3 = ConsentAuditLog.builder()
                .consentReference("CONSENT-20260920-002")
                .customerId(subash.getId())
                .action("CONSENT_CREATED")
                .actorRole("CONSENT_REQUESTER")
                .actorIdentifier("amazon-pay-tpp")
                .details("Requested scopes: READ_ACCOUNTS,READ_BALANCES for purpose: Payment Initiation Service - E-Commerce Checkout")
                .build();

        ConsentAuditLog a4 = ConsentAuditLog.builder()
                .consentReference("CONSENT-20260922-003")
                .customerId(alice.getId())
                .action("CONSENT_CREATED")
                .actorRole("CONSENT_REQUESTER")
                .actorIdentifier("quick-loan-tpp")
                .details("Requested scopes: READ_ACCOUNTS,READ_TRANSACTIONS for purpose: Credit Scoring Analysis")
                .build();

        ConsentAuditLog a5 = ConsentAuditLog.builder()
                .consentReference("CONSENT-20260922-003")
                .customerId(alice.getId())
                .action("CONSENT_REJECTED")
                .actorRole("CUSTOMER")
                .actorIdentifier("alice.j@example.com")
                .details("Customer declined third party data access")
                .build();

        auditLogRepository.saveAll(List.of(a1, a2, a3, a4, a5));

        log.info("Open Banking demo data seeded successfully!");
    }
}
