package com.smartcity.app;

import com.smartcity.controller.AdminDashboardController;
import com.smartcity.controller.AgentDashboardController;
import com.smartcity.controller.CitizenDashboardController;
import com.smartcity.controller.ForgotPasswordController;
import com.smartcity.controller.LoginController;
import com.smartcity.controller.ReportsController;
import com.smartcity.controller.RegisterController;
import com.smartcity.service.GpsApiServer;
import com.smartcity.utils.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
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

    private CitizenDashboardController activeCitizenController;
    private AgentDashboardController activeAgentController;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SmartCity - Gestion des Dechets");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(650);

        startGpsServer();
        startWebSocketServer();
        primaryStage.setOnCloseRequest(e -> shutdown());

        if (!com.smartcity.utils.DatabaseConnection.testConnection()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Base de donnees inaccessible");
            alert.setContentText(
                "Impossible de se connecter a MySQL.\n\n"
                    + "Verifiez que :\n"
                    + "  - MySQL est demarre\n"
                    + "  - Les parametres dans config.properties sont corrects\n"
                    + "  - La base db_smartcity existe");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        showLoginScreen();
    }

    private void startGpsServer() {
        gpsApiServer = new GpsApiServer();
        try {
            gpsApiServer.start(0);
            logger.info("GPS API Server demarre sur le port {}", GpsApiServer.PORT);
        } catch (IOException e) {
            logger.warn("Impossible de demarrer le GPS API Server (port {} occupe ?): {}",
                GpsApiServer.PORT, e.getMessage());
        }
    }

    private void startWebSocketServer() {
        webSocketServer = new com.smartcity.websocket.WebSocketServer();
        webSocketServer.start();
    }

    private void shutdown() {
        if (gpsApiServer != null) {
            gpsApiServer.stop();
        }
        if (webSocketServer != null) {
            webSocketServer.stop();
        }
        SessionManager.shutdown();
        com.smartcity.utils.DatabaseConnection.closeDataSource();
    }

    public void showLoginScreen() {
        if (activeCitizenController != null) {
            activeCitizenController.cleanup();
            activeCitizenController = null;
        }
        if (activeAgentController != null) {
            activeAgentController.cleanup();
            activeAgentController = null;
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
            showAlert("Erreur critique", "Impossible d'afficher l'ecran de connexion.");
        }
    }

    public void showForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgot_password.fxml"));
            Parent root = loader.load();
            ForgotPasswordController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de forgot_password.fxml", e);
            showAlert("Erreur", "Impossible d'afficher l'écran de réinitialisation.");
        }
    }

    private void showAlert(String title, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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
            showAlert("Erreur", "Impossible d'afficher l'ecran d'inscription.");
        }
    }

    public void showReportsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reports_dashboard.fxml"));
            Parent root = loader.load();
            ReportsController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(new Scene(root, 1100, 680));
            primaryStage.setWidth(1100);
            primaryStage.setHeight(680);
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de reports_dashboard.fxml", e);
            showAlert("Erreur", "Impossible d'afficher l'ecran des rapports.");
        }
    }

    public void showDashboard(String role) {
        try {
            String normalizedRole = role == null ? "" : role.trim();
            String fxmlFile;
            switch (normalizedRole) {
                case "Administrateur":
                    fxmlFile = "/fxml/admin_dashboard.fxml";
                    break;
                case "Agent":
                    fxmlFile = "/fxml/agent_dashboard.fxml";
                    break;
                case "Citoyen":
                default:
                    fxmlFile = "/fxml/citizen_dashboard.fxml";
                    break;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Object controller = loader.getController();

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            if (controller instanceof AgentDashboardController) {
                AgentDashboardController agentController = (AgentDashboardController) controller;
                agentController.setMainApp(this);
                activeAgentController = agentController;
            } else if (controller instanceof AdminDashboardController) {
                AdminDashboardController adminController = (AdminDashboardController) controller;
                adminController.setMainApp(this);
                // chargerDonnees() est déjà appelé dans initialize() via Platform.runLater
            } else if (controller instanceof CitizenDashboardController) {
                CitizenDashboardController citizenController = (CitizenDashboardController) controller;
                citizenController.setMainApp(this);
                activeCitizenController = citizenController;
                // chargerDonnees() est déjà appelé dans initialize()
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du dashboard pour le role {}", role, e);
            showAlert(
                "Erreur de navigation",
                "La connexion a reussi, mais le dashboard n'a pas pu s'ouvrir.\n"
                    + "Verifiez le role utilisateur et les fichiers FXML."
            );
            showLoginScreen();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
