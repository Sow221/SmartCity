package com.smartcity.service;

import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.sql.DataSource;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AffectationServiceTest {

    @Mock DataSource dataSource;
    @Mock Connection connection;
    @Mock PreparedStatement preparedStatement;
    @Mock ResultSet resultSet;

    AffectationService service;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        service = new AffectationService(dataSource);
    }

    // --- creerAffectation ---

    @Test
    void creerAffectation_retourneVrai_siInsertionReussie() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(service.creerAffectation(1, 2));
    }

    @Test
    void creerAffectation_retourneFaux_siAucuneLigneInseree() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(service.creerAffectation(1, 2));
    }

    @Test
    void creerAffectation_retourneFaux_siSQLException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Erreur DB"));
        assertFalse(service.creerAffectation(1, 2));
    }

    // --- marquerCollecte ---

    @Test
    void marquerCollecte_retourneVrai_siMiseAJourReussie() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(service.marquerCollecte(10, "Collecte effectuee"));
    }

    @Test
    void marquerCollecte_retourneFaux_siAffectationInexistante() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(service.marquerCollecte(999, "commentaire"));
    }

    // --- estDejaAffecte ---

    @Test
    void estDejaAffecte_retourneVrai_siAffectationExiste() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("idAffectation")).thenReturn(1);
        when(resultSet.getInt("idSignalement")).thenReturn(5);
        when(resultSet.getInt("idAgent")).thenReturn(3);
        when(resultSet.getTimestamp("dateAffectation")).thenReturn(null);
        assertTrue(service.estDejaAffecte(5));
    }

    @Test
    void estDejaAffecte_retourneFaux_siAucuneAffectation() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        assertFalse(service.estDejaAffecte(99));
    }

    // --- supprimerAffectation ---

    @Test
    void supprimerAffectation_retourneVrai_siSuppressionReussie() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(service.supprimerAffectation(1));
    }

    @Test
    void supprimerAffectation_retourneFaux_siIdInexistant() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(service.supprimerAffectation(999));
    }

    // --- sauvegarderCommentaire ---

    @Test
    void sauvegarderCommentaire_retourneVrai_siMiseAJourOK() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(service.sauvegarderCommentaire(1, "Bon travail"));
    }

    // --- getCommentaireBySignalement ---

    @Test
    void getCommentaireBySignalement_retourneCommentaire_siExiste() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("commentaire")).thenReturn("Zone nettoyee");
        assertEquals("Zone nettoyee", service.getCommentaireBySignalement(1));
    }

    @Test
    void getCommentaireBySignalement_retourneNull_siAucunResultat() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        assertNull(service.getCommentaireBySignalement(99));
    }
}
