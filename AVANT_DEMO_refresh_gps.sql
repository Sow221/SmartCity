-- ============================================================
-- SCRIPT À LANCER 2 MIN AVANT LA PRÉSENTATION
-- Rafraîchit les timestamps GPS pour que les agents
-- apparaissent LIVE sur la carte admin
-- ============================================================

-- Agent Diop - Pikine - Marché de Pikine (Avenue Bourguiba)
-- En déplacement vers signalement #14 (Organique, En cours)
UPDATE position_agent SET
    latitude  = 14.75460000,
    longitude = -17.39120000,
    updatedAt = DATE_SUB(NOW(), INTERVAL 2 MINUTE)
WHERE idAgent = 2;

-- Agent Sow - Guediawaye - Rond-point Guediawaye
-- En déplacement vers signalement #7 (Organique, En cours)
UPDATE position_agent SET
    latitude  = 14.77710000,
    longitude = -17.40380000,
    updatedAt = DATE_SUB(NOW(), INTERVAL 4 MINUTE)
WHERE idAgent = 3;

-- Agent test1 - Guediawaye - Quartier Sam
-- Hors ligne intentionnel (montre la différence Live/Hors ligne)
UPDATE position_agent SET
    latitude  = 14.78100000,
    longitude = -17.39950000,
    updatedAt = DATE_SUB(NOW(), INTERVAL 20 MINUTE)
WHERE idAgent = 39;

-- Vérification immédiate
SELECT
    u.nom,
    z.nomZone,
    pa.latitude,
    pa.longitude,
    TIMESTAMPDIFF(MINUTE, pa.updatedAt, NOW()) AS minutes_ago,
    CASE
        WHEN TIMESTAMPDIFF(MINUTE, pa.updatedAt, NOW()) < 15 THEN '🟢 LIVE'
        ELSE '⚫ Hors ligne'
    END AS statut_carte
FROM position_agent pa
JOIN Utilisateur u ON pa.idAgent = u.idUser
JOIN Zone z ON u.idZone = z.idZone
ORDER BY u.idUser;
