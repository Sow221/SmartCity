-- Migration : Politique de suivi Citoyen → Administrateur
-- À exécuter sur db_smartcity existante

USE db_smartcity;

-- ============================================================
-- 1. HISTORIQUE DES CHANGEMENTS DE STATUT (audit trail)
-- ============================================================
CREATE TABLE IF NOT EXISTS HistoriqueStatut (
    idHistorique    INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement   INT NOT NULL,
    ancienStatut    VARCHAR(50),
    nouveauStatut   VARCHAR(50) NOT NULL,
    idAuteur        INT NULL,          -- NULL = système automatique
    dateChangement  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire     TEXT NULL,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idAuteur)      REFERENCES Utilisateur(idUser) ON DELETE SET NULL,
    INDEX idx_hist_sig  (idSignalement),
    INDEX idx_hist_date (dateChangement)
);

-- ============================================================
-- 2. ÉVALUATION CITOYEN après collecte (satisfaction)
-- ============================================================
CREATE TABLE IF NOT EXISTS EvaluationCollecte (
    idEvaluation    INT AUTO_INCREMENT PRIMARY KEY,
    idSignalement   INT NOT NULL UNIQUE,
    idCitoyen       INT NOT NULL,
    note            TINYINT NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire     TEXT NULL,
    dateEvaluation  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
    FOREIGN KEY (idCitoyen)     REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);

-- ============================================================
-- 3. TRIGGER : enregistrer automatiquement chaque changement
--    de statut dans HistoriqueStatut
-- ============================================================
DROP TRIGGER IF EXISTS trg_statut_change;

DELIMITER $$
CREATE TRIGGER trg_statut_change
AFTER UPDATE ON Signalement
FOR EACH ROW
BEGIN
    IF OLD.statut <> NEW.statut THEN
        INSERT INTO HistoriqueStatut (idSignalement, ancienStatut, nouveauStatut, dateChangement)
        VALUES (NEW.idSignalement, OLD.statut, NEW.statut, NOW());
    END IF;
END$$
DELIMITER ;

-- ============================================================
-- 4. VUE : suivi complet d'un signalement (citoyen + admin)
-- ============================================================
CREATE OR REPLACE VIEW vue_suivi_signalement AS
SELECT
    s.idSignalement,
    s.description,
    s.categorie,
    s.statut,
    s.dateSignalement,
    z.nomZone,
    u_citoyen.nom        AS nomCitoyen,
    u_citoyen.email      AS emailCitoyen,
    u_agent.nom          AS nomAgent,
    a.dateAffectation,
    a.dateCollecte,
    TIMESTAMPDIFF(HOUR, s.dateSignalement, IFNULL(a.dateCollecte, NOW())) AS heuresEcoules,
    CASE
        WHEN s.statut = 'Termine' AND a.dateCollecte IS NOT NULL
             THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte)
        ELSE NULL
    END AS heuresResolution,
    e.note               AS noteEvaluation,
    e.commentaire        AS commentaireEvaluation
FROM Signalement s
LEFT JOIN Zone        z         ON s.idZone       = z.idZone
LEFT JOIN Utilisateur u_citoyen ON s.idUser        = u_citoyen.idUser
LEFT JOIN Affectation a         ON s.idSignalement = a.idSignalement
LEFT JOIN Utilisateur u_agent   ON a.idAgent       = u_agent.idUser
LEFT JOIN EvaluationCollecte e  ON s.idSignalement = e.idSignalement;

-- ============================================================
-- 5. VUE : KPIs admin par zone et période
-- ============================================================
CREATE OR REPLACE VIEW vue_kpi_zone AS
SELECT
    z.nomZone,
    COUNT(s.idSignalement)                                          AS total,
    SUM(s.statut = 'En attente')                                    AS enAttente,
    SUM(s.statut = 'En cours')                                      AS enCours,
    SUM(s.statut = 'Termine')                                       AS termines,
    ROUND(SUM(s.statut = 'Termine') * 100.0 / NULLIF(COUNT(*), 0), 1) AS tauxResolution,
    ROUND(AVG(CASE WHEN s.statut = 'Termine' AND a.dateCollecte IS NOT NULL
                   THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte)
              END), 1)                                              AS tempsResolutionMoyenH,
    SUM(s.statut = 'En attente'
        AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS urgents
FROM Signalement s
LEFT JOIN Zone        z ON s.idZone       = z.idZone
LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement
GROUP BY z.idZone, z.nomZone;
