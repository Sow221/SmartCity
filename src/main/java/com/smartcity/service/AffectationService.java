package com.smartcity.service;

import com.smartcity.model.Affectation;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des affectations
 */
public class AffectationService {

    private Connection conn;

    public AffectationService() {
        conn = DatabaseConnection.getConnection();
    }

    /**
     * Crée une nouvelle affectation
     */
    public boolean creerAffectation(int idSignalement, int idAgent) {
        String query = "INSERT INTO Affectation (idSignalement, idAgent, dateAffectation) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            pstmt.setInt(2, idAgent);
            pstmt.setObject(3, LocalDateTime.now());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'affectation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère toutes les affectations
     */
    public List<Affectation> getAllAffectations() {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation ORDER BY dateAffectation DESC";

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Affectation affectation = mapResultSetToAffectation(rs);
                liste.add(affectation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les affectations par agent
     */
    public List<Affectation> getAffectationsByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation WHERE idAgent = ? ORDER BY dateAffectation DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Affectation affectation = mapResultSetToAffectation(rs);
                liste.add(affectation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère une affectation par signalement
     */
    public Affectation getAffectationBySignalement(int idSignalement) {
        String query = "SELECT * FROM Affectation WHERE idSignalement = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAffectation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return null;
    }

    /**
     * Supprime une affectation
     */
    public boolean supprimerAffectation(int idAffectation) {
        String query = "DELETE FROM Affectation WHERE idAffectation = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idAffectation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Affectation
     */
    private Affectation mapResultSetToAffectation(ResultSet rs) throws SQLException {
        Affectation affectation = new Affectation();
        affectation.setIdAffectation(rs.getInt("idAffectation"));
        affectation.setIdSignalement(rs.getInt("idSignalement"));
        affectation.setIdAgent(rs.getInt("idAgent"));

        Timestamp timestamp = rs.getTimestamp("dateAffectation");
        if (timestamp != null) {
            affectation.setDateAffectation(timestamp.toLocalDateTime());
        }

        return affectation;
    }
}
