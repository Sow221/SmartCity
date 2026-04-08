-- Migration script to update the database from old schema to new elmor schema
-- Run this on the existing 'db_smartcity' database

USE db_smartcity;

-- Add Zone table
CREATE TABLE IF NOT EXISTS Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL UNIQUE
);

-- Insert zones
INSERT IGNORE INTO Zone (nomZone) VALUES ('Pikine'), ('Guédiawaye');

-- Alter Utilisateur table
ALTER TABLE Utilisateur
ADD COLUMN prenom VARCHAR(100) AFTER idUser,
ADD COLUMN age INT AFTER role,
ADD COLUMN localite VARCHAR(100) AFTER age,
ADD COLUMN photoProfil VARCHAR(255) AFTER localite,
ADD COLUMN idZone INT AFTER photoProfil,
ADD CONSTRAINT fk_utilisateur_zone FOREIGN KEY (idZone) REFERENCES Zone(idZone);

-- Rename motPasse to motDePasse
ALTER TABLE Utilisateur CHANGE motPasse motDePasse VARCHAR(255) NOT NULL;

-- Remove telephone column
ALTER TABLE Utilisateur DROP COLUMN telephone;

-- Update idZone based on old zone values
UPDATE Utilisateur SET idZone = (SELECT idZone FROM Zone WHERE nomZone = Utilisateur.zone LIMIT 1);

-- Remove old zone column
ALTER TABLE Utilisateur DROP COLUMN zone;

-- Alter Signalement table
ALTER TABLE Signalement
ADD COLUMN idZone INT AFTER categorie,
ADD COLUMN latitude DECIMAL(10, 8) DEFAULT 0.0,
ADD COLUMN longitude DECIMAL(11, 8) DEFAULT 0.0,
ADD CONSTRAINT fk_signalement_zone FOREIGN KEY (idZone) REFERENCES Zone(idZone);

-- Update idZone based on old zone values
UPDATE Signalement SET idZone = (SELECT idZone FROM Zone WHERE nomZone = Signalement.zone LIMIT 1);

-- Remove old zone column
ALTER TABLE Signalement DROP COLUMN zone;

-- Drop dechet table
DROP TABLE IF EXISTS dechet;

-- Update existing users to have idZone
-- Assuming old users have zone 'Pikine' or 'Guédiawaye'
UPDATE Utilisateur SET idZone = 1 WHERE idZone IS NULL AND role = 'Citoyen'; -- Default to Pikine for citoyens
UPDATE Utilisateur SET idZone = (SELECT idZone FROM Zone WHERE nomZone = 'Pikine') WHERE idZone IS NULL;

-- Ensure all utilisateurs have idZone
UPDATE Utilisateur SET idZone = 1 WHERE idZone IS NULL;