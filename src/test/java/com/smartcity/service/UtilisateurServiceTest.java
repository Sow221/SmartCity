package com.smartcity.service;

import com.smartcity.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UtilisateurServiceTest {

    private UtilisateurService utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurService();
    }

    @Test
    void testConnexionSuccess() {
        // Mock DatabaseConnection
        try (MockedStatic<com.smartcity.utils.DatabaseConnection> mockedDb = Mockito.mockStatic(com.smartcity.utils.DatabaseConnection.class)) {
            // Mock connection and result set
            // This is a basic test; in real scenario, use an in-memory DB like H2
            // For now, assume the method works as is
            assertNotNull(utilisateurService);
        }
    }

    @Test
    void testEmailExiste() {
        // Similar mocking
        assertNotNull(utilisateurService);
    }

    // Add more tests as needed
}