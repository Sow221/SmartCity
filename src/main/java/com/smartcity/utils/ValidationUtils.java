package com.smartcity.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Utilitaires de validation et de sécurisation des données
 */
public class ValidationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    
    // Expressions régulières sécurisées
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Patterns ciblés : balises HTML dangereuses et SQL structurel uniquement
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(;\\s*(drop|alter|truncate|create)\\s|--\\s|/\\*.*\\*/)"
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<script|javascript:|on\\w+\\s*=", 
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Valide et nettoie un email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String cleanEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        boolean valid = EMAIL_PATTERN.matcher(cleanEmail).matches() && cleanEmail.length() <= 254;
        
        if (!valid) {
            logger.warn("⚠️ Email invalide détecté: {}", maskEmail(email));
        }
        
        return valid;
    }
    
    /**
     * Valide la force d'un mot de passe
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null) {
            return new ValidationResult(false, "Mot de passe requis");
        }
        
        if (password.length() < 6) {
            return new ValidationResult(false, "Mot de passe trop court (minimum 6 caractères)");
        }
        
        if (password.length() > 128) {
            return new ValidationResult(false, "Mot de passe trop long (maximum 128 caractères)");
        }
        
        // Vérifier mots de passe faibles
        String lowerPassword = password.toLowerCase(java.util.Locale.ROOT);
        String[] weakPasswords = {"123456", "password", "admin", "qwerty", "abc123"};
        
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return new ValidationResult(false, "Mot de passe trop faible");
            }
        }
        
        return new ValidationResult(true, "Mot de passe valide");
    }
    
    /**
     * Nettoie et valide une chaîne contre l'injection SQL
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        String cleaned = input.trim();
        
        // Détecter tentatives d'injection SQL
        if (SQL_INJECTION_PATTERN.matcher(cleaned).find()) {
            logger.warn("🚨 Tentative d'injection SQL détectée: {}", cleaned.substring(0, Math.min(50, cleaned.length())));
            throw new SecurityException("Contenu suspect détecté");
        }
        
        // Détecter tentatives XSS
        if (XSS_PATTERN.matcher(cleaned).find()) {
            logger.warn("🚨 Tentative XSS détectée: {}", cleaned.substring(0, Math.min(50, cleaned.length())));
            throw new SecurityException("Contenu suspect détecté");
        }
        
        return cleaned;
    }
    
    /**
     * Valide des coordonnées GPS
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        boolean valid = latitude >= -90 && latitude <= 90 && 
                       longitude >= -180 && longitude <= 180 &&
                       latitude != 0.0 && longitude != 0.0; // Éviter (0,0)
        
        if (!valid) {
            logger.warn("⚠️ Coordonnées GPS invalides: lat={}, lon={}", latitude, longitude);
        }
        
        return valid;
    }
    
    /**
     * Valide un nom/prénom
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String cleaned = name.trim();
        
        // Longueur raisonnable
        if (cleaned.length() < 2 || cleaned.length() > 50) {
            return false;
        }
        
        // Caractères autorisés : lettres, espaces, tirets, apostrophes
        Pattern namePattern = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$");
        return namePattern.matcher(cleaned).matches();
    }
    
    /**
     * Valide un âge
     */
    public static boolean isValidAge(int age) {
        return age >= 13 && age <= 120; // RGPD : minimum 13 ans
    }
    
    /**
     * Masque un email pour les logs (protection RGPD)
     */
    public static String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (local.length() <= 2) {
            return "***" + domain;
        }
        
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
    
    /**
     * Valide un ID numérique
     */
    public static boolean isValidId(Integer id) {
        return id != null && id > 0;
    }
    
    /**
     * Classe pour les résultats de validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Valide les données d'un signalement
     */
    public static ValidationResult validateSignalement(String description, String categorie, 
                                                     double latitude, double longitude) {
        if (description == null || description.trim().isEmpty()) {
            return new ValidationResult(false, "Description requise");
        }
        
        if (description.length() > 500) {
            return new ValidationResult(false, "Description trop longue (max 500 caractères)");
        }
        
        if (categorie == null || categorie.trim().isEmpty()) {
            return new ValidationResult(false, "Catégorie requise");
        }
        
        if (!isValidCoordinates(latitude, longitude)) {
            return new ValidationResult(false, "Coordonnées GPS invalides");
        }
        
        try {
            sanitizeInput(description);
            sanitizeInput(categorie);
        } catch (SecurityException e) {
            return new ValidationResult(false, "Contenu suspect détecté");
        }
        
        return new ValidationResult(true, "Signalement valide");
    }
}
