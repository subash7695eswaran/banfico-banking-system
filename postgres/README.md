# PostgreSQL Configuration for Banfico Banking System

## Database Details
- **Database Name**: `banfico_banking`
- **Default Port**: `5432`
- **Default User**: `postgres`
- **Default Password**: `postgres`

## Running with Docker Compose
PostgreSQL is managed automatically by `docker-compose.yml`:
```bash
docker compose up -d postgres
```

The database initialization script at `./init.sql` runs automatically on first container startup to create all tables and initial seed data.

## Running with Local PostgreSQL
If running local PostgreSQL (e.g. pgAdmin or PostgreSQL service):
1. Connect using `psql` or pgAdmin:
   ```bash
   psql -U postgres
   ```
2. Create database:
   ```sql
   CREATE DATABASE banfico_banking;
   ```
3. Execute `init.sql`:
   ```bash
   psql -U postgres -d banfico_banking -f init.sql
   ```
