-- Migration : création des tables GPS position_agent et position_citoyen
-- À exécuter UNE SEULE FOIS sur db_smartcity
-- Cause : GpsApiServer, AffectationService et PositionAgentService échouent
--         avec SQLSyntaxErrorException car ces tables n'existent pas.

USE db_smartcity;

CREATE TABLE IF NOT EXISTS position_agent (
    idAgent     INT          NOT NULL,
    latitude    DECIMAL(10,8) NOT NULL,
    longitude   DECIMAL(11,8) NOT NULL,
    updatedAt   DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (idAgent),
    CONSTRAINT fk_pos_agent FOREIGN KEY (idAgent)
        REFERENCES Utilisateur(idUser) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS position_citoyen (
    idCitoyen   INT          NOT NULL,
    latitude    DECIMAL(10,8) NOT NULL,
    longitude   DECIMAL(11,8) NOT NULL,
    updatedAt   DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (idCitoyen),
    CONSTRAINT fk_pos_citoyen FOREIGN KEY (idCitoyen)
        REFERENCES Utilisateur(idUser) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- S'assurer que les colonnes GPS existent dans Zone (migration_zones_gps.sql)
ALTER TABLE Zone
    ADD COLUMN IF NOT EXISTS latitude  DECIMAL(10,8) DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(11,8) DEFAULT 0.0;

-- Coordonnées réelles Pikine et Guédiawaye
UPDATE Zone SET latitude = 14.7646, longitude = -17.3920
    WHERE nomZone = 'Pikine' AND (latitude = 0.0 OR latitude IS NULL);
UPDATE Zone SET latitude = 14.7765, longitude = -17.4047
    WHERE nomZone IN ('Guédiawaye', 'Guediawaye') AND (latitude = 0.0 OR latitude IS NULL);

-- Vérification
SELECT 'position_agent'  AS table_name, COUNT(*) AS rows FROM position_agent
UNION ALL
SELECT 'position_citoyen', COUNT(*) FROM position_citoyen
UNION ALL
SELECT CONCAT('Zone:', nomZone), latitude FROM Zone;
