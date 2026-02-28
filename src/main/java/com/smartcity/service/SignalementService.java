package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des signalements
 */
public class SignalementService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    /**
     * Ajoute un nouveau signalement
     */
    public boolean ajouterSignalement(Signalement signalement) {
        String query = "INSERT INTO Signalement (description, categorie, zone, dateSignalement, statut, photo, idUser) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, signalement.getDescription());
            pstmt.setString(2, signalement.getCategorie());
            pstmt.setString(3, signalement.getZone());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5, "En attente");
            pstmt.setString(6, signalement.getPhoto());
            pstmt.setInt(7, signalement.getIdUser());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du signalement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère tous les signalements
     */
    public List<Signalement> getAllSignalements() {
        List<Signalement> liste = new ArrayList<>();
        String query = "SELECT * FROM Signalement ORDER BY dateSignalement DESC";

        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                liste.add(mapResultSetToSignalement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les signalements par zone
     */
    public List<Signalement> getSignalementsByZone(String zone) {
        List<Signalement> liste = new ArrayList<>();
        String query = "SELECT * FROM Signalement WHERE zone = ? ORDER BY dateSignalement DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToSignalement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les signalements par utilisateur
     */
    public List<Signalement> getSignalementsByUtilisateur(int idUser) {
        List<Signalement> liste = new ArrayList<>();
        String query = "SELECT * FROM Signalement WHERE idUser = ? ORDER BY dateSignalement DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToSignalement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Met à jour le statut d'un signalement
     */
    public boolean updateStatut(int idSignalement, String nouveauStatut) {
        String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un signalement
     */
    public boolean supprimerSignalement(int idSignalement) {
        String query = "DELETE FROM Signalement WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte les signalements par statut
     */
    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM Signalement WHERE statut = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Compte les signalements par zone
     */
    public int countByZone(String zone) {
        String query = "SELECT COUNT(*) FROM Signalement WHERE zone = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Mappe un ResultSet vers un objet Signalement
     */
    private Signalement mapResultSetToSignalement(ResultSet rs) throws SQLException {
        Signalement signalement = new Signalement();
        signalement.setIdSignalement(rs.getInt("idSignalement"));
        signalement.setDescription(rs.getString("description"));
        signalement.setCategorie(rs.getString("categorie"));
        signalement.setZone(rs.getString("zone"));
        signalement.setStatut(rs.getString("statut"));
        signalement.setPhoto(rs.getString("photo"));

        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null) {
            signalement.setDateSignalement(timestamp.toLocalDateTime());
        }

        signalement.setIdUser(rs.getInt("idUser"));
        return signalement;
    }
}
