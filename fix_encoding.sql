-- Script de correction de l'encodage UTF-8 pour MySQL
-- Résout le problème des caractères spéciaux français

-- Supprimer et recréer la base avec UTF-8
DROP DATABASE IF EXISTS db_smartcity;
CREATE DATABASE db_smartcity 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE db_smartcity;

-- =============================================
-- CRÉATION DES TABLES AVEC UTF-8
-- =============================================

-- Table Zone avec support UTF-8
CREATE TABLE Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Utilisateur avec support UTF-8  
CREATE TABLE Utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    email VARCHAR(150) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    role ENUM('Citoyen', 'Agent', 'Administrateur') NOT NULL,
    idZone INT,
    actif BOOLEAN DEFAULT TRUE,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Signalement avec support UTF-8
CREATE TABLE Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8) DEFAULT 0,
    longitude DECIMAL(11, 8) DEFAULT 0,
    dateSignalement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('En attente', 'Affecté', 'En cours', 'Terminé', 'Collecte', 'Archivé') DEFAULT 'En attente',
    photo LONGTEXT,
    idUser INT NOT NULL,
    idZone INT NOT NULL,
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser),
    FOREIGN KEY (idZone) REFERENCES Zone(idZone)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Affectation avec support UTF-8
CREATE TABLE Affectation (
    idAffectation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT NOT NULL,
    idAgent INT NOT NULL,
    dateAffectation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement),
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================
-- INSERTION DES DONNÉES AVEC ENCODAGE CORRECT
-- =============================================

-- Zones (avec accents français)
INSERT INTO Zone (nomZone) VALUES 
('Pikine'),
('Guédiawaye');

-- Utilisateurs (avec accents français)
INSERT INTO Utilisateur (nom, prenom, email, motDePasse, role, idZone) VALUES
('Administrateur', 'Système', 'admin@smartcity.sn', 'admin123', 'Administrateur', 1),
('Diop', 'Amadou', 'agentpikine@smartcity.sn', 'agent123', 'Agent', 1),
('Sarr', 'Fatou', 'agentguediawaye@smartcity.sn', 'agent123', 'Agent', 2),
('Ndiaye', 'Moussa', 'citoyen@smartcity.sn', 'citizen123', 'Citoyen', 1);

-- Signalements de test (avec accents français)
INSERT INTO Signalement (description, categorie, latitude, longitude, statut, idUser, idZone) VALUES
('Dépôt sauvage près du marché central', 'Dépôt sauvage', 14.7549, -17.3925, 'En attente', 4, 1),
('Poubelle débordante à la cité', 'Débordement', 14.7692, -17.4281, 'En attente', 4, 2),
('Ramassage manqué depuis 3 jours', 'Collecte manquée', 14.7580, -17.3950, 'En cours', 4, 1);

-- Affectations de test
INSERT INTO Affectation (idSignalement, idAgent, dateAffectation) VALUES
(3, 2, NOW());

-- =============================================
-- INDEX DE PERFORMANCE (UTF-8 compatible)
-- =============================================

-- Index pour les recherches fréquentes
CREATE INDEX idx_signalement_zone_date ON Signalement(idZone, dateSignalement);
CREATE INDEX idx_signalement_statut ON Signalement(statut);
CREATE INDEX idx_signalement_user_date ON Signalement(idUser, dateSignalement);
CREATE INDEX idx_signalement_categorie ON Signalement(categorie);
CREATE INDEX idx_user_role_zone ON Utilisateur(role, idZone);
CREATE INDEX idx_user_email ON Utilisateur(email);
CREATE INDEX idx_user_actif ON Utilisateur(actif);
CREATE INDEX idx_affectation_agent ON Affectation(idAgent);
CREATE INDEX idx_affectation_signalement ON Affectation(idSignalement);
CREATE INDEX idx_affectation_date ON Affectation(dateAffectation);

-- =============================================
-- VUES AVEC SUPPORT UTF-8
-- =============================================

-- Vue complète des signalements
CREATE VIEW v_signalements_complets AS
SELECT 
    s.idSignalement,
    s.description,
    s.categorie,
    s.latitude,
    s.longitude,
    s.dateSignalement,
    s.statut,
    s.photo,
    s.idUser,
    s.idZone,
    z.nomZone,
    CONCAT(COALESCE(u.prenom, ''), ' ', u.nom) as utilisateurComplet,
    u.nom as utilisateurNom,
    u.email as utilisateurEmail,
    u.role as utilisateurRole,
    TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) as ancienneteHeures,
    CASE 
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 72 THEN 'Critique'
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 24 THEN 'Urgent' 
        ELSE 'Normal'
    END as priorite
FROM Signalement s
LEFT JOIN Zone z ON s.idZone = z.idZone
LEFT JOIN Utilisateur u ON s.idUser = u.idUser;

-- Vue des statistiques par zone
CREATE VIEW v_stats_zones AS
SELECT 
    z.idZone,
    z.nomZone,
    COUNT(s.idSignalement) as totalSignalements,
    SUM(CASE WHEN s.statut = 'En attente' THEN 1 ELSE 0 END) as enAttente,
    SUM(CASE WHEN s.statut = 'En cours' THEN 1 ELSE 0 END) as enCours,
    SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) as termines,
    COUNT(DISTINCT s.idUser) as utilisateursActifs,
    CASE 
        WHEN COUNT(s.idSignalement) > 0 
        THEN ROUND((SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) * 100.0 / COUNT(s.idSignalement)), 2)
        ELSE 0 
    END as tauxResolution
FROM Zone z
LEFT JOIN Signalement s ON z.idZone = s.idZone
GROUP BY z.idZone, z.nomZone;

-- Vue dashboard temps réel
CREATE VIEW v_dashboard_realtime AS
SELECT 
    (SELECT COUNT(*) FROM Signalement) as totalSignalements,
    (SELECT COUNT(*) FROM Signalement WHERE DATE(dateSignalement) = CURDATE()) as signalementsAujourdhui,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'En attente') as enAttente,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'En cours') as enCours,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'Terminé') as termines,
    (SELECT COUNT(*) FROM Utilisateur WHERE actif = 1) as utilisateursActifs,
    (SELECT COUNT(*) FROM Utilisateur WHERE role = 'Agent' AND actif = 1) as agentsActifs,
    (SELECT COUNT(*) FROM Utilisateur WHERE role = 'Citoyen' AND actif = 1) as citoyensActifs,
    (SELECT COUNT(*) FROM Signalement 
     WHERE statut = 'En attente' 
     AND dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)) as signalementsUrgents;

-- =============================================
-- VÉRIFICATION DE L'ENCODAGE
-- =============================================

-- Tester les caractères français
SELECT 
    'Test encodage UTF-8 : éàùçôî' as test_caracteres,
    COUNT(*) as total_signalements,
    (SELECT nomZone FROM Zone WHERE idZone = 2) as zone_guediawaye
FROM Signalement;