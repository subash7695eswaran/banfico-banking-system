@echo off
echo ========================================================
echo Starting Banfico Banking Backend (Java 21 + Spring Boot)
echo ========================================================
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set MVN_CMD=mvn
) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" (
    set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd"
) else (
    set MVN_CMD=mvn
)

set MAVEN_OPTS=-Xmx512m -Xms128m
cd backend
"%MVN_CMD%" spring-boot:run
pause

