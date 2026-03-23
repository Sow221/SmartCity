-- Optimisation de la base de données SmartCity
-- Script à exécuter après la création initiale

USE db_smartcity;

-- =============================================
-- INDEX DE PERFORMANCE
-- =============================================

-- Index pour les recherches fréquentes sur les signalements
CREATE INDEX IF NOT EXISTS idx_signalement_zone_date ON Signalement(idZone, dateSignalement);
CREATE INDEX IF NOT EXISTS idx_signalement_statut ON Signalement(statut);
CREATE INDEX IF NOT EXISTS idx_signalement_user_date ON Signalement(idUser, dateSignalement);
CREATE INDEX IF NOT EXISTS idx_signalement_categorie ON Signalement(categorie);

-- Index pour les utilisateurs
CREATE INDEX IF NOT EXISTS idx_user_role_zone ON Utilisateur(role, idZone);
CREATE INDEX IF NOT EXISTS idx_user_email ON Utilisateur(email);
CREATE INDEX IF NOT EXISTS idx_user_actif ON Utilisateur(actif);

-- Index pour les affectations
CREATE INDEX IF NOT EXISTS idx_affectation_agent ON Affectation(idAgent);
CREATE INDEX IF NOT EXISTS idx_affectation_signalement ON Affectation(idSignalement);
CREATE INDEX IF NOT EXISTS idx_affectation_date ON Affectation(dateAffectation);

-- Index composé pour les requêtes complexes
CREATE INDEX IF NOT EXISTS idx_signalement_zone_statut_date ON Signalement(idZone, statut, dateSignalement);

-- =============================================
-- VUES OPTIMISÉES POUR LES RAPPORTS
-- =============================================

-- Vue pour les statistiques des signalements avec détails
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
    CONCAT(COALESCE(u.prenom, ''), ' ', u.nom) as utilisateurComplet,
    u.nom as utilisateurNom,
    u.email as utilisateurEmail,
    u.role as utilisateurRole,
    -- Calcul de l'ancienneté du signalement
    TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) as ancienneteHeures,
    CASE 
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 72 THEN 'Critique'
        WHEN TIMESTAMPDIFF(HOUR, s.dateSignalement, NOW()) > 24 THEN 'Urgent' 
        ELSE 'Normal'
    END as priorite
FROM Signalement s
LEFT JOIN Zone z ON s.idZone = z.idZone
LEFT JOIN Utilisateur u ON s.idUser = u.idUser;

-- Vue pour les statistiques par zone
CREATE OR REPLACE VIEW v_stats_zones AS
SELECT 
    z.idZone,
    z.nomZone,
    COUNT(s.idSignalement) as totalSignalements,
    SUM(CASE WHEN s.statut = 'En attente' THEN 1 ELSE 0 END) as enAttente,
    SUM(CASE WHEN s.statut = 'En cours' THEN 1 ELSE 0 END) as enCours,
    SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) as termines,
    COUNT(DISTINCT s.idUser) as utilisateursActifs,
    -- Pourcentage de résolution
    CASE 
        WHEN COUNT(s.idSignalement) > 0 
        THEN ROUND((SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) * 100.0 / COUNT(s.idSignalement)), 2)
        ELSE 0 
    END as tauxResolution
FROM Zone z
LEFT JOIN Signalement s ON z.idZone = s.idZone
GROUP BY z.idZone, z.nomZone;

-- Vue pour les performances des agents
CREATE OR REPLACE VIEW v_performance_agents AS
SELECT 
    u.idUser,
    u.nom,
    u.email,
    z.nomZone as zoneAssignee,
    COUNT(a.idAffectation) as missionsTotal,
    SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) as missionsTerminees,
    SUM(CASE WHEN s.statut = 'En cours' THEN 1 ELSE 0 END) as missionsEnCours,
    CASE 
        WHEN COUNT(a.idAffectation) > 0 
        THEN ROUND((SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) * 100.0 / COUNT(a.idAffectation)), 2)
        ELSE 0 
    END as tauxReussite,
    -- Temps moyen de traitement en heures
    CASE 
        WHEN SUM(CASE WHEN s.statut = 'Terminé' THEN 1 ELSE 0 END) > 0
        THEN ROUND(AVG(CASE WHEN s.statut = 'Terminé' THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateAffectation) END), 1)
        ELSE NULL
    END as tempsTraitementMoyen
FROM Utilisateur u
LEFT JOIN Affectation a ON u.idUser = a.idAgent
LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement
LEFT JOIN Zone z ON u.idZone = z.idZone
WHERE u.role = 'Agent'
GROUP BY u.idUser, u.nom, u.email, z.nomZone;

-- Vue pour le dashboard temps réel
CREATE OR REPLACE VIEW v_dashboard_realtime AS
SELECT 
    (SELECT COUNT(*) FROM Signalement) as totalSignalements,
    (SELECT COUNT(*) FROM Signalement WHERE DATE(dateSignalement) = CURDATE()) as signalementsAujourdhui,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'En attente') as enAttente,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'En cours') as enCours,
    (SELECT COUNT(*) FROM Signalement WHERE statut = 'Terminé') as termines,
    (SELECT COUNT(*) FROM Utilisateur WHERE actif = 1) as utilisateursActifs,
    (SELECT COUNT(*) FROM Utilisateur WHERE role = 'Agent' AND actif = 1) as agentsActifs,
    (SELECT COUNT(*) FROM Utilisateur WHERE role = 'Citoyen' AND actif = 1) as citoyensActifs,
    -- Signalements urgents (>24h en attente)
    (SELECT COUNT(*) FROM Signalement 
     WHERE statut = 'En attente' 
     AND dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)) as signalementsUrgents;

