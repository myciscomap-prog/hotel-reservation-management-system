@echo off
cd /d "%~dp0"
echo Running Hotel Reservation Management System test suite...
call mvn test
if errorlevel 1 (
    echo.
    echo Some tests failed. See the Maven output above for details.
    pause
)
