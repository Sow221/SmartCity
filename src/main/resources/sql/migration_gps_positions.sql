-- Migration GPS: tables de positions temps réel
USE db_smartcity;

CREATE TABLE IF NOT EXISTS position_agent (
    idAgent INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS position_citoyen (
    idCitoyen INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
);
