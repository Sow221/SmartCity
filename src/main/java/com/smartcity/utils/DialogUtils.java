package com.smartcity.utils;

import com.smartcity.model.Utilisateur;
import com.smartcity.service.UtilisateurService;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.Optional;

/**
 * Utilitaire partage pour les dialogs recurrents (mot de passe, etc.)
 */
public class DialogUtils {

    private DialogUtils() {}

    /**
     * Affiche un dialog de changement de mot de passe.
     * Retourne true si le mot de passe a ete mis a jour avec succes.
     */
    public static boolean showChangePasswordDialog(Utilisateur current, UtilisateurService service) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changer le mot de passe");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(
            DialogUtils.class.getResource("/css/design-system.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("app-root");
        if (SessionManager.isDarkMode()) dialog.getDialogPane().getStyleClass().add("dark-mode");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Min. 6 caracteres");
        pwField.setAccessibleText("Nouveau mot de passe");
        pwField.getStyleClass().add("dash-field");
        pwField.setPrefHeight(44);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Repetez le mot de passe");
        confirmField.setAccessibleText("Confirmation mot de passe");
        confirmField.getStyleClass().add("dash-field");
        confirmField.setPrefHeight(44);

        Label confirmError = new Label("");
        confirmError.getStyleClass().add("auth-inline-error");
        confirmError.setVisible(false);

        VBox content = new VBox(8,
            new Label("Nouveau mot de passe *"), pwField,
            new Label("Confirmer le mot de passe *"), confirmField,
            confirmError);
        content.setPadding(new Insets(20, 24, 8, 24));
        content.setPrefWidth(360);
        content.getChildren().stream()
            .filter(n -> n instanceof Label && !n.getStyleClass().contains("auth-inline-error"))
            .forEach(n -> ((Label) n).getStyleClass().add("auth-field-label"));
        dialog.getDialogPane().setContent(content);

        javafx.scene.Node saveBtn = dialog.getDialogPane().lookupButton(saveType);
        saveBtn.getStyleClass().add("action-button");
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!pwField.getText().equals(confirmField.getText())) {
                confirmError.setText("Les mots de passe ne correspondent pas");
                confirmError.setVisible(true);
                evt.consume();
            }
        });

        dialog.setResultConverter(bt -> bt == saveType ? pwField.getText() : null);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return false;

        ValidationUtils.ValidationResult check = ValidationUtils.validatePassword(result.get());
        if (!check.isValid()) return false;

        return service.updateMotDePasse(current.getIdUser(), result.get());
    }
}
