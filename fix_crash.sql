-- Fix SmartCity crash - Insert missing zones
USE db_smartcity;

INSERT IGNORE INTO Zone (nomZone) VALUES ('Pikine'), ('Guédiawaye');
SELECT 'Zones inserted. Run ./mvnw javafx:run' as Status;
