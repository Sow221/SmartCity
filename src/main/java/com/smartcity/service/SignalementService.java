package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            String statutDb = SignalementStatut.toDbValue(
                signalement.getStatut(),
                SignalementStatut.EN_ATTENTE.dbValue());
            pstmt.setString(1, signalement.getDescription());
            pstmt.setString(2, signalement.getCategorie());
            pstmt.setInt(3, signalement.getIdZone());
            pstmt.setDouble(4, signalement.getLatitude());
            pstmt.setDouble(5, signalement.getLongitude());
            pstmt.setString(6, statutDb);
            pstmt.setString(7, signalement.getPhoto());
            pstmt.setInt(8, signalement.getIdSignalement());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                signalement.setStatut(SignalementStatut.toLabel(statutDb, signalement.getStatut()));
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
        String statutInitialDb = SignalementStatut.EN_ATTENTE.dbValue();
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, signalement.getDescription());
            pstmt.setString(2, signalement.getCategorie());
            pstmt.setInt(3, signalement.getIdZone());
            pstmt.setDouble(4, signalement.getLatitude());
            pstmt.setDouble(5, signalement.getLongitude());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(7, statutInitialDb);
            pstmt.setString(8, signalement.getPhoto());
            pstmt.setInt(9, signalement.getIdUser());
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                signalement.setStatut(SignalementStatut.EN_ATTENTE.label());
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idGenere = generatedKeys.getInt(1);
                        signalement.setIdSignalement(idGenere);
                        boolean affecte = new AffectationService().affecterAgentParZone(idGenere, signalement.getIdZone());
                        signalement.setStatut(affecte
                            ? SignalementStatut.AFFECTE.label()
                            : SignalementStatut.EN_ATTENTE.label());
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
        return getSignalementsFiltres(zoneNom, statut, categorie, null, null);
    }

    public List<Signalement> getSignalementsFiltres(String zoneNom, String statut, String categorie,
                                                     LocalDate dateDebut, LocalDate dateFin) {
        return getSignalementsFiltres(zoneNom, statut, categorie, dateDebut, dateFin, null, null);
    }

    public List<Signalement> getSignalementsFiltres(String zoneNom, String statut, String categorie,
                                                     LocalDate dateDebut, LocalDate dateFin,
                                                     String priorite, String affectation) {
        StringBuilder query = new StringBuilder(
                "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom FROM Signalement s "
                        + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
                        + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                        + "LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (zoneNom != null && !zoneNom.isBlank()) {
            query.append(" AND z.nomZone = ?");
            params.add(zoneNom);
        }
        if (statut != null && !statut.isBlank()) {
            query.append(" AND s.statut = ?");
            params.add(SignalementStatut.toDbValueOrSelf(statut));
        }
        if (categorie != null && !categorie.isBlank()) {
            query.append(" AND s.categorie = ?");
            params.add(categorie);
        }
        if (dateDebut != null) {
            query.append(" AND DATE(s.dateSignalement) >= ?");
            params.add(Date.valueOf(dateDebut));
        }
        if (dateFin != null) {
            query.append(" AND DATE(s.dateSignalement) <= ?");
            params.add(Date.valueOf(dateFin));
        }
        if (priorite != null && !priorite.isBlank()) {
            String prioriteNorm = normalizeFilterToken(priorite);
            if ("urgent >24h".equals(prioriteNorm)) {
                query.append(" AND s.statut = 'En attente' AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)");
            } else if ("critique >72h".equals(prioriteNorm)) {
                query.append(" AND s.statut = 'En attente' AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 72 HOUR)");
            } else if ("a traiter aujourd'hui".equals(prioriteNorm) || "a traiter aujourd hui".equals(prioriteNorm)) {
                query.append(" AND s.statut IN ('En attente', 'Affecte', 'En cours')");
            }
        }
        if (affectation != null && !affectation.isBlank()) {
            String affectationNorm = normalizeFilterToken(affectation);
            if ("assignes".equals(affectationNorm)) {
                query.append(" AND a.idAffectation IS NOT NULL");
            } else if ("non assignes".equals(affectationNorm)) {
                query.append(" AND a.idAffectation IS NULL");
            }
        }

        query.append(" ORDER BY s.dateSignalement DESC");
        return executeSignalementQuery(query.toString(), params.toArray());
    }

    public boolean updateStatut(int idSignalement, String nouveauStatut) {
        boolean result = false;
        try {
            String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";
            String statutDb = SignalementStatut.toDbValue(
                nouveauStatut,
                SignalementStatut.EN_ATTENTE.dbValue());
            try (Connection conn = getConn();
                    PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, statutDb);
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

    public int countUrgents() {
        return countSimple(
            "SELECT COUNT(*) FROM Signalement WHERE statut = 'En attente' "
            + "AND dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)");
    }

    public int countCritiques72h() {
        return countSimple(
            "SELECT COUNT(*) FROM Signalement WHERE statut = 'En attente' "
            + "AND dateSignalement < DATE_SUB(NOW(), INTERVAL 72 HOUR)");
    }

    public int countNonAssignes() {
        return countSimple(
            "SELECT COUNT(*) FROM Signalement s "
            + "LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement "
            + "WHERE s.statut IN ('En attente', 'Affecte', 'En cours') "
            + "AND a.idAffectation IS NULL");
    }

    public int countTourneesActives() {
        return countSimple(
            "SELECT COUNT(DISTINCT a.idAgent) FROM Affectation a "
            + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
            + "WHERE s.statut IN ('En attente', 'Affecte', 'En cours')");
    }

    public int countByStatut(String statut) {
        return countSimple(
            "SELECT COUNT(*) FROM Signalement WHERE statut = ?",
            SignalementStatut.toDbValueOrSelf(statut));
    }

    public int countByZone(String zoneNom) {
        return countSimple("SELECT COUNT(*) FROM Signalement s JOIN Zone z ON s.idZone = z.idZone WHERE z.nomZone = ?",
                zoneNom);
    }

    public int countByStatutAndZone(String statut, String zoneNom) {
        return countSimple(
                "SELECT COUNT(*) FROM Signalement s JOIN Zone z ON s.idZone = z.idZone WHERE s.statut = ? AND z.nomZone = ?",
                SignalementStatut.toDbValueOrSelf(statut), zoneNom);
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
        return countSimple(
            "SELECT COUNT(*) FROM Signalement WHERE statut = ? AND idUser = ?",
            SignalementStatut.toDbValueOrSelf(statut), idUser);
    }

    public int countMissionsActivesParAgent(int idAgent) {
        return countSimple(
            "SELECT COUNT(*) FROM Affectation a "
            + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
            + "WHERE a.idAgent = ? AND s.statut NOT IN ('Termine', 'Termin\u00e9')",
            idAgent);
    }

    public int countTraitesByAgent(int idAgent) {
        return countSimple(
                "SELECT COUNT(*) FROM Affectation a "
                + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
                + "WHERE a.idAgent = ? AND s.statut IN ('Termine', 'Termin\u00e9')",
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

    private String normalizeFilterToken(String value) {
        if (value == null) {
            return "";
        }
        String out = Normalizer.normalize(value, Normalizer.Form.NFD);
        out = out.replaceAll("\\p{M}+", "");
        out = out.toLowerCase(Locale.ROOT).trim();
        out = out.replaceAll("\\s+", " ");
        return out;
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
        signalement.setStatut(SignalementStatut.toLabelOrSelf(rs.getString("statut")));
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
