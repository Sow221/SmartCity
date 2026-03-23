-- Script de vérification de la base de données SmartCity
-- À exécuter via : mysql -u root -p77884455 < verify_database.sql

USE db_smartcity;

-- Vérifier les tables
SELECT 'VERIFICATION DES TABLES' as 'ETAPE';
SHOW TABLES;

-- Vérifier la structure de la table Zone
SELECT 'STRUCTURE TABLE ZONE' as 'ETAPE';
DESCRIBE Zone;

-- Vérifier la structure de la table Utilisateur
SELECT 'STRUCTURE TABLE UTILISATEUR' as 'ETAPE';
DESCRIBE Utilisateur;

-- Vérifier la structure de la table Signalement
SELECT 'STRUCTURE TABLE SIGNALEMENT' as 'ETAPE';
DESCRIBE Signalement;

-- Vérifier la structure de la table Affectation
SELECT 'STRUCTURE TABLE AFFECTATION' as 'ETAPE';
DESCRIBE Affectation;

-- Vérifier les données initiales
SELECT 'DONNEES ZONES' as 'ETAPE';
SELECT * FROM Zone;

SELECT 'DONNEES UTILISATEURS' as 'ETAPE';
SELECT idUser, nom, email, role, idZone, actif, dateInscription FROM Utilisateur;

SELECT 'VERIFICATION FOREIGN KEYS' as 'ETAPE';
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE REFERENCED_TABLE_SCHEMA = 'db_smartcity';

SELECT 'VERIFICATION TERMINEE - TOUT EST PRET!' as 'STATUS';