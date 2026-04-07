package com.smartcity.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    @DisplayName("isValidEmail() accepte les emails valides")
    void testEmailValide() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("test.user+tag@domain.co"));
    }

    @Test
    @DisplayName("isValidEmail() rejette les emails invalides")
    void testEmailInvalide() {
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("pas-un-email"));
        assertFalse(ValidationUtils.isValidEmail("@domain.com"));
    }

    @Test
    @DisplayName("validatePassword() accepte un mot de passe valide")
    void testPasswordValide() {
        assertTrue(ValidationUtils.validatePassword("monMotDePasse").isValid());
    }

    @Test
    @DisplayName("validatePassword() rejette un mot de passe trop court")
    void testPasswordTropCourt() {
        assertFalse(ValidationUtils.validatePassword("abc").isValid());
    }

    @Test
    @DisplayName("validatePassword() rejette les mots de passe faibles")
    void testPasswordFaible() {
        assertFalse(ValidationUtils.validatePassword("123456").isValid());
        assertFalse(ValidationUtils.validatePassword("password").isValid());
    }

    @Test
    @DisplayName("isValidCoordinates() accepte des coordonnees valides")
    void testCoordonneesValides() {
        assertTrue(ValidationUtils.isValidCoordinates(14.7646, -17.3920));
    }

    @Test
    @DisplayName("isValidCoordinates() rejette (0,0) et hors bornes")
    void testCoordonneesInvalides() {
        assertFalse(ValidationUtils.isValidCoordinates(0.0, 0.0));
        assertFalse(ValidationUtils.isValidCoordinates(91.0, 0.0));
        assertFalse(ValidationUtils.isValidCoordinates(0.0, 181.0));
    }

    @Test
    @DisplayName("validateSignalement() valide un signalement correct")
    void testSignalementValide() {
        assertTrue(ValidationUtils.validateSignalement(
            "Depot de plastique", "Plastique", 14.7646, -17.3920).isValid());
    }

    @Test
    @DisplayName("validateSignalement() rejette description vide")
    void testSignalementDescriptionVide() {
        assertFalse(ValidationUtils.validateSignalement("", "Plastique", 14.7646, -17.3920).isValid());
    }

    @Test
    @DisplayName("validateSignalement() rejette coordonnees nulles")
    void testSignalementCoordonneesNulles() {
        assertFalse(ValidationUtils.validateSignalement("Description", "Plastique", 0.0, 0.0).isValid());
    }

    @Test
    @DisplayName("sanitizeInput() leve SecurityException sur injection SQL")
    void testSanitizeInjectionSQL() {
        assertThrows(SecurityException.class,
            () -> ValidationUtils.sanitizeInput("test; DROP TABLE Signalement"));
    }

    @Test
    @DisplayName("sanitizeInput() leve SecurityException sur XSS")
    void testSanitizeXSS() {
        assertThrows(SecurityException.class,
            () -> ValidationUtils.sanitizeInput("<script>alert('xss')</script>"));
    }

    @Test
    @DisplayName("sanitizeInput() accepte un texte normal")
    void testSanitizeNormal() {
        assertDoesNotThrow(() -> {
            String result = ValidationUtils.sanitizeInput("  Depot de dechets plastiques  ");
            assertEquals("Depot de dechets plastiques", result);
        });
    }
}
