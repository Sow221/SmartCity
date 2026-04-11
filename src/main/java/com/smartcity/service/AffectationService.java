package com.smartcity.service;

import com.smartcity.model.Affectation;
import com.smartcity.model.SignalementStatut;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des affectations.
 */
public class AffectationService {
    private static final Logger logger = LoggerFactory.getLogger(AffectationService.class);

    private final javax.sql.DataSource dataSource;

    public AffectationService() {
        this.dataSource = null;
    }

    public AffectationService(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() throws SQLException {
        if (dataSource != null) return dataSource.getConnection();
        return DatabaseConnection.getConnection();
    }

    public java.util.Map<Integer, String> getAgentNomParSignalement() {
        java.util.Map<Integer, String> map = new java.util.HashMap<>();
        String query = "SELECT a.idSignalement, u.nom FROM Affectation a "
            + "JOIN Utilisateur u ON a.idAgent = u.idUser";
        try (Connection conn = getConn();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) map.put(rs.getInt("idSignalement"), rs.getString("nom"));
        } catch (SQLException e) {
            logger.error("Erreur getAgentNomParSignalement", e);
        }
        return map;
    }

    public boolean creerAffectation(int idSignalement, int idAgent) {
        String query = "INSERT INTO Affectation (idSignalement, idAgent) VALUES (?, ?)";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            pstmt.setInt(2, idAgent);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation de l'affectation", e);
            return false;
        }
    }

    public boolean marquerCollecte(int idAffectation, String commentaire) {
        String query = "UPDATE Affectation SET dateCollecte = ?, commentaire = ? WHERE idAffectation = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, commentaire);
            pstmt.setInt(3, idAffectation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors du marquage de collecte", e);
            return false;
        }
    }

    public List<Affectation> getAllAffectations() {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation ORDER BY dateAffectation DESC";
        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) liste.add(mapResultSetToAffectation(rs));
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des affectations", e);
        }
        return liste;
    }

    public List<Affectation> getAffectationsByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation WHERE idAgent = ? ORDER BY dateAffectation DESC";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) liste.add(mapResultSetToAffectation(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des affectations par agent", e);
        }
        return liste;
    }

    public Affectation getAffectationBySignalement(int idSignalement) {
        String query = "SELECT * FROM Affectation WHERE idSignalement = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToAffectation(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation de l'affectation par signalement", e);
        }
        return null;
    }

    public boolean estDejaAffecte(int idSignalement) {
        return getAffectationBySignalement(idSignalement) != null;
    }

    /**
     * Affecte l'agent le moins charge de la zone au signalement,
     * puis passe le statut du signalement a "Affecte".
     */
    public boolean affecterAgentParZone(int idSignalement, int idZone) {
        String queryAgent = "SELECT u.idUser FROM Utilisateur u "
                + "LEFT JOIN Affectation a ON a.idAgent = u.idUser "
                + "AND a.idSignalement IN (SELECT idSignalement FROM Signalement WHERE statut != ?) "
                + "WHERE u.idZone = ? AND u.role = 'Agent' AND u.actif = 1 "
                + "GROUP BY u.idUser ORDER BY COUNT(a.idAffectation) ASC LIMIT 1 FOR UPDATE";
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                // FUNC-02 : vérifier l'existence déjà dans la transaction (après le lock)
                try (PreparedStatement chk = conn.prepareStatement(
                        "SELECT idAffectation FROM Affectation WHERE idSignalement = ? FOR UPDATE")) {
                    chk.setInt(1, idSignalement);
                    try (ResultSet rsChk = chk.executeQuery()) {
                        if (rsChk.next()) {
                            conn.rollback();
                            logger.info("Signalement #{} déjà affecté (race condition évitée)", idSignalement);
                            return false;
                        }
                    }
                }
                int idAgent;
                try (PreparedStatement pstmt = conn.prepareStatement(queryAgent)) {
                    pstmt.setString(1, SignalementStatut.TERMINE.dbValue());
                    pstmt.setInt(2, idZone);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            logger.warn("Aucun agent actif disponible pour la zone #{} — signalement #{} reste En attente",
                                idZone, idSignalement);
                            return false;
                        }
                        idAgent = rs.getInt("idUser");
                    }
                }
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "INSERT INTO Affectation (idSignalement, idAgent) VALUES (?, ?)")) {
                    ps1.setInt(1, idSignalement);
                    ps1.setInt(2, idAgent);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE Signalement SET statut = ? WHERE idSignalement = ?")) {
                    ps2.setString(1, SignalementStatut.AFFECTE.dbValue());
                    ps2.setInt(2, idSignalement);
                    ps2.executeUpdate();
                }
                conn.commit();
                logger.info("Signalement #{} affect\u00e9 \u00e0 l'agent #{}", idSignalement, idAgent);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Erreur affecterAgentParZone, rollback effectu\u00e9", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Erreur connexion affecterAgentParZone", e);
            return false;
        }
    }

    public List<com.smartcity.model.Signalement> getSignalementsByAgent(int idAgent) {
        List<com.smartcity.model.Signalement> liste = new ArrayList<>();
        String query = "SELECT s.*, a.dateCollecte AS dateCollecte, u.nom AS utilisateurNom, z.nomZone AS zoneNom "
                + "FROM Affectation a "
                + "JOIN Signalement s ON s.idSignalement = a.idSignalement "
                + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser AND u.actif = 1 "
                + "LEFT JOIN Zone z ON s.idZone = z.idZone "
                + "WHERE a.idAgent = ? ORDER BY s.dateSignalement DESC";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) liste.add(mapSignalement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur getSignalementsByAgent", e);
        }
        return liste;
    }

    private com.smartcity.model.Signalement mapSignalement(ResultSet rs) throws SQLException {
        com.smartcity.model.Signalement s = new com.smartcity.model.Signalement();
        s.setIdSignalement(rs.getInt("idSignalement"));
        s.setDescription(rs.getString("description"));
        s.setCategorie(rs.getString("categorie"));
        s.setIdZone(rs.getInt("idZone"));
        s.setLatitude(rs.getDouble("latitude"));
        s.setLongitude(rs.getDouble("longitude"));
        s.setStatut(SignalementStatut.toLabelOrSelf(rs.getString("statut")));
        s.setPhoto(rs.getString("photo"));
        s.setIdUser(rs.getInt("idUser"));
        Timestamp ts = rs.getTimestamp("dateSignalement");
        if (ts != null) s.setDateSignalement(ts.toLocalDateTime());
        Timestamp tsCollecte = rs.getTimestamp("dateCollecte");
        if (tsCollecte != null) s.setDateCollecte(tsCollecte.toLocalDateTime());
        String zoneNom = rs.getString("zoneNom");
        s.setZoneNom(zoneNom != null ? zoneNom : "Zone #" + s.getIdZone());
        String utilisateurNom = rs.getString("utilisateurNom");
        s.setUtilisateurNom(utilisateurNom != null ? utilisateurNom : "Utilisateur #" + s.getIdUser());
        return s;
    }

    public boolean supprimerAffectation(int idAffectation) {
        String query = "DELETE FROM Affectation WHERE idAffectation = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idAffectation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression d'une affectation", e);
            return false;
        }
    }

    public boolean supprimerAffectationParSignalement(int idSignalement) {
        String query = "DELETE FROM Affectation WHERE idSignalement = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Erreur supprimerAffectationParSignalement", e);
            return false;
        }
    }

    private Affectation mapResultSetToAffectation(ResultSet rs) throws SQLException {
        Affectation a = new Affectation();
        a.setIdAffectation(rs.getInt("idAffectation"));
        a.setIdSignalement(rs.getInt("idSignalement"));
        a.setIdAgent(rs.getInt("idAgent"));
        Timestamp tsAffectation = rs.getTimestamp("dateAffectation");
        if (tsAffectation != null) a.setDateAffectation(tsAffectation.toLocalDateTime());
        Timestamp tsCollecte = rs.getTimestamp("dateCollecte");
        if (tsCollecte != null) a.setDateCollecte(tsCollecte.toLocalDateTime());
        try { a.setCommentaire(rs.getString("commentaire")); } catch (SQLException ignored) {}
        return a;
    }

    public boolean sauvegarderCommentaire(int idSignalement, String commentaire) {
        String query = "UPDATE Affectation SET commentaire = ? WHERE idSignalement = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, commentaire);
            pstmt.setInt(2, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur sauvegarderCommentaire", e);
            return false;
        }
    }

    public String getCommentaireBySignalement(int idSignalement) {
        String query = "SELECT commentaire FROM Affectation WHERE idSignalement = ? LIMIT 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("commentaire") : null;
            }
        } catch (SQLException e) {
            logger.error("Erreur getCommentaireBySignalement", e);
            return null;
        }
    }

    private static volatile boolean positionAgentTableMissing = false;

    /** Réinitialise le flag après création de la table (appelé par GpsApiServer). */
    public static void resetPositionTableFlag() {
        positionAgentTableMissing = false;
    }

    public List<AgentLiveStatus> getAgentLiveStatuses() {
        if (positionAgentTableMissing) return new ArrayList<>();
        List<AgentLiveStatus> list = new ArrayList<>();
        String query = "SELECT u.idUser, u.nom, z.nomZone, pa.latitude, pa.longitude, pa.updatedAt, "
            + "SUM(CASE WHEN s.statut IN ('En attente', 'Affecte', 'En cours') THEN 1 ELSE 0 END) AS missionsActives, "
            + "SUM(CASE WHEN s.statut = 'En cours' THEN 1 ELSE 0 END) AS missionsEnCours, "
            + "MIN(CASE WHEN s.statut IN ('En attente', 'Affecte', 'En cours') THEN s.dateSignalement END) AS prochaineMissionDate "
            + "FROM Utilisateur u "
            + "LEFT JOIN Zone z ON u.idZone = z.idZone "
            + "LEFT JOIN position_agent pa ON pa.idAgent = u.idUser "
            + "LEFT JOIN Affectation a ON a.idAgent = u.idUser "
            + "LEFT JOIN Signalement s ON s.idSignalement = a.idSignalement "
            + "WHERE u.role = 'Agent' AND u.actif = 1 "
            + "GROUP BY u.idUser, u.nom, z.nomZone, pa.latitude, pa.longitude, pa.updatedAt "
            + "ORDER BY missionsActives DESC, u.nom ASC";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                AgentLiveStatus status = new AgentLiveStatus();
                status.idAgent = rs.getInt("idUser");
                status.nomAgent = rs.getString("nom");
                status.zoneNom = rs.getString("nomZone");
                status.latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                status.longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
                Timestamp updated = rs.getTimestamp("updatedAt");
                if (updated != null) {
                    status.dernierePosition = updated.toLocalDateTime();
                }
                status.missionsActives = rs.getInt("missionsActives");
                status.missionsEnCours = rs.getInt("missionsEnCours");
                Timestamp prochaineMission = rs.getTimestamp("prochaineMissionDate");
                if (prochaineMission != null) {
                    status.prochaineMission = prochaineMission.toLocalDateTime();
                }
                list.add(status);
            }
        } catch (java.sql.SQLSyntaxErrorException e) {
            positionAgentTableMissing = true;
            logger.warn("Table position_agent absente — live tracking désactivé. Exécuter scripts/migrations/2026-04-create_gps_tables.sql");
        } catch (SQLException e) {
            logger.error("Erreur getAgentLiveStatuses", e);
        }
        return list;
    }

    public static class AgentLiveStatus {
        public int idAgent;
        public String nomAgent;
        public String zoneNom;
        public Integer missionsActives;
        public Integer missionsEnCours;
        public Double latitude;
        public Double longitude;
        public LocalDateTime dernierePosition;
        public LocalDateTime prochaineMission;
    }
}
