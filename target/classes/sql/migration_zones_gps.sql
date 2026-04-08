-- Ajouter colonnes GPS au centre de chaque zone
ALTER TABLE Zone
    ADD COLUMN IF NOT EXISTS latitude  DECIMAL(10,8) DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8) DEFAULT 0.0;

-- Coordonnées réelles de Pikine et Guédiawaye (Sénégal)
UPDATE Zone SET latitude = 14.7646, longitude = -17.3920 WHERE nomZone = 'Pikine';
UPDATE Zone SET latitude = 14.7765, longitude = -17.4047 WHERE nomZone = 'Guédiawaye';
UPDATE Zone SET latitude = 14.7765, longitude = -17.4047 WHERE nomZone = 'Guediawaye';
