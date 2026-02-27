package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Contrôleur pour l'écran d'inscription
 */
public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField motPasseField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private ComboBox<String> zoneCombo;

    private UtilisateurService utilisateurService;
    private MainApp mainApp;
    private ObservableList<String> roles;
    private ObservableList<String> zones;

    public RegisterController() {
        utilisateurService = new UtilisateurService();
        roles = FXCollections.observableArrayList("Citoyen", "Agent", "Administrateur");
        zones = FXCollections.observableArrayList("Pikine", "Guédiawaye");
    }

    /**
     * Définit la référence à MainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initialise le contrôleur
     */
    @FXML
    private void initialize() {
        roleCombo.setItems(roles);
        zoneCombo.setItems(zones);
    }

    /**
     * Gère le clic sur le bouton S'inscrire
     */
    @FXML
    private void handleInscription() {
        String nom = nomField.getText();
        String email = emailField.getText();
        String motPasse = motPasseField.getText();
        String role = roleCombo.getValue();
        String zone = zoneCombo.getValue();

        if (nom.isEmpty() || email.isEmpty() || motPasse.isEmpty() || role == null || zone == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        Utilisateur utilisateur = new Utilisateur(nom, email, motPasse, role, zone);

        if (utilisateurService.inscription(utilisateur)) {
            showAlert("Succès", "Inscription réussie! Vous pouvez maintenant vous connecter.");
            mainApp.showLoginScreen();
        } else {
            showAlert("Erreur", "Échec de l'inscription. L'email existe peut-être déjà.");
        }
    }

    /**
     * Gère le clic sur le bouton Retour
     */
    @FXML
    private void handleRetour() {
        mainApp.showLoginScreen();
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
