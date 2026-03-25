-- Script SQL pour la base de données SmartCity Déchets
-- Création de la base de données

CREATE DATABASE IF NOT EXISTS smartcity_dechets;
USE smartcity_dechets;

-- =====================================================
-- Table Utilisateur (Citoyen, Agent, Administrateur)
-- =====================================================
CREATE TABLE IF NOT EXISTS Utilisateur (
                                           idUser INT AUTO_INCREMENT PRIMARY KEY,
                                           nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    age INT,
    motDePasse VARCHAR(255) NOT NULL,
    localite VARCHAR(100),
    photoProfil VARCHAR(255),
    role ENUM('ADMIN','AGENT','CITOYEN') NOT NULL,
    dateInscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- Table Zone
-- =====================================================
CREATE TABLE IF NOT EXISTS Zone (
                                    idZone INT AUTO_INCREMENT PRIMARY KEY,
                                    nomZone VARCHAR(100) NOT NULL UNIQUE
    );

-- =====================================================
-- Table Signalement
-- =====================================================
CREATE TABLE IF NOT EXISTS Signalement (
                                           idSignalement INT AUTO_INCREMENT PRIMARY KEY,
                                           idZone INT NOT NULL,
                                           idUser INT NOT NULL,
                                           description VARCHAR(255) NOT NULL,
    categorie ENUM('PLASTIQUE','PAPIER','ORGANIQUE','VERRE') NOT NULL,
    dateSignalement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('EN_ATTENTE','EN_COURS','COLLECTE') NOT NULL DEFAULT 'EN_ATTENTE',
    photoDepot VARCHAR(255),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    FOREIGN KEY (idZone) REFERENCES Zone(idZone) ON DELETE CASCADE,
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
    );

-- =====================================================
-- Table Affectation (Agent -> Signalement)
-- =====================================================
CREATE TABLE IF NOT EXISTS Affectation (
                                           idAffectation INT AUTO_INCREMENT PRIMARY KEY,
                                           idSignalement INT NOT NULL UNIQUE,
                                           idAgent INT NOT NULL,
                                           dateAffectation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
    );

-- =====================================================
-- INSERTION DES DONNÉES DE TEST
-- =====================================================

-- Insertion des zones
INSERT INTO Zone (nomZone) VALUES
                               ('Dakar'),
                               ('Pikine'),
                               ('Guédiawaye'),

-- Insertion d'un administrateur (mot de passe: admin123)
INSERT INTO Utilisateur (nom, prenom, email, age, motDePasse, localite, role, dateInscription)
VALUES ('Admin', 'System', 'admin@email.sn', 35, 'admin123', 'Dakar', 'ADMIN', NOW());

-- Insertion d'agents (mot de passe: agent123)
INSERT INTO Utilisateur (nom, prenom, email, age, motDePasse, localite, role, dateInscription)
VALUES
    ('Diop', 'Moussa', 'agent@email.sn', 32, 'agent123', 'Pikine', 'AGENT', NOW()),

-- Insertion de citoyens (mot de passe: citoyen123)
INSERT INTO Utilisateur (nom, prenom, email, age, motDePasse, localite, role, dateInscription)
VALUES
    ('Diouf', 'Marie', 'citoyen@email.com', 35, 'citoyen123', 'Pikine', 'CITOYEN', NOW()),




ADMINISTRATEUR :
----------------
Email: admin@email.sn
Mot de passe: admin123
Rôle: ADMIN

AGENTS :
--------
1. Email: agent@email.sn | Mot de passe: agent123 | Zone: Pikine

CITOYENS :

2. Email: citoyen@email.com | Mot de passe: citoyen123 | Localité: Pikine


