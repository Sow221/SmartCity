package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.utils.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration sur SignalementService et AffectationService.
 * Nécessite une connexion DB active (db_smartcity).
 * Skippés automatiquement si la DB est inaccessible.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignalementServiceTest {

    private static SignalementService signalementService;
    private static AffectationService affectationService;
    private static boolean dbAvailable;
    private static int idSignalementTest = -1;

    @BeforeAll
    static void setup() {
        signalementService = new SignalementService();
        affectationService = new AffectationService();
        dbAvailable = DatabaseConnection.testConnection();
        if (!dbAvailable) {
            System.out.println("[SKIP] Base de données inaccessible — tests ignorés.");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static boolean isDbAvailable() {
        return dbAvailable;
    }

    // ── Tests SignalementService ──────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("countAll() retourne un entier >= 0")
    void testCountAll() {
        if (!dbAvailable) return;
        int count = signalementService.countAll();
        assertTrue(count >= 0, "countAll doit retourner >= 0");
    }

    @Test
    @Order(2)
    @DisplayName("countByStatut() retourne un entier >= 0 pour chaque statut")
    void testCountByStatut() {
        if (!dbAvailable) return;
        for (String statut : new String[]{"En attente", "Affecté", "En cours", "Terminé"}) {
            int count = signalementService.countByStatut(statut);
            assertTrue(count >= 0, "countByStatut(" + statut + ") doit retourner >= 0");
        }
    }

    @Test
    @Order(3)
    @DisplayName("getAllSignalements() retourne une liste non nulle")
    void testGetAllSignalements() {
        if (!dbAvailable) return;
        List<Signalement> liste = signalementService.getAllSignalements();
        assertNotNull(liste, "getAllSignalements ne doit pas retourner null");
    }

    @Test
    @Order(4)
    @DisplayName("getAllSignalements() : chaque signalement a un id > 0 et un statut non null")
    void testSignalementsValides() {
        if (!dbAvailable) return;
        List<Signalement> liste = signalementService.getAllSignalements();
        for (Signalement s : liste) {
            assertTrue(s.getIdSignalement() > 0, "idSignalement doit être > 0");
            assertNotNull(s.getStatut(), "statut ne doit pas être null");
            assertNotNull(s.getZoneNom(), "zoneNom ne doit pas être null");
        }
    }

    @Test
    @Order(5)
    @DisplayName("ajouterSignalement() crée un signalement et retourne true")
    void testAjouterSignalement() {
        if (!dbAvailable) return;
        Signalement s = new Signalement();
        s.setDescription("Test unitaire automatique");
        s.setCategorie("Plastique");
        s.setIdZone(1);
        s.setLatitude(14.7646);
        s.setLongitude(-17.3920);
        s.setIdUser(1); // admin par défaut
        boolean result = signalementService.ajouterSignalement(s);
        assertTrue(result, "ajouterSignalement doit retourner true");
        assertTrue(s.getIdSignalement() > 0, "idSignalement doit être renseigné après ajout");
        idSignalementTest = s.getIdSignalement();
    }

    @Test
    @Order(6)
    @DisplayName("updateStatut() change le statut correctement")
    void testUpdateStatut() {
        if (!dbAvailable || idSignalementTest < 0) return;
        boolean result = signalementService.updateStatut(idSignalementTest, "En cours");
        assertTrue(result, "updateStatut doit retourner true");
        // Vérifier en relisant
        List<Signalement> liste = signalementService.getAllSignalements();
        Signalement trouve = liste.stream()
            .filter(s -> s.getIdSignalement() == idSignalementTest)
            .findFirst().orElse(null);
        assertNotNull(trouve, "Le signalement doit exister après updateStatut");
        assertEquals("En cours", trouve.getStatut(), "Le statut doit être 'En cours'");
    }

    @Test
    @Order(7)
    @DisplayName("countByStatutAndZone() cohérent avec countByZone()")
    void testCountByStatutAndZone() {
        if (!dbAvailable) return;
        int total = signalementService.countByZone("Pikine");
        int attente = signalementService.countByStatutAndZone("En attente", "Pikine");
        int affecte = signalementService.countByStatutAndZone("Affecté", "Pikine");
        int enCours = signalementService.countByStatutAndZone("En cours", "Pikine");
        int termine = signalementService.countByStatutAndZone("Terminé", "Pikine");
        assertEquals(total, attente + affecte + enCours + termine,
            "La somme des statuts doit égaler le total pour Pikine");
    }

    @Test
    @Order(8)
    @DisplayName("getSignalementsByUtilisateur() ne retourne que les signalements du bon user")
    void testGetSignalementsByUtilisateur() {
        if (!dbAvailable) return;
        List<Signalement> liste = signalementService.getSignalementsByUtilisateur(1);
        assertNotNull(liste);
        for (Signalement s : liste) {
            assertEquals(1, s.getIdUser(), "Tous les signalements doivent appartenir à l'user 1");
        }
    }

    // ── Tests AffectationService ──────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("getAffectationBySignalement() retourne null pour un id inexistant")
    void testGetAffectationInexistante() {
        if (!dbAvailable) return;
        assertNull(affectationService.getAffectationBySignalement(999999),
            "Doit retourner null pour un signalement inexistant");
    }

    @Test
    @Order(10)
    @DisplayName("estDejaAffecte() retourne false pour un signalement inexistant")
    void testEstDejaAffecteInexistant() {
        if (!dbAvailable) return;
        assertFalse(affectationService.estDejaAffecte(999999));
    }

    // ── Nettoyage ─────────────────────────────────────────────────────────────

    @AfterAll
    static void cleanup() {
        if (dbAvailable && idSignalementTest > 0) {
            signalementService.supprimerSignalement(idSignalementTest);
            System.out.println("[CLEANUP] Signalement test #" + idSignalementTest + " supprimé.");
        }
    }
}
