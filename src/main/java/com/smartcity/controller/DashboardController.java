package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Dechet;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.DechetService;
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
    private TableView<Dechet> tableSignalements;

    @FXML
    private TableColumn<Dechet, Integer> colId;

    @FXML
    private TableColumn<Dechet, String> colDescription;

    @FXML
    private TableColumn<Dechet, String> colCategorie;

    @FXML
    private TableColumn<Dechet, String> colZone;

    @FXML
    private TableColumn<Dechet, String> colStatut;

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    private DechetService dechetService;
    private MainApp mainApp;
    private ObservableList<Dechet> listeSignalements;

    public DashboardController() {
        dechetService = new DechetService();
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idDechet"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tableSignalements.setItems(listeSignalements);
    }

    /**
     * Charge les données
     */
    @FXML
    private void chargerDonnees() {
        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
        List<Dechet> signalements;

        if (SessionManager.isAdmin()) {
            signalements = dechetService.getAllSignalements();
        } else if (SessionManager.isAgent()) {
            signalements = dechetService.getSignalementsByZone(utilisateur.getZone());
        } else {
            signalements = dechetService.getSignalementsByUtilisateur(utilisateur.getIdUser());
        }

        listeSignalements.clear();
        listeSignalements.addAll(signalements);

        mettreAJourGraphiques();
    }

    /**
     * Met à jour les graphiques
     */
    private void mettreAJourGraphiques() {
        // PieChart - Signalements par statut
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("En attente", dechetService.countByStatut("En attente")),
                new PieChart.Data("En cours", dechetService.countByStatut("En cours")),
                new PieChart.Data("Collecté", dechetService.countByStatut("Collecté")));
        pieChart.setData(pieData);

        // BarChart - Signalements par zone
        barChart.setTitle("Signalements par zone");
    }

    /**
     * Gère leclic sur le bouton Déconnexion
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
