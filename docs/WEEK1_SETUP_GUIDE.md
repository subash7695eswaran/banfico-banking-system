# Week 1 Guide: Setup, Git, Spring Boot & Health APIs

## Objectives
- Verify local developer workstation readiness (Java 21, Git, Node/npm, PostgreSQL/Docker).
- Initialize Spring Boot project structure with layered architecture.
- Connect backend to PostgreSQL database.
- Expose and test baseline health and metadata endpoints:
  - `GET /health`
  - `GET /api/info`
- Verify with Postman and push initial baseline to Git repository.

## Step-by-Step Walkthrough

### 1. Environment Verification
Run the following in PowerShell / Terminal:
```powershell
# 1. Verify Java 21 LTS
java -version

# 2. Verify Git
git --version

# 3. Verify Node & NPM (for frontend)
node --version
npm --version
```

### 2. Database Setup (PostgreSQL)
Ensure PostgreSQL is running locally or via Docker:
```bash
# Option A: Run PostgreSQL via Docker
docker run --name banfico-postgres -e POSTGRES_DB=banfico_banking -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine

# Option B: Run full Docker Compose stack
docker compose up -d postgres
```

### 3. Running Backend Locally
Navigate to the `backend/` directory:
```bash
cd backend

# On Windows with Maven:
mvn spring-boot:run

# Or with custom DB credentials passed via Environment Variables:
$env:DB_HOST="localhost"; $env:DB_PORT="5432"; $env:DB_NAME="banfico_banking"; $env:DB_USERNAME="postgres"; $env:DB_PASSWORD="postgres"; mvn spring-boot:run
```

### 4. Verifying Health & Info APIs with Postman / cURL
```bash
# 1. Check Health (returns UP and database CONNECTED status)
curl -X GET http://localhost:8080/health

# Response:
# {
#   "status": "UP",
#   "service": "Banfico Mini Banking System",
#   "timestamp": "2026-09-24T22:45:00",
#   "database": "CONNECTED",
#   "details": {
#     "databaseProduct": "PostgreSQL",
#     "activeProfiles": "[dev]"
#   }
# }

# 2. Check Info API
curl -X GET http://localhost:8080/api/info
```

### 5. Git Commit & Push
```bash
git init
git add .
git commit -m "feat(week1): initial project setup with Spring Boot 3, Java 21, PostgreSQL & Health APIs"
```
