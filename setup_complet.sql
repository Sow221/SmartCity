-- ============================================================
-- SMARTCITY - SETUP COMPLET (nouveau membre equipe)
-- Usage : mysql -u <ton_user> -p < setup_complet.sql
-- Remplace <ton_user> par ton identifiant MySQL local
-- ============================================================

SET NAMES utf8mb4;
SET foreign_key_checks = 0;

-- ============================================================
-- 1. BASE DE DONNEES
-- ============================================================
DROP DATABASE IF EXISTS db_smartcity;
CREATE DATABASE db_smartcity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_smartcity;

-- ============================================================
-- 2. SCHEMA
-- ============================================================
CREATE TABLE Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10,8) NULL,
    longitude DECIMAL(11,8) NULL
);

CREATE TABLE Utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    prenom VARCHAR(100),
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    role ENUM('Citoyen','Agent','Administrateur') NOT NULL,
    age INT,
    localite VARCHAR(100),
    photoProfil VARCHAR(255),
    idZone INT,
    actif TINYINT(1) NOT NULL DEFAULT 1,
    dateInscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone)
);

CREATE TABLE Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie ENUM('Plastique','Papier','Verre','Métal','Organique','Autre'),
    idZone INT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    dateSignalement DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('En attente','Affecte','En cours','Termine') DEFAULT 'En attente',
    photo VARCHAR(255),
    idUser INT,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone),
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser)
);

CREATE TABLE Affectation (
    idAffectation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT UNIQUE,
    idAgent INT,
    dateAffectation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateCollecte DATETIME NULL,
    commentaire TEXT NULL,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

CREATE TABLE position_agent (
    idAgent INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
);

CREATE TABLE position_citoyen (
    idCitoyen INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser)
);

CREATE TABLE MissionEvent (
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

CREATE TABLE HistoriqueStatut (
    idHistorique INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT NOT NULL,
    ancienStatut VARCHAR(50),
    nouveauStatut VARCHAR(50) NOT NULL,
    idAuteur INT NULL,
    dateChangement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT NULL,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAuteur) REFERENCES Utilisateur(idUser) ON DELETE SET NULL,
    INDEX idx_hist_sig (idSignalement),
    INDEX idx_hist_date (dateChangement)
);

CREATE TABLE EvaluationCollecte (
    idEvaluation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT NOT NULL UNIQUE,
    idCitoyen INT NOT NULL,
    note TINYINT NOT NULL,
    commentaire TEXT NULL,
    dateEvaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);

-- ============================================================
-- 3. ZONES
-- ============================================================
INSERT INTO Zone (idZone, nomZone, latitude, longitude) VALUES
(1, 'Pikine',      14.7646, -17.3920),
(2, 'Guédiawaye',  14.7765, -17.4047);

-- ============================================================
-- 4. UTILISATEURS (mots de passe BCrypt)
-- admin123 / agent123 / citizen123
-- ============================================================
INSERT INTO Utilisateur (idUser, prenom, nom, email, motDePasse, role, age, localite, idZone, actif) VALUES
(1,  NULL,     'Administrateur',   'admin@smartcity.sn',             '$2a$10$k2pYI/rj3jxvkVWJFYNieexdMatKMvEBQeH0Jx.b6MbeLMzLpPKim', 'Administrateur', NULL, NULL,         1, 1),
(2,  'Ibrahima','Diop',            'agentpikine@smartcity.sn',       '$2a$10$UgeOgJw9aNUwIvhHgnrVHOYSU3OFqav4/S4EsEM8tMALb2oy.vEIi', 'Agent',          30,   'Pikine',     1, 1),
(3,  'Moussa',  'Sow',             'agentguediawaye@smartcity.sn',   '$2a$10$aruaxDTQj85j6QJHRt9wbeqjGuHorJbQd8PxP3EPLH3ZIahckCpHG', 'Agent',          28,   'Guédiawaye', 2, 1),
(4,  'Awa',     'Diop',            'citoyen@smartcity.sn',           '$2a$10$KINU/N5bTSKuTAu.jLVOE.YX0pn5kjvLCC3hMg9ksqj0yxsuGPzri', 'Citoyen',        22,   'Pikine',     1, 1),
(5,  'Oumar',   'Ba',              'citoyen2@smartcity.sn',          '$2a$10$KINU/N5bTSKuTAu.jLVOE.YX0pn5kjvLCC3hMg9ksqj0yxsuGPzri', 'Citoyen',        40,   'Guédiawaye', 2, 1);

