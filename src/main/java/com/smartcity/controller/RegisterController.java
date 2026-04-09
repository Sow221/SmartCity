package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
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
    private final ZoneService zoneService;
    private MainApp mainApp;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final String DEFAULT_ROLE = "Citoyen";

    public RegisterController() {
        utilisateurService = new UtilisateurService();
        zoneService = new ZoneService();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        zoneCombo.setItems(javafx.collections.FXCollections.observableArrayList(
            zoneService.getAllZones().stream()
                .map(z -> z.getNomZone())
                .collect(java.util.stream.Collectors.toList())));
        clearStatusMessage();
        backToLoginLink.setOnAction(evt -> mainApp.showLoginScreen());
        

    }

    @FXML
    private void handleInscription() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String zone = zoneCombo.getValue();
        String motPasse = motPasseField.getText();
        String confirm = confirmField.getText();
        String role = DEFAULT_ROLE;

        if (nom.isEmpty() || !com.smartcity.utils.ValidationUtils.isValidName(nom)) {
            showErrorMessage("Nom invalide (2-50 caractères, lettres uniquement)");
            nomField.requestFocus();
            return;
        }

        if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showErrorMessage("Email invalide");
            emailField.requestFocus();
            return;
        }

        com.smartcity.utils.ValidationUtils.ValidationResult pwCheck =
            com.smartcity.utils.ValidationUtils.validatePassword(motPasse);
        if (!pwCheck.isValid()) {
            showErrorMessage(pwCheck.getMessage());
            motPasseField.requestFocus();
            return;
        }

        if (!motPasse.equals(confirm)) {
            showErrorMessage("Les mots de passe ne correspondent pas");
            confirmField.requestFocus();
            return;
        }

        if (zone == null || zone.isEmpty()) {
            showErrorMessage("Merci de sélectionner une zone");
            zoneCombo.requestFocus();
            return;
        }

        int idZone = zoneService.getAllZones().stream()
            .filter(z -> z.getNomZone().equals(zone))
            .mapToInt(com.smartcity.model.Zone::getIdZone)
            .findFirst().orElse(1);

        if (utilisateurService.emailExiste(email)) {
            showErrorMessage("Cet email est déjà utilisé.");
            emailField.requestFocus();
            return;
        }

        // nom complet dans le champ nom (NOT NULL en DB), prenom laissé vide
        Utilisateur utilisateur = new Utilisateur("", nom, email, motPasse, role, idZone);

        if (utilisateurService.inscription(utilisateur)) {
            showSuccessMessage("Inscription réussie ! Redirection vers la connexion...");
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(evt -> mainApp.showLoginScreen());
            delay.play();
        } else {
            showErrorMessage("Échec de l'inscription. Veuillez réessayer.");
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