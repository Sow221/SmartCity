-- ============================================================
-- Migration: Harmonisation complète des ENUMs et vues
-- Problème: incohérence 'Terminé' vs 'Termine', 'Affecté' vs 'Affecte'
-- Référence unique: valeurs SANS accents (cohérentes avec le code Java)
-- ============================================================

USE db_smartcity;

-- 1. Corriger les données existantes qui auraient des valeurs avec accents
UPDATE Signalement SET statut = 'Termine'  WHERE statut IN ('Terminé', 'Termine', 'Résolu', 'Collecté');
UPDATE Signalement SET statut = 'Affecte'  WHERE statut IN ('Affecté', 'Affectée');
UPDATE Signalement SET statut = 'En cours' WHERE statut = 'En Cours';
UPDATE Signalement SET statut = 'En attente' WHERE statut IN ('En Attente', 'en attente');

-- 2. Normaliser l'ENUM (sans accents, cohérent avec SignalementStatut.java)
ALTER TABLE Signalement
  MODIFY COLUMN statut ENUM('En attente','Affecte','En cours','Termine')
  NOT NULL DEFAULT 'En attente';

-- 3. Corriger les vues pour utiliser les bonnes valeurs
CREATE OR REPLACE VIEW v_stats_zones AS
SELECT
    z.idZone,
    z.nomZone,
    COUNT(s.idSignalement)                                                        AS totalSignalements,
    SUM(CASE WHEN s.statut = 'En attente' THEN 1 ELSE 0 END)                     AS enAttente,
    SUM(CASE WHEN s.statut = 'Affecte'   THEN 1 ELSE 0 END)                     AS affectes,
    SUM(CASE WHEN s.statut = 'En cours'  THEN 1 ELSE 0 END)                     AS enCours,
    SUM(CASE WHEN s.statut = 'Termine'   THEN 1 ELSE 0 END)                     AS termines,
    COUNT(DISTINCT s.idUser)                                                      AS utilisateursActifs,
    CASE
        WHEN COUNT(s.idSignalement) > 0
        THEN ROUND(SUM(CASE WHEN s.statut = 'Termine' THEN 1 ELSE 0 END) * 100.0
                   / COUNT(s.idSignalement), 2)
        ELSE 0
    END AS tauxResolution
FROM Zone z
LEFT JOIN Signalement s ON z.idZone = s.idZone
GROUP BY z.idZone, z.nomZone;

CREATE OR REPLACE VIEW v_performance_agents AS
SELECT
    u.idUser,
    u.nom,
    u.email,
    z.nomZone                                                                     AS zoneAssignee,
    COUNT(a.idAffectation)                                                        AS missionsTotal,
    SUM(CASE WHEN s.statut = 'Termine'  THEN 1 ELSE 0 END)                      AS missionsTerminees,
    SUM(CASE WHEN s.statut = 'En cours' THEN 1 ELSE 0 END)                      AS missionsEnCours,
    CASE
        WHEN COUNT(a.idAffectation) > 0
        THEN ROUND(SUM(CASE WHEN s.statut = 'Termine' THEN 1 ELSE 0 END) * 100.0
                   / COUNT(a.idAffectation), 2)
        ELSE 0
    END AS tauxReussite,
    ROUND(AVG(CASE WHEN s.statut = 'Termine'
                   THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateAffectation)
              END), 1)                                                            AS tempsTraitementMoyen
FROM Utilisateur u
LEFT JOIN Affectation a  ON u.idUser       = a.idAgent
LEFT JOIN Signalement s  ON a.idSignalement = s.idSignalement
LEFT JOIN Zone z         ON u.idZone        = z.idZone
WHERE u.role = 'Agent' AND u.actif = 1
GROUP BY u.idUser, u.nom, u.email, z.nomZone;

CREATE OR REPLACE VIEW v_dashboard_realtime AS
SELECT
    (SELECT COUNT(*)   FROM Signalement)                                                                          AS totalSignalements,
    (SELECT COUNT(*)   FROM Signalement WHERE DATE(dateSignalement) = CURDATE())                                  AS signalementsAujourdhui,
    (SELECT COUNT(*)   FROM Signalement WHERE statut = 'En attente')                                              AS enAttente,
    (SELECT COUNT(*)   FROM Signalement WHERE statut = 'Affecte')                                                 AS affectes,
    (SELECT COUNT(*)   FROM Signalement WHERE statut = 'En cours')                                                AS enCours,
    (SELECT COUNT(*)   FROM Signalement WHERE statut = 'Termine')                                                 AS termines,
    (SELECT COUNT(*)   FROM Utilisateur WHERE actif = 1)                                                          AS utilisateursActifs,
    (SELECT COUNT(*)   FROM Utilisateur WHERE role = 'Agent'   AND actif = 1)                                     AS agentsActifs,
    (SELECT COUNT(*)   FROM Utilisateur WHERE role = 'Citoyen' AND actif = 1)                                     AS citoyensActifs,
    (SELECT COUNT(*)   FROM Signalement
     WHERE statut = 'En attente'
       AND dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR))                                                   AS signalementsUrgents;

CREATE OR REPLACE VIEW v_signalements_complets AS
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
    CONCAT(COALESCE(u.prenom, ''), ' ', u.nom)          AS utilisateurComplet,
    u.nom                                                AS utilisateurNom,
    u.email                                              AS utilisateurEmail,
    u.role                                               AS utilisateurRole,
    TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW())        AS ancienneteHeures,
    CASE
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 72 THEN 'Critique'
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 24 THEN 'Urgent'
        ELSE 'Normal'
    END AS priorite
FROM Signalement s
LEFT JOIN Zone z ON s.idZone = z.idZone
LEFT JOIN Utilisateur u ON s.idUser = u.idUser;

SELECT 'Migration ENUM + vues OK' AS status;
