-- Migration: harmoniser l'ENUM Signalement.statut
-- Objectif: supporter l'affectation (Affecte) et éviter les soucis d'encodage.
--
-- À exécuter sur la base cible (db_smartcity) après sauvegarde.

USE db_smartcity;

-- Vérification (optionnel):
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='Signalement' AND COLUMN_NAME='statut';

ALTER TABLE Signalement
  MODIFY COLUMN statut ENUM('En attente', 'Affecte', 'En cours', 'Termine')
  NOT NULL
  DEFAULT 'En attente';

