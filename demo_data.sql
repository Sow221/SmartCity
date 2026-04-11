-- ============================================================
-- SCRIPT DEMO SMARTCITY - Données réelles Pikine & Guediawaye
-- Coordonnées GPS vérifiées sur OpenStreetMap
-- ============================================================

SET NAMES utf8mb4;
SET foreign_key_checks = 0;

-- ============================================================
-- 1. POSITIONS GPS AGENTS (positions réelles dans leurs zones)
-- ============================================================
-- Agent Diop (idUser=2) : Pikine - Marché Tilène
INSERT INTO position_agent (idAgent, latitude, longitude, updatedAt)
VALUES (2, 14.75520, -17.39180, DATE_SUB(NOW(), INTERVAL 8 MINUTE))
ON DUPLICATE KEY UPDATE latitude=14.75520, longitude=-17.39180, updatedAt=DATE_SUB(NOW(), INTERVAL 8 MINUTE);

-- Agent Sow (idUser=3) : Guediawaye - Cité Fadia
INSERT INTO position_agent (idAgent, latitude, longitude, updatedAt)
VALUES (3, 14.77420, -17.40310, DATE_SUB(NOW(), INTERVAL 3 MINUTE))
ON DUPLICATE KEY UPDATE latitude=14.77420, longitude=-17.40310, updatedAt=DATE_SUB(NOW(), INTERVAL 3 MINUTE);

-- Agent test1 (idUser=39) : Guediawaye - Sam Notaire
INSERT INTO position_agent (idAgent, latitude, longitude, updatedAt)
VALUES (39, 14.78050, -17.39920, DATE_SUB(NOW(), INTERVAL 45 MINUTE))
ON DUPLICATE KEY UPDATE latitude=14.78050, longitude=-17.39920, updatedAt=DATE_SUB(NOW(), INTERVAL 45 MINUTE);

-- ============================================================
-- 2. POSITIONS GPS CITOYENS (pour démo QR code)
-- ============================================================
-- Citoyen Diop (idUser=4) : Pikine - Quartier Guinaw Rails
INSERT INTO position_citoyen (idCitoyen, latitude, longitude, updatedAt)
VALUES (4, 14.75180, -17.39420, DATE_SUB(NOW(), INTERVAL 2 MINUTE))
ON DUPLICATE KEY UPDATE latitude=14.75180, longitude=-17.39420, updatedAt=DATE_SUB(NOW(), INTERVAL 2 MINUTE);

-- Citoyen Ba (idUser=5) : Guediawaye - Cité Fadia
INSERT INTO position_citoyen (idCitoyen, latitude, longitude, updatedAt)
VALUES (5, 14.77650, -17.40470, DATE_SUB(NOW(), INTERVAL 5 MINUTE))
ON DUPLICATE KEY UPDATE latitude=14.77650, longitude=-17.40470, updatedAt=DATE_SUB(NOW(), INTERVAL 5 MINUTE);

-- ============================================================
-- 3. METTRE A JOUR dateCollecte sur les signalements Termines
--    (nécessaire pour les stats de temps de traitement)
-- ============================================================
UPDATE Affectation a
JOIN Signalement s ON a.idSignalement = s.idSignalement
SET a.dateCollecte = DATE_ADD(s.dateSignalement, INTERVAL FLOOR(2 + RAND()*22) HOUR)
WHERE s.statut = 'Termine' AND a.dateCollecte IS NULL;

-- ============================================================
-- 4. COMMENTAIRES D'AFFECTATION (colonne Agent dans admin)
-- ============================================================
UPDATE Affectation SET commentaire = 'Collecte effectuée, zone nettoyée' WHERE idAffectation = 6;
UPDATE Affectation SET commentaire = 'Déchets organiques retirés' WHERE idAffectation = 7;
UPDATE Affectation SET commentaire = 'Mission accomplie' WHERE idAffectation = 8;
UPDATE Affectation SET commentaire = 'Zone sécurisée après collecte' WHERE idAffectation = 9;
UPDATE Affectation SET commentaire = 'Collecte terminée en 3h' WHERE idAffectation = 11;
UPDATE Affectation SET commentaire = 'Déchets métalliques évacués' WHERE idAffectation = 13;
UPDATE Affectation SET commentaire = 'Verre collecté et trié' WHERE idAffectation = 14;

