package com.smartcity.controller;

import com.smartcity.model.Dechet;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.DechetService;
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

    private DechetService dechetService;
    private ObservableList<String> categories;
    private ObservableList<String> zones;

    public GestionDechetsController() {
        dechetService = new DechetService();
        categories = FXCollections.observableArrayList("Plastique", "Papier", "Organique", "Verre");
        zones = FXCollections.observableArrayList("Pikine", "Guédiawaye");
    }

    /**
     * Initialise le contrôleur
     */
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
        String zone = zoneCombo.getValue();
        String quartier = quartierField.getText();
        String photo = photoField.getText();

        if (description.isEmpty() || categorie == null || zone == null || quartier.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();

        Dechet dechet = new Dechet();
        dechet.setDescription(description);
        dechet.setCategorie(categorie);
        dechet.setZone(zone);
        dechet.setQuartier(quartier);
        dechet.setPhoto(photo);
        dechet.setStatut("En attente");
        dechet.setDateSignalement(LocalDateTime.now());
        dechet.setIdUtilisateur(utilisateur.getIdUser());

        if (dechetService.ajouterSignalement(dechet)) {
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
