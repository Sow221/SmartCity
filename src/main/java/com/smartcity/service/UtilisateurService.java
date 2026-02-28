package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des utilisateurs.
 * Les mots de passe sont conserves en clair selon le besoin du projet.
 */
public class UtilisateurService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    public boolean inscription(Utilisateur utilisateur) {
        String query = "INSERT INTO Utilisateur (nom, email, motPasse, role, zone, telephone) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getMotPasse());
            pstmt.setString(4, utilisateur.getRole());
            pstmt.setString(5, utilisateur.getZone());
            pstmt.setString(6, utilisateur.getTelephone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'inscription: " + e.getMessage());
            return false;
        }
    }

    public Utilisateur connexion(String email, String motPasse) {
        String query = "SELECT * FROM Utilisateur WHERE email = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && motPasse.equals(rs.getString("motPasse"))) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExiste(String email) {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE email = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la verification email: " + e.getMessage());
            return false;
        }
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur WHERE actif = 1 ORDER BY nom";

        try (Statement stmt = getConn().createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recuperation des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    public List<Utilisateur> getUtilisateursByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur WHERE role = ? AND actif = 1 ORDER BY nom";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recuperation des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    public Utilisateur getUtilisateurById(int idUser) {
        String query = "SELECT * FROM Utilisateur WHERE idUser = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recuperation: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET nom = ?, email = ?, zone = ?, telephone = ? WHERE idUser = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getZone());
            pstmt.setString(4, utilisateur.getTelephone());
            pstmt.setInt(5, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise a jour: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUtilisateurAdmin(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET nom = ?, email = ?, motPasse = ?, role = ?, zone = ?, telephone = ? " +
                "WHERE idUser = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getMotPasse());
            pstmt.setString(4, utilisateur.getRole());
            pstmt.setString(5, utilisateur.getZone());
            pstmt.setString(6, utilisateur.getTelephone());
            pstmt.setInt(7, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise a jour admin: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUtilisateur(int idUser) {
        String query = "UPDATE Utilisateur SET actif = 0 WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la desactivation: " + e.getMessage());
            return false;
        }
    }

    public int countAllActifs() {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage utilisateurs: " + e.getMessage());
            return 0;
        }
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUser(rs.getInt("idUser"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        u.setMotPasse(rs.getString("motPasse"));
        u.setRole(rs.getString("role"));
        u.setZone(rs.getString("zone"));

        try {
            u.setTelephone(rs.getString("telephone"));
        } catch (SQLException e) {
            u.setTelephone(null);
        }

        try {
            u.setActif(rs.getInt("actif") == 1);
        } catch (SQLException e) {
            u.setActif(true);
        }

        try {
            Timestamp ts = rs.getTimestamp("dateInscription");
            if (ts != null) {
                u.setDateInscription(ts.toLocalDateTime());
            }
        } catch (SQLException e) {
            // ignore
        }

        return u;
    }
}
