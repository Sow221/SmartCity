@echo off
echo ======================================
echo DEMARRAGE SERVEUR GPS API SEPARE
echo ======================================
echo.

cd /d "%~dp0"

echo Compilation en cours...
call mvn compile -q
if errorlevel 1 (
    echo ERREUR: Compilation echouee.
    pause
    exit /b 1
)

echo.
echo Demarrage Serveur GPS API sur le port 8081...
echo Accessible sur http://localhost:8081
echo Accessible sur le reseau local: http://[IP]:8081
echo.
echo Appuyez sur CTRL+C pour arreter
echo ======================================
echo.

call mvn exec:java -Dexec.mainClass="com.smartcity.service.GpsApiServer"

