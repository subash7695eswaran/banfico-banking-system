@echo off
echo ========================================================
echo Starting Full Banfico Banking Stack via Docker Compose...
echo ========================================================
docker compose up --build -d
echo.
echo All services launched!
echo - Gateway & Single Entrypoint: http://localhost:8080
echo - Web Dashboard: http://localhost:8080/
echo - Backend APIs: http://localhost:8080/api/
echo - Keycloak IAM: http://localhost:8081
echo - Database: localhost:5432
echo.
pause
