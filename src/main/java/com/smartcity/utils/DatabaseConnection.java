package com.smartcity.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe de connexion sécurisée à la base de données MySQL avec pool de connexions
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    
    // Configuration sécurisée avec variables d'environnement
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static HikariDataSource dataSource;
    
    static {
        // Chargement sécurisé de la configuration
        URL = getConfigProperty("db.url", "jdbc:mysql://localhost:3306/db_smartcity?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        USER = getConfigProperty("db.user", "root");
        PASSWORD = getConfigProperty("db.password", null);
        
        if (PASSWORD == null) {
            logger.error("❌ ERREUR CRITIQUE: Mot de passe DB non configuré! Définir DB_PASSWORD ou db.password");
            throw new RuntimeException("Configuration DB manquante");
        }
        
        initializeDataSource();
    }
    
    /**
     * Récupère une propriété de configuration de manière sécurisée
     * Priorité: Variable d'environnement > Fichier config.properties > Valeur par défaut
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
    
    /**
     * Initialise le pool de connexions HikariCP
     */
    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName(DRIVER);
            
            // Configuration optimisée du pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 secondes
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute
            
            // Optimisations de performance
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            dataSource = new HikariDataSource(config);
            logger.info("🚀 Pool de connexions HikariCP initialisé avec succès!");
            
        } catch (Exception e) {
            logger.error("❌ Erreur initialisation pool de connexions: {}", e.getMessage(), e);
            throw new RuntimeException("Échec initialisation DB", e);
        }
    }

        /**
     * Obtient une connexion du pool (recommandé avec try-with-resources)
     * 
     * @return Connection du pool HikariCP
     * @throws SQLException si erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            logger.error("❌ DataSource non initialisé!");
            throw new SQLException("Pool de connexions non disponible");
        }
        
        try {
            Connection conn = dataSource.getConnection();
            logger.debug("📊 Connexion obtenue du pool (actives: {}, idle: {})", 
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections());
            return conn;
        } catch (SQLException e) {
            logger.error("❌ Erreur obtention connexion: {}", e.getMessage());
            throw e;
        }
    }

        /**
     * Ferme le pool de connexions (à appeler au shutdown de l'application)
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("🔒 Fermeture du pool de connexions...");
            dataSource.close();
            logger.info("✅ Pool de connexions fermé avec succès");
        }
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
                logger.debug("✅ Test connexion DB réussi");
            } else {
                logger.warn("⚠️ Test connexion DB échoué");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("❌ Erreur test connexion: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Statistiques du pool de connexions pour monitoring
     */
    public static String getPoolStats() {
        if (dataSource == null) return "Pool non initialisé";
        
        return String.format("Pool Stats - Actives: %d, Idle: %d, Total: %d, En attente: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
    
    /**
     * Vérifie si le pool est en bonne santé
     */
    public static boolean isHealthy() {
        return dataSource != null && !dataSource.isClosed() && testConnection();
    }
}