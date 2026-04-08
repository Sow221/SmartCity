-- ============================================================
-- Migration: Index analytiques + colonne priorite
-- Améliore les performances des requêtes de reporting
-- ============================================================

USE db_smartcity;

-- Index sur date seule pour les requêtes GROUP BY DATE(dateSignalement)
-- MySQL 8.0+ supporte les index fonctionnels
ALTER TABLE Signalement
    ADD INDEX IF NOT EXISTS idx_signalement_date_only ((DATE(dateSignalement)));

-- Index composé zone+statut+date pour les filtres admin les plus fréquents
ALTER TABLE Signalement
    ADD INDEX IF NOT EXISTS idx_sig_zone_statut_date (idZone, statut, dateSignalement);

-- Index sur statut+date pour les requêtes de tendance
ALTER TABLE Signalement
    ADD INDEX IF NOT EXISTS idx_sig_statut_date (statut, dateSignalement);

-- Colonne priorite calculée automatiquement (Generated Column)
-- Critique: >72h en attente | Urgent: >24h | Normal: sinon
ALTER TABLE Signalement
    ADD COLUMN IF NOT EXISTS priorite VARCHAR(10)
    GENERATED ALWAYS AS (
        CASE
            WHEN statut = 'En attente' AND TIMESTAMPDIFF(HOUR, dateSignalement, NOW()) > 72 THEN 'Critique'
            WHEN statut = 'En attente' AND TIMESTAMPDIFF(HOUR, dateSignalement, NOW()) > 24 THEN 'Urgent'
            ELSE 'Normal'
        END
    ) VIRTUAL;

-- Index sur priorite pour les alertes dashboard
ALTER TABLE Signalement
    ADD INDEX IF NOT EXISTS idx_signalement_priorite (priorite);

-- Vérification
SELECT
    statut,
    priorite,
    COUNT(*) AS nb
FROM Signalement
GROUP BY statut, priorite
ORDER BY statut, priorite;

SELECT 'Index analytiques + priorite OK' AS status;
