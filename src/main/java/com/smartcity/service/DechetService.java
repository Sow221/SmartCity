package com.smartcity.service;

import com.smartcity.model.Dechet;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DechetService {
    private static final Logger logger = LoggerFactory.getLogger(DechetService.class);

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public boolean ajouterSignalement(Dechet dechet) {
        String query = "INSERT INTO dechet (description, categorie, zone, quartier, photo, statut, dateSignalement, idUtilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
            logger.error("Erreur lors de l'ajout du signalement", e);
            return false;
        }
    }

    public List<Dechet> getAllSignalements() {
        List<Dechet> liste = new ArrayList<>();
        String query = "SELECT * FROM dechet ORDER BY dateSignalement DESC";
        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                liste.add(mapResultSetToDechet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des signalements", e);
        }
        return liste;
    }

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
            logger.error("Erreur lors de la récupération par zone", e);
        }
        return liste;
    }

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
            logger.error("Erreur lors de la récupération par utilisateur", e);
        }
        return liste;
    }

    public boolean updateStatut(int idDechet, String nouveauStatut) {
        String query = "UPDATE dechet SET statut = ? WHERE idDechet = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idDechet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut", e);
            return false;
        }
    }

    public boolean supprimerSignalement(int idDechet) {
        String query = "DELETE FROM dechet WHERE idDechet = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idDechet);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression", e);
            return false;
        }
    }

    public int countByStatut(String statut) {
        String query = "SELECT COUNT(*) FROM dechet WHERE statut = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage par statut", e);
        }
        return 0;
    }

    public int countByZone(String zone) {
        String query = "SELECT COUNT(*) FROM dechet WHERE zone = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage par zone", e);
        }
        return 0;
    }

    public int countByCategorie(String categorie) {
        String query = "SELECT COUNT(*) FROM dechet WHERE categorie = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, categorie);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage par catégorie", e);
        }
        return 0;
    }

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

