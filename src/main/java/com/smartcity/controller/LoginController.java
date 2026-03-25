package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label messageLabel;

    private MainApp mainApp;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    /**
     * Initialise le contrôleur
     */
    @FXML
    private void initialize() {
        // Configurer les événements
        setupEnterKeyHandling();

        // Ajouter des listeners pour la validation en temps réel
        setupValidationListeners();

        // Message d'information
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    /**
     * Définit l'application principale
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Configure la gestion de la touche Entrée
     */
    private void setupEnterKeyHandling() {
        // Permet de soumettre le formulaire avec Entrée
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    /**
     * Configure les listeners de validation
     */
    private void setupValidationListeners() {
        // Activer/désactiver le bouton selon que les champs sont remplis
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Valider initialement
        validateForm();
    }

    /**
     * Valide le formulaire
     */
    private void validateForm() {
        boolean isValid = !emailField.getText().isBlank() && !passwordField.getText().isBlank();
        loginButton.setDisable(!isValid);
    }

    /**
     * Gère la connexion
     */
    @FXML
    private void handleLogin() {
        // Récupérer les valeurs
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation de base
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", false);
            return;
        }

        // Validation du format email
        if (!isValidEmail(email)) {
            showMessage("Format d'email invalide", false);
            return;
        }

        // Désactiver le bouton pendant la tentative de connexion
        loginButton.setDisable(true);
        loginButton.setText("Connexion en cours...");

        // Tenter la connexion (dans un thread séparé pour ne pas bloquer l'UI)
        new Thread(() -> {
            try {
                Utilisateur utilisateur = utilisateurService.connexion(email, password);

                javafx.application.Platform.runLater(() -> {
                    if (utilisateur != null) {
                        // Connexion réussie
                        SessionManager.setUtilisateurConnecte(utilisateur);
                        showMessage("Connexion réussie !", true);

                        // Rediriger vers le dashboard approprié
                        if (mainApp != null) {
                            mainApp.showDashboard();
                        }
                    } else {
                        // Connexion échouée
                        showMessage("Email ou mot de passe incorrect", false);
                        loginButton.setDisable(false);
                        loginButton.setText("Se connecter");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showMessage("Erreur de connexion: " + e.getMessage(), false);
                    loginButton.setDisable(false);
                    loginButton.setText("Se connecter");
                });
            }
        }).start();
    }

    /**
     * Gère l'inscription
     */
    @FXML
    private void handleRegister() {
        if (mainApp != null) {
            mainApp.showRegisterScreen();
        }
    }

    /**
     * Gère le mot de passe oublié
     */
    @FXML
    private void handleForgotPassword() {
        // Afficher une boîte de dialogue pour réinitialiser le mot de passe
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Veuillez saisir votre email :");

        dialog.showAndWait().ifPresent(email -> {
            if (!email.isBlank() && isValidEmail(email)) {
                // Vérifier si l'email existe
                if (utilisateurService.emailExiste(email)) {
                    showMessage("Un email de réinitialisation a été envoyé à " + email, true);
                    // Ici, vous implémenteriez l'envoi d'email
                } else {
                    showMessage("Aucun compte trouvé avec cet email", false);
                }
            } else {
                showMessage("Email invalide", false);
            }
        });
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    /**
     * Affiche un message
     */
    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        if (success) {
            messageLabel.getStyleClass().remove("error-message");
            messageLabel.getStyleClass().add("success-message");
        } else {
            messageLabel.getStyleClass().remove("success-message");
            messageLabel.getStyleClass().add("error-message");
        }

        // Faire disparaître le message après 5 secondes pour les messages de succès
        if (success) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    javafx.application.Platform.runLater(() -> {
                        messageLabel.setVisible(false);
                        messageLabel.setManaged(false);
                    });
                } catch (InterruptedException e) {
                    // Ignorer
                }
            }).start();
        }
    }

    /**
     * Nettoie les champs du formulaire
     */
    public void clearForm() {
        emailField.clear();
        passwordField.clear();
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        loginButton.setDisable(false);
        loginButton.setText("Se connecter");
    }

    public Hyperlink getRegisterLink() {
        return registerLink;
    }

    public void setRegisterLink(Hyperlink registerLink) {
        this.registerLink = registerLink;
    }

    public Hyperlink getForgotPasswordLink() {
        return forgotPasswordLink;
    }

    public void setForgotPasswordLink(Hyperlink forgotPasswordLink) {
        this.forgotPasswordLink = forgotPasswordLink;
    }
}