-- Jeu de données SmartCity pour tests complets

-- Zones
INSERT IGNORE INTO Zone (idZone, nomZone) VALUES (1, 'Pikine'), (2, 'Guediawaye');

-- Utilisateurs (admin, agents, citoyens)
INSERT INTO Utilisateur (prenom, nom, email, motDePasse, role, age, localite, photoProfil, idZone, actif)
VALUES
('Admin', 'Administrateur', 'admin@smartcity.sn', 'admin123', 'Administrateur', 35, 'Pikine', NULL, 1, 1),
('Fatou', 'AgentPikine', 'agentpikine@smartcity.sn', 'agent123', 'Agent', 28, 'Pikine', NULL, 1, 1),
('Moussa', 'AgentGuediawaye', 'agentguediawaye@smartcity.sn', 'agent123', 'Agent', 30, 'Guediawaye', NULL, 2, 1),
('Awa', 'CitoyenTest', 'citoyen@smartcity.sn', 'citizen123', 'Citoyen', 22, 'Pikine', NULL, 1, 1),
('Oumar', 'Citoyen2', 'citoyen2@smartcity.sn', 'citizen123', 'Citoyen', 40, 'Guediawaye', NULL, 2, 1);

-- Signalements (divers statuts, zones, utilisateurs)
INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, statut, photo, idUser)
VALUES
('Tas de plastique devant l’école', 'Plastique', 1, 14.75, -17.45, 'En attente', NULL, 4),
('Déchets organiques au marché', 'Organique', 2, 14.77, -17.42, 'En cours', NULL, 5),
('Bouteilles en verre cassées', 'Verre', 1, 14.76, -17.43, 'Terminé', NULL, 4),
('Papier et cartons brûlés', 'Papier', 2, 14.78, -17.41, 'En attente', NULL, 5),
('Décharge sauvage près du stade', 'Plastique', 1, 14.74, -17.44, 'En cours', NULL, 5),
('Sacs plastiques dans le canal', 'Plastique', 2, 14.79, -17.40, 'En attente', NULL, 4),
('Déchets métalliques sur la route', 'Métal', 1, 14.73, -17.46, 'Terminé', NULL, 5),
('Restes alimentaires abandonnés', 'Organique', 2, 14.80, -17.39, 'En cours', NULL, 4),
('Cartons mouillés', 'Papier', 1, 14.72, -17.47, 'En attente', NULL, 5),
('Bouteilles plastiques dans le parc', 'Plastique', 2, 14.81, -17.38, 'En attente', NULL, 4),
('Verre brisé devant la mairie', 'Verre', 1, 14.71, -17.48, 'En cours', NULL, 5),
('Déchets divers arrêt de bus', 'Autre', 2, 14.82, -17.37, 'En attente', NULL, 4),
('Papiers gras sur la place', 'Papier', 1, 14.70, -17.49, 'Terminé', NULL, 5),
('Déchets organiques marché central', 'Organique', 2, 14.83, -17.36, 'En attente', NULL, 4),
('Canettes métalliques dispersées', 'Métal', 1, 14.69, -17.50, 'En cours', NULL, 5),
('Déchets plastiques terrain vague', 'Plastique', 2, 14.84, -17.35, 'En attente', NULL, 4),
('Verre et plastique mélangés', 'Verre', 1, 14.68, -17.51, 'En attente', NULL, 5),
('Déchets organiques école', 'Organique', 2, 14.85, -17.34, 'Terminé', NULL, 4),
('Papier journal dispersé', 'Papier', 1, 14.67, -17.52, 'En cours', NULL, 5),
('Déchets divers parking', 'Autre', 2, 14.86, -17.33, 'En attente', NULL, 4);

-- Affectations (agents sur signalements)
INSERT INTO Affectation (idSignalement, idAgent)
VALUES
(1, 2),
(2, 3),
(3, 2);

-- Vérifier que tous les utilisateurs sont actifs
UPDATE Utilisateur SET actif = 1;
