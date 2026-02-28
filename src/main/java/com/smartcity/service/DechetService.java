package com.smartcity.service;

import com.smartcity.model.Dechet;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des déchets/signalements
 */
public class DechetService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    /**
     * Ajoute un nouveau signalement
     */
    public boolean ajouterSignalement(Dechet dechet) {
        String query = "INSERT INTO dechet (description, categorie, zone, quartier, photo, statut, dateSignalement, idUtilisateur) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, dechet.getDescription());
            pstmt.setString(2, dechet.getCategorie());
            pstmt.setString(3, dechet.getZone());
            pstmt.setString(4, dechet.getQuartier());
            pstmt.setString(5, dechet.getPhoto());
            pstmt.setString(6, "En attente");
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(8, dechet.getIdUtilisateur());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du signalement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère tous les signalements
     */
    public List<Dechet> getAllSignalements() {
        List<Dechet> liste = new ArrayList<>();
        String query = "SELECT * FROM dechet ORDER BY dateSignalement DESC";

        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                liste.add(mapResultSetToDechet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les signalements par zone
     */
    public List<Dechet> getSignalementsByZone(String zone) {
        List<Dechet> liste = new ArrayList<>();
        String query = "SELECT * FROM dechet WHERE zone = ? ORDER BY dateSignalement DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToDechet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les signalements par utilisateur
     */
    public List<Dechet> getSignalementsByUtilisateur(int idUtilisateur) {
        List<Dechet> liste = new ArrayList<>();
        String query = "SELECT * FROM dechet WHERE idUtilisateur = ? ORDER BY dateSignalement DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUtilisateur);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToDechet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Met à jour le statut d'un signalement
     */
    public boolean updateStatut(int idDechet, String nouveauStatut) {
        String query = "UPDATE dechet SET statut = ? WHERE idDechet = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idDechet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un signalement
     */
    public boolean supprimerSignalement(int idDechet) {
        String query = "DELETE FROM dechet WHERE idDechet = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idDechet);
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
        String query = "SELECT COUNT(*) FROM dechet WHERE statut = ?";

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
        String query = "SELECT COUNT(*) FROM dechet WHERE zone = ?";

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
     * Compte les signalements par catégorie
     */
    public int countByCategorie(String categorie) {
        String query = "SELECT COUNT(*) FROM dechet WHERE categorie = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, categorie);
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
     * Mappe un ResultSet vers un objet Dechet
     */
    private Dechet mapResultSetToDechet(ResultSet rs) throws SQLException {
        Dechet dechet = new Dechet();
        dechet.setIdDechet(rs.getInt("idDechet"));
        dechet.setDescription(rs.getString("description"));
        dechet.setCategorie(rs.getString("categorie"));
        dechet.setZone(rs.getString("zone"));
        dechet.setQuartier(rs.getString("quartier"));
        dechet.setPhoto(rs.getString("photo"));
        dechet.setStatut(rs.getString("statut"));

        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null) {
            dechet.setDateSignalement(timestamp.toLocalDateTime());
        }

        dechet.setIdUtilisateur(rs.getInt("idUtilisateur"));
        return dechet;
    }
}
