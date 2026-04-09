package com.smartcity.app;

import com.smartcity.controller.AdminDashboardController;
import com.smartcity.controller.AgentDashboardController;
import com.smartcity.controller.CitizenDashboardController;
import com.smartcity.controller.LoginController;
import com.smartcity.controller.RegisterController;
import com.smartcity.service.GpsApiServer;
import com.smartcity.utils.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Reinitialisation du mot de passe");
        dialog.setHeaderText("Entrez votre adresse email");
        javafx.scene.control.ButtonType btnReset = new javafx.scene.control.ButtonType(
            "Reinitialiser", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnReset, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefWidth(280);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8,
            new javafx.scene.control.Label("Email enregistre sur votre compte :"),
            emailField);
        content.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(content);

        javafx.scene.Node resetBtn = dialog.getDialogPane().lookupButton(btnReset);
        resetBtn.setDisable(true);
        emailField.textProperty().addListener((obs, oldValue, newValue) -> resetBtn.setDisable(newValue.isBlank()));
        dialog.setResultConverter(bt -> bt == btnReset ? emailField.getText().trim() : null);

        dialog.showAndWait().ifPresent(email -> {
            if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
                showAlert("Email invalide", "Le format de l'email est incorrect.");
                return;
            }

            com.smartcity.service.UtilisateurService svc = new com.smartcity.service.UtilisateurService();
            if (!svc.emailExiste(email)) {
                showAlert("Demande envoyee",
                    "Si un compte existe pour " + email
                        + ",\nun administrateur vous contactera pour reinitialiser votre mot de passe.");
                return;
            }

            String tempPwd = "Temp" + (int) (Math.random() * 9000 + 1000) + "!";
            com.smartcity.model.Utilisateur utilisateur = svc.getUtilisateurByEmail(email);
            if (utilisateur != null && svc.updateMotDePasse(utilisateur.getIdUser(), tempPwd)) {
                showTemporaryPasswordDialog(tempPwd);
            } else {
                showAlert("Erreur", "Impossible de reinitialiser. Contactez un administrateur.");
            }
        });
    }

    private void showTemporaryPasswordDialog(String temporaryPassword) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Mot de passe temporaire");
        dialog.setHeaderText("Un mot de passe temporaire a ete genere");
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);

        PasswordField hiddenField = new PasswordField();
        hiddenField.setText(temporaryPassword);
        hiddenField.setEditable(false);
        hiddenField.setPrefWidth(220);

        TextField visibleField = new TextField(temporaryPassword);
        visibleField.setEditable(false);
        visibleField.setVisible(false);
        visibleField.setManaged(false);
        visibleField.setPrefWidth(220);

        CheckBox revealCheck = new CheckBox("Afficher");
        revealCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            hiddenField.setVisible(!selected);
            hiddenField.setManaged(!selected);
            visibleField.setVisible(selected);
            visibleField.setManaged(selected);
        });

        javafx.scene.control.Button copyButton = new javafx.scene.control.Button("Copier");
        copyButton.setOnAction(evt -> {
            javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
            clipboardContent.putString(temporaryPassword);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(clipboardContent);
            copyButton.setText("Copie");
        });

        HBox passwordBox = new HBox(8, hiddenField, visibleField, revealCheck, copyButton);
        javafx.scene.control.Label warning = new javafx.scene.control.Label(
            "Conservez-le temporairement puis changez-le immediatement depuis Mon Profil.");
        warning.setWrapText(true);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(
            10,
            new javafx.scene.control.Label(
                "Le mot de passe n'est plus affiche en clair dans une alerte simple."),
            passwordBox,
            warning
        );
        content.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
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
                Platform.runLater(agentController::chargerDonnees);
            } else if (controller instanceof AdminDashboardController) {
                AdminDashboardController adminController = (AdminDashboardController) controller;
                adminController.setMainApp(this);
                Platform.runLater(adminController::chargerDonnees);
            } else if (controller instanceof CitizenDashboardController) {
                CitizenDashboardController citizenController = (CitizenDashboardController) controller;
                citizenController.setMainApp(this);
                activeCitizenController = citizenController;
                Platform.runLater(citizenController::chargerDonnees);
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