-- ============================================================
-- 5. SIGNALEMENTS (20 signalements, statuts variés)
-- ============================================================
INSERT INTO Signalement (idSignalement, description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser) VALUES
(1,  'Tas de plastique devant l\'école',                    'Plastique', 1, 14.75580, -17.39050, DATE_SUB(NOW(), INTERVAL 30 HOUR),  'Affecte',   4),
(2,  'Déchets organiques au marché Tilène',                 'Organique', 1, 14.75320, -17.39280, DATE_SUB(NOW(), INTERVAL 6 DAY),    'En cours',  4),
(3,  'Bouteilles en verre cassées rue 12',                  'Verre',     1, 14.76000, -17.43000, DATE_SUB(NOW(), INTERVAL 10 DAY),   'Termine',   4),
(4,  'Papier et cartons brûlés',                            'Papier',    2, 14.78000, -17.41000, DATE_SUB(NOW(), INTERVAL 8 DAY),    'Termine',   5),
(5,  'Décharge sauvage près du stade',                      'Plastique', 1, 14.74000, -17.44000, DATE_SUB(NOW(), INTERVAL 7 DAY),    'Termine',   5),
(6,  'Sacs plastiques dans le canal',                       'Plastique', 2, 14.79000, -17.40000, DATE_SUB(NOW(), INTERVAL 5 HOUR),   'Affecte',   4),
(7,  'Déchets métalliques sur la route',                    'Métal',     1, 14.73000, -17.46000, DATE_SUB(NOW(), INTERVAL 4 DAY),    'En cours',  5),
(8,  'Restes alimentaires abandonnés',                      'Organique', 2, 14.80000, -17.39000, DATE_SUB(NOW(), INTERVAL 12 DAY),   'Termine',   4),
(9,  'Cartons mouillés',                                    'Papier',    1, 14.72000, -17.47000, DATE_SUB(NOW(), INTERVAL 2 HOUR),   'En attente',5),
(10, 'Bouteilles plastiques dans le parc',                  'Plastique', 2, 14.81000, -17.38000, DATE_SUB(NOW(), INTERVAL 9 DAY),    'Termine',   4),
(11, 'Verre brisé devant la mairie',                        'Verre',     1, 14.71000, -17.48000, DATE_SUB(NOW(), INTERVAL 3 DAY),    'En cours',  5),
(12, 'Déchets divers arrêt de bus',                         'Autre',     2, 14.82000, -17.37000, DATE_SUB(NOW(), INTERVAL 1 HOUR),   'En attente',4),
(13, 'Papiers gras sur la place',                           'Papier',    1, 14.70000, -17.49000, DATE_SUB(NOW(), INTERVAL 6 DAY),    'Termine',   5),
(14, 'Déchets organiques marché central',                   'Organique', 2, 14.83000, -17.36000, DATE_SUB(NOW(), INTERVAL 3 HOUR),   'En cours',  4),
(15, 'Canettes métalliques dispersées',                     'Métal',     1, 14.69000, -17.50000, DATE_SUB(NOW(), INTERVAL 2 DAY),    'En cours',  5),
(16, 'Déchets plastiques terrain vague',                    'Plastique', 2, 14.84000, -17.35000, DATE_SUB(NOW(), INTERVAL 4 HOUR),   'En attente',4),
(17, 'Verre et plastique mélangés',                         'Verre',     1, 14.68000, -17.51000, DATE_SUB(NOW(), INTERVAL 6 HOUR),   'En attente',5),
(18, 'Déchets organiques école',                            'Organique', 2, 14.85000, -17.34000, DATE_SUB(NOW(), INTERVAL 5 DAY),    'Termine',   4),
(19, 'Bouteilles en verre cassées Sam Notaire',             'Verre',     2, 14.78100, -17.39850, DATE_SUB(NOW(), INTERVAL 15 DAY),   'Termine',   5),
(20, 'Déchets divers parking',                              'Autre',     2, 14.86000, -17.33000, DATE_SUB(NOW(), INTERVAL 3 DAY),    'Termine',   4);

-- ============================================================
-- 6. AFFECTATIONS
-- ============================================================
INSERT INTO Affectation (idAffectation, idSignalement, idAgent, dateAffectation, dateCollecte, commentaire) VALUES
(1,  1,  2, DATE_SUB(NOW(), INTERVAL 28 HOUR), NULL,                                          NULL),
(2,  2,  2, DATE_SUB(NOW(), INTERVAL 6 DAY),   NULL,                                          'Agent en route'),
(3,  3,  2, DATE_SUB(NOW(), INTERVAL 10 DAY),  DATE_SUB(NOW(), INTERVAL 8 DAY),               'Collecte effectuée, zone nettoyée'),
(4,  4,  3, DATE_SUB(NOW(), INTERVAL 8 DAY),   DATE_SUB(NOW(), INTERVAL 7 DAY),               'Déchets organiques retirés'),
(5,  5,  2, DATE_SUB(NOW(), INTERVAL 7 DAY),   DATE_SUB(NOW(), INTERVAL 6 DAY),               'Mission accomplie'),
(6,  6,  3, DATE_SUB(NOW(), INTERVAL 4 HOUR),  NULL,                                          NULL),
(7,  7,  2, DATE_SUB(NOW(), INTERVAL 4 DAY),   NULL,                                          'Zone sécurisée après collecte'),
(8,  8,  3, DATE_SUB(NOW(), INTERVAL 12 DAY),  DATE_SUB(NOW(), INTERVAL 10 DAY),              'Collecte terminée en 3h'),
(9,  10, 3, DATE_SUB(NOW(), INTERVAL 9 DAY),   DATE_SUB(NOW(), INTERVAL 8 DAY),               NULL),
(10, 11, 2, DATE_SUB(NOW(), INTERVAL 3 DAY),   NULL,                                          NULL),
(11, 13, 2, DATE_SUB(NOW(), INTERVAL 6 DAY),   DATE_SUB(NOW(), INTERVAL 5 DAY),               'Déchets métalliques évacués'),
(12, 14, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR),  NULL,                                          NULL),
(13, 15, 2, DATE_SUB(NOW(), INTERVAL 2 DAY),   NULL,                                          NULL),
(14, 18, 3, DATE_SUB(NOW(), INTERVAL 5 DAY),   DATE_SUB(NOW(), INTERVAL 4 DAY),               'Verre collecté et trié'),
(15, 19, 3, DATE_SUB(NOW(), INTERVAL 15 DAY),  DATE_SUB(NOW(), INTERVAL 13 DAY),              'Verre collecté et sécurisé'),
(16, 20, 3, DATE_SUB(NOW(), INTERVAL 3 DAY),   DATE_SUB(NOW(), INTERVAL 2 DAY),               NULL);

