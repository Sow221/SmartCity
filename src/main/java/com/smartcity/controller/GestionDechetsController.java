package com.smartcity.controller;

import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur de gestion des déchets (vue simplifiée signalements).
 * Utilisé comme fallback ou vue dédiée.
 */
public class GestionDechetsController {

    @FXML private TableView<Signalement> tableSignalements;
    @FXML private TableColumn<Signalement, Integer> colId;
    @FXML private TableColumn<Signalement, String> colDescription;
    @FXML private TableColumn<Signalement, String> colCategorie;
    @FXML private TableColumn<Signalement, String> colZone;
    @FXML private TableColumn<Signalement, String> colStatut;
    @FXML private TableColumn<Signalement, String> colDate;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private Label statusLabel;

    private final SignalementService signalementService = new SignalementService();
    private final ZoneService zoneService = new ZoneService();
    private final ObservableList<Signalement> signalements = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        tableSignalements.setItems(signalements);
        chargerDonnees();
    }

    private void configureTable() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        if (colDescription != null) colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colCategorie != null) colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        if (colZone != null) colZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        if (colStatut != null) colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        if (colDate != null) {
            colDate.setCellValueFactory(cell -> {
                if (cell.getValue().getDateSignalement() != null) {
                    return new SimpleStringProperty(cell.getValue().getDateSignalement().format(DATE_FORMATTER));
                }
                return new SimpleStringProperty("");
            });
        }
    }

    private void configureFilters() {
        if (filterStatutCombo != null) {
            filterStatutCombo.setItems(FXCollections.observableArrayList(
                "Tous", SignalementStatut.EN_ATTENTE.label(), SignalementStatut.AFFECTE.label(), SignalementStatut.EN_COURS.label(), SignalementStatut.TERMINE.label()));
            filterStatutCombo.setValue("Tous");
        }
    }

    @FXML
    public void chargerDonnees() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        List<Signalement> liste;
        if (SessionManager.isAdmin()) {
            liste = signalementService.getAllSignalements();
        } else if (SessionManager.isAgent()) {
            liste = signalementService.getSignalementsByZone(current.getIdZone());
        } else {
            liste = signalementService.getSignalementsByUtilisateur(current.getIdUser());
        }
        signalements.setAll(liste);
    }

    @FXML
    private void handleFiltrer() {
        if (filterStatutCombo == null) return;
        String statut = filterStatutCombo.getValue();
        if (statut == null || "Tous".equals(statut)) {
            chargerDonnees();
        } else {
            signalements.setAll(signalementService.getSignalementsFiltres(null, statut, null));
        }
    }

    @FXML
    private void handleModifierStatut() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Sélectionnez un signalement.", false);
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatut(),
            SignalementStatut.EN_ATTENTE.label(), SignalementStatut.AFFECTE.label(), SignalementStatut.EN_COURS.label(), SignalementStatut.TERMINE.label());
        dialog.setTitle("Modifier statut");
        dialog.setHeaderText("Signalement #" + selected.getIdSignalement());
        dialog.setContentText("Nouveau statut :");
        dialog.showAndWait().ifPresent(statut -> {
            if (signalementService.updateStatut(selected.getIdSignalement(), statut)) {
                showStatus("Statut mis à jour : " + statut, true);
                chargerDonnees();
            } else {
                showStatus("Échec de la mise à jour.", false);
            }
        });
    }

    @FXML
    private void handleSupprimer() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Sélectionnez un signalement.", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer le signalement #" + selected.getIdSignalement() + " ?");
        alert.showAndWait().filter(btn -> btn == ButtonType.OK).ifPresent(btn -> {
            if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
                showStatus("Signalement supprimé.", true);
                chargerDonnees();
            } else {
                showStatus("Échec de la suppression.", false);
            }
        });
    }

    private void showStatus(String message, boolean success) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle(success
            ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
            : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
    }
}