-- =============================================
-- PROCÉDURES STOCKÉES POUR L'OPTIMISATION
-- =============================================

DELIMITER //

-- Procédure pour nettoyer les anciennes données
CREATE PROCEDURE CleanOldData()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Archiver les signalements terminés de plus de 6 mois
    -- (Dans un vrai système, on créerait une table d'archive)
    UPDATE Signalement 
    SET statut = 'Archivé' 
    WHERE statut = 'Terminé' 
    AND dateSignalement < DATE_SUB(NOW(), INTERVAL 6 MONTH);
    
    COMMIT;
END//

-- Procédure pour mettre à jour les statistiques
CREATE PROCEDURE UpdateStatistics()
BEGIN
    -- Cette procédure peut être appelée périodiquement pour pré-calculer des stats
    -- Dans un vrai système, on utiliserait une table de cache pour les statistiques
    
    -- Exemple : Mettre à jour une table de statistiques quotidiennes
    -- INSERT INTO daily_stats (date, total_signalements, resolus, zone_id)
    -- SELECT CURDATE(), COUNT(*), SUM(CASE WHEN statut='Terminé' THEN 1 ELSE 0 END), idZone
    -- FROM Signalement 
    -- WHERE DATE(dateSignalement) = CURDATE()
    -- GROUP BY idZone
    -- ON DUPLICATE KEY UPDATE 
    -- total_signalements = VALUES(total_signalements),
    -- resolus = VALUES(resolus);
    
    SELECT 'Statistiques mises à jour' as message;
END//

-- Procédure pour optimiser l'affectation automatique
CREATE PROCEDURE AutoAssignSignalements()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE sig_id INT;
    DECLARE zone_id INT;
    DECLARE agent_id INT;
    
    -- Curseur pour les signalements en attente
    DECLARE cur CURSOR FOR 
        SELECT s.idSignalement, s.idZone 
        FROM Signalement s 
        WHERE s.statut = 'En attente'
        AND s.idSignalement NOT IN (SELECT idSignalement FROM Affectation)
        ORDER BY s.dateSignalement ASC
        LIMIT 10;
        
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO sig_id, zone_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Trouver un agent disponible dans la zone
        SELECT u.idUser INTO agent_id
        FROM Utilisateur u
        LEFT JOIN (
            SELECT idAgent, COUNT(*) as charge
            FROM Affectation a
            JOIN Signalement s ON a.idSignalement = s.idSignalement
            WHERE s.statut IN ('En cours', 'En attente')
            GROUP BY idAgent
        ) workload ON u.idUser = workload.idAgent
        WHERE u.role = 'Agent' 
        AND u.idZone = zone_id 
        AND u.actif = 1
        ORDER BY COALESCE(workload.charge, 0) ASC
        LIMIT 1;
        
        -- Affecter si un agent est trouvé
        IF agent_id IS NOT NULL THEN
            INSERT INTO Affectation (idSignalement, idAgent, dateAffectation)
            VALUES (sig_id, agent_id, NOW());
            
            UPDATE Signalement 
            SET statut = 'Affecté' 
            WHERE idSignalement = sig_id;
        END IF;
        
    END LOOP;
    
    CLOSE cur;
END//

DELIMITER ;

-- =============================================
-- DÉCLENCHEURS POUR L'INTÉGRITÉ DES DONNÉES
-- =============================================

-- Déclencheur pour mettre à jour automatiquement les coordonnées par défaut
DELIMITER //
CREATE TRIGGER tr_signalement_coordinates 
    BEFORE INSERT ON Signalement
    FOR EACH ROW
BEGIN
    -- Si pas de coordonnées fournies, utiliser celles du centre de la zone
    IF NEW.latitude = 0 AND NEW.longitude = 0 THEN
        CASE NEW.idZone
            WHEN 1 THEN -- Pikine
                SET NEW.latitude = 14.7549, NEW.longitude = -17.3925;
            WHEN 2 THEN -- Guédiawaye  
                SET NEW.latitude = 14.7692, NEW.longitude = -17.4281;
            ELSE
                SET NEW.latitude = 14.7549, NEW.longitude = -17.3925;
        END CASE;
        
        -- Ajouter un petit offset aléatoire
        SET NEW.latitude = NEW.latitude + (RAND() - 0.5) * 0.01;
        SET NEW.longitude = NEW.longitude + (RAND() - 0.5) * 0.01;
    END IF;
END//
DELIMITER ;

-- =============================================
-- CONFIGURATION DES PARAMÈTRES MYSQL
-- =============================================

-- Optimisation pour les requêtes de rapports
SET GLOBAL query_cache_type = ON;
SET GLOBAL query_cache_size = 67108864; -- 64MB

-- Configuration pour les performances
SET GLOBAL innodb_buffer_pool_size = 134217728; -- 128MB (ajuster selon la RAM)

-- =============================================
-- REQUÊTES DE VÉRIFICATION
-- =============================================

-- Vérifier les index créés
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'db_smartcity' 
AND TABLE_NAME IN ('Signalement', 'Utilisateur', 'Affectation')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Statistiques des tables
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH,
    INDEX_LENGTH,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as SIZE_MB
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_smartcity';

-- Test des vues
SELECT * FROM v_dashboard_realtime;
SELECT * FROM v_stats_zones LIMIT 5;
SELECT * FROM v_performance_agents LIMIT 5;