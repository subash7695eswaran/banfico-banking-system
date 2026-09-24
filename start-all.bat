@echo off
echo ========================================================
echo Starting Banfico Banking Full System...
echo ========================================================

start "Banfico Backend" cmd /k "cd backend && mvn spring-boot:run"
start "Banfico Frontend" cmd /k "cd frontend && npm run dev"

echo Backend and Frontend launched in separate windows!
echo - Frontend: http://localhost:5173
echo - Backend API: http://localhost:8080/health
echo - Swagger Docs: http://localhost:8080/swagger-ui.html
pause
