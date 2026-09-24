# Banfico Mini Banking REST API Specification

## Base URLs
- **Direct Backend**: `http://localhost:8080` (or `http://localhost:8082` when via Docker)
- **Nginx Gateway**: `http://localhost:8080/api`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`

---

## 1. Health & Metadata Endpoints (Week 1)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/health` | Healthcheck (Service & PostgreSQL status) | Public |
| `GET` | `/api/info` | Application metadata, Java/Spring version, available endpoints | Public |

---

## 2. Customer Management (Week 2)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/customers` | Register a new customer | Public / Admin |
| `GET` | `/api/customers` | Retrieve all registered customers | Public / Admin |
| `GET` | `/api/customers/{id}` | Get customer details by ID | Public / Customer |
| `GET` | `/api/customers/{id}/accounts` | Get all bank accounts of a customer | Public / Customer |
| `GET` | `/api/customers/{id}/consents` | Get all consents linked to customer | Public / Customer |

---

## 3. Bank Accounts (Week 2)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/accounts` | Open account with IBAN for customer | Maker / Admin |
| `GET` | `/api/accounts` | List all accounts in the core bank | Public / Admin |
| `GET` | `/api/accounts/{accountId}` | Get account details by ID | Customer / Admin |
| `GET` | `/api/accounts/number/{accNo}` | Get account details by IBAN | Customer / Admin |

---

## 4. Transactions (Week 2)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/accounts/{id}/transactions` | Execute DEPOSIT, WITHDRAWAL, or TRANSFER | Maker / Customer |
| `GET` | `/api/accounts/{id}/transactions` | Get account statement / transaction history | Customer / Admin |
| `GET` | `/api/transactions` | Global ledger audit of all transactions | Admin / Checker |
| `GET` | `/api/transactions/{ref}` | Get transaction details by reference | Admin / Maker |

---

## 5. Beneficiaries (Week 2)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/beneficiaries` | Add a trusted payee | Customer / Maker |
| `GET` | `/api/beneficiaries` | List all beneficiaries | Admin |
| `GET` | `/api/beneficiaries/customer/{id}` | List customer payee book | Customer / Maker |
| `DELETE` | `/api/beneficiaries/{id}` | Delete a payee | Customer / Admin |

---

## 6. Open Banking Consents (Week 6 / Capstone)
| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/consents` | Initiate TPP Consent request | TPP / Maker |
| `GET` | `/api/consents` | List all Open Banking consents | Admin / Checker |
| `GET` | `/api/consents/{id}` | Get consent status & purpose | Customer / Checker |
| `POST` | `/api/consents/{id}/approve` | Authorise consent (Grant TPP Access) | Customer / Checker |
| `POST` | `/api/consents/{id}/reject` | Reject consent request | Customer / Checker |
| `PUT` | `/api/consents/{id}/status` | Update consent lifecycle status | Admin / System |
