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
        String envKey = key.replace(".", "_").toUpperCase(java.util.Locale.ROOT);
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

        // 3. Fichier de configuration (répertoire courant d'abord, puis classpath)
        java.io.File externalConfig = new java.io.File("config.properties");
        if (externalConfig.exists()) {
            try (InputStream input = new java.io.FileInputStream(externalConfig)) {
                Properties props = new Properties();
                props.load(input);
                String propValue = props.getProperty(key);
                if (propValue != null) {
                    logger.info("📁 Configuration DB chargée depuis config.properties (externe): {}", key);
                    return propValue;
                }
            } catch (IOException e) {
                logger.warn("⚠️ Erreur lecture config.properties externe: {}", e.getMessage());
            }
        }
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                String propValue = props.getProperty(key);
                if (propValue != null) {
                    logger.info("📁 Configuration DB chargée depuis config.properties (classpath): {}", key);
                    return propValue;
                }
            }
        } catch (IOException e) {
            logger.warn("⚠️ Erreur lecture config.properties: {}", e.getMessage());
        }

        return defaultValue;
    }

    private static final com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
    private static final com.zaxxer.hikari.HikariDataSource ds;

    static {
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setDriverClassName(DRIVER);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new com.zaxxer.hikari.HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("Impossible d'obtenir connexion depuis HikariCP");
        }
        return conn;
    }

    public static void closeDataSource() {
        if (!ds.isClosed()) {
            ds.close();
            logger.info("HikariCP pool fermé.");
        }
    }

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