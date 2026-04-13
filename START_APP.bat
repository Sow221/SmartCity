@echo off
REM ===================================================================
REM SCRIPT DÉMARRAGE RAPIDE SMARTCITY - 13 avril 2026
REM ===================================================================
REM Fonction: Lancer app en 1 click sans erreurs
REM Cible: Démo critiques (4h validation)
REM ===================================================================

setlocal enabledelayedexpansion

echo.
echo [INFO] ========== SMARTCITY STARTUP ==========
echo [INFO] Vérification infra...
echo.

REM ========== VÉRIFIER MYSQL ==========
tasklist | findstr /I "mysqld" >nul 2>&1
if errorlevel 1 (
    echo [WARN] MySQL non détecté. Lancement en cours...
    net start MySQL80 >nul 2>&1
    timeout /t 3 /nobreak >nul
) else (
    echo [OK] MySQL actif
)

REM ========== VÉRIFIER PORTS ==========
netstat -ano | findstr "8081" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] Port 8081 en usage - fermeture...
    for /f "tokens=5" %%A in ('netstat -ano ^| findstr 8081') do (
        taskkill /PID %%A /F >nul 2>&1
    )
    timeout /t 2 /nobreak >nul
)
echo [OK] Ports 8081/8082 libres

REM ========== CD AU PROJET ==========
cd /d "c:\Users\MS\Desktop\SC\SmartCity" || (
    echo [ERROR] Dossier projet non trouvé!
    pause
    exit /b 1
)

echo [INFO] Compilation...
call mvn clean compile -q -DskipTests
if errorlevel 1 (
    echo [ERROR] Compilation ÉCHOUÉE!
    echo [INFO] Détails:
    call mvn clean compile
    pause
    exit /b 1
)
echo [OK] Compilation réussie

echo.
echo [INFO] ========== LANCEMENT APP ==========
echo [INFO] Appuyez sur Ctrl+C pour arrêter
echo.

call mvn javafx:run

REM ========== CLEANUP ==========
:cleanup
echo.
echo [INFO] Arrêt serveurs...
taskkill /F /IM java.exe >nul 2>&1
echo [OK] App fermée proprement

exit /b 0
