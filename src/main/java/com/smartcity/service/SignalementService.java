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
        String query = "INSERT INTO Signalement (idZone, idUser, description, categorie, dateSignalement, statut, photoDepot, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, signalement.getIdZone());
            pstmt.setInt(2, signalement.getIdUser());
            pstmt.setString(3, signalement.getDescription());
            pstmt.setString(4, signalement.getCategorie());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(6, "EN_ATTENTE");
            pstmt.setString(7, signalement.getPhotoDepot());
            pstmt.setDouble(8, signalement.getLatitude());
            pstmt.setDouble(9, signalement.getLongitude());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du signalement: " + e.getMessage());
            return false;
        }
    }

    public List<Signalement> getAllSignalements() {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query);
    }

    public List<Signalement> getSignalementsByZone(int idZone) {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "WHERE s.idZone = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idZone);
    }

    public List<Signalement> getSignalementsByUtilisateur(int idUser) {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "WHERE s.idUser = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idUser);
    }

    public List<Signalement> getSignalementsByZoneAndDate(int idZone, LocalDate date) {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "WHERE s.idZone = ? AND DATE(s.dateSignalement) = ? " +
                "ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idZone, Date.valueOf(date));
    }

    public List<Signalement> getSignalementsByAgent(int idAgent) {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "INNER JOIN Affectation a ON s.idSignalement = a.idSignalement " +
                "WHERE a.idAgent = ? ORDER BY s.dateSignalement DESC";
        return executeSignalementQuery(query, idAgent);
    }

    public List<Signalement> getSignalementsFiltres(Integer idZone, String statut, String categorie) {
        StringBuilder query = new StringBuilder(
                "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                        "FROM Signalement s " +
                        "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                        "LEFT JOIN Zone z ON s.idZone = z.idZone WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (idZone != null) {
            query.append(" AND s.idZone = ?");
            params.add(idZone);
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

    public Signalement getSignalementById(int idSignalement) {
        String query = "SELECT s.*, u.nom, u.prenom, z.nomZone " +
                "FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "WHERE s.idSignalement = ?";

        List<Signalement> result = executeSignalementQuery(query, idSignalement);
        return result.isEmpty() ? null : result.get(0);
    }

    public boolean updateStatut(int idSignalement, String nouveauStatut) {
        String query = "UPDATE Signalement SET statut = ? WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauStatut);
            pstmt.setInt(2, idSignalement);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSignalement(Signalement signalement) {
        String query = "UPDATE Signalement SET idZone = ?, description = ?, categorie = ?, " +
                "photoDepot = ?, latitude = ?, longitude = ? WHERE idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, signalement.getIdZone());
            pstmt.setString(2, signalement.getDescription());
            pstmt.setString(3, signalement.getCategorie());
            pstmt.setString(4, signalement.getPhotoDepot());
            pstmt.setDouble(5, signalement.getLatitude());
            pstmt.setDouble(6, signalement.getLongitude());
            pstmt.setInt(7, signalement.getIdSignalement());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du signalement: " + e.getMessage());
            return false;
        }
    }

    public boolean supprimerSignalement(int idSignalement) {
        // D'abord supprimer les affectations liées
        String deleteAffectations = "DELETE FROM Affectation WHERE idSignalement = ?";
        String deleteSignalement = "DELETE FROM Signalement WHERE idSignalement = ?";

        try (PreparedStatement pstmt1 = getConn().prepareStatement(deleteAffectations);
             PreparedStatement pstmt2 = getConn().prepareStatement(deleteSignalement)) {

            // Supprimer les affectations
            pstmt1.setInt(1, idSignalement);
            pstmt1.executeUpdate();

            // Supprimer le signalement
            pstmt2.setInt(1, idSignalement);
            return pstmt2.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    public boolean affecterAgent(int idSignalement, int idAgent) {
        String query = "INSERT INTO Affectation (idSignalement, idAgent, dateAffectation) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            pstmt.setInt(2, idAgent);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            // Mettre à jour le statut du signalement
            updateStatut(idSignalement, "EN_COURS");

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affectation: " + e.getMessage());
            return false;
        }
    }

    public Integer getAgentAffecte(int idSignalement) {
        String query = "SELECT idAgent FROM Affectation WHERE idSignalement = ? ORDER BY dateAffectation DESC LIMIT 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idAgent");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'agent: " + e.getMessage());
        }
        return null;
    }

    // Méthodes de comptage
    public int countAll() {
        return countSimple("SELECT COUNT(*) FROM Signalement");
    }

    public int countByStatut(String statut) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ?", statut);
    }

    public int countByStatutAndUtilisateur(String statut, int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE statut = ? AND idUser = ?", statut, idUser);
    }

    public int countByZone(int idZone) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE idZone = ?", idZone);
    }

    public int countByUtilisateur(int idUser) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE idUser = ?", idUser);
    }

    public int countByCategorie(String categorie) {
        return countSimple("SELECT COUNT(*) FROM Signalement WHERE categorie = ?", categorie);
    }

    public int countByPeriode(LocalDate debut, LocalDate fin) {
        String query = "SELECT COUNT(*) FROM Signalement WHERE DATE(dateSignalement) BETWEEN ? AND ?";
        return countSimple(query, Date.valueOf(debut), Date.valueOf(fin));
    }

    public int countTraitesByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation a " +
                "JOIN Signalement s ON s.idSignalement = a.idSignalement " +
                "WHERE a.idAgent = ? AND s.statut = 'COLLECTE'";

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

    public int countEnCoursByAgent(int idAgent) {
        String query = "SELECT COUNT(*) FROM Affectation a " +
                "JOIN Signalement s ON s.idSignalement = a.idSignalement " +
                "WHERE a.idAgent = ? AND s.statut = 'EN_COURS'";

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

    // Méthodes privées utilitaires
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
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }

        return liste;
    }

    private Signalement mapResultSetToSignalement(ResultSet rs) throws SQLException {
        Signalement signalement = new Signalement();
        signalement.setIdSignalement(rs.getInt("idSignalement"));
        signalement.setIdZone(rs.getInt("idZone"));
        signalement.setIdUser(rs.getInt("idUser"));
        signalement.setDescription(rs.getString("description"));
        signalement.setCategorie(rs.getString("categorie"));
        signalement.setStatut(rs.getString("statut"));
        signalement.setPhotoDepot(rs.getString("photoDepot"));

        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null) {
            signalement.setDateSignalement(timestamp.toLocalDateTime());
        }

        // Gestion des valeurs nulles pour latitude/longitude
        double latitude = rs.getDouble("latitude");
        if (!rs.wasNull()) {
            signalement.setLatitude(latitude);
        }

        double longitude = rs.getDouble("longitude");
        if (!rs.wasNull()) {
            signalement.setLongitude(longitude);
        }

        // Récupération des informations supplémentaires (sans setters dédiés)
        // Ces informations seront utilisées directement dans les contrôleurs
        // via des getters que vous pouvez ajouter si nécessaire

        return signalement;
    }

    // Méthodes utilitaires pour obtenir les informations des jointures
    public String getNomUtilisateurForSignalement(int idSignalement) {
        String query = "SELECT u.nom, u.prenom FROM Signalement s " +
                "LEFT JOIN Utilisateur u ON s.idUser = u.idUser " +
                "WHERE s.idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null || prenom != null) {
                    StringBuilder nomComplet = new StringBuilder();
                    if (prenom != null) nomComplet.append(prenom).append(" ");
                    if (nom != null) nomComplet.append(nom);
                    return nomComplet.toString().trim();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom: " + e.getMessage());
        }
        return "Utilisateur #" + idSignalement;
    }

    public String getNomZoneForSignalement(int idSignalement) {
        String query = "SELECT z.nomZone FROM Signalement s " +
                "LEFT JOIN Zone z ON s.idZone = z.idZone " +
                "WHERE s.idSignalement = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idSignalement);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nomZone = rs.getString("nomZone");
                if (nomZone != null) {
                    return nomZone;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la zone: " + e.getMessage());
        }
        return "Zone inconnue";
    }
}