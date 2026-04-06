USE db_smartcity;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Ramener toutes les FK vers les zones 1 et 2
UPDATE Signalement SET idZone = 1 WHERE idZone IN (3, 5);
UPDATE Signalement SET idZone = 2 WHERE idZone IN (4, 6);
UPDATE Utilisateur  SET idZone = 1 WHERE idZone IN (3, 5);
UPDATE Utilisateur  SET idZone = 2 WHERE idZone IN (4, 6);

-- Supprimer les zones dupliquées
DELETE FROM Zone WHERE idZone IN (3, 4, 5, 6);

-- Corriger le nom corrompu de la zone 2
UPDATE Zone SET nomZone = 'Guédiawaye' WHERE idZone = 2;

SET FOREIGN_KEY_CHECKS = 1;

-- Vérification
SELECT 'ZONES APRES NETTOYAGE :' AS '';
SELECT * FROM Zone;
SELECT 'REPARTITION SIGNALEMENTS PAR ZONE :' AS '';
SELECT z.nomZone, COUNT(*) AS nb FROM Signalement s JOIN Zone z ON s.idZone = z.idZone GROUP BY z.nomZone;
SELECT 'REPARTITION UTILISATEURS PAR ZONE :' AS '';
SELECT z.nomZone, COUNT(*) AS nb FROM Utilisateur u JOIN Zone z ON u.idZone = z.idZone GROUP BY z.nomZone;
