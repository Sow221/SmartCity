@echo off
echo ==========================================
echo   TEST SERVEUR GPS SMARTCITY - PORT 8081
echo ==========================================
echo.

REM Construire le classpath depuis target/classes + toutes les deps Maven
set CP=target\classes

REM Ajouter toutes les JARs du repo Maven local
for /r ".m2\repository" %%f in (*.jar) do set CP=!CP!;%%f

REM Methode plus simple : utiliser le classpath Maven
echo Demarrage du serveur GPS...
echo.

mvnw.cmd -DskipTests exec:java -Dexec.mainClass="com.smartcity.service.GpsApiServer" 2>&1

pause
