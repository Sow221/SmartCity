-- Indexes pour les colonnes de filtrage frequent (DATA-04)
CREATE INDEX IF NOT EXISTS idx_signalement_statut ON Signalement(statut);
CREATE INDEX IF NOT EXISTS idx_signalement_zone   ON Signalement(idZone);
CREATE INDEX IF NOT EXISTS idx_signalement_user   ON Signalement(idUser);
CREATE INDEX IF NOT EXISTS idx_affectation_agent  ON Affectation(idAgent);
