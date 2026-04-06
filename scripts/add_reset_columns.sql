-- Ajout colonnes reset mot de passe
ALTER TABLE Utilisateur
    ADD COLUMN resetCode VARCHAR(6) NULL,
    ADD COLUMN resetExpiration DATETIME NULL;
