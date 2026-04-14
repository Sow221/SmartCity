package com.smartcity.app;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcity.controller.AdminDashboardController;
import com.smartcity.controller.AgentDashboardController;
import com.smartcity.controller.CitizenDashboardController;
import com.smartcity.controller.ForgotPasswordController;
import com.smartcity.controller.LoginController;
import com.smartcity.controller.RegisterController;
import com.smartcity.controller.ReportsController;
import com.smartcity.service.GpsApiServer;
import com.smartcity.utils.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application SmartCity Déchets.
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private static Stage primaryStage;
    private static GpsApiServer gpsApiServer;
    private static com.smartcity.websocket.WebSocketServer webSocketServer;

    private CitizenDashboardController activeCitizenController;
    private AgentDashboardController   activeAgentController;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SmartCity - Gestion des Déchets");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(650);

        startGpsServer();
        startWebSocketServer();
        primaryStage.setOnCloseRequest(e -> shutdown());

        if (!com.smartcity.utils.DatabaseConnection.testConnection()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Base de données inaccessible");
            alert.setContentText(
                    "Impossible de se connecter à MySQL.\n\n"
                    + "Vérifiez que :\n"
                    + "  - MySQL est démarré\n"
                    + "  - Les paramètres dans config.properties sont corrects\n"
                    + "  - La base db_smartcity existe");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        showLoginScreen();
    }

    private void startGpsServer() {
        // ✅ Vérifier que les ports sont libres avant démarrage
        if (!isPortAvailable(GpsApiServer.PORT) || !isPortAvailable(GpsApiServer.HTTPS_PORT)) {
            logger.error("❌ Ports {} ou {} déjà occupés. Un autre processus les utilise.", 
                GpsApiServer.PORT, GpsApiServer.HTTPS_PORT);
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        gpsApiServer = new GpsApiServer();
        try {
            gpsApiServer.start(0); // defaultAgentId=0 : tokens crees a la demande par chaque agent
            logger.info("✅ GPS API Server démarré sur les ports {} et {}", GpsApiServer.PORT, GpsApiServer.HTTPS_PORT);
        } catch (IOException e) {
            logger.error("❌ Impossible de démarrer le GPS API Server (ports {} / {} occupés ?): {}",
                    GpsApiServer.PORT, GpsApiServer.HTTPS_PORT, e.getMessage());
        }
    }

    private void startWebSocketServer() {
        // ✅ Vérifier que le port est libre avant démarrage
        int wsPort = com.smartcity.config.GeoConfig.getWebSocketPort();
        if (!isPortAvailable(wsPort)) {
            logger.error("❌ Port {} (WebSocket) déjà occupé.", wsPort);
            return;
        }
        
        webSocketServer = new com.smartcity.websocket.WebSocketServer();
        try {
            webSocketServer.start();
            logger.info("✅ WebSocket Server démarré sur le port {}", wsPort);
        } catch (Exception e) {
            logger.error("❌ Impossible de démarrer le WebSocket Server (port {} occupé ?): {}", wsPort, e.getMessage());
        }
    }
    
    /** ✅ Vérifier si un port est disponible */
    private static boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private void shutdown() {
        if (gpsApiServer != null)    gpsApiServer.stop();
        if (webSocketServer != null) webSocketServer.stop();
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
            logger.error("Erreur chargement login.fxml", e);
            showAlert("Erreur critique", "Impossible d'afficher l'écran de connexion.");
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
            logger.error("Erreur chargement forgot_password.fxml", e);
            showAlert("Erreur", "Impossible d'afficher l'écran de réinitialisation.");
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
            logger.error("Erreur chargement register.fxml", e);
            showAlert("Erreur", "Impossible d'afficher l'écran d'inscription.");
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
            logger.error("Erreur chargement reports_dashboard.fxml", e);
            showAlert("Erreur", "Impossible d'afficher l'écran des rapports.");
        }
    }

    public void showDashboard(String role) {
        try {
            String normalizedRole = role == null ? "" : role.trim();
            String fxmlFile;
            switch (normalizedRole) {
                case "Administrateur": fxmlFile = "/fxml/admin_dashboard.fxml";   break;
                case "Agent":          fxmlFile = "/fxml/agent_dashboard.fxml";   break;
                default:               fxmlFile = "/fxml/citizen_dashboard.fxml"; break;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Object controller = loader.getController();

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

            if (controller instanceof AgentDashboardController agentCtrl) {
                agentCtrl.setMainApp(this);
                activeAgentController = agentCtrl;
            } else if (controller instanceof AdminDashboardController adminCtrl) {
                adminCtrl.setMainApp(this);
            } else if (controller instanceof CitizenDashboardController citizenCtrl) {
                citizenCtrl.setMainApp(this);
                activeCitizenController = citizenCtrl;
            }
        } catch (Exception e) {
            logger.error("Erreur chargement dashboard pour le rôle {}", role, e);
            showAlert("Erreur de navigation",
                    "La connexion a réussi, mais le dashboard n'a pas pu s'ouvrir.\n"
                    + "Vérifiez le rôle utilisateur et les fichiers FXML.");
            showLoginScreen();
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

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(); }
}
