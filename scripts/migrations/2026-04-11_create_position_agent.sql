-- Migration : création de la table position_agent (manquante)
-- À exécuter UNE FOIS avant le lancement de l'application
-- Corrige : SQLSyntaxErrorException: Table 'db_smartcity.position_agent' doesn't exist

CREATE TABLE IF NOT EXISTS position_agent (
    idAgent     INT          NOT NULL,
    latitude    DOUBLE       NOT NULL DEFAULT 0.0,
    longitude   DOUBLE       NOT NULL DEFAULT 0.0,
    updatedAt   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (idAgent),
    CONSTRAINT fk_position_agent_user
        FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
