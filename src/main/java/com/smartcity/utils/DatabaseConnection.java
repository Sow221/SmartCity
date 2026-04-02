package com.smartcity.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe de connexion sécurisée à la base de données MySQL avec pool de
 * connexions
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    // Configuration sécurisée avec variables d'environnement
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Suppression du pool HikariCP, gestion simple JDBC

    static {
        // Chargement sécurisé de la configuration
        URL = getConfigProperty("db.url",
                "jdbc:mysql://localhost:3306/db_smartcity?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        USER = getConfigProperty("db.user", "root");
        PASSWORD = getConfigProperty("db.password", null);

        if (PASSWORD == null) {
            logger.error("❌ ERREUR CRITIQUE: Mot de passe DB non configuré! Définir DB_PASSWORD ou db.password");
            throw new RuntimeException("Configuration DB manquante");
        }

        // Plus de pool HikariCP à initialiser
    }

    /**
     * Récupère une propriété de configuration de manière sécurisée
     * Priorité: Variable d'environnement > Fichier config.properties > Valeur par
     * défaut
     */
    private static String getConfigProperty(String key, String defaultValue) {
        // 1. Variables d'environnement (priorité haute)
        String envKey = key.replace(".", "_").toUpperCase();
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            logger.info("✅ Configuration DB chargée depuis variable d'environnement: {}", envKey);
            return envValue;
        }

        // 2. System properties
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }

        // 3. Fichier de configuration
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                String propValue = props.getProperty(key);
                if (propValue != null) {
                    logger.info("📁 Configuration DB chargée depuis config.properties: {}", key);
                    return propValue;
                }
            }
        } catch (IOException e) {
            logger.warn("⚠️ Erreur lecture config.properties: {}", e.getMessage());
        }

        return defaultValue;
    }

    // Suppression de l'initialisation du pool HikariCP

    /**
     * Obtient une connexion du pool (recommandé avec try-with-resources)
     * 
     * @return Connection du pool HikariCP
     * @throws SQLException si erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.debug("📊 Connexion JDBC obtenue");
            return conn;
        } catch (ClassNotFoundException e) {
            logger.error("❌ Driver JDBC non trouvé: {}", e.getMessage());
            throw new SQLException("Driver JDBC non trouvé", e);
        } catch (SQLException e) {
            logger.error("❌ Erreur obtention connexion JDBC: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Ferme le pool de connexions (à appeler au shutdown de l'application)
     */
    // Plus de pool à fermer avec JDBC simple
    public static void closeDataSource() {
        // Ne fait rien
    }

    /**
     * @deprecated Utiliser try-with-resources avec getConnection()
     */
    @Deprecated
    public static void closeConnection() {
        logger.warn("⚠️ Méthode dépréciée: Utiliser try-with-resources");
    }

    /**
     * Teste la santé du pool de connexions
     * 
     * @return true si le pool est opérationnel
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn != null && conn.isValid(5); // 5 secondes timeout
            if (isValid) {
                logger.debug("✅ Test connexion JDBC réussi");
            } else {
                logger.warn("⚠️ Test connexion JDBC échoué");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("❌ Erreur test connexion JDBC: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Statistiques du pool de connexions pour monitoring
     */
    public static String getPoolStats() {
        return "Mode JDBC simple : pas de pool";
    }

    /**
     * Vérifie si le pool est en bonne santé
     */
    public static boolean isHealthy() {
        return testConnection();
    }
}