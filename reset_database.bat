@echo off
echo ===============================================
echo    REINITIALISATION BASE DE DONNEES SMARTCITY
echo ===============================================
echo.

set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set MYSQLDUMP_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"

echo 1. Verification de MySQL...
%MYSQL_PATH% -u root -p77884455 -e "SELECT VERSION();" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] MySQL n'est pas accessible avec ces credentials
    echo Verifiez que MySQL est demarré et que le mot de passe est correct
    pause
    exit /b 1
)
echo [OK] MySQL accessible

echo.
echo 2. Sauvegarde de l'ancienne base (si elle existe)...
mysql -u root -p77884455 -e "CREATE DATABASE IF NOT EXISTS backup_db_smartcity;" >nul 2>&1
%MYSQLDUMP_PATH% -u root -p77884455 db_smartcity > backup_db_smartcity_%date:~0,2%-%date:~3,2%-%date:~6,4%.sql 2>nul
if exist backup_db_smartcity_%date:~0,2%-%date:~3,2%-%date:~6,4%.sql (
    echo [OK] Sauvegarde créée: backup_db_smartcity_%date:~0,2%-%date:~3,2%-%date:~6,4%.sql
) else (
    echo [INFO] Pas de base existante à sauvegarder
)

echo.
echo 3. Suppression et recréation de la base...
%MYSQL_PATH% -u root -p77884455 < scripts\reset_database.sql
if %errorlevel% neq 0 (
    echo [ERREUR] Échec de la recréation de la base
    pause
    exit /b 1
)
echo [OK] Base de données recréée avec succès

echo.
echo 4. Vérification de la structure...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SHOW TABLES;"
echo.
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT role, COUNT(*) as nombre FROM Utilisateur GROUP BY role;"

echo.
echo ===============================================
echo    REINITIALISATION TERMINEE AVEC SUCCES!
echo ===============================================
echo.
echo Comptes de test disponibles:
echo - Admin: admin@smartcity.sn / admin123
echo - Agent Pikine: agentpikine@smartcity.sn / agent123  
echo - Agent Guédiawaye: agentguediawaye@smartcity.sn / agent123
echo - Citoyen: citoyen@smartcity.sn / citizen123
echo.
echo Vous pouvez maintenant lancer l'application:
echo mvnw.cmd javafx:run
echo.
pause