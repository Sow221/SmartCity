package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des signalements.
 */
public class SignalementService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

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

    public List<Signalement> getAllSignalements() {
        String query = "SELECT s.*, u.nom AS utilisateurNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query);
    }

    public List<Signalement> getSignalementsByZone(String zone) {
        String query = "SELECT s.*, u.nom AS utilisateurNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "WHERE s.zone = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, zone);
    }

    public List<Signalement> getSignalementsByUtilisateur(int idUser) {
        String query = "SELECT s.*, u.nom AS utilisateurNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "WHERE s.idUser = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idUser);
    }

    public List<Signalement> getSignalementsByZoneAndDate(String zone, LocalDate date) {
        String query = "SELECT s.*, u.nom AS utilisateurNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "WHERE s.zone = ? AND DATE(s.dateSignalement) = ? "
                + "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, zone, Date.valueOf(date));
    }

    public List<Signalement> getSignalementsFiltres(String zone, String statut, String categorie) {
        StringBuilder query = new StringBuilder(
                "SELECT s.*, u.nom AS utilisateurNom FROM Signalement s " +
                        "LEFT JOIN Utilisateur u ON s.idUser = u.idUser WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (zone != null && !zone.isBlank()) {
            query.append(" AND s.zone = ?");
            params.add(zone);
        }
        if (statut != null && !statut.isBlank()) {
            query.append(" AND s.statut = ?");
            params.add(statut);
        }
        if (categorie != null && !categorie.isBlank()) {
            query.append(" AND s.categorie = ?");
            params.add(categorie);
        }

        query.append(" ORDER BY s.dateSignalement DESC");
        return executeSignalementQuery(query.toString(), params.toArray());
    }

    public boolean updateStatut(int idSignalement, String nouveauStatut) {
        String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise a jour: " + e.getMessage());
            return false;
        }
    }

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

    public int countAll() {
        return countSimple("SELECT COUNT(*) FROM Signalement");
    }

    public int countByStatut(String statut) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ?", statut);
    }

    public int countByZone(String zone) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE zone = ?", zone);
    }

    public int countByUtilisateur(int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE idUser = ?", idUser);
    }

    public int countByStatutAndUtilisateur(String statut, int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ? AND idUser = ?", statut, idUser);
    }

    public int countTraitesByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation a "
                + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
                + "WHERE a.idAgent = ? AND s.statut = 'Collecte'";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return countSimple("SELECT COUNT(*) FROM Signalement WHERE zone = (SELECT zone FROM Utilisateur WHERE idUser = ?) AND statut = 'Collecte'", idAgent);
        }
        return 0;
    }

    private int countSimple(String query, Object... params) {
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            return 0;
        }
    }

    private List<Signalement> executeSignalementQuery(String query, Object... params) {
        List<Signalement> liste = new ArrayList<>();

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSetToSignalement(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recuperation: " + e.getMessage());
        }

        return liste;
    }

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
        try {
            String utilisateurNom = rs.getString("utilisateurNom");
            signalement.setUtilisateurNom(utilisateurNom != null ? utilisateurNom : "Utilisateur #" + signalement.getIdUser());
        } catch (SQLException e) {
            signalement.setUtilisateurNom("Utilisateur #" + signalement.getIdUser());
        }

        return signalement;
    }
}
