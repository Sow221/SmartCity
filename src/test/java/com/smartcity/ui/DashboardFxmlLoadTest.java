package com.smartcity.ui;

import com.smartcity.model.Utilisateur;
import com.smartcity.utils.SessionManager;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;

class DashboardFxmlLoadTest {

    @BeforeAll
    static void initFx() {
        new JFXPanel();
    }

    @AfterEach
    void clearSession() {
        SessionManager.logout();
    }

    @Test
    void adminDashboardLoads() throws Exception {
        Utilisateur admin = new Utilisateur();
        admin.setIdUser(1);
        admin.setNom("Admin");
        admin.setEmail("admin@smartcity.sn");
        admin.setRole("Administrateur");
        SessionManager.setUtilisateurConnecte(admin);

        Exception error = loadOnFxThread("/fxml/admin_dashboard.fxml");
        assertNull(error, "admin_dashboard.fxml failed to load: " + format(error));
    }

    @Test
    void agentDashboardLoads() throws Exception {
        Utilisateur agent = new Utilisateur();
        agent.setIdUser(2);
        agent.setNom("Agent");
        agent.setEmail("agent@smartcity.sn");
        agent.setRole("Agent");
        SessionManager.setUtilisateurConnecte(agent);

        Exception error = loadOnFxThread("/fxml/agent_dashboard.fxml");
        assertNull(error, "agent_dashboard.fxml failed to load: " + format(error));
    }

    @Test
    void citizenDashboardLoads() throws Exception {
        Utilisateur citoyen = new Utilisateur();
        citoyen.setIdUser(3);
        citoyen.setNom("Citoyen");
        citoyen.setEmail("citoyen@smartcity.sn");
        citoyen.setRole("Citoyen");
        SessionManager.setUtilisateurConnecte(citoyen);

        Exception error = loadOnFxThread("/fxml/citizen_dashboard.fxml");
        assertNull(error, "citizen_dashboard.fxml failed to load: " + format(error));
    }

    private Exception loadOnFxThread(String fxmlPath) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        javafx.application.Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                loader.load();
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(20, TimeUnit.SECONDS);
        if (!completed) {
            return new RuntimeException("Timeout while loading " + fxmlPath);
        }
        return errorRef.get();
    }

    private String format(Exception e) {
        if (e == null) {
            return "";
        }
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : " - " + message);
    }
}
