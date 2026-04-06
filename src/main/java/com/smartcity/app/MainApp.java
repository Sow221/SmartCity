package com.smartcity.app;

import com.smartcity.controller.AdminDashboardController;
import com.smartcity.controller.AgentDashboardController;
import com.smartcity.controller.CitizenDashboardController;
import com.smartcity.controller.DashboardController;
import com.smartcity.controller.LoginController;
import com.smartcity.controller.RegisterController;
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

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("SmartCity - Gestion des Dechets");
        primaryStage.setWidth(1100);
        primaryStage.setHeight(650);
        showLoginScreen();
    }

    public void showLoginScreen() {
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
                ((CitizenDashboardController) controller).setMainApp(this);
            } else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMainApp(this);
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du dashboard", e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
