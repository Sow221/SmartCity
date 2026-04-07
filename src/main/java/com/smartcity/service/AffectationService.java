package com.smartcity.service;

import com.smartcity.model.Affectation;
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

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
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
        if (estDejaAffecte(idSignalement)) return false;
        String queryAgent = "SELECT u.idUser FROM Utilisateur u "
                + "LEFT JOIN Affectation a ON a.idAgent = u.idUser "
                + "AND a.idSignalement IN (SELECT idSignalement FROM Signalement WHERE statut NOT IN ('Termin\u00e9','Termine')) "
                + "WHERE u.idZone = ? AND u.role = 'Agent' AND u.actif = 1 "
                + "GROUP BY u.idUser ORDER BY COUNT(a.idAffectation) ASC LIMIT 1";
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                int idAgent;
                try (PreparedStatement pstmt = conn.prepareStatement(queryAgent)) {
                    pstmt.setInt(1, idZone);
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
                    ps2.setString(1, "Affecté");
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
        String query = "SELECT s.*, u.nom AS utilisateurNom, z.nomZone AS zoneNom "
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
        s.setStatut(rs.getString("statut"));
        s.setPhoto(rs.getString("photo"));
        s.setIdUser(rs.getInt("idUser"));
        Timestamp ts = rs.getTimestamp("dateSignalement");
        if (ts != null) s.setDateSignalement(ts.toLocalDateTime());
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
        a.setCommentaire(rs.getString("commentaire"));
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
            logger.error("Erreur sauvegarderCommentaire: {}", e.getMessage());
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
            logger.error("Erreur getCommentaireBySignalement: {}", e.getMessage());
            return null;
        }
    }
}
