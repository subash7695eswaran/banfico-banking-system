# Banfico Mini Banking & Open Banking Consent Management System

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18-61dafb.svg)](https://react.dev/)
[![Keycloak](https://img.shields.io/badge/Keycloak-24.0.5-red.svg)](https://www.keycloak.org/)
[![Nginx](https://img.shields.io/badge/Nginx-API%20Gateway-green.svg)](https://nginx.org/)

An enterprise-ready **Mini Banking and Open Banking Consent Management System** built for the **Banfico University Full-Stack Developer Training & Selection Program**.

---

## 🏛️ Project Architecture

```
banfico-banking-system/
│
├── backend/                  # Java 21 & Spring Boot 3 Layered REST API
│   ├── src/main/java/com/banfico/banking/
│   │   ├── config/           # CORS & Swagger / OpenAPI 3.0 configuration
│   │   ├── controller/       # Health, Customer, Account, Transaction, Beneficiary, Consent
│   │   ├── dto/              # Strongly typed Request & Response envelopes
│   │   ├── entity/           # JPA Entities (Customer, BankAccount, Transaction, Beneficiary, Consent)
│   │   ├── exception/        # Global Exception Handler & Domain Exceptions
│   │   ├── mapper/           # Entity to DTO mappers
│   │   ├── repository/       # Spring Data JPA Repositories
│   │   ├── security/         # Spring Security & Keycloak JWT Realm Role Converter
│   │   └── service/          # Transactional business logic layer
│   ├── pom.xml               # Maven configuration
│   └── Dockerfile
│
├── frontend/                 # React 18 + Vite Glassmorphic Dashboard
│   ├── src/
│   │   ├── api/              # Axios REST Client
│   │   ├── components/       # Reusable UI widgets & modals
│   │   └── pages/            # Dashboard, Customers, Accounts, Transactions, Consents, Diagnostics
│   └── Dockerfile
│
├── nginx/                    # API Gateway reverse proxy configuration
│   ├── nginx.conf
│   └── Dockerfile
│
├── keycloak/                 # OAuth2 / OpenID Connect Realm configuration
│   └── realm-export.json     # Preconfigured realm, clients, roles (admin, maker, checker, customer)
│
├── postgres/                 # Database initialization & seed data
│   └── init.sql
│
├── postman/                  # Ready-to-import Postman Collection & Environment
│   ├── Banfico_Banking_System.postman_collection.json
│   └── Banfico_Local.postman_environment.json
│
├── docs/                     # Architecture & Weekly Guides
│   ├── ARCHITECTURE.md
│   ├── WEEK1_SETUP_GUIDE.md
│   ├── OPEN_BANKING_CONSENT_FLOW.md
│   └── API_SPECIFICATION.md
│
├── docker-compose.yml        # Full-stack multi-container orchestration
└── .gitignore
```

---

## 🚀 Training Roadmap & Weekly Milestones

| Week | Focus Area | Deliverables & Features |
| :--- | :--- | :--- |
| **Week 1** | **Setup & Health APIs** | Environment verification, Java 21, Spring Boot REST baseline `GET /health` & `GET /api/info` |
| **Week 2** | **Core Banking CRUD** | Customers, Bank Accounts with IBAN generation, Transactions (Deposit/Withdraw/Transfer), Beneficiaries |
| **Week 3** | **Frontend Integration** | React Vite glassmorphic web dashboard, real-time balances, statement inspection, interactive modals |
| **Week 4** | **Keycloak & Security** | OAuth2 / OpenID Connect, JWT validation, Realm roles (`admin`, `maker`, `checker`, `customer`) |
| **Week 5** | **API Gateway & Docker** | Nginx reverse proxy single entrypoint (`:8080`), full-stack multi-container Docker Compose |
| **Week 6** | **Open Banking Consent** | Capstone: Regulated Open Banking AIS & PIS Consent lifecycle state machine |

---

## 🛠️ Environment Verification Summary

- **Operating System**: Microsoft Windows 11 Pro 64-bit
- **Java**: Java 21.0.11 LTS (`Java(TM) SE Runtime Environment 21.0.11+9-LTS-211`)
- **Git**: Git 2.52.0.windows.1
- **Node.js & npm**: Node v22.15.1, npm 10.9.2
- **Port Availability**: All necessary ports (`8080`, `5173`, `5432`, `8081`, `8082`) are tested and free.

---

## ⚡ Quick Start: Running Week 1 & Week 2 Locally

### 1. Database Setup (PostgreSQL)
Ensure PostgreSQL is running locally on port `5432` with database `banfico_banking`:
```bash
# If using Docker:
docker compose up -d postgres
```

### 2. Start Backend Service
```bash
cd backend
mvn spring-boot:run
```
*Backend will start on `http://localhost:8080`.*

### 3. Start Frontend Dashboard
```bash
cd frontend
npm install
npm run dev
```
*Frontend will open on `http://localhost:5173`.*

---

## 🧪 Testing APIs with Postman / cURL

### 1. Healthcheck (`GET /health`)
```bash
curl -X GET http://localhost:8080/health
```
```json
{
  "status": "UP",
  "service": "Banfico Mini Banking System",
  "timestamp": "2026-09-24T22:45:00",
  "database": "CONNECTED",
  "details": {
    "databaseProduct": "PostgreSQL",
    "activeProfiles": "[dev]"
  }
}
```

### 2. Application Info (`GET /api/info`)
```bash
curl -X GET http://localhost:8080/api/info
```

### 3. Interactive Swagger UI
Open your browser to:
👉 **`http://localhost:8080/swagger-ui.html`**

---

## 🐳 Full Stack Deployment with Docker Compose (Week 5)

To launch all 5 services simultaneously:
```bash
docker compose up --build -d
```

### Service Port Map:
- **Nginx Gateway (Single Entry Point)**: `http://localhost:8080`
  - Web UI: `http://localhost:8080/`
  - Backend API: `http://localhost:8080/api/`
  - Keycloak Auth: `http://localhost:8080/auth/`
- **Keycloak Admin Console**: `http://localhost:8081` *(Credentials: `admin` / `admin`)*
- **PostgreSQL Database**: `localhost:5432` *(Database: `banfico_banking`)*

---

## 🔐 Open Banking Consent Lifecycle

1. **Initiate Consent Request**: TPP calls `POST /api/consents` specifying customer, purpose, and client ID.
2. **Pending State**: Consent is recorded in `PENDING` status.
3. **Customer / Checker Decision**:
   - `POST /api/consents/{id}/approve` -> Moves status to `AUTHORISED`.
   - `POST /api/consents/{id}/reject` -> Moves status to `REJECTED`.
4. **Data Access Enforcement**: Protected APIs verify active, non-expired consent before releasing banking statements.
