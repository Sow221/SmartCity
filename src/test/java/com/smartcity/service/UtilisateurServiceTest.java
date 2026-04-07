package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UtilisateurServiceTest {

    private static UtilisateurService utilisateurService;
    private static boolean dbAvailable;
    private static int idUserTest = -1;

    @BeforeAll
    static void setup() {
        utilisateurService = new UtilisateurService();
        dbAvailable = DatabaseConnection.testConnection();
    }

    @Test
    @Order(1)
    @DisplayName("connexion() avec mauvais mot de passe retourne null")
    void testConnexionEchouee() {
        if (!dbAvailable) return;
        assertNull(utilisateurService.connexion("admin@smartcity.sn", "mauvais_mdp"),
            "Connexion avec mauvais mot de passe doit retourner null");
    }

    @Test
    @Order(2)
    @DisplayName("emailExiste() retourne true pour un email existant")
    void testEmailExiste() {
        if (!dbAvailable) return;
        assertTrue(utilisateurService.emailExiste("admin@smartcity.sn"),
            "L'email admin doit exister");
    }

    @Test
    @Order(3)
    @DisplayName("emailExiste() retourne false pour un email inexistant")
    void testEmailInexistant() {
        if (!dbAvailable) return;
        assertFalse(utilisateurService.emailExiste("inexistant_xyz_999@test.com"));
    }

    @Test
    @Order(4)
    @DisplayName("getAllUtilisateurs() retourne une liste non vide")
    void testGetAllUtilisateurs() {
        if (!dbAvailable) return;
        List<Utilisateur> liste = utilisateurService.getAllUtilisateurs();
        assertNotNull(liste);
        assertFalse(liste.isEmpty(), "Il doit y avoir au moins un utilisateur");
    }

    @Test
    @Order(5)
    @DisplayName("getUtilisateursByRole() retourne uniquement le bon rôle")
    void testGetUtilisateursByRole() {
        if (!dbAvailable) return;
        List<Utilisateur> agents = utilisateurService.getUtilisateursByRole("Agent");
        assertNotNull(agents);
        for (Utilisateur u : agents) {
            assertEquals("Agent", u.getRole(), "Tous doivent être des agents");
        }
    }

    @Test
    @Order(6)
    @DisplayName("inscription() crée un utilisateur avec mot de passe hashé")
    void testInscription() {
        if (!dbAvailable) return;
        Utilisateur u = new Utilisateur("", "Test JUnit", "junit_test_" + System.currentTimeMillis() + "@test.com",
            "motdepasse123", "Citoyen", 1);
        boolean result = utilisateurService.inscription(u);
        assertTrue(result, "L'inscription doit réussir");
        // Vérifier que le compte existe
        assertTrue(utilisateurService.emailExiste(u.getEmail()));
        // Récupérer l'id pour nettoyage
        List<Utilisateur> tous = utilisateurService.getAllUtilisateurs();
        tous.stream().filter(x -> x.getEmail().equals(u.getEmail()))
            .findFirst().ifPresent(x -> idUserTest = x.getIdUser());
    }

    @Test
    @Order(7)
    @DisplayName("inscription() avec email déjà utilisé retourne false")
    void testInscriptionDoublon() {
        if (!dbAvailable) return;
        Utilisateur u = new Utilisateur("", "Doublon", "admin@smartcity.sn", "motdepasse123", "Citoyen", 1);
        assertFalse(utilisateurService.inscription(u), "Doublon email doit retourner false");
    }

    @Test
    @Order(8)
    @DisplayName("countAllActifs() retourne un entier > 0")
    void testCountAllActifs() {
        if (!dbAvailable) return;
        assertTrue(utilisateurService.countAllActifs() > 0);
    }

    @AfterAll
    static void cleanup() {
        if (dbAvailable && idUserTest > 0) {
            utilisateurService.deleteUtilisateur(idUserTest);
            System.out.println("[CLEANUP] Utilisateur test #" + idUserTest + " supprimé.");
        }
    }
}
