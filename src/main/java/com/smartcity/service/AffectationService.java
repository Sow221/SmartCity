package com.smartcity.service;

import com.smartcity.model.Affectation;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Service pour la gestion des affectations
 */
public class AffectationService {
    private static final Logger logger = LoggerFactory.getLogger(AffectationService.class);

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    /**
     * Crée une nouvelle affectation.
     * idSignalement est UNIQUE : une seule affectation par signalement.
     * dateAffectation est gérée par MySQL (current_timestamp).
     */
    public boolean creerAffectation(int idSignalement, int idAgent) {
        String query = "INSERT INTO Affectation (idSignalement, idAgent) VALUES (?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            pstmt.setInt(2, idAgent);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'affectation", e);
            return false;
        }
    }

    /**
     * Enregistre la date de collecte et un commentaire optionnel
     * lorsque l'agent marque la collecte comme effectuée.
     */
    public boolean marquerCollecte(int idAffectation, String commentaire) {
        String query = "UPDATE Affectation SET dateCollecte = ?, commentaire = ? WHERE idAffectation = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, commentaire);
            pstmt.setInt(3, idAffectation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors du marquage de collecte", e);
            return false;
        }
    }

    /**
     * Récupère toutes les affectations
     */
    public List<Affectation> getAllAffectations() {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation ORDER BY dateAffectation DESC";
        try (Statement stmt = getConn().createStatement()) {
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    liste.add(mapResultSetToAffectation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des affectations", e);
        }
        return liste;
    }

    /**
     * Récupère les affectations d'un agent
     */
    public List<Affectation> getAffectationsByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT * FROM Affectation WHERE idAgent = ? ORDER BY dateAffectation DESC";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToAffectation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des affectations par agent", e);
        }
        return liste;
    }

    /**
     * Récupère l'affectation d'un signalement (unique par contrainte DB)
     */
    public Affectation getAffectationBySignalement(int idSignalement) {
        String query = "SELECT * FROM Affectation WHERE idSignalement = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAffectation(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de l'affectation par signalement", e);
        }
        return null;
    }

    /**
     * Vérifie si un signalement est déjà affecté
     */
    public boolean estDejaAffecte(int idSignalement) {
        return getAffectationBySignalement(idSignalement) != null;
    }

    /**
     * Supprime une affectation
     */
    public boolean supprimerAffectation(int idAffectation) {
        String query = "DELETE FROM Affectation WHERE idAffectation = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAffectation);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression d'une affectation", e);
            return false;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Affectation (toutes les colonnes)
     */
    private Affectation mapResultSetToAffectation(ResultSet rs) throws SQLException {
        Affectation a = new Affectation();
        a.setIdAffectation(rs.getInt("idAffectation"));
        a.setIdSignalement(rs.getInt("idSignalement"));
        a.setIdAgent(rs.getInt("idAgent"));

        Timestamp tsAffectation = rs.getTimestamp("dateAffectation");
        if (tsAffectation != null) {
            a.setDateAffectation(tsAffectation.toLocalDateTime());
        }

        Timestamp tsCollecte = rs.getTimestamp("dateCollecte");
        if (tsCollecte != null) {
            a.setDateCollecte(tsCollecte.toLocalDateTime());
        }

        a.setCommentaire(rs.getString("commentaire"));
        return a;
    }
}
