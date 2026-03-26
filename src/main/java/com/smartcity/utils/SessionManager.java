package com.smartcity.utils;

import com.smartcity.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gestionnaire de session sécurisé avec expiration automatique
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // Session actuelle (pour l'application desktop)
    private static SessionData currentSession;
    private static boolean darkMode = false;
    
    // Configuration de sécurité
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int MAX_INACTIVE_MINUTES = 10;
    
    // Service de nettoyage automatique
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Sessions multiples (pour évolutions futures)
    private static final java.util.concurrent.ConcurrentHashMap<String, SessionData> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();
    
    static {
        // Nettoyage automatique des sessions expirées toutes les 5 minutes
        scheduler.scheduleAtFixedRate(SessionManager::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Classe interne pour stocker les données de session
     */
    private static class SessionData {
        private final String sessionId;
        private final Utilisateur utilisateur;
        private final LocalDateTime loginTime;
        private LocalDateTime lastActivity;
        private boolean isActive;
        
        public SessionData(Utilisateur utilisateur) {
            this.sessionId = UUID.randomUUID().toString();
            this.utilisateur = utilisateur;
            this.loginTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.isActive = true;
        }
        
        public void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }
        
        public boolean isExpired() {
            LocalDateTime now = LocalDateTime.now();
            long minutesSinceLogin = ChronoUnit.MINUTES.between(loginTime, now);
            long minutesSinceActivity = ChronoUnit.MINUTES.between(lastActivity, now);
            
            return minutesSinceLogin > SESSION_TIMEOUT_MINUTES || 
                   minutesSinceActivity > MAX_INACTIVE_MINUTES;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public Utilisateur getUtilisateur() { return utilisateur; }
        public LocalDateTime getLoginTime() { return loginTime; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public boolean isActive() { return isActive; }
        
        public void setActive(boolean active) { this.isActive = active; }
    }

        /**
     * Définit l'utilisateur connecté avec session sécurisée
     */
    public static String setUtilisateurConnecte(Utilisateur utilisateur) {
        if (utilisateur == null) {
            logger.warn("⚠️ Tentative de connexion avec utilisateur null");
            return null;
        }
        
        // Créer nouvelle session
        SessionData newSession = new SessionData(utilisateur);
        currentSession = newSession;
        
        // Ajouter aux sessions actives pour monitoring
        activeSessions.put(newSession.getSessionId(), newSession);
        
        logger.info("🔐 Nouvelle session créée pour: {} (ID: {})", 
            maskEmail(utilisateur.getEmail()), newSession.getSessionId().substring(0, 8));
        
        return newSession.getSessionId();
    }

        /**
     * Retourne l'utilisateur connecté avec vérification d'expiration
     */
    public static Utilisateur getUtilisateurConnecte() {
        if (currentSession == null) {
            return null;
        }
        
        // Vérifier expiration
        if (currentSession.isExpired()) {
            logger.warn("⏰ Session expirée pour: {}", maskEmail(currentSession.getUtilisateur().getEmail()));
            logout();
            return null;
        }
        
        // Mettre à jour activité
        currentSession.updateActivity();
        return currentSession.getUtilisateur();
    }

        /**
     * Vérifie si un utilisateur est connecté avec session valide
     */
    public static boolean isLoggedIn() {
        return currentSession != null && 
               currentSession.isActive() && 
               !currentSession.isExpired();
    }

        /**
     * Vérifie si l'utilisateur est admin
     */
    public static boolean isAdmin() {
        Utilisateur user = getUtilisateurConnecte();
        return user != null && "Administrateur".equals(user.getRole());
    }

    /**
     * Vérifie si l'utilisateur est agent
     */
    public static boolean isAgent() {
        Utilisateur user = getUtilisateurConnecte();
        return user != null && "Agent".equals(user.getRole());
    }

    /**
     * Vérifie si l'utilisateur est citoyen
     */
    public static boolean isCitoyen() {
        Utilisateur user = getUtilisateurConnecte();
        return user != null && "Citoyen".equals(user.getRole());
    }

        /**
     * Déconnexion sécurisée
     */
    public static void logout() {
        if (currentSession != null) {
            logger.info("👋 Déconnexion utilisateur: {} (session: {}min)", 
                maskEmail(currentSession.getUtilisateur().getEmail()),
                ChronoUnit.MINUTES.between(currentSession.getLoginTime(), LocalDateTime.now()));
            
            // Invalider session
            currentSession.setActive(false);
            activeSessions.remove(currentSession.getSessionId());
            currentSession = null;
        }
    }
    
    /**
     * Prolonger la session (activité utilisateur)
     */
    public static void extendSession() {
        if (currentSession != null && !currentSession.isExpired()) {
            currentSession.updateActivity();
            logger.debug("🔄 Session prolongée pour: {}", maskEmail(currentSession.getUtilisateur().getEmail()));
        }
    }
    
    /**
     * Obtenir les informations de session
     */
    public static String getSessionInfo() {
        if (currentSession == null) {
            return "Aucune session active";
        }
        
        long sessionMinutes = ChronoUnit.MINUTES.between(currentSession.getLoginTime(), LocalDateTime.now());
        long inactiveMinutes = ChronoUnit.MINUTES.between(currentSession.getLastActivity(), LocalDateTime.now());
        
        return String.format("Session: %s | Durée: %dmin | Inactivité: %dmin", 
            currentSession.getSessionId().substring(0, 8), sessionMinutes, inactiveMinutes);
    }
    
    /**
     * Nettoie les sessions expirées
     */
    private static void cleanupExpiredSessions() {
        boolean cleaned = activeSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (cleaned) {
            logger.info("🧹 Sessions expirées nettoyées");
        }
    }
    
    /**
     * Masque l'email pour les logs (RGPD)
     */
    private static String maskEmail(String email) {
        if (email == null || email.length() < 3) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return "***";
        return email.substring(0, Math.min(2, atIndex)) + "***" + email.substring(atIndex);
    }

    // Gestion du thème
    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        logger.debug("🎨 Thème changé: {}", enabled ? "Sombre" : "Clair");
    }

    public static void toggleDarkMode() {
        setDarkMode(!darkMode);
    }
    
    /**
     * Arrêt propre du gestionnaire de session
     */
    public static void shutdown() {
        logger.info("🔒 Arrêt du gestionnaire de session...");
        scheduler.shutdown();
        activeSessions.clear();
        currentSession = null;
    }
}
