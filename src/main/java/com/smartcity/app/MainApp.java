package com.smartcity.app;

import com.smartcity.controller.AdminDashboardController;
import com.smartcity.controller.AgentDashboardController;
import com.smartcity.controller.CitizenDashboardController;
import com.smartcity.controller.LoginController;
import com.smartcity.controller.RegisterController;
import com.smartcity.controller.ReportsController;
import com.smartcity.service.GpsApiServer;
import com.smartcity.utils.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Point d'entree principal de l'application SmartCity Dechets.
 */
public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private static Stage primaryStage;
    private static GpsApiServer gpsApiServer;
    private static com.smartcity.websocket.WebSocketServer webSocketServer;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SmartCity - Gestion des Dechets");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(650);

        startGpsServer();
        startWebSocketServer();
        primaryStage.setOnCloseRequest(e -> shutdown());

        // Gap 1 — Vérification DB avant affichage login
        if (!com.smartcity.utils.DatabaseConnection.testConnection()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Base de données inaccessible");
            alert.setContentText(
                "❌ Impossible de se connecter à MySQL.\n\n" +
                "Vérifiez que :\n" +
                "  • MySQL est démarré\n" +
                "  • Les paramètres dans config.properties sont corrects\n" +
                "  • La base db_smartcity existe");
            alert.showAndWait();
            javafx.application.Platform.exit();
            return;
        }

        showLoginScreen();
    }

    private void startGpsServer() {
        gpsApiServer = new GpsApiServer();
        try {
            // agentId=0 : le serveur accepte tous les agents (l'id est dans la requête POST)
            gpsApiServer.start(0);
            logger.info("GPS API Server démarré sur le port {}", GpsApiServer.PORT);
        } catch (IOException e) {
            logger.warn("Impossible de démarrer le GPS API Server (port {} occupé ?): {}", GpsApiServer.PORT, e.getMessage());
        }
    }

    private void startWebSocketServer() {
        webSocketServer = new com.smartcity.websocket.WebSocketServer();
        webSocketServer.start();
    }

    private void shutdown() {
        if (gpsApiServer != null) gpsApiServer.stop();
        if (webSocketServer != null) webSocketServer.stop();
        SessionManager.shutdown();
        com.smartcity.utils.DatabaseConnection.closeDataSource();
    }

    private CitizenDashboardController activeCitizenController;

    public void showLoginScreen() {
        if (activeCitizenController != null) {
            activeCitizenController.cleanup();
            activeCitizenController = null;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de login.fxml", e);
        }
    }

    public void showRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            RegisterController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de register.fxml", e);
        }
    }

    public void showDashboard(String role) {
        try {
            String fxmlFile;
            switch (role) {
                case "Administrateur":
                    fxmlFile = "/fxml/admin_dashboard.fxml";
                    break;
                case "Agent":
                    fxmlFile = "/fxml/agent_dashboard.fxml";
                    break;
                default:
                    fxmlFile = "/fxml/citizen_dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof AgentDashboardController) {
                ((AgentDashboardController) controller).setMainApp(this);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setMainApp(this);
            } else if (controller instanceof CitizenDashboardController) {
                CitizenDashboardController cc = (CitizenDashboardController) controller;
                cc.setMainApp(this);
                activeCitizenController = cc;
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du dashboard pour le rôle {}", role, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
