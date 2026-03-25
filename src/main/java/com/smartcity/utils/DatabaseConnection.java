package com.smartcity.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe de connexion à la base de données MySQL
 */
public class DatabaseConnection {

    // Configuration de la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/smart_city";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static Connection connection;

    /**
     * Établit la connexion à la base de données
     * 
     * @return Connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Ferme la connexion à la base de données
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée!");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }

    /**
     * Teste la connexion à la base de données
     * 
     * @return true si la connexion est établie
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
