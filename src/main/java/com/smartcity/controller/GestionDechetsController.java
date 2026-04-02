
package com.smartcity.controller;

import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;

/**
 * Contrôleur pour la gestion des déchets/signalements
 */
public class GestionDechetsController {

    @FXML
    private TextField descriptionField;

    @FXML
    private ComboBox<String> categorieCombo;

    @FXML
    private ComboBox<String> zoneCombo;

    @FXML
    private TextField quartierField;

    @FXML
    private TextField photoField;

    private SignalementService signalementService;
    private ObservableList<String> categories;
    private ObservableList<String> zones;

    public GestionDechetsController() {
        signalementService = new SignalementService();
        categories = FXCollections.observableArrayList("Plastique", "Papier", "Organique", "Verre");
        zones = FXCollections.observableArrayList("Pikine", "Guédiawaye");
    }

    /**
     * Initialise le contrôleur
     */
    /**
     * Méthode utilitaire pour retrouver l'idZone à partir du nom de la zone.
     * À adapter selon la logique de votre application (ex: requête en base ou
     * mapping statique).
     */
    private int getIdZoneByNom(String zoneNom) {
        // Exemple de mapping statique, à remplacer par une vraie requête si besoin
        if (zoneNom.equalsIgnoreCase("Pikine"))
            return 1;
        if (zoneNom.equalsIgnoreCase("Guédiawaye"))
            return 2;
        return 0; // ou lever une exception si zone inconnue
    }

    @FXML
    private void initialize() {
        categorieCombo.setItems(categories);
        zoneCombo.setItems(zones);
    }

    /**
     * Gère le clic sur le bouton Ajouter un signalement
     */
    @FXML
    private void handleAjouterSignalement() {
        String description = descriptionField.getText();
        String categorie = categorieCombo.getValue();
        String zoneNom = zoneCombo.getValue();
        String quartier = quartierField.getText();
        String photo = photoField.getText();

        if (description.isEmpty() || categorie == null || zoneNom == null || quartier.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();

        // Ici, il faut retrouver l'idZone à partir du nom de la zone (zoneNom)
        int idZone = getIdZoneByNom(zoneNom); // À implémenter selon votre logique

        Signalement signalement = new Signalement();
        signalement.setDescription(description);
        signalement.setCategorie(categorie);
        signalement.setIdZone(idZone);
        signalement.setLatitude(0.0); // À adapter si GPS disponible
        signalement.setLongitude(0.0); // À adapter si GPS disponible
        signalement.setPhoto(photo);
        signalement.setStatut("En attente");
        signalement.setDateSignalement(LocalDateTime.now());
        signalement.setIdUser(utilisateur.getIdUser());

        if (signalementService.ajouterSignalement(signalement)) {
            showAlert("Succès", "Signalement ajouté avec succès!");
            effacerChamps();
        } else {
            showAlert("Erreur", "Échec de l'ajout du signalement");
        }
    }

    /**
     * Efface les champs du formulaire
     */
    private void effacerChamps() {
        descriptionField.clear();
        categorieCombo.setValue(null);
        zoneCombo.setValue(null);
        quartierField.clear();
        photoField.clear();
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
