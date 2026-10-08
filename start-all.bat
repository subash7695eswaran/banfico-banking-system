@echo off
echo ========================================================
echo Starting Banfico Banking Full System...
echo ========================================================

start "Banfico Backend" cmd /k "call start-backend.bat"
start "Banfico Frontend" cmd /k "call start-frontend.bat"

echo Backend and Frontend launched in separate windows!
echo - Frontend: http://localhost:5173
echo - Backend API: http://localhost:8080/health
echo - Swagger Docs: http://localhost:8080/swagger-ui.html
pause
