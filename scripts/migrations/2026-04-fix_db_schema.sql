-- Migration : correction schema DB SmartCity
-- 1. Colonne dateCollecte manquante dans Affectation
-- 2. Colonnes GPS manquantes dans Zone
-- 3. Mot de passe admin en clair

ALTER TABLE Affectation
    ADD COLUMN IF NOT EXISTS dateCollecte DATETIME NULL;

ALTER TABLE Zone
    ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,8) NULL,
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8) NULL;

UPDATE Zone SET latitude = 14.7646, longitude = -17.3920 WHERE nomZone = 'Pikine' AND (latitude IS NULL OR latitude = 0);
UPDATE Zone SET latitude = 14.7765, longitude = -17.4047 WHERE nomZone = 'Guédiawaye' AND (latitude IS NULL OR latitude = 0);

UPDATE Utilisateur
SET motDePasse = '$2a$10$k2pYI/rj3jxvkVWJFYNieexdMatKMvEBQeH0Jx.b6MbeLMzLpPKim'
WHERE email = 'admin@smartcity.sn'
  AND motDePasse NOT LIKE '$2a$%';
