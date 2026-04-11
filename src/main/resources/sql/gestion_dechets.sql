-- Script SQL pour la base de données SmartCity Déchets
-- Création de la base de données

CREATE DATABASE IF NOT EXISTS db_smartcity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE db_smartcity;

SET NAMES utf8mb4;

-- Table Zone
CREATE TABLE IF NOT EXISTS Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL
);

-- Table Utilisateur (Citoyen, Agent, Administrateur)
CREATE TABLE IF NOT EXISTS Utilisateur (
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
CREATE TABLE IF NOT EXISTS Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie ENUM('Plastique', 'Papier', 'Verre', 'Métal', 'Organique', 'Autre'),
    idZone INT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    dateSignalement DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('En attente', 'Affecte', 'En cours', 'Termine') DEFAULT 'En attente',
    photo VARCHAR(255),
    idUser INT,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone),
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser)
);

-- Table Affectation (Agent -> Signalement)
CREATE TABLE IF NOT EXISTS Affectation (
    idAffectation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT UNIQUE,
    idAgent INT,
    dateAffectation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateCollecte DATETIME NULL,
    commentaire TEXT NULL,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

-- Insertion des zones avec coordonnees GPS reelles
INSERT IGNORE INTO Zone (nomZone, latitude, longitude) VALUES ('Pikine', 14.7646, -17.3920);
INSERT IGNORE INTO Zone (nomZone, latitude, longitude) VALUES ('Guédiawaye', 14.7765, -17.4047);

-- Insertion d'un administrateur par défaut (mot de passe: admin123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Administrateur', 'admin@smartcity.sn', '$2a$10$k2pYI/rj3jxvkVWJFYNieexdMatKMvEBQeH0Jx.b6MbeLMzLpPKim', 'Administrateur', 1);

-- Insertion d'agents par défaut (mot de passe: agent123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Agent Pikine', 'agentpikine@smartcity.sn', '$2a$10$UgeOgJw9aNUwIvhHgnrVHOYSU3OFqav4/S4EsEM8tMALb2oy.vEIi', 'Agent', 1);

INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Agent Guédiawaye', 'agentguediawaye@smartcity.sn', '$2a$10$aruaxDTQj85j6QJHRt9wbeqjGuHorJbQd8PxP3EPLH3ZIahckCpHG', 'Agent', 2);

-- Insertion d'un citoyen test (mot de passe: citizen123)
INSERT INTO Utilisateur (nom, email, motDePasse, role, idZone) 
VALUES ('Citoyen Test', 'citoyen@smartcity.sn', '$2a$10$KINU/N5bTSKuTAu.jLVOE.YX0pn5kjvLCC3hMg9ksqj0yxsuGPzri', 'Citoyen', 1);

-- Table position GPS temps réel des agents
CREATE TABLE IF NOT EXISTS position_agent (
    idAgent INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

-- Table position GPS temps réel des citoyens
CREATE TABLE IF NOT EXISTS position_citoyen (
    idCitoyen INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser)
);

-- Table événements mission (pour calcul temps réel de traitement par agent)
CREATE TABLE IF NOT EXISTS MissionEvent (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT NOT NULL,
    idAgent INT NOT NULL,
    evenement ENUM('demarre','termine') NOT NULL,
    dateEvenement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_agent (idAgent),
    INDEX idx_event_sig (idSignalement),
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);