-- Script SQL pour la base de données SmartCity Déchets
-- Création de la base de données

CREATE DATABASE IF NOT EXISTS db_sc;

USE db_sc;

-- Table Utilisateur (Citoyen, Agent, Administrateur)
CREATE TABLE IF NOT EXISTS Utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    motPasse VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    zone VARCHAR(50)
);

-- Table Signalement (Déchets)
CREATE TABLE IF NOT EXISTS Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie VARCHAR(50),
    zone VARCHAR(50),
    dateSignalement DATETIME,
    statut VARCHAR(20),
    photo VARCHAR(255),
    idUser INT,
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser)
);

-- Table Affectation (Agent -> Signalement)
CREATE TABLE IF NOT EXISTS Affectation (
    idAffectation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT,
    idAgent INT,
    dateAffectation DATETIME,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement),
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

-- Insertion d'un administrateur par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Administrateur', 'admin@smartcity.sn', '$2a$10$8K1p/a0dL3.XjvLdL.5v/.mJ5P8Z5vQ5vQ5vQ5vQ5vQ5vQ5vQ5u', 'Administrateur', 'Pikine');

-- Insertion d'agents par défaut
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Agent Pikine', 'agentpikine@smartcity.sn', '$2a$10$8K1p/a0dL3.XjvLdL.5v/.mJ5P8Z5vQ5vQ5vQ5vQ5vQ5vQ5vQ5u', 'Agent', 'Pikine');

INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Agent Guédiawaye', 'agentguédiawaye@smartcity.sn', '$2a$10$8K1p/a0dL3.XjvLdL.5v/.mJ5P8Z5vQ5vQ5vQ5vQ5vQ5vQ5vQ5u', 'Agent', 'Guédiawaye');

-- Insertion d'un citoyen test
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Citoyen Test', 'citoyen@smartcity.sn', '$2a$10$8K1p/a0dL3.XjvLdL.5v/.mJ5P8Z5vQ5vQ5vQ5vQ5vQ5vQ5vQ5u', 'Citoyen', 'Pikine');

