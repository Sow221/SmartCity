package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service pour la gestion des utilisateurs.
 * Les mots de passe sont maintenant hashés avec BCrypt.
 */
public class UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);

    // Protection brute-force : compteur de tentatives par email
    private static final ConcurrentHashMap<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lockoutUntil = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 15 * 60 * 1000L; // 15 minutes
    private static final java.util.prefs.Preferences prefs =
        java.util.prefs.Preferences.userNodeForPackage(UtilisateurService.class);

    private Connection getConn() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("❌ Impossible d'obtenir une connexion à la base de données");
        }
        return conn;
    }

        public boolean inscription(Utilisateur utilisateur) {
        // Validation des données
        if (utilisateur == null) {
            logger.error("Tentative d'inscription avec utilisateur null");
            return false;
        }
        
        if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
            logger.error("Email requis pour l'inscription");
            return false;
        }
        
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().length() < 6) {
            logger.error("Mot de passe trop faible pour: {}", utilisateur.getEmail());
            return false;
        }

        if (emailExiste(utilisateur.getEmail())) {
            logger.warn("Email déjà utilisé lors de l'inscription: {}", utilisateur.getEmail());
            return false;
        }
        
        String hashedPassword = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
        String query = "INSERT INTO Utilisateur (prenom, nom, email, motDePasse, role, age, localite, photoProfil, idZone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, utilisateur.getPrenom());
            pstmt.setString(2, utilisateur.getNom());
            pstmt.setString(3, utilisateur.getEmail().toLowerCase().trim());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, utilisateur.getRole());
            pstmt.setInt(6, utilisateur.getAge());
            pstmt.setString(7, utilisateur.getLocalite());
            pstmt.setString(8, utilisateur.getPhotoProfil());
            pstmt.setInt(9, utilisateur.getIdZone());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logger.info("✅ Inscription réussie pour: {}", utilisateur.getEmail());
                return true;
            } else {
                logger.warn("⚠️ Aucune ligne affectée lors de l'inscription");
                return false;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                logger.warn("Email déjà utilisé: {}", utilisateur.getEmail());
            } else {
                logger.error("❌ Erreur SQL lors de l'inscription pour {}: {}", utilisateur.getEmail(), e.getMessage());
            }
            return false;
        }
    }

        public Utilisateur connexion(String email, String motPasse) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Tentative de connexion sans email");
            return null;
        }
        
        if (motPasse == null || motPasse.isEmpty()) {
            logger.warn("Tentative de connexion sans mot de passe pour: {}", email);
            return null;
        }
        
        String key = email.toLowerCase().trim();

        // Vérifier verrouillage
        Long until = lockoutUntil.get(key);
        if (until == null) {
            long persistedUntil = prefs.getLong(lockKey(key), 0L);
            if (persistedUntil > 0L) {
                until = persistedUntil;
                lockoutUntil.put(key, persistedUntil);
            }
        }
        if (until != null && System.currentTimeMillis() < until) {
            long remaining = (until - System.currentTimeMillis()) / 1000 / 60;
            logger.warn("🔒 Compte verrouillé pour: {} — encore {}min", key, remaining + 1);
            return null;
        }

        String query = "SELECT * FROM Utilisateur WHERE email = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("motDePasse");
                    if (!storedPassword.startsWith("$2")) {
                        logger.warn("⚠️ Compte avec mot de passe non-BCrypt pour: {} — connexion refusée", key);
                        return null;
                    }
                    if (BCrypt.checkpw(motPasse, storedPassword)) {
                        loginAttempts.remove(key);
                        lockoutUntil.remove(key);
                        prefs.remove(lockKey(key));
                        logger.info("✅ Connexion réussie pour: {}", key);
                        return mapResultSetToUtilisateur(rs);
                    } else {
                        recordFailedAttempt(key);
                        logger.warn("❌ Mot de passe incorrect pour: {}", key);
                    }
                } else {
                    logger.warn("❌ Utilisateur non trouvé: {}", key);
                }
            }
        } catch (SQLException e) {
            logger.error("❌ Erreur SQL lors de la connexion pour {}: {}", key, e.getMessage());
        }
        return null;
    }

    private void recordFailedAttempt(String key) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int count = attempts.incrementAndGet();
        if (count >= MAX_ATTEMPTS) {
            long until = System.currentTimeMillis() + LOCKOUT_MS;
            lockoutUntil.put(key, until);
            prefs.putLong(lockKey(key), until);
            loginAttempts.remove(key);
            logger.warn("🔒 Compte verrouillé 15min après {} tentatives: {}", MAX_ATTEMPTS, key);
        }
    }

    /** Retourne le message d'erreur de verrouillage si applicable, null sinon. */
    public String getLoginBlockMessage(String email) {
        if (email == null) return null;
        String key = email.toLowerCase().trim();
        Long until = lockoutUntil.get(key);
        if (until == null) {
            long persistedUntil = prefs.getLong(lockKey(key), 0L);
            if (persistedUntil > 0L) {
                until = persistedUntil;
                lockoutUntil.put(key, persistedUntil);
            }
        }
        if (until != null && System.currentTimeMillis() < until) {
            long remaining = (until - System.currentTimeMillis()) / 1000 / 60;
            return "Compte temporairement bloqué. Réessayez dans " + (remaining + 1) + " minute(s).";
        }
        if (until != null && System.currentTimeMillis() >= until) {
            lockoutUntil.remove(key);
            prefs.remove(lockKey(key));
        }
        return null;
    }

    private static String lockKey(String emailKey) {
        return "lockout_" + emailKey.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public boolean emailExiste(String email) {
        if (email == null) return false;
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE email = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur WHERE actif = 1 ORDER BY nom";
        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) utilisateurs.add(mapResultSetToUtilisateur(rs));
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les utilisateurs", e);
        }
        return utilisateurs;
    }

    public List<Utilisateur> getUtilisateursByRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM Utilisateur WHERE role = ? AND actif = 1 ORDER BY nom";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des utilisateurs par rôle", e);
        }
        return utilisateurs;
    }

    public Utilisateur getUtilisateurByEmail(String email) {
        if (email == null) return null;
        String query = "SELECT * FROM Utilisateur WHERE email = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur getUtilisateurByEmail", e);
        }
        return null;
    }

    public Utilisateur getUtilisateurById(int idUser) {
        String query = "SELECT * FROM Utilisateur WHERE idUser = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de l'utilisateur par id", e);
        }
        return null;
    }

    public boolean updateUtilisateur(Utilisateur utilisateur) {
        String query = "UPDATE Utilisateur SET prenom = ?, nom = ?, email = ?, age = ?, localite = ?, photoProfil = ?, idZone = ? WHERE idUser = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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

    public boolean updateMotDePasse(int idUser, String nouveauMotDePasse) {
        String hashed = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt());
        String query = "UPDATE Utilisateur SET motDePasse = ? WHERE idUser = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hashed);
            pstmt.setInt(2, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur updateMotDePasse: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateUtilisateurAdmin(Utilisateur utilisateur) {
        // Ne re-hasher que si le mot de passe n'est pas déjà un hash BCrypt
        String motDePasse = utilisateur.getMotDePasse();
        if (motDePasse == null || motDePasse.isEmpty()) {
            logger.error("Mot de passe vide pour updateUtilisateurAdmin");
            return false;
        }
        String hashedPassword = motDePasse.startsWith("$2") ? motDePasse : BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        String query = "UPDATE Utilisateur SET prenom = ?, nom = ?, email = ?, motDePasse = ?, role = ?, age = ?, localite = ?, photoProfil = ?, idZone = ? "
                + "WHERE idUser = ? AND actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
            logger.error("Erreur updateUtilisateurAdmin: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteUtilisateur(int idUser) {
        // Libérer les affectations actives du citoyen avant désactivation
        String queryAffectations = "SELECT s.idSignalement FROM Signalement s "
            + "WHERE s.idUser = ? AND s.statut != ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(queryAffectations)) {
            ps.setInt(1, idUser);
            ps.setString(2, com.smartcity.model.SignalementStatut.TERMINE.dbValue());
            try (ResultSet rs = ps.executeQuery()) {
                AffectationService affService = new AffectationService();
                while (rs.next()) {
                    affService.supprimerAffectationParSignalement(rs.getInt("idSignalement"));
                }
            }
        } catch (SQLException e) {
            logger.warn("Erreur libération affectations pour user #{}: {}", idUser, e.getMessage());
        }
        String query = "UPDATE Utilisateur SET actif = 0 WHERE idUser = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur deleteUtilisateur: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retourne les agents avec leurs stats en une seule requête SQL (élimine le N+1).
     */
    public List<AgentStats> getAgentsWithStats() {
        List<AgentStats> result = new ArrayList<>();
        String query =
            "SELECT u.idUser, u.nom, z.nomZone, " +
            "COUNT(CASE WHEN s.statut = ? THEN 1 END) AS traites " +
            "FROM Utilisateur u " +
            "LEFT JOIN Zone z ON u.idZone = z.idZone " +
            "LEFT JOIN Affectation a ON a.idAgent = u.idUser " +
            "LEFT JOIN Signalement s ON s.idSignalement = a.idSignalement " +
            "WHERE u.role = 'Agent' AND u.actif = 1 " +
            "GROUP BY u.idUser, u.nom, z.nomZone ORDER BY u.nom";
        try (Connection conn = getConn();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, com.smartcity.model.SignalementStatut.TERMINE.dbValue());
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new AgentStats(
                        rs.getInt("idUser"),
                        rs.getString("nom"),
                        rs.getString("nomZone") != null ? rs.getString("nomZone") : "Zone inconnue",
                        rs.getInt("traites")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur getAgentsWithStats: {}", e.getMessage());
        }
        return result;
    }

    /** DTO léger pour les stats agent — évite de charger l'objet Utilisateur complet. */
    public record AgentStats(int idUser, String nom, String zoneNom, int traites) {}

    public int countAllActifs() {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query);
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
