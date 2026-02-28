-- Script SQL pour la base de données SmartCity Déchets
-- Création de la base de données

CREATE DATABASE IF NOT EXISTS db_smartcity;

USE db_smartcity;

-- Table Utilisateur (Citoyen, Agent, Administrateur)
CREATE TABLE IF NOT EXISTS Utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    motPasse VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    zone VARCHAR(50),
    telephone VARCHAR(30),
    actif TINYINT(1) NOT NULL DEFAULT 1,
    dateInscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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

-- Table Dechet (pour signalements - version alternative)
CREATE TABLE IF NOT EXISTS dechet (
    idDechet INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie VARCHAR(50),
    zone VARCHAR(50),
    quartier VARCHAR(100),
    photo VARCHAR(255),
    statut VARCHAR(20),
    dateSignalement DATETIME,
    idUtilisateur INT,
    FOREIGN KEY (idUtilisateur) REFERENCES Utilisateur(idUser)
);

-- Insertion d'un administrateur par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Administrateur', 'admin@smartcity.sn', 'admin123', 'Administrateur', 'Pikine');

-- Insertion d'agents par défaut (mot de passe: agent123)
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Agent Pikine', 'agentpikine@smartcity.sn', 'agent123', 'Agent', 'Pikine');

INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Agent Guédiawaye', 'agentguediawaye@smartcity.sn', 'agent123', 'Agent', 'Guédiawaye');

-- Insertion d'un citoyen test (mot de passe: citizen123)
INSERT INTO Utilisateur (nom, email, motPasse, role, zone) 
VALUES ('Citoyen Test', 'citoyen@smartcity.sn', 'citizen123', 'Citoyen', 'Pikine');
