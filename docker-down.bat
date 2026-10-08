@echo off
echo ========================================================
echo Stopping and Removing all Banfico Docker Containers...
echo ========================================================
docker compose down
echo.
echo All containers stopped and networks removed.
echo.
pause
