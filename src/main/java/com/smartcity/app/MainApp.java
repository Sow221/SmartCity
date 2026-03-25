package com.smartcity.app;

import com.smartcity.controller.LoginController;
import com.smartcity.controller.RegisterController;
import com.smartcity.model.Utilisateur;
import com.smartcity.utils.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée principal de l'application SmartCity Déchets.
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SmartCity - Gestion des Déchets");
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

        // Centre la fenêtre sur l'écran
        primaryStage.centerOnScreen();

        // Afficher l'écran de connexion
        showLoginScreen();

        // Gérer la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            SessionManager.logout();
        });
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement",
                    "Impossible de charger l'écran de connexion.");
        }
    }

    public void showRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement",
                    "Impossible de charger l'écran d'inscription.");
        }
    }

    public void showDashboard() {
        Utilisateur currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) {
            showLoginScreen();
            return;
        }

        try {
            String fxmlFile;
            String role = currentUser.getRole();

            switch (role) {
                case "ADMIN":
                    fxmlFile = "/fxml/admin_dashboard.fxml";
                    break;
                case "AGENT":
                    fxmlFile = "/fxml/agent_dashboard.fxml";
                    break;
                case "CITOYEN":
                    fxmlFile = "/fxml/citizen_dashboard.fxml";
                    break;
                default:
                    fxmlFile = "/fxml/citizen_dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pas besoin de setMainApp pour les dashboards si vous n'en avez pas besoin
            // Les données seront chargées automatiquement par les contrôleurs dans initialize()

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement",
                    "Impossible de charger le tableau de bord.");
        }
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showCitizenDashboard() {

    }
}