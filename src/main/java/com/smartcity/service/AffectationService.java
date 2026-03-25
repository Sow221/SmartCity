package com.smartcity.service;

import com.smartcity.model.Affectation;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des affectations
 */
public class AffectationService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    /**
     * Crée une nouvelle affectation.
     * Un signalement ne peut avoir qu'une seule affectation active.
     */
    public boolean creerAffectation(int idSignalement, int idAgent) {
        // Vérifier d'abord si le signalement n'est pas déjà affecté
        if (estDejaAffecte(idSignalement)) {
            System.err.println("Ce signalement est déjà affecté à un agent");
            return false;
        }

        String query = "INSERT INTO Affectation (idSignalement, idAgent, dateAffectation) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            pstmt.setInt(2, idAgent);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int result = pstmt.executeUpdate();

            // Si l'affectation est créée avec succès, mettre à jour le statut du signalement
            if (result > 0) {
                mettreAJourStatutSignalement(idSignalement, "EN_COURS");
            }

            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'affectation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour le statut d'un signalement
     */
    private void mettreAJourStatutSignalement(int idSignalement, String nouveauStatut) {
        String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idSignalement);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
        }
    }

    /**
     * Marque une collecte comme terminée
     */
    public boolean marquerCollecteTerminee(int idSignalement) {
        return marquerCollecteTerminee(idSignalement, null);
    }

    /**
     * Marque une collecte comme terminée avec un commentaire
     */
    public boolean marquerCollecteTerminee(int idSignalement, String commentaire) {
        String query = "UPDATE Affectation SET dateCollecte = ?, commentaire = ? WHERE idSignalement = ? AND dateCollecte IS NULL";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, commentaire);
            pstmt.setInt(3, idSignalement);

            int result = pstmt.executeUpdate();

            // Si la collecte est marquée comme terminée, mettre à jour le statut du signalement
            if (result > 0) {
                mettreAJourStatutSignalement(idSignalement, "COLLECTE");
            }

            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de collecte: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère toutes les affectations
     */
    public List<Affectation> getAllAffectations() {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT a.*, s.description as signalement_desc, u.nom as agent_nom, u.prenom as agent_prenom " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "LEFT JOIN Utilisateur u ON a.idAgent = u.idUser " +
                "ORDER BY a.dateAffectation DESC";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                liste.add(mapResultSetToAffectation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des affectations: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les affectations d'un agent
     */
    public List<Affectation> getAffectationsByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT a.*, s.description as signalement_desc " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "WHERE a.idAgent = ? " +
                "ORDER BY a.dateAffectation DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToAffectation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des affectations: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les affectations en cours d'un agent (non collectées)
     */
    public List<Affectation> getAffectationsEnCoursByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT a.*, s.description as signalement_desc " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "WHERE a.idAgent = ? AND a.dateCollecte IS NULL " +
                "ORDER BY a.dateAffectation DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToAffectation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des affectations en cours: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les affectations terminées d'un agent (collectées)
     */
    public List<Affectation> getAffectationsTermineesByAgent(int idAgent) {
        List<Affectation> liste = new ArrayList<>();
        String query = "SELECT a.*, s.description as signalement_desc " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "WHERE a.idAgent = ? AND a.dateCollecte IS NOT NULL " +
                "ORDER BY a.dateCollecte DESC";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                liste.add(mapResultSetToAffectation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des affectations terminées: " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère l'affectation d'un signalement
     */
    public Affectation getAffectationBySignalement(int idSignalement) {
        String query = "SELECT a.*, s.description as signalement_desc, u.nom as agent_nom, u.prenom as agent_prenom " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "LEFT JOIN Utilisateur u ON a.idAgent = u.idUser " +
                "WHERE a.idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAffectation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'affectation: " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupère une affectation par son ID
     */
    public Affectation getAffectationById(int idAffectation) {
        String query = "SELECT a.*, s.description as signalement_desc, u.nom as agent_nom, u.prenom as agent_prenom " +
                "FROM Affectation a " +
                "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement " +
                "LEFT JOIN Utilisateur u ON a.idAgent = u.idUser " +
                "WHERE a.idAffectation = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAffectation);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAffectation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'affectation: " + e.getMessage());
        }
        return null;
    }

    /**
     * Vérifie si un signalement est déjà affecté
     */
    public boolean estDejaAffecte(int idSignalement) {
        String query = "SELECT COUNT(*) FROM Affectation WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification d'affectation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère le nombre d'affectations par agent
     */
    public int countByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation WHERE idAgent = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
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
     * Récupère le nombre d'affectations en cours par agent
     */
    public int countEnCoursByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation WHERE idAgent = ? AND dateCollecte IS NULL";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
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
     * Récupère le nombre d'affectations terminées par agent
     */
    public int countTermineesByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation WHERE idAgent = ? AND dateCollecte IS NOT NULL";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAgent);
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
     * Supprime une affectation
     */
    public boolean supprimerAffectation(int idAffectation) {
        // Récupérer d'abord l'ID du signalement pour mettre à jour son statut
        Affectation affectation = getAffectationById(idAffectation);

        String query = "DELETE FROM Affectation WHERE idAffectation = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idAffectation);
            int result = pstmt.executeUpdate();

            // Si la suppression réussit, remettre le statut du signalement en attente
            if (result > 0 && affectation != null) {
                mettreAJourStatutSignalement(affectation.getIdSignalement(), "EN_ATTENTE");
            }

            return result > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime toutes les affectations d'un signalement
     */
    public boolean supprimerAffectationsBySignalement(int idSignalement) {
        String query = "DELETE FROM Affectation WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mappe un ResultSet vers un objet Affectation
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

        // Vérifier si la colonne dateCollecte existe (elle n'existe pas dans votre table)
        try {
            Timestamp tsCollecte = rs.getTimestamp("dateCollecte");
            if (tsCollecte != null) {
                a.setDateCollecte(tsCollecte.toLocalDateTime());
            }
        } catch (SQLException e) {
            // La colonne n'existe pas, on ignore
        }

        // Vérifier si la colonne commentaire existe
        try {
            a.setCommentaire(rs.getString("commentaire"));
        } catch (SQLException e) {
            // La colonne n'existe pas, on ignore
        }

        // Récupérer les informations supplémentaires si disponibles
        try {
            a.setSignalementDescription(rs.getString("signalement_desc"));
        } catch (SQLException e) {
            // Ignorer
        }

        try {
            String agentNom = rs.getString("agent_nom");
            String agentPrenom = rs.getString("agent_prenom");
            if (agentNom != null || agentPrenom != null) {
                StringBuilder nomComplet = new StringBuilder();
                if (agentPrenom != null) nomComplet.append(agentPrenom).append(" ");
                if (agentNom != null) nomComplet.append(agentNom);
                a.setAgentNom(nomComplet.toString().trim());
            }
        } catch (SQLException e) {
            // Ignorer
        }

        return a;
    }
}