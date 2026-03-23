-- Script de réinitialisation de la base de données SmartCity
-- À exécuter via : mysql -u root -p77884455 < reset_database.sql

-- 1. Supprimer l'ancienne base de données
DROP DATABASE IF EXISTS db_smartcity;

-- 2. Recréer la base de données
CREATE DATABASE db_smartcity;

USE db_smartcity;

-- 3. Créer les tables avec le nouveau schéma

-- Table Zone
CREATE TABLE Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL UNIQUE
);

-- Table Utilisateur (Citoyen, Agent, Administrateur)
CREATE TABLE Utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    prenom VARCHAR(100),
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    role ENUM('Citoyen', 'Agent', 'Administrateur') NOT NULL,
    age INT,
    localite VARCHAR(100),
    photoProfil VARCHAR(255),
    idZone INT,
    actif TINYINT(1) NOT NULL DEFAULT 1,
    dateInscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone)
);

-- Table Signalement
CREATE TABLE Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie ENUM('Plastique', 'Papier', 'Verre', 'Métal', 'Organique', 'Autre'),
    idZone INT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    dateSignalement DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('En attente', 'Affecté', 'En cours', 'Terminé') DEFAULT 'En attente',
    photo VARCHAR(255),
    idUser INT,
    zoneNom VARCHAR(100),
    utilisateurNom VARCHAR(100),
    FOREIGN KEY (idZone) REFERENCES Zone(idZone),
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser)
);

-- Table Affectation (Agent -> Signalement)
CREATE TABLE Affectation (
    idAffectation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT,
    idAgent INT,
    dateAffectation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement),
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

-- 4. Insérer les données initiales

-- Insertion des zones
INSERT INTO Zone (nomZone) VALUES ('Pikine'), ('Guédiawaye');

-- Insertion d'un administrateur par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Administrateur', 'admin@smartcity.sn', 'admin123', 'Administrateur', 1);

-- Insertion d'agents par défaut (mot de passe: agent123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Agent Pikine', 'agentpikine@smartcity.sn', 'agent123', 'Agent', 1);

INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Agent Guédiawaye', 'agentguediawaye@smartcity.sn', 'agent123', 'Agent', 2);

-- Insertion d'un citoyen test (mot de passe: citizen123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Citoyen Test', 'citoyen@smartcity.sn', 'citizen123', 'Citoyen', 1);

-- Affichage de confirmation
SELECT 'Base de données réinitialisée avec succès!' as Status;
SELECT COUNT(*) as 'Nombre de zones' FROM Zone;
SELECT COUNT(*) as 'Nombre d\'utilisateurs' FROM Utilisateur;