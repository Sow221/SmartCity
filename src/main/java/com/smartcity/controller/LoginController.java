package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import com.smartcity.utils.ValidationUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Contrôleur pour l'écran de connexion
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField motPasseField;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink createAccountLink;

    @FXML
    private Button connexionButton;

    @FXML
    private Label statusMessageLabel;

    private final UtilisateurService utilisateurService;
    private MainApp mainApp;

    public LoginController() {
        utilisateurService = new UtilisateurService();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> emailField.requestFocus());
        clearStatusMessage();
        forgotPasswordLink.setOnAction(evt -> showErrorMessage("Mot de passe oublié ? Contactez support@smartcity.sn"));
        createAccountLink.setOnAction(evt -> mainApp.showRegisterScreen());

        // Validation en temps réel
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());
        motPasseField.textProperty().addListener((observable, oldValue, newValue) -> validatePassword());
    }



    @FXML
    private void handleConnexion() {
        String email = emailField.getText().trim();
        String motPasse = motPasseField.getText();

        clearStatusMessage();

        if (email.isBlank()) {
            showErrorMessage("Ce champ est requis");
            emailField.requestFocus();
            return;
        }

        if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showErrorMessage("Format email invalide");
            emailField.requestFocus();
            return;
        }

        if (motPasse.isBlank()) {
            showErrorMessage("Ce champ est requis");
            motPasseField.requestFocus();
            return;
        }

        com.smartcity.utils.ValidationUtils.ValidationResult pwCheck =
            com.smartcity.utils.ValidationUtils.validatePassword(motPasse);
        if (!pwCheck.isValid()) {
            showErrorMessage(pwCheck.getMessage());
            motPasseField.requestFocus();
            return;
        }

        Utilisateur utilisateur = utilisateurService.connexion(email, motPasse);

        if (utilisateur != null) {
            showSuccessMessage("Connexion réussie !");
            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(evt -> {
                SessionManager.setUtilisateurConnecte(utilisateur);
                mainApp.showDashboard(utilisateur.getRole());
            });
            delay.play();
        } else {
            showErrorMessage("Email ou mot de passe incorrect");
        }
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showErrorMessage("Ce champ est requis");
        } else if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showErrorMessage("Format email invalide");
        } else {
            clearStatusMessage();
        }
    }

    private void validatePassword() {
        String password = motPasseField.getText();
        if (password.isEmpty()) {
            showErrorMessage("Ce champ est requis");
        } else if (password.length() < 6) {
            showErrorMessage("Le mot de passe doit contenir au moins 6 caractères");
        } else {
            clearStatusMessage();
        }
    }

    private void showErrorMessage(String message) {
        showStatusMessage(message, "#D32F2F");
    }

    private void showSuccessMessage(String message) {
        showStatusMessage(message, "#388E3C");
    }

    private void showStatusMessage(String message, String color) {
        statusMessageLabel.setVisible(true);
        statusMessageLabel.setText(message);
        statusMessageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-text-alignment: center;");
    }

    private void clearStatusMessage() {
        statusMessageLabel.setVisible(false);
        statusMessageLabel.setText("");
    }
}