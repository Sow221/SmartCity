-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: db_smartcity
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `affectation`
--

DROP TABLE IF EXISTS `affectation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `affectation` (
  `idAffectation` int(11) NOT NULL AUTO_INCREMENT,
  `idSignalement` int(11) DEFAULT NULL,
  `idAgent` int(11) DEFAULT NULL,
  `dateAffectation` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`idAffectation`),
  KEY `idSignalement` (`idSignalement`),
  KEY `idAgent` (`idAgent`),
  CONSTRAINT `affectation_ibfk_1` FOREIGN KEY (`idSignalement`) REFERENCES `signalement` (`idSignalement`),
  CONSTRAINT `affectation_ibfk_2` FOREIGN KEY (`idAgent`) REFERENCES `utilisateur` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `affectation`
--

LOCK TABLES `affectation` WRITE;
/*!40000 ALTER TABLE `affectation` DISABLE KEYS */;
/*!40000 ALTER TABLE `affectation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `signalement`
--

DROP TABLE IF EXISTS `signalement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `signalement` (
  `idSignalement` int(11) NOT NULL AUTO_INCREMENT,
  `description` text DEFAULT NULL,
  `categorie` enum('Plastique','Papier','Verre','M?®tal','Organique','Autre') DEFAULT NULL,
  `idZone` int(11) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `dateSignalement` datetime DEFAULT current_timestamp(),
  `statut` enum('En attente','Affect?®','En cours','Termin?®') DEFAULT 'En attente',
  `photo` varchar(255) DEFAULT NULL,
  `idUser` int(11) DEFAULT NULL,
  `zoneNom` varchar(100) DEFAULT NULL,
  `utilisateurNom` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idSignalement`),
  KEY `idZone` (`idZone`),
  KEY `idUser` (`idUser`),
  CONSTRAINT `signalement_ibfk_1` FOREIGN KEY (`idZone`) REFERENCES `zone` (`idZone`),
  CONSTRAINT `signalement_ibfk_2` FOREIGN KEY (`idUser`) REFERENCES `utilisateur` (`idUser`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `signalement`
--

LOCK TABLES `signalement` WRITE;
/*!40000 ALTER TABLE `signalement` DISABLE KEYS */;
/*!40000 ALTER TABLE `signalement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateur` (
  `idUser` int(11) NOT NULL AUTO_INCREMENT,
  `prenom` varchar(100) DEFAULT NULL,
  `nom` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `motDePasse` varchar(255) NOT NULL,
  `role` enum('Citoyen','Agent','Administrateur') NOT NULL,
  `age` int(11) DEFAULT NULL,
  `localite` varchar(100) DEFAULT NULL,
  `photoProfil` varchar(255) DEFAULT NULL,
  `idZone` int(11) DEFAULT NULL,
  `actif` tinyint(1) NOT NULL DEFAULT 1,
  `dateInscription` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`idUser`),
  UNIQUE KEY `email` (`email`),
  KEY `idZone` (`idZone`),
  CONSTRAINT `utilisateur_ibfk_1` FOREIGN KEY (`idZone`) REFERENCES `zone` (`idZone`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateur`
--

LOCK TABLES `utilisateur` WRITE;
/*!40000 ALTER TABLE `utilisateur` DISABLE KEYS */;
/*!40000 ALTER TABLE `utilisateur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zone`
--

DROP TABLE IF EXISTS `zone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zone` (
  `idZone` int(11) NOT NULL AUTO_INCREMENT,
  `nomZone` varchar(100) NOT NULL,
  PRIMARY KEY (`idZone`),
  UNIQUE KEY `nomZone` (`nomZone`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zone`
--

LOCK TABLES `zone` WRITE;
/*!40000 ALTER TABLE `zone` DISABLE KEYS */;
/*!40000 ALTER TABLE `zone` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-23 16:12:37
