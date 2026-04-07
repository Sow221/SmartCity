-- Correction des coordonnées GPS des signalements de test
-- Pikine centre : 14.7646, -17.3920
-- Guédiawaye centre : 14.7765, -17.4047
-- Ces coordonnées sont des positions réelles dans les deux communes

USE db_smartcity;

-- Signalements zone Pikine (idZone=1) : positions réelles dans Pikine
UPDATE Signalement SET latitude = 14.7612, longitude = -17.3891 WHERE idSignalement = 1;
UPDATE Signalement SET latitude = 14.7658, longitude = -17.3945 WHERE idSignalement = 3;
UPDATE Signalement SET latitude = 14.7634, longitude = -17.3872 WHERE idSignalement = 5;
UPDATE Signalement SET latitude = 14.7601, longitude = -17.3910 WHERE idSignalement = 7;
UPDATE Signalement SET latitude = 14.7678, longitude = -17.3856 WHERE idSignalement = 9;
UPDATE Signalement SET latitude = 14.7590, longitude = -17.3934 WHERE idSignalement = 11;
UPDATE Signalement SET latitude = 14.7623, longitude = -17.3867 WHERE idSignalement = 13;
UPDATE Signalement SET latitude = 14.7645, longitude = -17.3901 WHERE idSignalement = 15;
UPDATE Signalement SET latitude = 14.7667, longitude = -17.3923 WHERE idSignalement = 17;
UPDATE Signalement SET latitude = 14.7589, longitude = -17.3878 WHERE idSignalement = 19;

-- Signalements zone Guédiawaye (idZone=2) : positions réelles dans Guédiawaye
UPDATE Signalement SET latitude = 14.7789, longitude = -17.4012 WHERE idSignalement = 2;
UPDATE Signalement SET latitude = 14.7745, longitude = -17.4067 WHERE idSignalement = 4;
UPDATE Signalement SET latitude = 14.7812, longitude = -17.3989 WHERE idSignalement = 6;
UPDATE Signalement SET latitude = 14.7756, longitude = -17.4034 WHERE idSignalement = 8;
UPDATE Signalement SET latitude = 14.7823, longitude = -17.4078 WHERE idSignalement = 10;
UPDATE Signalement SET latitude = 14.7734, longitude = -17.3956 WHERE idSignalement = 12;
UPDATE Signalement SET latitude = 14.7801, longitude = -17.4023 WHERE idSignalement = 14;
UPDATE Signalement SET latitude = 14.7768, longitude = -17.4089 WHERE idSignalement = 16;
UPDATE Signalement SET latitude = 14.7845, longitude = -17.3967 WHERE idSignalement = 18;
UPDATE Signalement SET latitude = 14.7712, longitude = -17.4045 WHERE idSignalement = 20;

-- Vérification
SELECT idSignalement, categorie, idZone, latitude, longitude, statut
FROM Signalement
ORDER BY idZone, idSignalement;
