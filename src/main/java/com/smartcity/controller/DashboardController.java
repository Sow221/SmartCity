package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    private SignalementService signalementService;
    private MainApp mainApp;
    private ObservableList<Signalement> listeSignalements;

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

        tableSignalements.setItems(listeSignalements);
    }

    /**
     * Charge les données
     */
    public void chargerDonnees() {
        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
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
     * Met à jour les graphiques
     */
    private void mettreAJourGraphiques() {
        if (pieChart != null) {
            // PieChart - Signalements par statut
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("En attente", signalementService.countByStatut("En attente")),
                    new PieChart.Data("En cours", signalementService.countByStatut("En cours")),
                    new PieChart.Data("Collecté", signalementService.countByStatut("Collecté")));
            pieChart.setData(pieData);
        }
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
