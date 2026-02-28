package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour le tableau de bord
 */
public class DashboardController {

    @FXML
    private TableView<Signalement> tableSignalements;

    @FXML
    private TableColumn<Signalement, Integer> colId;

    @FXML
    private TableColumn<Signalement, String> colDescription;

    @FXML
    private TableColumn<Signalement, String> colCategorie;

    @FXML
    private TableColumn<Signalement, String> colZone;

    @FXML
    private TableColumn<Signalement, String> colStatut;

    // Colonne date présente dans les 3 dashboards FXML
    @FXML
    private TableColumn<Signalement, String> colDate;

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    private SignalementService signalementService;
    private MainApp mainApp;
    private ObservableList<Signalement> listeSignalements;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DashboardController() {
        signalementService = new SignalementService();
        listeSignalements = FXCollections.observableArrayList();
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne date : formater le LocalDateTime en chaîne lisible
        if (colDate != null) {
            colDate.setCellValueFactory(cellData -> {
                if (cellData.getValue().getDateSignalement() != null) {
                    return new SimpleStringProperty(
                            cellData.getValue().getDateSignalement().format(DATE_FORMATTER));
                }
                return new SimpleStringProperty("");
            });
        }

        tableSignalements.setItems(listeSignalements);
    }

    /**
     * Charge les données selon le rôle de l'utilisateur connecté.
     * Méthode publique ET annotée @FXML pour permettre l'appel
     * depuis le bouton Rafraîchir dans les FXML.
     */
    @FXML
    public void chargerDonnees() {
        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
        if (utilisateur == null) {
            return;
        }

        List<Signalement> signalements;

        if (SessionManager.isAdmin()) {
            signalements = signalementService.getAllSignalements();
        } else if (SessionManager.isAgent()) {
            signalements = signalementService.getSignalementsByZone(utilisateur.getZone());
        } else {
            signalements = signalementService.getSignalementsByUtilisateur(utilisateur.getIdUser());
        }

        listeSignalements.clear();
        listeSignalements.addAll(signalements);

        if (SessionManager.isAdmin()) {
            mettreAJourGraphiques();
        }
    }

    /**
     * Met à jour les graphiques (admin seulement)
     */
    private void mettreAJourGraphiques() {
        // PieChart – Signalements par statut
        if (pieChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("En attente", signalementService.countByStatut("En attente")),
                    new PieChart.Data("En cours",   signalementService.countByStatut("En cours")),
                    new PieChart.Data("Collecté",   signalementService.countByStatut("Collecté")));
            pieChart.setData(pieData);
            pieChart.setLegendVisible(true);
        }

        // BarChart – Signalements par zone
        if (barChart != null) {
            barChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Signalements");
            series.getData().add(new XYChart.Data<>("Pikine",      signalementService.countByZone("Pikine")));
            series.getData().add(new XYChart.Data<>("Guédiawaye",  signalementService.countByZone("Guédiawaye")));
            barChart.getData().add(series);
        }
    }

    /**
     * Agent : prendre en charge le signalement sélectionné
     */
    @FXML
    private void handlePrendreEnCharge() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un signalement.");
            return;
        }
        if (!"En attente".equals(selected.getStatut())) {
            showAlert("Action impossible", "Ce signalement n'est pas en attente.");
            return;
        }
        if (signalementService.updateStatut(selected.getIdSignalement(), "En cours")) {
            showAlert("Succès", "Signalement pris en charge.");
            chargerDonnees();
        } else {
            showAlert("Erreur", "Impossible de mettre à jour le statut.");
        }
    }

    /**
     * Agent : marquer le signalement sélectionné comme collecté
     */
    @FXML
    private void handleMarquerCollecte() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un signalement.");
            return;
        }
        if ("Collecté".equals(selected.getStatut())) {
            showAlert("Action impossible", "Ce signalement est déjà marqué comme collecté.");
            return;
        }
        if (signalementService.updateStatut(selected.getIdSignalement(), "Collecté")) {
            showAlert("Succès", "Signalement marqué comme collecté.");
            chargerDonnees();
        } else {
            showAlert("Erreur", "Impossible de mettre à jour le statut.");
        }
    }

    /**
     * Citoyen : ouvrir le formulaire de nouveau signalement
     */
    @FXML
    private void handleNouveauSignalement() {
        showAlert("Nouveau signalement",
                "Fonctionnalité disponible.\nUtilisez le formulaire de signalement.");
    }

    /**
     * Gère le clic sur le bouton Déconnexion
     */
    @FXML
    private void handleDeconnexion() {
        SessionManager.logout();
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
