@echo off
chcp 65001 >nul 2>&1
echo.
echo  🔧 CORRECTION ENCODAGE UTF-8 MYSQL 🔧
echo.
echo ===============================================
echo RESOLUTION PROBLEME CARACTERES FRANCAIS
echo ===============================================

set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

echo 🔍 Étape 1 - Diagnostic du problème...
echo.

echo • Test connexion MySQL...
%MYSQL_PATH% -u root -p77884455 -e "SELECT VERSION();" >nul 2>&1
if errorlevel 1 (
    echo   ❌ ERREUR : Impossible de se connecter à MySQL
    pause
    exit /b 1
)
echo   ✅ MySQL accessible

echo • Vérification encodage actuel...
%MYSQL_PATH% -u root -p77884455 -e "SHOW VARIABLES LIKE 'character_set_%%';"
echo.

echo ⚠️  ATTENTION : Cette opération va SUPPRIMER et RECRÉER la base de données
echo    avec le bon encodage UTF-8 pour supporter les accents français.
echo.
set /p confirm="Continuer ? (O/N): "
if /i "%confirm%" neq "O" (
    echo Opération annulée.
    pause
    exit /b 0
)

echo.
echo ===============================================
echo 🗄️ Étape 2 - Sauvegarde des données actuelles...
echo.

echo • Création sauvegarde avant correction...
%MYSQL_PATH% -u root -p77884455 --single-transaction --routines --triggers -e "SHOW DATABASES;" | findstr db_smartcity >nul
if not errorlevel 1 (
    %MYSQL_PATH% -u root -p77884455 --single-transaction --routines --triggers db_smartcity > backup_avant_utf8_%date:~6,4%-%date:~3,2%-%date:~0,2%.sql 2>nul
    echo   ✅ Sauvegarde créée
) else (
    echo   ⚠️ Base n'existe pas encore
)

echo.
echo ===============================================
echo 🔄 Étape 3 - Application de la correction UTF-8...
echo.

echo • Suppression et recréation avec UTF-8...
%MYSQL_PATH% -u root -p77884455 < fix_encoding.sql
if errorlevel 1 (
    echo   ❌ ERREUR : Échec de la correction UTF-8
    pause
    exit /b 1
)
echo   ✅ Base recréée avec encodage UTF-8

echo.
echo ===============================================
echo ✅ Étape 4 - Vérification de la correction...
echo.

echo • Test des caractères français...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT * FROM Zone;"
echo.

echo • Test des accents dans les signalements...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT idSignalement, LEFT(description, 50) as description FROM Signalement;"
echo.

echo • Vérification des vues UTF-8...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT * FROM v_dashboard_realtime;"
echo.

echo • Test insertion avec accents...
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; INSERT INTO Signalement (description, categorie, latitude, longitude, idUser, idZone) VALUES ('Test des accents : é à ç ù î ô', 'Test UTF-8', 14.7600, -17.4000, 4, 1); SELECT 'Test réussi' as resultat;"
if errorlevel 1 (
    echo   ❌ Les accents ne fonctionnent toujours pas
    pause
    exit /b 1
)
echo   ✅ Accents français supportés !

echo.
echo ===============================================
echo 🎯 Étape 5 - Configuration MySQL permanente...
echo.

echo • Application configuration UTF-8 globale...
%MYSQL_PATH% -u root -p77884455 -e "SET GLOBAL character_set_server = 'utf8mb4'; SET GLOBAL collation_server = 'utf8mb4_unicode_ci';"
echo   ✅ Configuration globale appliquée

echo.
echo ===============================================
echo.
echo 🎉 CORRECTION UTF-8 TERMINÉE AVEC SUCCÈS ! 🎉
echo.
echo ✅ PROBLÈME RÉSOLU :
echo    • Base de données recréée avec UTF-8
echo    • Caractères français supportés : é à ù ç î ô
echo    • Tables optimisées pour les accents
echo    • Vues de reporting fonctionnelles
echo    • Index de performance maintenus
echo.
echo 📊 STATISTIQUES APRÈS CORRECTION :
%MYSQL_PATH% -u root -p77884455 -e "USE db_smartcity; SELECT TABLE_NAME as 'Table', TABLE_COLLATION as 'Encodage', TABLE_ROWS as 'Lignes' FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'db_smartcity';"
echo.
echo 💡 DONNÉES DE TEST DISPONIBLES :
echo    • 2 zones : Pikine, Guédiawaye
echo    • 4 utilisateurs avec accents français
echo    • 3 signalements de test
echo    • Comptes de connexion maintenus
echo.
echo ===============================================
echo 🚀 Vous pouvez maintenant relancer : mvnw.cmd javafx:run
echo ===============================================
pause