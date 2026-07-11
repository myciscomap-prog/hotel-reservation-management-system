@echo off
cd /d "%~dp0"
echo Starting Hotel Reservation Management System...
call mvn javafx:run
if errorlevel 1 (
    echo.
    echo Build or run failed. See the Maven output above for details.
    pause
)
