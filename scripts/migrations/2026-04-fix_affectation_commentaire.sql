-- Migration: ajout colonne commentaire dans Affectation (manquante en DB)
-- Erreur corrigée: Column 'commentaire' not found (AffectationService)
ALTER TABLE Affectation ADD COLUMN IF NOT EXISTS commentaire TEXT NULL;
