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

    private static final Logger logger = LoggerFactory.getLogger(SignalementService.class);

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public boolean modifierSignalement(Signalement signalement) {
        String query = "UPDATE Signalement SET description=?, categorie=?, idZone=?, latitude=?, longitude=?, statut=?, photo=? WHERE idSignalement=?";
        try (Connection conn = getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
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

    // Ajouter un signalement
    public boolean ajouterSignalement(Signalement signalement) {
        String query = "INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, dateSignalement, statut, photo, idUser) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idGenere = generatedKeys.getInt(1);
                        signalement.setIdSignalement(idGenere);
                        new AffectationService().affecterAgentParZone(idGenere, signalement.getIdZone());
                    }
                }
                String missionJson = com.smartcity.utils.JsonUtils.toJson(signalement);
                com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout du signalement: " + e.getMessage(), e);
            return false;
        }
    }

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
        boolean result = false;
        try {
            String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";
            try (Connection conn = getConn();
                    PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, nouveauStatut);
                pstmt.setInt(2, idSignalement);
                result = pstmt.executeUpdate() > 0;
            }
            if (result) {
                Signalement sig = getSignalementById(idSignalement);
                if (sig != null) {
                    String missionJson = com.smartcity.utils.JsonUtils.toJson(sig);
                    com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour: " + e.getMessage(), e);
        }
        return result;
    }

    private Signalement getSignalementById(int id) {
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom "
                + "FROM Signalement s "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "WHERE s.idSignalement = ?";
        try (Connection conn = getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToSignalement(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur getSignalementById: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean supprimerSignalement(int idSignalement) {
        Signalement sig = getSignalementById(idSignalement);
        try {
            new AffectationService().supprimerAffectationParSignalement(idSignalement);
        } catch (Exception e) {
            logger.warn("Affectation non supprimee pour signalement #{}: {}", idSignalement, e.getMessage());
        }
        String query = "DELETE FROM Signalement WHERE idSignalement = ?";
        boolean result = false;
        try (Connection conn = getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            result = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression: " + e.getMessage(), e);
        }
        if (result && sig != null) {
            String missionJson = com.smartcity.utils.JsonUtils.toJson(sig);
            com.smartcity.websocket.AgentMissionEndpoint.pushMission(missionJson);
        }
        return result;
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

    public int countByStatutAndZone(String statut, String zoneNom) {
        return countSimple(
                "SELECT COUNT(*) FROM Signalement s JOIN Zone z ON s.idZone = z.idZone WHERE s.statut = ? AND z.nomZone = ?",
                statut, zoneNom);
    }

    public int countByCategorie(String categorie) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE categorie = ?", categorie);
    }

    public int countMoyenneParJour() {
        String query = "SELECT ROUND(COUNT(*) / GREATEST(DATEDIFF(MAX(dateSignalement), MIN(dateSignalement)), 1)) FROM Signalement";
        return countSimple(query);
    }

    public int countByUtilisateur(int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE idUser = ?", idUser);
    }

    public int countByStatutAndUtilisateur(String statut, int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ? AND idUser = ?", statut, idUser);
    }

    public int countMissionsActivesParAgent(int idAgent) {
        return countSimple(
            "SELECT COUNT(*) FROM Affectation a "
            + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
            + "WHERE a.idAgent = ? AND s.statut NOT IN ('Terminé', 'Termine')",
            idAgent);
    }

    public int countTraitesByAgent(int idAgent) {
        return countSimple(
                "SELECT COUNT(*) FROM Affectation a "
                + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
                + "WHERE a.idAgent = ? AND (s.statut = 'Terminé' OR s.statut = 'Termine')",
                idAgent);
    }

    // Méthode utilitaire pour compter
    private int countSimple(String query, Object... params) {
        try (Connection conn = getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
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

    private List<Signalement> executeSignalementQuery(String query, Object... params) {
        List<Signalement> liste = new ArrayList<>();
        try (Connection conn = getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
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