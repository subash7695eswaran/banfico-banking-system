@echo off
echo ========================================================
echo Starting PostgreSQL & Keycloak containers in Docker...
echo ========================================================
docker compose up -d postgres keycloak
echo.
echo Containers started!
echo - PostgreSQL: localhost:5432 (Database: banfico_banking)
echo - Keycloak: http://localhost:8081
echo.
pause
