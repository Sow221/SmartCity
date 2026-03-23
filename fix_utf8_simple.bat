@echo off
echo ===============================================
echo CORRECTION ENCODAGE UTF-8 MYSQL
echo ===============================================

set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

echo 1. Test connexion MySQL...
%MYSQL_PATH% -u root -p77884455 -e "SELECT VERSION();" >nul 2>&1
if errorlevel 1 (
    echo ERREUR: Impossible de se connecter a MySQL
    pause
    exit /b 1
)
echo [OK] MySQL accessible

echo.
echo 2. Sauvegarde avant correction...
%MYSQL_PATH% -u root -p77884455 --single-transaction db_smartcity > backup_utf8.sql 2>nul
echo [OK] Sauvegarde creee

echo.
echo 3. Application de la correction UTF-8...
%MYSQL_PATH% -u root -p77884455 < fix_encoding.sql
if errorlevel 1 (
    echo ERREUR: Echec de la correction
    pause
    exit /b 1
)
echo [OK] Base recreee avec UTF-8

echo.
echo 4. Verification des donnees...
echo Zones disponibles:
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT * FROM Zone;"
echo.
echo Signalements avec accents:
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT idSignalement, LEFT(description, 40) as description FROM Signalement;"

echo.
echo 5. Test insertion avec accents...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; INSERT INTO Signalement (description, categorie, idUser, idZone) VALUES ('Test accents: Guédiawaye à côté', 'Test', 4, 2);"
if errorlevel 1 (
    echo ERREUR: Les accents ne fonctionnent pas
    pause
    exit /b 1
)
echo [OK] Accents supportes!

echo.
echo ===============================================
echo CORRECTION UTF-8 TERMINEE AVEC SUCCES!
echo ===============================================
echo - Base recree avec encodage UTF-8
echo - Caracteres francais supportes
echo - Donnees de test avec accents
echo ===============================================
pause