-- ============================================================
-- 7. POSITIONS GPS AGENTS
-- ============================================================
INSERT INTO position_agent (idAgent, latitude, longitude, updatedAt) VALUES
(2, 14.75460, -17.39120, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(3, 14.77710, -17.40380, DATE_SUB(NOW(), INTERVAL 4 MINUTE));

-- ============================================================
-- 8. POSITIONS GPS CITOYENS
-- ============================================================
INSERT INTO position_citoyen (idCitoyen, latitude, longitude, updatedAt) VALUES
(4, 14.75180, -17.39420, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(5, 14.77650, -17.40470, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

-- ============================================================
-- 9. HISTORIQUE STATUTS
-- ============================================================
INSERT INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire) VALUES
(2,  'En attente', 'Affecte',  2, DATE_SUB(NOW(), INTERVAL 6 DAY),  'Affectation automatique'),
(2,  'Affecte',    'En cours', 2, DATE_SUB(NOW(), INTERVAL 5 DAY),  'Agent en route'),
(3,  'En attente', 'Affecte',  2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Affectation automatique'),
(3,  'Affecte',    'En cours', 2, DATE_SUB(NOW(), INTERVAL 9 DAY),  'Collecte démarrée'),
(3,  'En cours',   'Termine',  2, DATE_SUB(NOW(), INTERVAL 8 DAY),  'Zone nettoyée, déchets évacués'),
(7,  'En attente', 'Affecte',  2, DATE_SUB(NOW(), INTERVAL 4 DAY),  'Affectation automatique'),
(7,  'Affecte',    'En cours', 2, DATE_SUB(NOW(), INTERVAL 3 DAY),  'Intervention en cours'),
(8,  'En attente', 'Affecte',  3, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Affectation automatique'),
(8,  'Affecte',    'En cours', 3, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Collecte démarrée'),
(8,  'En cours',   'Termine',  3, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Papiers collectés et évacués'),
(19, 'En attente', 'Affecte',  3, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Affectation automatique'),
(19, 'Affecte',    'En cours', 3, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Collecte démarrée'),
(19, 'En cours',   'Termine',  3, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Verre collecté et sécurisé');

-- ============================================================
-- 10. EVALUATIONS CITOYENS
-- ============================================================
INSERT INTO EvaluationCollecte (idSignalement, idCitoyen, note, commentaire, dateEvaluation) VALUES
(3,  4, 5, 'Très rapide, zone parfaitement nettoyée',     DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4,  4, 4, 'Bon travail, quelques déchets résiduels',     DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5,  4, 5, 'Excellent service, agent très professionnel', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8,  5, 4, 'Collecte efficace',                           DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10, 5, 3, 'Correct mais délai un peu long',              DATE_SUB(NOW(), INTERVAL 9 DAY)),
(19, 5, 5, 'Parfait, zone sécurisée rapidement',          DATE_SUB(NOW(), INTERVAL 13 DAY)),
(13, 4, 4, 'Bonne intervention',                          DATE_SUB(NOW(), INTERVAL 5 DAY)),
(18, 4, 5, 'Très satisfait du service',                   DATE_SUB(NOW(), INTERVAL 4 DAY)),
(20, 4, 4, 'Rapide et efficace',                          DATE_SUB(NOW(), INTERVAL 3 DAY));

SET foreign_key_checks = 1;

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT 'Utilisateurs' as table_name, COUNT(*) as total FROM Utilisateur
UNION ALL SELECT 'Signalements', COUNT(*) FROM Signalement
UNION ALL SELECT 'Affectations', COUNT(*) FROM Affectation
UNION ALL SELECT 'HistoriqueStatut', COUNT(*) FROM HistoriqueStatut
UNION ALL SELECT 'EvaluationCollecte', COUNT(*) FROM EvaluationCollecte;

SELECT statut, COUNT(*) as total FROM Signalement GROUP BY statut;
