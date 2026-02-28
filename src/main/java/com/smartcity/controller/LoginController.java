package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

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
    private Label statusMessageLabel;

    private final UtilisateurService utilisateurService;
    private MainApp mainApp;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

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

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showErrorMessage("Format email invalide");
            emailField.requestFocus();
            return;
        }

        if (motPasse.isBlank()) {
            showErrorMessage("Ce champ est requis");
            motPasseField.requestFocus();
            return;
        }

        if (motPasse.length() < 6) {
            showErrorMessage("Le mot de passe doit contenir au moins 6 caractères");
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
