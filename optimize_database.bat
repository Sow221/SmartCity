@echo off
echo ===============================================
echo OPTIMISATION BASE DE DONNEES SMARTCITY
echo ===============================================

set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

echo 1. Verification de MySQL...
%MYSQL_PATH% -u root -p77884455 -e "SELECT VERSION();" >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Impossible de se connecter a MySQL
    pause
    exit /b 1
)
echo [OK] MySQL accessible

echo.
echo 2. Execution des optimisations...
%MYSQL_PATH% -u root -p77884455 < src\main\resources\sql\optimizations.sql
if errorlevel 1 (
    echo [ERREUR] Echec des optimisations
    pause
    exit /b 1
)
echo [OK] Optimisations appliquees

echo.
echo 3. Verification des index...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SHOW INDEX FROM Signalement;"
echo.

echo 4. Test des vues...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT * FROM v_dashboard_realtime;"
echo.

echo 5. Statistiques des tables...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT TABLE_NAME, TABLE_ROWS, ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as SIZE_MB FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'db_smartcity';"

echo.
echo ===============================================
echo OPTIMISATION TERMINEE AVEC SUCCES!
echo ===============================================
echo.
echo Les améliorations suivantes ont été appliquées :
echo - Index de performance sur les requêtes fréquentes
echo - Vues optimisées pour les rapports
echo - Procédures stockées pour l'automatisation
echo - Déclencheurs pour l'intégrité des données
echo - Configuration MySQL optimisée
echo.
pause