-- ============================================================
-- 5. NOUVEAUX SIGNALEMENTS avec vraies coordonnées GPS
--    Pikine : autour de 14.752 / -17.393
--    Guediawaye : autour de 14.776 / -17.403
--    Affectation automatique simulée (comme l'app le fait)
-- ============================================================

-- Signalement Pikine - Plastique urgent (>24h)
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Accumulation de sacs plastiques devant le marche Tilene', 'Plastique', 1, 14.75580, -17.39050, DATE_SUB(NOW(), INTERVAL 30 HOUR), 'En attente', 4);

-- Signalement Pikine - Organique
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Dechets organiques pres de la mosquee centrale', 'Organique', 1, 14.75320, -17.39280, DATE_SUB(NOW(), INTERVAL 5 HOUR), 'En attente', 4);

-- Signalement Guediawaye - Verre urgent (>72h)
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Bouteilles en verre cassees sur la voie publique Sam Notaire', 'Verre', 2, 14.78100, -17.39850, DATE_SUB(NOW(), INTERVAL 80 HOUR), 'En attente', 5);

-- Signalement Guediawaye - Metal
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Ferraille abandonnee cite Fadia bloc 3', 'Metal', 2, 14.77380, -17.40280, DATE_SUB(NOW(), INTERVAL 12 HOUR), 'En attente', 5);

-- Signalement Pikine - Papier (citoyen testeurA)
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Cartons et papiers entassés rue 10 Pikine', 'Papier', 1, 14.75420, -17.39320, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'En attente', 40);

-- Signalement Guediawaye - Autre (citoyen Mamadou)
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, idUser)
VALUES ('Dechets divers devant ecole primaire Guediawaye', 'Autre', 2, 14.77820, -17.40150, DATE_SUB(NOW(), INTERVAL 18 HOUR), 'En attente', 18);

-- ============================================================
-- 6. AFFECTATION AUTOMATIQUE des nouveaux signalements
--    Agent Diop (id=2) pour Pikine, Agent Sow (id=3) pour Guediawaye
-- ============================================================

-- Récupérer les IDs des nouveaux signalements et les affecter
INSERT INTO Affectation (idSignalement, idAgent, dateAffectation)
SELECT s.idSignalement, 2, NOW()
FROM Signalement s
LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement
WHERE s.idZone = 1 AND s.statut = 'En attente' AND a.idAffectation IS NULL;

INSERT INTO Affectation (idSignalement, idAgent, dateAffectation)
SELECT s.idSignalement, 3, NOW()
FROM Signalement s
LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement
WHERE s.idZone = 2 AND s.statut = 'En attente' AND a.idAffectation IS NULL;

-- Passer les signalements nouvellement affectés en statut 'Affecte'
UPDATE Signalement s
INNER JOIN Affectation a ON s.idSignalement = a.idSignalement
SET s.statut = 'Affecte'
WHERE s.statut = 'En attente';

