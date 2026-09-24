# Architecture Overview: Banfico Mini Banking & Open Banking Consent Management System

## 1. High-Level Architecture

The system is architected as an enterprise-grade, cloud-ready financial microservices platform adhering to Open Banking specifications (UK Open Banking / Berlin Group NextGenPSD2 standards).

```mermaid
graph TD
    Client[Browser / Single Page App / Postman] -->|Port 8080| Nginx[Nginx API Gateway]
    
    subgraph Core Platform
        Nginx -->|/| Frontend[React + Vite Frontend (Port 80/5173)]
        Nginx -->|/api/*| Backend[Spring Boot 3.3.4 REST API (Port 8080/8082)]
        Nginx -->|/auth/*| Keycloak[Keycloak IAM Server (Port 8081)]
        
        Backend -->|JPA / Hibernate| Postgres[(PostgreSQL 16 Database)]
        Backend -.->|Validate JWT / JWKS| Keycloak
    end
```

## 2. Technology Stack & Decision Matrix

| Layer | Technology | Rationale |
| :--- | :--- | :--- |
| **Language** | Java 21 LTS | Modern virtual threads, record patterns, pattern matching, high performance |
| **Backend Framework** | Spring Boot 3.3.4 | Industry-standard enterprise Java framework, robust ecosystem |
| **ORM / Persistence** | Spring Data JPA / Hibernate | Object-Relational mapping, transactional integrity |
| **Database** | PostgreSQL 16 | ACID compliance, precise numeric arithmetic for monetary values |
| **Frontend UI** | React 18 + Vite | High responsiveness, fast build times, modern Component architecture |
| **Identity & Access** | Keycloak 24.0.5 | Regulated OAuth2 / OpenID Connect, JWT issuance, RBAC |
| **API Gateway** | Nginx | Single unified ingress entrypoint, SSL termination, reverse proxy |
| **Containerization** | Docker & Docker Compose | Repeatable, portable local and production multi-container orchestration |

## 3. Layered Backend Design Pattern

```
HTTP Request (JSON)
       │
       ▼
[ Controller Layer ]      <-- Request validation, OpenAPI annotations, HTTP status code mapping
       │
       ▼
[ DTO Mapper ]            <-- Converts Request DTOs into Domain Entities & vice-versa
       │
       ▼
[ Service Layer ]         <-- Pure business logic, transactional boundaries (@Transactional)
       │
       ▼
[ Repository Layer ]      <-- Spring Data JPA interfaces, JPQL queries
       │
       ▼
[ PostgreSQL Database ]   <-- Relational persistence with foreign keys, constraints & sequences
```

## 4. Key Security & Authorization Model (Keycloak RBAC)
- **admin**: Full system privileges (create accounts, manage beneficiaries, system maintenance).
- **maker**: Initiates payments, creates accounts, initiates consent requests.
- **checker**: Approves or rejects high-value transactions and open banking consent grants (Dual-control Principle).
- **customer**: Self-service profile, account overview, consent authorisations.
