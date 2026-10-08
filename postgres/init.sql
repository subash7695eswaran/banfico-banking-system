-- =====================================================================
-- Banfico Mini Banking / Open Banking Consent Management System
-- Initial Database Schema and Seed Data
-- =====================================================================

-- 1. Create Enums (or check constraints)
-- Note: Spring JPA will map strings / enums to VARCHAR columns with CHECK constraints

-- 2. Create Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. Create Bank Accounts Table
CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(34) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'CHECKING')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_positive_balance CHECK (balance >= 0)
);

-- 4. Create Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_reference VARCHAR(64) NOT NULL UNIQUE,
    transaction_type VARCHAR(20) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    description VARCHAR(255),
    initiated_by VARCHAR(100),
    approved_by VARCHAR(100),
    rejection_reason VARCHAR(255),
    source_account_id BIGINT REFERENCES bank_accounts(id),
    destination_account_id BIGINT REFERENCES bank_accounts(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_tx_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT chk_tx_status CHECK (status IN ('PENDING', 'PENDING_APPROVAL', 'COMPLETED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

-- 5. Create Beneficiaries Table
CREATE TABLE IF NOT EXISTS beneficiaries (
    id BIGSERIAL PRIMARY KEY,
    beneficiary_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(34) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. Create Open Banking Consents Table
CREATE TABLE IF NOT EXISTS consents (
    id BIGSERIAL PRIMARY KEY,
    consent_reference VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    bank_account_id BIGINT REFERENCES bank_accounts(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    purpose VARCHAR(255) NOT NULL,
    scopes VARCHAR(255) NOT NULL DEFAULT 'READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS',
    tpp_client_id VARCHAR(100) NOT NULL DEFAULT 'tpp-fintech-app',
    valid_until TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    revocation_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_consent_status CHECK (status IN ('PENDING', 'AUTHORISED', 'REJECTED', 'REVOKED', 'EXPIRED'))
);

-- 7. Create Consent Audit Logs Table (Full Consent Lifecycle Tracking)
CREATE TABLE IF NOT EXISTS consent_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    consent_reference VARCHAR(64) NOT NULL,
    customer_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    actor_identifier VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for optimal lookup performance
CREATE INDEX IF NOT EXISTS idx_accounts_customer ON bank_accounts(customer_id);
CREATE INDEX IF NOT EXISTS idx_tx_source ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_tx_destination ON transactions(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_tx_created ON transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer ON beneficiaries(customer_id);
CREATE INDEX IF NOT EXISTS idx_consents_customer ON consents(customer_id);
CREATE INDEX IF NOT EXISTS idx_consents_status ON consents(status);
CREATE INDEX IF NOT EXISTS idx_consent_audit_ref ON consent_audit_logs(consent_reference);
CREATE INDEX IF NOT EXISTS idx_consent_audit_cust ON consent_audit_logs(customer_id);

-- =====================================================================
-- Seed Initial Demo Data (for testing & evaluations)
-- =====================================================================

INSERT INTO customers (id, name, email, phone, created_at, updated_at)
VALUES 
(1, 'Subash Murugan', 'subash@banfico.com', '+91 9876543210', NOW(), NOW()),
(2, 'Alice Johnson', 'alice.j@example.com', '+91 9876543211', NOW(), NOW()),
(3, 'Bob Smith', 'bob.smith@example.com', '+91 9876543212', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO bank_accounts (id, account_number, account_type, balance, status, customer_id, created_at, updated_at)
VALUES 
(1, 'IN29BNFC10000000000001', 'SAVINGS', 250000.00, 'ACTIVE', 1, NOW(), NOW()),
(2, 'IN29BNFC10000000000002', 'CURRENT', 142500.50, 'ACTIVE', 1, NOW(), NOW()),
(3, 'IN29BNFC10000000000003', 'SAVINGS', 89000.00, 'ACTIVE', 2, NOW(), NOW()),
(4, 'IN29BNFC10000000000004', 'CURRENT', 35000.75, 'ACTIVE', 3, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO beneficiaries (id, beneficiary_name, account_number, bank_name, customer_id, created_at)
VALUES 
(1, 'Alice Johnson', 'IN29BNFC10000000000003', 'Banfico Bank', 1, NOW()),
(2, 'Tech Retailer India', 'IN12HDFC20202020202020', 'HDFC Bank', 1, NOW()),
(3, 'Utility Services', 'IN99ICIC40404040404040', 'ICICI Bank', 2, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO transactions (id, transaction_reference, transaction_type, amount, currency, status, description, initiated_by, approved_by, source_account_id, destination_account_id, created_at)
VALUES 
(1, 'TXN-20260901-0001', 'DEPOSIT', 250000.00, 'INR', 'COMPLETED', 'Initial opening balance', 'system', NULL, NULL, 1, NOW() - INTERVAL '15 days'),
(2, 'TXN-20260905-0002', 'TRANSFER', 5000.00, 'INR', 'COMPLETED', 'Transfer to Alice', 'subash@banfico.com', NULL, 1, 3, NOW() - INTERVAL '10 days'),
(3, 'TXN-20260910-0003', 'DEPOSIT', 150000.00, 'INR', 'COMPLETED', 'Salary credited', 'system', NULL, NULL, 2, NOW() - INTERVAL '5 days'),
(4, 'TXN-20260912-0004', 'WITHDRAWAL', 7500.00, 'INR', 'COMPLETED', 'ATM Cash Withdrawal', 'subash@banfico.com', NULL, 2, NULL, NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO consents (id, consent_reference, customer_id, bank_account_id, status, purpose, scopes, tpp_client_id, valid_until, created_at, updated_at)
VALUES 
(1, 'CONSENT-20260915-001', 1, 1, 'AUTHORISED', 'Account Information Service - Aggregator App', 'READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS', 'fintech-aggregator-tpp', NOW() + INTERVAL '90 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(2, 'CONSENT-20260920-002', 1, 2, 'PENDING', 'Payment Initiation Service - E-Commerce Checkout', 'READ_ACCOUNTS,READ_BALANCES', 'amazon-pay-tpp', NOW() + INTERVAL '30 days', NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days'),
(3, 'CONSENT-20260922-003', 2, 3, 'REJECTED', 'Credit Scoring Analysis', 'READ_ACCOUNTS,READ_TRANSACTIONS', 'quick-loan-tpp', NOW() + INTERVAL '30 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO consent_audit_logs (id, consent_reference, customer_id, action, actor_role, actor_identifier, details, created_at)
VALUES
(1, 'CONSENT-20260915-001', 1, 'CONSENT_CREATED', 'TPP', 'fintech-aggregator-tpp', 'Requested scopes: READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS for purpose: Account Information Service - Aggregator App', NOW() - INTERVAL '5 days'),
(2, 'CONSENT-20260915-001', 1, 'CONSENT_AUTHORISED', 'CUSTOMER', 'subash@banfico.com', 'Customer granted explicit consent for 90 days', NOW() - INTERVAL '5 days'),
(3, 'CONSENT-20260920-002', 1, 'CONSENT_CREATED', 'TPP', 'amazon-pay-tpp', 'Requested scopes: READ_ACCOUNTS,READ_BALANCES for purpose: Payment Initiation Service - E-Commerce Checkout', NOW() - INTERVAL '1 days'),
(4, 'CONSENT-20260922-003', 2, 'CONSENT_CREATED', 'TPP', 'quick-loan-tpp', 'Requested scopes: READ_ACCOUNTS,READ_TRANSACTIONS for purpose: Credit Scoring Analysis', NOW() - INTERVAL '2 days'),
(5, 'CONSENT-20260922-003', 2, 'CONSENT_REJECTED', 'CUSTOMER', 'alice.j@example.com', 'Customer declined third party data access', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- Reset sequence values to max IDs
SELECT setval('customers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM customers));
SELECT setval('bank_accounts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM bank_accounts));
SELECT setval('transactions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM transactions));
SELECT setval('beneficiaries_id_seq', (SELECT COALESCE(MAX(id), 1) FROM beneficiaries));
SELECT setval('consents_id_seq', (SELECT COALESCE(MAX(id), 1) FROM consents));
SELECT setval('consent_audit_logs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM consent_audit_logs));