-- ============================================================
-- 7. HISTORIQUE DES STATUTS (pour double-clic citoyen)
-- ============================================================
CREATE TABLE IF NOT EXISTS HistoriqueStatut (
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

-- Historique pour signalement 2 (En cours - agent Diop)
INSERT IGNORE INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire)
VALUES
(2, 'En attente', 'Affecte',  2, DATE_SUB(NOW(), INTERVAL 6 DAY), 'Affectation automatique'),
(2, 'Affecte',   'En cours',  2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Agent en route');

-- Historique pour signalement 3 (Termine)
INSERT IGNORE INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire)
VALUES
(3, 'En attente', 'Affecte',  2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Affectation automatique'),
(3, 'Affecte',   'En cours',  2, DATE_SUB(NOW(), INTERVAL 9 DAY),  'Collecte démarrée'),
(3, 'En cours',  'Termine',   2, DATE_SUB(NOW(), INTERVAL 8 DAY),  'Zone nettoyée, déchets évacués');

-- Historique pour signalement 7 (En cours - agent Sow)
INSERT IGNORE INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire)
VALUES
(7, 'En attente', 'Affecte',  3, DATE_SUB(NOW(), INTERVAL 4 DAY), 'Affectation automatique'),
(7, 'Affecte',   'En cours',  3, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Intervention en cours');

-- Historique pour signalement 8 (Termine)
INSERT IGNORE INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire)
VALUES
(8, 'En attente', 'Affecte',  3, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Affectation automatique'),
(8, 'Affecte',   'En cours',  3, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Collecte démarrée'),
(8, 'En cours',  'Termine',   3, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Papiers collectés et évacués');

-- Historique pour signalement 19 (Termine)
INSERT IGNORE INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, idAuteur, dateChangement, commentaire)
VALUES
(19, 'En attente', 'Affecte', 3, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Affectation automatique'),
(19, 'Affecte',   'En cours', 3, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Collecte démarrée'),
(19, 'En cours',  'Termine',  3, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Verre collecté et sécurisé');

-- ============================================================
-- 8. EVALUATIONS CITOYENS (pour colonne étoiles)
-- ============================================================
CREATE TABLE IF NOT EXISTS EvaluationCollecte (
    idEvaluation INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement INT NOT NULL UNIQUE,
    idCitoyen INT NOT NULL,
    note TINYINT NOT NULL,
    commentaire TEXT NULL,
    dateEvaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);

INSERT IGNORE INTO EvaluationCollecte (idSignalement, idCitoyen, note, commentaire, dateEvaluation)
VALUES
(3,  4, 5, 'Très rapide, zone parfaitement nettoyée',        DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4,  4, 4, 'Bon travail, quelques déchets résiduels',        DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5,  4, 5, 'Excellent service, agent très professionnel',    DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8,  5, 4, 'Collecte efficace',                              DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10, 5, 3, 'Correct mais délai un peu long',                 DATE_SUB(NOW(), INTERVAL 9 DAY)),
(19, 5, 5, 'Parfait, zone sécurisée rapidement',             DATE_SUB(NOW(), INTERVAL 13 DAY)),
(13, 4, 4, 'Bonne intervention',                             DATE_SUB(NOW(), INTERVAL 5 DAY)),
(18, 4, 5, 'Très satisfait du service',                      DATE_SUB(NOW(), INTERVAL 4 DAY)),
(20, 4, 4, 'Rapide et efficace',                             DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================================
-- 9. VERIFICATION FINALE
-- ============================================================
SELECT 'SIGNALEMENTS PAR STATUT' as info;
SELECT statut, COUNT(*) as total FROM Signalement GROUP BY statut;

SELECT 'SIGNALEMENTS PAR ZONE' as info;
SELECT z.nomZone, COUNT(*) as total FROM Signalement s JOIN Zone z ON s.idZone=z.idZone GROUP BY z.nomZone;

SELECT 'MISSIONS PAR AGENT' as info;
SELECT u.nom, u.email, COUNT(a.idAffectation) as missions FROM Utilisateur u
LEFT JOIN Affectation a ON u.idUser=a.idAgent
WHERE u.role='Agent' AND u.actif=1 GROUP BY u.idUser;

SELECT 'POSITIONS AGENTS' as info;
SELECT u.nom, pa.latitude, pa.longitude, pa.updatedAt FROM position_agent pa
JOIN Utilisateur u ON pa.idAgent=u.idUser;

SELECT 'EVALUATIONS' as info;
SELECT COUNT(*) as total_evaluations, ROUND(AVG(note),1) as note_moyenne FROM EvaluationCollecte;

SELECT 'HISTORIQUE' as info;
SELECT COUNT(*) as total_entrees FROM HistoriqueStatut;

SET foreign_key_checks = 1;
