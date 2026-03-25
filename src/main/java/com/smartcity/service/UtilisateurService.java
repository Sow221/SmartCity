package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des utilisateurs.
 * Les mots de passe sont conservés en clair selon le besoin du projet.
 */
public class UtilisateurService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    public boolean inscription(Utilisateur utilisateur) {
        String query = "INSERT INTO Utilisateur (nom, prenom, email, age, motDePasse, localite, photoProfil, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setInt(4, utilisateur.getAge());
            pstmt.setString(5, utilisateur.getMotDePasse());
            pstmt.setString(6, utilisateur.getLocalite());
            pstmt.setString(7, utilisateur.getPhotoProfil());
            pstmt.setString(8, utilisateur.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'inscription: " + e.getMessage());
            return false;
        }
    }

    public Utilisateur connexion(String email, String motDePasse) {
        String query = "SELECT * FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && motDePasse.equals(rs.getString("motDePasse"))) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    public boolean emailExiste(String email) {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification email: " + e.getMessage());
            return false;
        }
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur ORDER BY nom, prenom";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    public List<Utilisateur> getUtilisateursByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur WHERE role = ? ORDER BY nom, prenom";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    public List<Utilisateur> getAgents() {
        return getUtilisateursByRole("AGENT");
    }

    public List<Utilisateur> getCitoyens() {
        return getUtilisateursByRole("CITOYEN");
    }

    public List<Utilisateur> getAdmins() {
        return getUtilisateursByRole("ADMIN");
    }

    public Utilisateur getUtilisateurById(int idUser) {
        String query = "SELECT * FROM Utilisateur WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return null;
    }

    public Utilisateur getUtilisateurByEmail(String email) {
        String query = "SELECT * FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET nom = ?, prenom = ?, email = ?, age = ?, " +
                "localite = ?, photoProfil = ? WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setInt(4, utilisateur.getAge());
            pstmt.setString(5, utilisateur.getLocalite());
            pstmt.setString(6, utilisateur.getPhotoProfil());
            pstmt.setInt(7, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMotDePasse(int idUser, String nouveauMotDePasse) {
        String query = "UPDATE Utilisateur SET motDePasse = ? WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nouveauMotDePasse);
            pstmt.setInt(2, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePhotoProfil(int idUser, String photoProfil) {
        String query = "UPDATE Utilisateur SET photoProfil = ? WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, photoProfil);
            pstmt.setInt(2, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la photo: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUtilisateurAdmin(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET nom = ?, prenom = ?, email = ?, age = ?, " +
                "motDePasse = ?, localite = ?, photoProfil = ?, role = ? WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setInt(4, utilisateur.getAge());
            pstmt.setString(5, utilisateur.getMotDePasse());
            pstmt.setString(6, utilisateur.getLocalite());
            pstmt.setString(7, utilisateur.getPhotoProfil());
            pstmt.setString(8, utilisateur.getRole());
            pstmt.setInt(9, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour admin: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUtilisateur(int idUser) {
        String query = "DELETE FROM Utilisateur WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    // Méthodes de comptage
    public int countAll() {
        String query = "SELECT COUNT(*) FROM Utilisateur";

        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            return 0;
        }
    }

    public int countByRole(String role) {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE role = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            return 0;
        }
    }

    public int countByLocalite(String localite) {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE localite = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, localite);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            return 0;
        }
    }

    public double getAgeMoyen() {
        String query = "SELECT AVG(age) FROM Utilisateur WHERE age IS NOT NULL";

        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de l'âge moyen: " + e.getMessage());
            return 0;
        }
    }

    public double getAgeMoyenByRole(String role) {
        String query = "SELECT AVG(age) FROM Utilisateur WHERE role = ? AND age IS NOT NULL";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de l'âge moyen: " + e.getMessage());
            return 0;
        }
    }

    public List<String> getAllLocalites() {
        List<String> localites = new ArrayList<>();
        String query = "SELECT DISTINCT localite FROM Utilisateur WHERE localite IS NOT NULL ORDER BY localite";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                localites.add(rs.getString("localite"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des localités: " + e.getMessage());
        }
        return localites;
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUser(rs.getInt("idUser"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setAge(rs.getInt("age"));
        u.setMotDePasse(rs.getString("motDePasse"));
        u.setLocalite(rs.getString("localite"));
        u.setPhotoProfil(rs.getString("photoProfil"));
        u.setRole(rs.getString("role"));

        // Gestion des valeurs null
      /*  if (rs.wasNull()) {
            u.setAge(null);
        }*/

        return u;
    }
}