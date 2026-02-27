package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des utilisateurs
 */
public class UtilisateurService {

    private Connection conn;

    public UtilisateurService() {
        conn = DatabaseConnection.getConnection();
    }

    /**
     * Inscrit un nouvel utilisateur
     */
    public boolean inscription(Utilisateur utilisateur) {
        String query = "INSERT INTO utilisateur (nom, email, motPasse, role, zone) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, BCrypt.hashpw(utilisateur.getMotPasse(), BCrypt.gensalt()));
            pstmt.setString(4, utilisateur.getRole());
            pstmt.setString(5, utilisateur.getZone());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'inscription: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authentifie un utilisateur
     */
    public Utilisateur connexion(String email, String motPasse) {
        String query = "SELECT * FROM utilisateur WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("motPasse");
                if (BCrypt.checkpw(motPasse, hashedPassword)) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setIdUser(rs.getInt("idUser"));
                    utilisateur.setNom(rs.getString("nom"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setRole(rs.getString("role"));
                    utilisateur.setZone(rs.getString("zone"));
                    return utilisateur;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur";

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUser(rs.getInt("idUser"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setZone(rs.getString("zone"));
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    /**
     * Récupère les utilisateurs par rôle
     */
    public List<Utilisateur> getUtilisateursByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM utilisateur WHERE role = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUser(rs.getInt("idUser"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setZone(rs.getString("zone"));
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    /**
     * Met à jour un utilisateur
     */
    public boolean updateUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE utilisateur SET nom = ?, email = ?, zone = ? WHERE idUser = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getZone());
            pstmt.setInt(4, utilisateur.getIdUser());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un utilisateur
     */
    public boolean deleteUtilisateur(int idUser) {
        String query = "DELETE FROM utilisateur WHERE idUser = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère un utilisateur par ID
     */
    public Utilisateur getUtilisateurById(int idUser) {
        String query = "SELECT * FROM utilisateur WHERE idUser = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUser(rs.getInt("idUser"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setZone(rs.getString("zone"));
                return utilisateur;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return null;
    }
}
