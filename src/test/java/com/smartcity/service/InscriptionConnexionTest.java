package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InscriptionConnexionTest {

    private static final UtilisateurService service = new UtilisateurService();
    private static final String TEST_EMAIL = "test_inscription_" + System.currentTimeMillis() + "@smartcity.sn";
    private static final String TEST_PASSWORD = "test123";

    @Test
    @Order(1)
    void testInscription_NomCompletDansNom() {
        Utilisateur u = new Utilisateur("", "Mamadou Diop", TEST_EMAIL, TEST_PASSWORD, "Citoyen", 1);
        boolean result = service.inscription(u);
        assertTrue(result, "L'inscription doit reussir");
    }

    @Test
    @Order(2)
    void testInscription_EmailDuplique() {
        Utilisateur u = new Utilisateur("", "Autre Nom", TEST_EMAIL, TEST_PASSWORD, "Citoyen", 1);
        boolean result = service.inscription(u);
        assertFalse(result, "L'inscription avec email duplique doit echouer");
    }

    @Test
    @Order(3)
    void testConnexion_ApresInscription() {
        Utilisateur u = service.connexion(TEST_EMAIL, TEST_PASSWORD);
        assertNotNull(u, "La connexion apres inscription doit reussir");
        assertEquals("Citoyen", u.getRole());
        assertEquals("Mamadou Diop", u.getNom());
    }

    @Test
    @Order(4)
    void testConnexion_MauvaisMotDePasse() {
        Utilisateur u = service.connexion(TEST_EMAIL, "mauvais");
        assertNull(u, "La connexion avec mauvais mot de passe doit echouer");
    }

    @Test
    @Order(5)
    void testInscription_MotDePasseTropCourt() {
        Utilisateur u = new Utilisateur("", "Test Court", "court@test.sn", "123", "Citoyen", 1);
        boolean result = service.inscription(u);
        assertFalse(result, "Mot de passe < 6 chars doit etre rejete");
    }

    @Test
    @Order(6)
    void testEmailExiste() {
        assertTrue(service.emailExiste(TEST_EMAIL), "emailExiste doit retourner true apres inscription");
        assertFalse(service.emailExiste("inexistant@test.sn"), "emailExiste doit retourner false pour email inconnu");
    }
}
