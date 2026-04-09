package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controleur pour l'ecran de connexion.
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField motPasseField;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink createAccountLink;
    @FXML private Button connexionButton;
    @FXML private Label statusMessageLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;

    @FXML private StackPane logoPane;
    @FXML private VBox brandTitleBox;
    @FXML private StackPane brandDivider;
    @FXML private HBox feature1;
    @FXML private HBox feature2;
    @FXML private HBox feature3;
    @FXML private VBox brandFooter;

    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private VBox emailGroup;
    @FXML private VBox passwordGroup;
    @FXML private HBox separatorBox;
    @FXML private HBox registerLinkBox;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        clearAllErrors();
        forgotPasswordLink.setOnAction(evt -> {
            if (mainApp != null) {
                mainApp.showForgotPassword();
            }
        });
        createAccountLink.setOnAction(evt -> {
            if (mainApp != null) {
                mainApp.showRegisterScreen();
            }
        });

        emailField.focusedProperty().addListener((obs, was, now) -> {
            if (!now && !emailField.getText().isBlank()) {
                validateEmailField();
            } else if (now) {
                clearFieldError(emailField, emailErrorLabel);
            }
        });
        motPasseField.focusedProperty().addListener((obs, was, now) -> {
            if (!now && !motPasseField.getText().isBlank()) {
                validatePasswordField();
            } else if (now) {
                clearFieldError(motPasseField, passwordErrorLabel);
            }
        });

        applyLinkHover(forgotPasswordLink);
        applyLinkHover(createAccountLink);

        Platform.runLater(() -> {
            playStaggerAnimation();
            emailField.requestFocus();
        });
    }

    public void playStaggerAnimation() {
        Interpolator spring = Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94);
        Node[] allNodes = {
            logoPane, brandTitleBox, brandDivider, feature1, feature2, feature3, brandFooter,
            formTitle, formSubtitle, emailGroup, passwordGroup, connexionButton, separatorBox, registerLinkBox
        };
        for (Node n : allNodes) {
            if (n != null) {
                n.setOpacity(0);
                n.setTranslateY(14);
            }
        }
        if (logoPane != null) {
            logoPane.setScaleX(0.6);
            logoPane.setScaleY(0.6);
            logoPane.setTranslateY(0);
        }

        int[] delays = {0, 80, 140, 200, 260, 320, 380, 120, 180, 260, 330, 400, 460, 510};
        staggerScaleIn(logoPane, delays[0], spring);
        staggerSlideIn(brandTitleBox, delays[1], spring);
        staggerSlideIn(brandDivider, delays[2], spring);
        staggerSlideIn(feature1, delays[3], spring);
        staggerSlideIn(feature2, delays[4], spring);
        staggerSlideIn(feature3, delays[5], spring);
        staggerSlideIn(brandFooter, delays[6], spring);
        staggerSlideIn(formTitle, delays[7], spring);
        staggerSlideIn(formSubtitle, delays[8], spring);
        staggerSlideIn(emailGroup, delays[9], spring);
        staggerSlideIn(passwordGroup, delays[10], spring);
        staggerSlideIn(connexionButton, delays[11], spring);
        staggerSlideIn(separatorBox, delays[12], spring);
        staggerSlideIn(registerLinkBox, delays[13], spring);
    }

    private void applyLinkHover(Hyperlink link) {
        if (link == null) {
            return;
        }
        link.setOnMouseEntered(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(120), link);
            s.setToX(1.04);
            s.setToY(1.04);
            s.setInterpolator(Interpolator.EASE_OUT);
            s.play();
        });
        link.setOnMouseExited(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(120), link);
            s.setToX(1.0);
            s.setToY(1.0);
            s.setInterpolator(Interpolator.EASE_IN);
            s.play();
        });
    }

    private void staggerSlideIn(Node node, int delayMs, Interpolator interp) {
        if (node == null) {
            return;
        }
        PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
        wait.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(260), node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(interp);

            TranslateTransition slide = new TranslateTransition(Duration.millis(260), node);
            slide.setFromY(14);
            slide.setToY(0);
            slide.setInterpolator(interp);

            ParallelTransition anim = new ParallelTransition(fade, slide);
            anim.setOnFinished(ev -> {
                node.setTranslateY(0);
                node.setOpacity(1.0);
            });
            anim.play();
        });
        wait.play();
    }

    private void staggerScaleIn(Node node, int delayMs, Interpolator interp) {
        if (node == null) {
            return;
        }
        PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
        wait.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setInterpolator(interp);

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), node);
            scale.setFromX(0.6);
            scale.setFromY(0.6);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0));

            ParallelTransition anim = new ParallelTransition(fade, scale);
            anim.setOnFinished(ev -> {
                node.setOpacity(1.0);
                node.setScaleX(1.0);
                node.setScaleY(1.0);
            });
            anim.play();
        });
        wait.play();
    }

    @FXML
    private void handleConnexion() {
        String email = emailField.getText().trim();
        String motPasse = motPasseField.getText();

        clearAllErrors();
        boolean valid = true;

        if (email.isBlank()) {
            showFieldError(emailField, emailErrorLabel, "Ce champ est requis");
            valid = false;
        } else if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showFieldError(emailField, emailErrorLabel, "Format email invalide");
            valid = false;
        }
        if (motPasse.isBlank()) {
            showFieldError(motPasseField, passwordErrorLabel, "Ce champ est requis");
            valid = false;
        }
        if (!valid) {
            com.smartcity.utils.AnimationUtils.shake(connexionButton).play();
            return;
        }

        connexionButton.setText("Connexion en cours...");
        connexionButton.setDisable(true);
        connexionButton.setStyle("-fx-opacity: 0.75;");

        javafx.concurrent.Task<Utilisateur> task = new javafx.concurrent.Task<>() {
            @Override
            protected Utilisateur call() {
                return utilisateurService.connexion(email, motPasse);
            }
        };

        task.setOnSucceeded(e -> {
            Utilisateur utilisateur = task.getValue();
            if (utilisateur != null) {
                connexionButton.setText("Connexion reussie");
                connexionButton.setStyle("-fx-background-color: #16a34a; -fx-opacity: 1;");
                showGlobalSuccess("Connexion reussie. Redirection en cours...");
                Platform.runLater(() -> redirectToDashboard(utilisateur));
            } else {
                connexionButton.setText("SE CONNECTER");
                connexionButton.setDisable(false);
                connexionButton.setStyle("");
                String blockMessage = utilisateurService.getLoginBlockMessage(email);
                if (blockMessage != null) {
                    showFieldError(emailField, emailErrorLabel, "Compte temporairement bloque");
                    showFieldError(motPasseField, passwordErrorLabel, blockMessage);
                } else {
                    showFieldError(emailField, emailErrorLabel, "Email ou mot de passe incorrect");
                    showFieldError(motPasseField, passwordErrorLabel, "Verifiez vos identifiants");
                }
                com.smartcity.utils.AnimationUtils.shake(connexionButton).play();
            }
        });

        task.setOnFailed(e -> {
            connexionButton.setText("SE CONNECTER");
            connexionButton.setDisable(false);
            connexionButton.setStyle("");
            showFieldError(emailField, emailErrorLabel, "Erreur de connexion. Reessayez.");
        });

        Thread loginThread = new Thread(task, "login-task");
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void redirectToDashboard(Utilisateur utilisateur) {
        try {
            SessionManager.setUtilisateurConnecte(utilisateur);
            if (mainApp == null) {
                showFieldError(emailField, emailErrorLabel, "Navigation indisponible");
                showFieldError(motPasseField, passwordErrorLabel, "Le controleur principal n'est pas initialise");
                connexionButton.setText("SE CONNECTER");
                connexionButton.setDisable(false);
                connexionButton.setStyle("");
                return;
            }
            mainApp.showDashboard(utilisateur.getRole());
        } catch (Exception ex) {
            connexionButton.setText("SE CONNECTER");
            connexionButton.setDisable(false);
            connexionButton.setStyle("");
            showFieldError(emailField, emailErrorLabel, "Connexion etablie mais redirection echouee");
            showFieldError(
                motPasseField,
                passwordErrorLabel,
                ex.getMessage() != null ? ex.getMessage() : "Erreur de navigation"
            );
        }
    }

    private void validateEmailField() {
        String email = emailField.getText().trim();
        if (!com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showFieldError(emailField, emailErrorLabel, "Format email invalide");
        } else {
            clearFieldError(emailField, emailErrorLabel);
        }
    }

    private void validatePasswordField() {
        if (motPasseField.getText().isEmpty()) {
            showFieldError(motPasseField, passwordErrorLabel, "Ce champ est requis");
        } else {
            clearFieldError(motPasseField, passwordErrorLabel);
        }
    }

    private void showFieldError(javafx.scene.control.Control field, Label errorLabel, String msg) {
        field.getStyleClass().remove("input-error");
        field.getStyleClass().add("input-error");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearFieldError(javafx.scene.control.Control field, Label errorLabel) {
        if (field != null) {
            field.getStyleClass().remove("input-error");
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearAllErrors() {
        clearFieldError(emailField, emailErrorLabel);
        clearFieldError(motPasseField, passwordErrorLabel);
        if (statusMessageLabel != null) {
            statusMessageLabel.setVisible(false);
            statusMessageLabel.setManaged(false);
        }
    }

    private void showGlobalSuccess(String message) {
        if (statusMessageLabel == null) {
            return;
        }
        statusMessageLabel.setText(message);
        statusMessageLabel.getStyleClass().removeAll("auth-status-success", "auth-status-error");
        statusMessageLabel.getStyleClass().add("auth-status-success");
        statusMessageLabel.setVisible(true);
        statusMessageLabel.setManaged(true);
    }
}
