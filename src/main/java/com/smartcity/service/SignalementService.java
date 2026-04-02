package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des signalements.
 */
public class SignalementService {
    // Modifier un signalement
    public boolean modifierSignalement(Signalement signalement) {
        String query = "UPDATE Signalement SET description=?, categorie=?, idZone=?, latitude=?, longitude=?, statut=?, photo=? WHERE idSignalement=?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, signalement.getDescription());
            pstmt.setString(2, signalement.getCategorie());
            pstmt.setInt(3, signalement.getIdZone());
            pstmt.setDouble(4, signalement.getLatitude());
            pstmt.setDouble(5, signalement.getLongitude());
            pstmt.setString(6, signalement.getStatut());
            pstmt.setString(7, signalement.getPhoto());
            pstmt.setInt(8, signalement.getIdSignalement());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                String missionJson = com.smartcity.utils.JsonUtils.toJson(signalement);
                com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Erreur lors de la modification du signalement: " + e.getMessage(), e);
            return false;
        }
    }

    // ...existing code...

    private static final Logger logger = LoggerFactory.getLogger(SignalementService.class);

    // Connexion à la base
    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Ajouter un signalement
    public boolean ajouterSignalement(Signalement signalement) {
        String query = "INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, photo, idUser) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, signalement.getDescription());
            pstmt.setString(2, signalement.getCategorie());
            pstmt.setInt(3, signalement.getIdZone());
            pstmt.setDouble(4, signalement.getLatitude());
            pstmt.setDouble(5, signalement.getLongitude());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(7, "En attente");
            pstmt.setString(8, signalement.getPhoto());
            pstmt.setInt(9, signalement.getIdUser());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                // Push WebSocket pour mise à jour carte
                String missionJson = com.smartcity.utils.JsonUtils.toJson(signalement);
                com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout du signalement: " + e.getMessage(), e);
            return false;
        }
    }
    // TODO: Ajouter push WebSocket dans les méthodes de modification et suppression
    // de signalement

    // Récupérer tous les signalements
    public List<Signalement> getAllSignalements() {
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query);
    }

    public List<Signalement> getSignalementsByZone(int idZone) {
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "WHERE s.idZone = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idZone);
    }

    public List<Signalement> getSignalementsByUtilisateur(int idUser) {
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "WHERE s.idUser = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idUser);
    }

    public List<Signalement> getSignalementsByZoneAndDate(int idZone, LocalDate date) {
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "WHERE s.idZone = ? AND DATE(s.dateSignalement) = ? "
                + "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idZone, Date.valueOf(date));
    }

    public List<Signalement> getSignalementsFiltres(String zoneNom, String statut, String categorie) {
        StringBuilder query = new StringBuilder(
                "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                        + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                        + "LEFT JOIN Zone z ON s.idZone = z.idZone WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (zoneNom != null && !zoneNom.isBlank()) {
            query.append(" AND z.nomZone = ?");
            params.add(zoneNom);
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
            logger.error("Erreur lors de la mise à jour: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean supprimerSignalement(int idSignalement) {
        String query = "DELETE FROM Signalement WHERE idSignalement = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression: " + e.getMessage(), e);
            return false;
        }
    }

    public int countAll() {
        return countSimple("SELECT COUNT(*) FROM Signalement");
    }

    public int countByStatut(String statut) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ?", statut);
    }

    public int countByZone(String zoneNom) {
        return countSimple("SELECT COUNT(*) FROM Signalement s JOIN Zone z ON s.idZone = z.idZone WHERE z.nomZone = ?",
                zoneNom);
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
                + "WHERE a.idAgent = ? AND s.statut = 'Terminé'";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur countTraitesByAgent", e);
            return countSimple(
                    "SELECT COUNT(*) FROM Signalement WHERE idZone = (SELECT idZone FROM Utilisateur WHERE idUser = ?) AND statut = 'Terminé'",
                    idAgent);
        }
        return 0;
    }

    // Méthode utilitaire pour compter
    private int countSimple(String query, Object... params) {
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage: " + e.getMessage(), e);
            return 0;
        }
    }

    // Exécuter requêtes de sélection
    private List<Signalement> executeSignalementQuery(String query, Object... params) {
        List<Signalement> liste = new ArrayList<>();
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToSignalement(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération: " + e.getMessage(), e);
        }
        return liste;
    }

    // Mapper ResultSet vers Signalement
    private Signalement mapResultSetToSignalement(ResultSet rs) throws SQLException {
        Signalement signalement = new Signalement();
        signalement.setIdSignalement(rs.getInt("idSignalement"));
        signalement.setDescription(rs.getString("description"));
        signalement.setCategorie(rs.getString("categorie"));
        signalement.setIdZone(rs.getInt("idZone"));
        signalement.setLatitude(rs.getDouble("latitude"));
        signalement.setLongitude(rs.getDouble("longitude"));
        signalement.setStatut(rs.getString("statut"));
        signalement.setPhoto(rs.getString("photo"));

        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null)
            signalement.setDateSignalement(timestamp.toLocalDateTime());

        signalement.setIdUser(rs.getInt("idUser"));
        String utilisateurNom = rs.getString("utilisateurNom");
        signalement
                .setUtilisateurNom(utilisateurNom != null ? utilisateurNom : "Utilisateur #" + signalement.getIdUser());

        String zoneNom = rs.getString("zoneNom");
        signalement.setZoneNom(zoneNom != null ? zoneNom : "Zone #" + signalement.getIdZone());

        return signalement;
    }
}