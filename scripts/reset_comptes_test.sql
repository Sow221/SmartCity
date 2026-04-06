-- ============================================================
-- RESET COMPTES DE TEST SmartCity
-- A executer si l'authentification echoue
-- Les mots de passe sont en clair (compatibles avec le fallback legacy)
-- ============================================================

USE db_smartcity;

-- Remettre les mots de passe en clair pour les comptes de test
UPDATE Utilisateur SET motDePasse = 'admin123'   WHERE email = 'admin@smartcity.sn';
UPDATE Utilisateur SET motDePasse = 'agent123'   WHERE email = 'agentpikine@smartcity.sn';
UPDATE Utilisateur SET motDePasse = 'agent123'   WHERE email = 'agentguediawaye@smartcity.sn';
UPDATE Utilisateur SET motDePasse = 'citizen123' WHERE email = 'citoyen@smartcity.sn';

-- S'assurer que tous les comptes sont actifs
UPDATE Utilisateur SET actif = 1 WHERE email IN (
    'admin@smartcity.sn',
    'agentpikine@smartcity.sn',
    'agentguediawaye@smartcity.sn',
    'citoyen@smartcity.sn'
);

-- Verifier
SELECT idUser, nom, email, role, LENGTH(motDePasse) as len_mdp,
       CASE WHEN motDePasse LIKE '$2%' THEN 'BCrypt' ELSE 'Clair' END as type_mdp,
       actif
FROM Utilisateur
ORDER BY role;
