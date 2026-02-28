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

import java.io.IOException;

/**
 * Point d'entree principal de l'application SmartCity Dechets.
 */
public class MainApp extends Application {

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
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de login.fxml");
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
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de register.fxml");
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

            if (controller instanceof DashboardController) {
                DashboardController dashboardController = (DashboardController) controller;
                dashboardController.setMainApp(this);
                dashboardController.chargerDonnees();
            } else if (controller instanceof AgentDashboardController) {
                AgentDashboardController agentDashboardController = (AgentDashboardController) controller;
                agentDashboardController.setMainApp(this);
                agentDashboardController.chargerDonnees();
            } else if (controller instanceof AdminDashboardController) {
                AdminDashboardController adminDashboardController = (AdminDashboardController) controller;
                adminDashboardController.setMainApp(this);
                adminDashboardController.chargerDonnees();
            } else if (controller instanceof CitizenDashboardController) {
                CitizenDashboardController citizenDashboardController = (CitizenDashboardController) controller;
                citizenDashboardController.setMainApp(this);
                citizenDashboardController.chargerDonnees();
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du dashboard");
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
