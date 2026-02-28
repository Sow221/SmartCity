package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.regex.Pattern;

/**
 * Contrôleur pour l'écran d'inscription
 */
public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField motPasseField;

    @FXML
    private PasswordField confirmField;

    @FXML
    private ComboBox<String> zoneCombo;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Hyperlink backToLoginLink;

    @FXML
    private Button inscriptionButton;

    private final UtilisateurService utilisateurService;
    private MainApp mainApp;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final String DEFAULT_ROLE = "Citoyen";
    private static final String[] ZONES = {"Pikine", "Guédiawaye"};

    public RegisterController() {
        utilisateurService = new UtilisateurService();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        zoneCombo.setItems(FXCollections.observableArrayList(ZONES));
        clearStatusMessage();
        backToLoginLink.setOnAction(evt -> mainApp.showLoginScreen());
        
        // Effets de survol du bouton
        inscriptionButton.setOnMouseEntered(e -> inscriptionButton.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand;"));
        inscriptionButton.setOnMouseExited(e -> inscriptionButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand;"));
    }

    @FXML
    private void handleInscription() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField != null ? telephoneField.getText().trim() : "";
        String motPasse = motPasseField.getText();
        String confirmation = confirmField.getText();
        String zone = zoneCombo.getValue();
        String role = DEFAULT_ROLE;

        clearStatusMessage();

        if (nom.isEmpty()) {
            showErrorMessage("Ce champ est requis");
            nomField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showErrorMessage("Ce champ est requis");
            emailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showErrorMessage("Format email invalide");
            emailField.requestFocus();
            return;
        }

        if (motPasse.isEmpty()) {
            showErrorMessage("Ce champ est requis");
            motPasseField.requestFocus();
            return;
        }

        if (motPasse.length() < 6) {
            showErrorMessage("Le mot de passe doit contenir au moins 6 caractères");
            motPasseField.requestFocus();
            return;
        }

        if (!motPasse.equals(confirmation)) {
            showErrorMessage("Les mots de passe ne correspondent pas");
            confirmField.requestFocus();
            return;
        }

        if (zone == null || zone.isEmpty()) {
            showErrorMessage("Merci de sélectionner une zone");
            zoneCombo.requestFocus();
            return;
        }

        Utilisateur utilisateur = new Utilisateur(nom, email, motPasse, role, zone,
                telephone.isEmpty() ? null : telephone);

        if (utilisateurService.inscription(utilisateur)) {
            showSuccessMessage("Inscription réussie ! Redirection vers la connexion...");
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(evt -> mainApp.showLoginScreen());
            delay.play();
        } else {
            showErrorMessage("Échec de l'inscription. L'email existe peut-être déjà.");
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
