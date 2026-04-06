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

    static {
        URL = getConfigProperty("db.url",
                "jdbc:mysql://localhost:3306/db_smartcity?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        USER = getConfigProperty("db.user", "root");
        PASSWORD = getConfigProperty("db.password", "");

        if (PASSWORD == null || PASSWORD.isEmpty()) {
            logger.warn("⚠️ Mot de passe DB non configuré — vérifier config.properties ou la variable DB_PASSWORD");
        }
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

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.error("❌ Driver JDBC non trouvé: {}", e.getMessage());
            throw new SQLException("Driver JDBC non trouvé", e);
        } catch (SQLException e) {
            logger.error("❌ Erreur obtention connexion JDBC: {}", e.getMessage());
            throw e;
        }
    }

    public static void closeDataSource() {}

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(5);
        } catch (SQLException e) {
            logger.error("❌ Erreur test connexion: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isHealthy() {
        return testConnection();
    }
}