package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Contrôleur pour l'écran de connexion
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField motPasseField;

    private UtilisateurService utilisateurService;
    private MainApp mainApp;

    public LoginController() {
        utilisateurService = new UtilisateurService();
    }

    /**
     * Définit la référence à MainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Gère le clic sur le bouton Connexion
     */
    @FXML
    private void handleConnexion() {
        String email = emailField.getText();
        String motPasse = motPasseField.getText();

        if (email.isEmpty() || motPasse.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        Utilisateur utilisateur = utilisateurService.connexion(email, motPasse);

        if (utilisateur != null) {
            SessionManager.setUtilisateurConnecte(utilisateur);
            mainApp.showDashboard(utilisateur.getRole());
        } else {
            showAlert("Erreur", "Email ou mot de passe incorrect");
        }
    }

    /**
     * Gère le clic sur le lien S'inscrire
     */
    @FXML
    private void handleInscription() {
        mainApp.showRegisterScreen();
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
