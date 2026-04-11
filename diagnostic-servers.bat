@echo off
REM ============================================================================
REM  🔧 DIAGNOSTIC SERVEURS - SmartCity (WINDOWS)
REM  Vérifie les serveurs (GpsApiServer, WebSocketServer) et géolocalisation
REM ============================================================================

setlocal enabledelayedexpansion

echo =====================================================================
echo   ^🔧 DIAGNOSTIC PRÉ-DÉMO - SmartCity Géolocalisation^
echo =====================================================================
echo.

REM ============================================================================
REM 1. VÉRIFIER LES PORTS
REM ============================================================================
echo 📍 [1/5] Vérification PORTS...
echo.

netstat -ano | findstr ":8081" >nul
if %errorlevel% equ 0 (
  echo   ❌ Port 8081 OCCUPÉ - À libérer !
  set PORT_8081_ERROR=1
) else (
  echo   ✅ Port 8081 LIBRE ^(GpsApiServer^)
  set PORT_8081_ERROR=0
)

netstat -ano | findstr ":8082" >nul
if %errorlevel% equ 0 (
  echo   ❌ Port 8082 OCCUPÉ - À libérer !
  set PORT_8082_ERROR=1
) else (
  echo   ✅ Port 8082 LIBRE ^(WebSocketServer^)
  set PORT_8082_ERROR=0
)

echo.

REM ============================================================================
REM 2. VÉRIFIER MYSQL
REM ============================================================================
echo 📍 [2/5] Vérification MYSQL...
echo.

mysql -u root -p77884455 -e "USE db_smartcity; SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
  echo   ✅ MySQL ACCESSIBLE
  echo   ✅ Base db_smartcity EXISTE
  
  REM Vérifier zones GPS
  mysql -u root -p77884455 -e "USE db_smartcity; SELECT COUNT(*) FROM Zone WHERE nomZone='Pikine' AND latitude IS NOT NULL;" >nul 2>&1
  if %errorlevel% equ 0 (
    echo   ✅ Zone Pikine avec coordonnées GPS présente
  ) else (
    echo   ⚠️  Zone Pikine GPS manquante
  )
  
  mysql -u root -p77884455 -e "USE db_smartcity; SELECT COUNT(*) FROM Zone WHERE nomZone='Guédiawaye' AND latitude IS NOT NULL;" >nul 2>&1
  if %errorlevel% equ 0 (
    echo   ✅ Zone Guédiawaye avec coordonnées GPS présente
  ) else (
    echo   ⚠️  Zone Guédiawaye GPS manquante
  )
  
  set MYSQL_ERROR=0
) else (
  echo   ❌ MYSQL NON ACCESSIBLE - Lancer: net start MySQL80
  set MYSQL_ERROR=1
)

echo.

REM ============================================================================
REM 3. VÉRIFIER COMPILATION
REM ============================================================================
echo 📍 [3/5] Vérification COMPILATION...
echo.

if exist "target" (
  echo   ✅ Target/ existe ^(dernière compilation présente^)
  set COMPILE_ERROR=0
) else (
  echo   ℹ️  Compilation requise...
  cd /d "%~dp0"
  call mvn clean compile -q
  if %errorlevel% equ 0 (
    echo   ✅ COMPILATION OK
    set COMPILE_ERROR=0
  ) else (
    echo   ❌ COMPILATION FAILED
    set COMPILE_ERROR=1
  )
)

echo.

REM ============================================================================
REM 4. VÉRIFIER FICHIERS CRITIQUES
REM ============================================================================
echo 📍 [4/5] Vérification FICHIERS CRITIQUES...
echo.

if exist "config.properties" (
  echo   ✅ config.properties
) else (
  echo   ❌ config.properties MANQUANT
)

if exist "src\main\java\com\smartcity\service\GpsApiServer.java" (
  echo   ✅ GpsApiServer.java
) else (
  echo   ❌ GpsApiServer.java MANQUANT
)

if exist "src\main\java\com\smartcity\service\RealTimeGPSService.java" (
  echo   ✅ RealTimeGPSService.java
) else (
  echo   ❌ RealTimeGPSService.java MANQUANT
)

if exist "src\main\resources\fxml\citizen_dashboard.fxml" (
  echo   ✅ citizen_dashboard.fxml
) else (
  echo   ❌ citizen_dashboard.fxml MANQUANT
)

if exist "src\main\resources\fxml\agent_dashboard.fxml" (
  echo   ✅ agent_dashboard.fxml
) else (
  echo   ❌ agent_dashboard.fxml MANQUANT
)

echo.

REM ============================================================================
REM 5. RÉSUMÉ
REM ============================================================================
echo 📍 [5/5] RÉSUMÉ FINAL...
echo.

set TOTAL_ERRORS=0
set /a TOTAL_ERRORS=%PORT_8081_ERROR% + %PORT_8082_ERROR% + %MYSQL_ERROR% + %COMPILE_ERROR%

if %TOTAL_ERRORS% equ 0 (
  echo 🟢 ^✅ TOUS LES DIAGNOSTICS OK - READY FOR DEMO
  exit /b 0
) else (
  echo 🟡 ^⚠️  %TOTAL_ERRORS% PROBLÈME^(S^) DÉTECTÉ^(S^) - Voir ci-dessus
  exit /b 1
)
