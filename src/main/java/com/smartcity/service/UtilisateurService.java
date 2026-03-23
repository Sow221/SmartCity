package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des utilisateurs.
 * Les mots de passe sont maintenant hashés avec BCrypt.
 */
public class UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    public boolean inscription(Utilisateur utilisateur) {
        String hashedPassword = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
        String query = "INSERT INTO Utilisateur (prenom, nom, email, motDePasse, role, age, localite, photoProfil, idZone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getPrenom());
            pstmt.setString(2, utilisateur.getNom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, utilisateur.getRole());
            pstmt.setInt(6, utilisateur.getAge());
            pstmt.setString(7, utilisateur.getLocalite());
            pstmt.setString(8, utilisateur.getPhotoProfil());
            pstmt.setInt(9, utilisateur.getIdZone());
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logger.info("Inscription réussie pour l'utilisateur: {}", utilisateur.getEmail());
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'inscription pour {}: {}", utilisateur.getEmail(), e.getMessage());
            return false;
        }
    }

    public Utilisateur connexion(String email, String motPasse) {
        String query = "SELECT * FROM Utilisateur WHERE email = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("motDePasse");
                boolean passwordMatches;
                if (storedPassword.startsWith("$")) {
                    // Hashed password
                    passwordMatches = BCrypt.checkpw(motPasse, storedPassword);
                } else {
                    // Plain password (legacy)
                    passwordMatches = motPasse.equals(storedPassword);
                }
                if (passwordMatches) {
                    logger.info("Connexion réussie pour: {}", email);
                    return mapResultSetToUtilisateur(rs);
                } else {
                    logger.warn("Tentative de connexion échouée pour: {}", email);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la connexion pour {}: {}", email, e.getMessage());
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
        }
        return null;
    }

    public boolean updateUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET prenom = ?, nom = ?, email = ?, age = ?, localite = ?, photoProfil = ?, idZone = ? WHERE idUser = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getPrenom());
            pstmt.setString(2, utilisateur.getNom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setInt(4, utilisateur.getAge());
            pstmt.setString(5, utilisateur.getLocalite());
            pstmt.setString(6, utilisateur.getPhotoProfil());
            pstmt.setInt(7, utilisateur.getIdZone());
            pstmt.setInt(8, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateUtilisateurAdmin(Utilisateur utilisateur) {
        String hashedPassword = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
        String query = "UPDATE Utilisateur SET prenom = ?, nom = ?, email = ?, motDePasse = ?, role = ?, age = ?, localite = ?, photoProfil = ?, idZone = ? " +
                "WHERE idUser = ? AND actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, utilisateur.getPrenom());
            pstmt.setString(2, utilisateur.getNom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, utilisateur.getRole());
            pstmt.setInt(6, utilisateur.getAge());
            pstmt.setString(7, utilisateur.getLocalite());
            pstmt.setString(8, utilisateur.getPhotoProfil());
            pstmt.setInt(9, utilisateur.getIdZone());
            pstmt.setInt(10, utilisateur.getIdUser());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteUtilisateur(int idUser) {
        String query = "UPDATE Utilisateur SET actif = 0 WHERE idUser = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public int countAllActifs() {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1";

        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUser(rs.getInt("idUser"));
        u.setPrenom(rs.getString("prenom"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("motDePasse"));
        u.setRole(rs.getString("role"));
        u.setAge(rs.getInt("age"));
        u.setLocalite(rs.getString("localite"));
        u.setPhotoProfil(rs.getString("photoProfil"));
        u.setIdZone(rs.getInt("idZone"));
        u.setActif(rs.getInt("actif") == 1);

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