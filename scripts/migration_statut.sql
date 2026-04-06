-- Migration SmartCity - base réelle
USE db_smartcity;
SET NAMES utf8mb4;

-- Etape 1 : passer l'ENUM en VARCHAR temporairement pour éviter les conflits d'accents
ALTER TABLE Signalement MODIFY statut VARCHAR(50) DEFAULT 'En attente';

-- Etape 2 : normaliser toutes les valeurs existantes
UPDATE Signalement SET statut = 'En attente' WHERE statut IN ('En attente', 'Affect??', 'Affecté', 'Affecte');
UPDATE Signalement SET statut = 'En cours'   WHERE statut = 'En cours';
UPDATE Signalement SET statut = 'Terminé'    WHERE statut IN ('Terminé', 'Termin??', 'Termine', 'Collecte', 'Archivé', 'Archiv??');

-- Etape 3 : remettre l'ENUM propre avec utf8mb4
ALTER TABLE Signalement MODIFY statut 
    ENUM('En attente','En cours','Terminé') 
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    DEFAULT 'En attente';

-- Etape 4 : corriger la zone sans accent
UPDATE Zone SET nomZone = 'Guédiawaye' WHERE nomZone = 'Guediawaye';

-- Verification
SELECT 'STATUTS APRES MIGRATION :' AS '';
SELECT statut, COUNT(*) AS nb FROM Signalement GROUP BY statut;
SELECT 'ENUM FINAL :' AS '';
SHOW COLUMNS FROM Signalement LIKE 'statut';
SELECT 'ZONES :' AS '';
SELECT * FROM Zone;
