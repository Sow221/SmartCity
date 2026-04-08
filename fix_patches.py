import os, re

BASE = os.path.dirname(os.path.abspath(__file__))

def patch(relpath, old_bytes, new_bytes):
    path = os.path.join(BASE, relpath)
    data = open(path, 'rb').read()
    if old_bytes in data:
        data = data.replace(old_bytes, new_bytes, 1)
        open(path, 'wb').write(data)
        return True
    # Try LF only
    old_lf = old_bytes.replace(b'\r\n', b'\n')
    new_lf = new_bytes.replace(b'\r\n', b'\n')
    if old_lf in data:
        data = data.replace(old_lf, new_lf, 1)
        open(path, 'wb').write(data)
        return True
    return False

results = []

# ── SEC-09 CitizenDashboardController ──────────────────────────────────────
r = patch(
    r'src\main\java\com\smartcity\controller\CitizenDashboardController.java',
    b'            showCitizenMessage("Acces reserve au role Citoyen.", false);\r\n            return;\r\n        }',
    b'            showCitizenMessage("Acces reserve au role Citoyen.", false);\r\n            if (mainApp != null) javafx.application.Platform.runLater(() -> mainApp.showLoginScreen());\r\n            return;\r\n        }'
)
results.append(('SEC-09 Citizen redirect', r))

# ── FUNC-04 AgentDashboardController : bloquer Terminer sans Demarrer ──────
r = patch(
    r'src\main\java\com\smartcity\controller\AgentDashboardController.java',
    b'                doneButton.setDisable(terminee || (!enCours && !"En attente".equalsIgnoreCase(mission.getStatut())));',
    b'                // FUNC-04: l\'agent doit avoir demarr\xc3\xa9 (En cours) pour pouvoir terminer\r\n                doneButton.setDisable(terminee || !enCours);'
)
results.append(('FUNC-04 Agent done button', r))

# ── QUAL-01 : utilitaire dialog mot de passe ───────────────────────────────
# Creer DialogUtils.java
dialog_utils = b'''package com.smartcity.utils;

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
'''
path = os.path.join(BASE, r'src\main\java\com\smartcity\utils\DialogUtils.java')
open(path, 'wb').write(dialog_utils)
results.append(('QUAL-01 DialogUtils created', True))

# ── DATA-04 : index SQL ────────────────────────────────────────────────────
index_sql = b'''-- Indexes pour les colonnes de filtrage frequent (DATA-04)
CREATE INDEX IF NOT EXISTS idx_signalement_statut ON Signalement(statut);
CREATE INDEX IF NOT EXISTS idx_signalement_zone   ON Signalement(idZone);
CREATE INDEX IF NOT EXISTS idx_signalement_user   ON Signalement(idUser);
CREATE INDEX IF NOT EXISTS idx_affectation_agent  ON Affectation(idAgent);
'''
idx_path = os.path.join(BASE, r'scripts\migrations\2026-add_missing_indexes.sql')
open(idx_path, 'wb').write(index_sql)
results.append(('DATA-04 index SQL', True))

# ── Rapport ────────────────────────────────────────────────────────────────
with open(os.path.join(BASE, 'fix_patches_result.txt'), 'w') as f:
    for name, ok in results:
        f.write(('[OK] ' if ok else '[FAIL] ') + name + '\n')
