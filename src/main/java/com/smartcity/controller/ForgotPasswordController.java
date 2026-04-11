package com.smartcity.controller;

import com.smartcity.app.MainApp;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button submitButton;
    @FXML private Hyperlink backToLoginLink;
    @FXML private Label emailErrorLabel;
    @FXML private Label statusMessageLabel;

    // Stagger — gauche
    @FXML private StackPane logoPane;
    @FXML private VBox brandTitleBox;
    @FXML private StackPane brandDivider;
    @FXML private VBox infoBox;
    @FXML private VBox brandFooter;

    // Stagger — droite
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private VBox emailGroup;
    @FXML private HBox backLinkBox;

    private MainApp mainApp;
    private static final Interpolator SPRING = Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94);

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        backToLoginLink.setOnAction(evt -> mainApp.showLoginScreen());
        applyLinkHover(backToLoginLink);
        emailField.focusedProperty().addListener((obs, was, now) -> {
            if (now) clearFieldError();
        });
        Platform.runLater(this::playStaggerAnimation);
    }

    public void playStaggerAnimation() {
        Node[] all = {logoPane, brandTitleBox, brandDivider, infoBox, brandFooter,
                      formTitle, formSubtitle, emailGroup, submitButton, backLinkBox};
        for (Node n : all) { if (n != null) { n.setOpacity(0); n.setTranslateY(14); } }
        if (logoPane != null) { logoPane.setScaleX(0.6); logoPane.setScaleY(0.6); logoPane.setTranslateY(0); }

        int[] delays = {0, 80, 140, 200, 300,   // gauche
                        120, 180, 260, 340, 400}; // droite
        staggerScaleIn(logoPane,     delays[0]);
        staggerSlideIn(brandTitleBox, delays[1]);
        staggerSlideIn(brandDivider,  delays[2]);
        staggerSlideIn(infoBox,       delays[3]);
        staggerSlideIn(brandFooter,   delays[4]);
        staggerSlideIn(formTitle,     delays[5]);
        staggerSlideIn(formSubtitle,  delays[6]);
        staggerSlideIn(emailGroup,    delays[7]);
        staggerSlideIn(submitButton,  delays[8]);
        staggerSlideIn(backLinkBox,   delays[9]);
    }

    @FXML
    private void handleSubmit() {
        String email = emailField.getText().trim();
        clearFieldError();

        if (email.isBlank() || !com.smartcity.utils.ValidationUtils.isValidEmail(email)) {
            showFieldError("Adresse email invalide");
            com.smartcity.utils.AnimationUtils.shake(submitButton).play();
            return;
        }

        submitButton.setDisable(true);
        submitButton.setText("Traitement...");

        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                com.smartcity.service.UtilisateurService svc = new com.smartcity.service.UtilisateurService();
                // Vérifier verrouillage
                String blockMsg = svc.getLoginBlockMessage(email);
                if (blockMsg != null) return "BLOCK:" + blockMsg;
                // Message neutre : ne pas exposer l'existence du compte ni générer
                // de mot de passe temporaire côté client.
                return "REQUEST_ACCEPTED";
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            submitButton.setText("ENVOYER LES INSTRUCTIONS");
            if (result.startsWith("BLOCK:")) {
                showFieldError(result.substring(6));
                submitButton.setDisable(false);
            } else if ("REQUEST_ACCEPTED".equals(result) || "NOTFOUND".equals(result)) {
                showSuccess("Demande prise en compte. Si un compte existe, un administrateur traitera la réinitialisation.");
            } else {
                showFieldError("Erreur lors de la réinitialisation. Contactez un administrateur.");
                submitButton.setDisable(false);
            }
        });

        task.setOnFailed(e -> {
            submitButton.setText("ENVOYER LES INSTRUCTIONS");
            submitButton.setDisable(false);
            showFieldError("Erreur réseau. Réessayez.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void showFieldError(String msg) {
        emailField.getStyleClass().remove("input-error");
        emailField.getStyleClass().add("input-error");
        emailErrorLabel.setText(msg);
        emailErrorLabel.setVisible(true);
        emailErrorLabel.setManaged(true);
    }

    private void clearFieldError() {
        emailField.getStyleClass().remove("input-error");
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
    }

    private void showSuccess(String msg) {
        statusMessageLabel.setText(msg);
        statusMessageLabel.getStyleClass().removeAll("auth-status-success", "auth-status-error");
        statusMessageLabel.getStyleClass().add("auth-status-success");
        statusMessageLabel.setVisible(true);
        statusMessageLabel.setManaged(true);
    }

    private void applyLinkHover(Hyperlink link) {
        link.setOnMouseEntered(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(120), link);
            s.setToX(1.04); s.setToY(1.04);
            s.setInterpolator(Interpolator.EASE_OUT); s.play();
        });
        link.setOnMouseExited(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(120), link);
            s.setToX(1.0); s.setToY(1.0);
            s.setInterpolator(Interpolator.EASE_IN); s.play();
        });
    }

    private void staggerSlideIn(Node node, int delayMs) {
        if (node == null) return;
        PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
        wait.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(260), node);
            fade.setFromValue(0.0); fade.setToValue(1.0); fade.setInterpolator(SPRING);
            TranslateTransition slide = new TranslateTransition(Duration.millis(260), node);
            slide.setFromY(14); slide.setToY(0); slide.setInterpolator(SPRING);
            ParallelTransition anim = new ParallelTransition(fade, slide);
            anim.setOnFinished(ev -> { node.setTranslateY(0); node.setOpacity(1.0); });
            anim.play();
        });
        wait.play();
    }

    private void staggerScaleIn(Node node, int delayMs) {
        if (node == null) return;
        PauseTransition wait = new PauseTransition(Duration.millis(delayMs));
        wait.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setFromValue(0.0); fade.setToValue(1.0); fade.setInterpolator(SPRING);
            ScaleTransition scale = new ScaleTransition(Duration.millis(400), node);
            scale.setFromX(0.6); scale.setFromY(0.6); scale.setToX(1.0); scale.setToY(1.0);
            scale.setInterpolator(Interpolator.SPLINE(0.34, 0.94, 0.64, 1.0));
            ParallelTransition anim = new ParallelTransition(fade, scale);
            anim.setOnFinished(ev -> { node.setOpacity(1.0); node.setScaleX(1.0); node.setScaleY(1.0); });
            anim.play();
        });
        wait.play();
    }
}
