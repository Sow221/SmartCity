package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.model.Zone;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgentDashboardController {

    // ==================== COMPOSANTS FXML ====================

    @FXML private VBox pageAgentDashboard;
    @FXML private VBox pageMesMissions;
    @FXML private VBox pageCarteZones;
    @FXML private VBox pageHistorique;
    @FXML private VBox pageMonProfil;

    @FXML private Label agentNameLabel;
    @FXML private Label agentMessageLabel;

    // Boutons de navigation
    @FXML private Button btnAgentDashboard;
    @FXML private Button btnMesMissions;
    @FXML private Button btnCarteZones;
    @FXML private Button btnHistorique;
    @FXML private Button btnMonProfil;
    @FXML private Button themeToggleButton;

    // Cartes de statistiques
    @FXML private Label agentCardTotal;
    @FXML private Label agentCardEnAttente;
    @FXML private Label agentCardEnCours;
    @FXML private Label agentCardTerminees;

    // Tableau des missions urgentes
    @FXML private TableView<Signalement> tableMissionsUrgentes;
    @FXML private TableColumn<Signalement, Integer> colUrgId;
    @FXML private TableColumn<Signalement, String> colUrgZone;
    @FXML private TableColumn<Signalement, String> colUrgAdresse;
    @FXML private TableColumn<Signalement, String> colUrgType;
    @FXML private TableColumn<Signalement, String> colUrgDate;
    @FXML private TableColumn<Signalement, String> colUrgStatut;

    // Tableau des missions
    @FXML private TableView<Signalement> tableMesMissions;
    @FXML private TableColumn<Signalement, Integer> colMissionId;
    @FXML private TableColumn<Signalement, String> colMissionZone;
    @FXML private TableColumn<Signalement, String> colMissionAdresse;
    @FXML private TableColumn<Signalement, String> colMissionType;
    @FXML private TableColumn<Signalement, String> colMissionDate;
    @FXML private TableColumn<Signalement, String> colMissionStatut;
    @FXML private TableColumn<Signalement, String> colMissionAction;

    // Composants de la carte
    @FXML private WebView mapWebView;
    @FXML private Label mapMissionIdLabel;
    @FXML private Label mapMissionZoneLabel;
    @FXML private Label mapMissionAdresseLabel;
    @FXML private Label mapMissionStatutLabel;

    // Historique
    @FXML private DatePicker historiqueDatePicker;
    @FXML private TableView<Signalement> tableHistorique;
    @FXML private TableColumn<Signalement, Integer> colHistId;
    @FXML private TableColumn<Signalement, String> colHistZone;
    @FXML private TableColumn<Signalement, String> colHistAdresse;
    @FXML private TableColumn<Signalement, String> colHistType;
    @FXML private TableColumn<Signalement, String> colHistDate;
    @FXML private TableColumn<Signalement, String> colHistStatut;

    // Profil
    @FXML private TextField profilNomField;
    @FXML private TextField profilEmailField;
    @FXML private TextField profilZoneField;

    // ==================== SERVICES ====================

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    // ==================== OBSERVABLE LISTS ====================

    private final ObservableList<Signalement> missionsList = FXCollections.observableArrayList();
    private final ObservableList<Signalement> urgentesList = FXCollections.observableArrayList();
    private final ObservableList<Signalement> historiqueList = FXCollections.observableArrayList();
    private final FilteredList<Signalement> filteredHistorique = new FilteredList<>(historiqueList, p -> true);

    // ==================== FORMATTERS ====================

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== MAIN APP REFERENCE ====================

    private MainApp mainApp;
    private Utilisateur currentUser;
    private Zone agentZone;

    // ==================== INITIALIZATION ====================

    @FXML
    private void initialize() {
        // Vérifier que l'utilisateur est connecté
        currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) {
            return;
        }

        // Vérifier que l'utilisateur est bien un agent
        if (!"AGENT".equals(currentUser.getRole())) {
            showMessage("Accès non autorisé", false);
            return;
        }

        // Afficher le nom de l'agent
        agentNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        // Récupérer la zone de l'agent
        chargerZoneAgent();

        // Configurer les tableaux
        configurerTableMissionsUrgentes();
        configurerTableMesMissions();
        configurerTableHistorique();
        configurerColonneAction();

        // Initialiser la carte
        initialiserCarte();

        // Charger les données
        chargerDonnees();

        // Afficher la page dashboard par défaut
        showPage(pageAgentDashboard, btnAgentDashboard);
    }

    /**
     * Définit l'application principale
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Récupère la zone de l'agent
     */
    private void chargerZoneAgent() {
        if (currentUser.getLocalite() != null) {
            agentZone = zoneService.getZoneByNom(currentUser.getLocalite());
        }
    }

    // ==================== CONFIGURATION DES TABLEAUX ====================

    private void configurerTableMissionsUrgentes() {
        colUrgId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));

        colUrgZone.setCellValueFactory(cellData -> {
            Zone zone = zoneService.getZoneById(cellData.getValue().getIdZone());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Inconnue");
        });

        colUrgAdresse.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            String adresse = "Lat: " + (s.getLatitude() != null ? s.getLatitude() : "N/A") +
                    ", Lon: " + (s.getLongitude() != null ? s.getLongitude() : "N/A");
            return new SimpleStringProperty(adresse);
        });

        colUrgType.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireCategorie(cellData.getValue().getCategorie())));

        colUrgDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateSignalement();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        colUrgStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireStatut(cellData.getValue().getStatut())));

        // Colorer les lignes selon le statut
        colUrgStatut.setCellFactory(column -> new TableCell<Signalement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("En attente".equals(item)) {
                        setStyle("-fx-background-color: #FFF3E0;");
                    } else if ("En cours".equals(item)) {
                        setStyle("-fx-background-color: #E3F2FD;");
                    } else if ("Collecté".equals(item)) {
                        setStyle("-fx-background-color: #E8F5E8;");
                    }
                }
            }
        });

        tableMissionsUrgentes.setItems(urgentesList);

        // Double-clic pour voir les détails
        tableMissionsUrgentes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement selected = tableMissionsUrgentes.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    prendreMissionSelectionnee(selected);
                }
            }
        });
    }

    private void configurerTableMesMissions() {
        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));

        colMissionZone.setCellValueFactory(cellData -> {
            Zone zone = zoneService.getZoneById(cellData.getValue().getIdZone());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Inconnue");
        });

        colMissionAdresse.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            String adresse = "Lat: " + (s.getLatitude() != null ? s.getLatitude() : "N/A") +
                    ", Lon: " + (s.getLongitude() != null ? s.getLongitude() : "N/A");
            return new SimpleStringProperty(adresse);
        });

        colMissionType.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireCategorie(cellData.getValue().getCategorie())));

        colMissionDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateSignalement();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        colMissionStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireStatut(cellData.getValue().getStatut())));

        tableMesMissions.setItems(missionsList);

        // Menu contextuel pour les missions
        ContextMenu contextMenu = new ContextMenu();
        MenuItem prendreItem = new MenuItem("Prendre en charge");
        MenuItem terminerItem = new MenuItem("Marquer comme collecté");
        MenuItem voirCarteItem = new MenuItem("Voir sur la carte");

        prendreItem.setOnAction(e -> prendreMissionDepuisMenu());
        terminerItem.setOnAction(e -> terminerMissionDepuisMenu());
        voirCarteItem.setOnAction(e -> voirMissionSurCarte());

        contextMenu.getItems().addAll(prendreItem, terminerItem, voirCarteItem);
        tableMesMissions.setContextMenu(contextMenu);

        // Double-clic pour action rapide
        tableMesMissions.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if ("EN_ATTENTE".equals(selected.getStatut())) {
                        prendreMissionSelectionnee(selected);
                    } else if ("EN_COURS".equals(selected.getStatut())) {
                        terminerMissionSelectionnee(selected);
                    }
                }
            }
        });
    }

    private void configurerColonneAction() {
        colMissionAction.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colMissionAction.setCellFactory(column -> new TableCell<Signalement, String>() {
            private final Button prendreBtn = new Button("Prendre");
            private final Button terminerBtn = new Button("Terminer");
            private final HBox box = new HBox(5, prendreBtn, terminerBtn);

            {
                prendreBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                terminerBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");

                prendreBtn.setOnAction(e -> {
                    Signalement s = getTableView().getItems().get(getIndex());
                    prendreMissionSelectionnee(s);
                });

                terminerBtn.setOnAction(e -> {
                    Signalement s = getTableView().getItems().get(getIndex());
                    terminerMissionSelectionnee(s);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Signalement s = getTableView().getItems().get(getIndex());
                    if ("EN_ATTENTE".equals(s.getStatut())) {
                        prendreBtn.setVisible(true);
                        terminerBtn.setVisible(false);
                        setGraphic(box);
                    } else if ("EN_COURS".equals(s.getStatut())) {
                        prendreBtn.setVisible(false);
                        terminerBtn.setVisible(true);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void configurerTableHistorique() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));

        colHistZone.setCellValueFactory(cellData -> {
            Zone zone = zoneService.getZoneById(cellData.getValue().getIdZone());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Inconnue");
        });

        colHistAdresse.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            String adresse = "Lat: " + (s.getLatitude() != null ? s.getLatitude() : "N/A") +
                    ", Lon: " + (s.getLongitude() != null ? s.getLongitude() : "N/A");
            return new SimpleStringProperty(adresse);
        });

        colHistType.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireCategorie(cellData.getValue().getCategorie())));

        colHistDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateSignalement();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        colHistStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireStatut(cellData.getValue().getStatut())));

        tableHistorique.setItems(filteredHistorique);
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    @FXML
    public void chargerDonnees() {
        if (currentUser == null) return;

        int userId = currentUser.getIdUser();

        // Charger toutes les missions de la zone de l'agent
        if (agentZone != null) {
            List<Signalement> zoneSignalements = signalementService.getSignalementsByZone(agentZone.getIdZone());

            // Missions urgentes (en attente)
            urgentesList.setAll(
                    zoneSignalements.stream()
                            .filter(s -> "EN_ATTENTE".equals(s.getStatut()))
                            .limit(5)
                            .collect(Collectors.toList())
            );

            // Missions de l'agent
            List<Signalement> agentSignalements = signalementService.getSignalementsByAgent(userId);
            missionsList.setAll(agentSignalements);

            // Historique (missions terminées)
            historiqueList.setAll(
                    zoneSignalements.stream()
                            .filter(s -> "COLLECTE".equals(s.getStatut()))
                            .collect(Collectors.toList())
            );
        }

        mettreAJourStatistiques();
        tableMissionsUrgentes.setItems(urgentesList);
        tableMesMissions.setItems(missionsList);
        tableHistorique.setItems(filteredHistorique);
    }

    private void mettreAJourStatistiques() {
        int userId = currentUser.getIdUser();

        int total = signalementService.countByUtilisateur(userId);
        int enAttente = 0;
        int enCours = 0;
        int terminees = 0;

        // Compter selon le statut
        for (Signalement s : missionsList) {
            switch (s.getStatut()) {
                case "EN_ATTENTE": enAttente++; break;
                case "EN_COURS": enCours++; break;
                case "COLLECTE": terminees++; break;
            }
        }

        agentCardTotal.setText(String.valueOf(total));
        agentCardEnAttente.setText(String.valueOf(enAttente));
        agentCardEnCours.setText(String.valueOf(enCours));
        agentCardTerminees.setText(String.valueOf(terminees));
    }

    // ==================== ACTIONS SUR LES MISSIONS ====================

    @FXML
    private void prendreMissionDepuisMenu() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            prendreMissionSelectionnee(selected);
        } else {
            showMessage("Veuillez sélectionner une mission", false);
        }
    }

    private void prendreMissionSelectionnee(Signalement mission) {
        if (!"EN_ATTENTE".equals(mission.getStatut())) {
            showMessage("Cette mission n'est pas en attente", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Prendre en charge la mission #" + mission.getIdSignalement());
        confirm.setContentText("Voulez-vous vraiment prendre cette mission ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (signalementService.affecterAgent(mission.getIdSignalement(), currentUser.getIdUser())) {
                chargerDonnees();
                showMessage("Mission prise en charge avec succès", true);
            } else {
                showMessage("Erreur lors de la prise en charge", false);
            }
        }
    }

    @FXML
    private void terminerMissionDepuisMenu() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            terminerMissionSelectionnee(selected);
        } else {
            showMessage("Veuillez sélectionner une mission", false);
        }
    }

    private void terminerMissionSelectionnee(Signalement mission) {
        if (!"EN_COURS".equals(mission.getStatut())) {
            showMessage("Cette mission n'est pas en cours", false);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Terminer la mission");
        dialog.setHeaderText("Mission #" + mission.getIdSignalement());
        dialog.setContentText("Commentaire (optionnel):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (signalementService.updateStatut(mission.getIdSignalement(), "COLLECTE")) {
                chargerDonnees();
                showMessage("Mission marquée comme collectée", true);
            } else {
                showMessage("Erreur lors de la mise à jour", false);
            }
        }
    }

    private void voirMissionSurCarte() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner une mission", false);
            return;
        }

        // Aller à la page carte
        showPage(pageCarteZones, btnCarteZones);

        // Afficher la mission sur la carte
        afficherMissionSurCarte(selected);
    }

    // ==================== CARTE ====================

    private void initialiserCarte() {
        WebEngine webEngine = mapWebView.getEngine();

        // Charger une carte OpenStreetMap simple
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <style>
                    body { margin: 0; padding: 0; }
                    #map { width: 100%; height: 100vh; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    function initMap() {
                        var map = L.map('map').setView([14.7167, -17.4677], 12);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(map);
                        window.map = map;
                    }
                </script>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script>initMap();</script>
            </body>
            </html>
        """;

        webEngine.loadContent(htmlContent);
    }

    @FXML
    private void handleItineraireOptimal() {
        // Simuler un itinéraire
        showMessage("Calcul de l'itinéraire optimal en cours...", true);
    }

    private void afficherMissionSurCarte(Signalement mission) {
        WebEngine webEngine = mapWebView.getEngine();

        if (mission.getLatitude() != null && mission.getLongitude() != null) {
            String js = String.format(
                    "if (window.map) {" +
                            "window.map.setView([%f, %f], 15);" +
                            "L.marker([%f, %f]).addTo(window.map)" +
                            ".bindPopup('Mission #%d<br>%s').openPopup();" +
                            "}",
                    mission.getLatitude(), mission.getLongitude(),
                    mission.getLatitude(), mission.getLongitude(),
                    mission.getIdSignalement(), mission.getDescription()
            );

            webEngine.executeScript(js);
        }

        // Mettre à jour les détails
        mapMissionIdLabel.setText("#" + mission.getIdSignalement());

        Zone zone = zoneService.getZoneById(mission.getIdZone());
        mapMissionZoneLabel.setText(zone != null ? zone.getNomZone() : "Inconnue");

        mapMissionAdresseLabel.setText(
                "Lat: " + mission.getLatitude() + "\nLon: " + mission.getLongitude()
        );

        mapMissionStatutLabel.setText(traduireStatut(mission.getStatut()));
    }

    // ==================== HISTORIQUE ====================

    @FXML
    private void handleFiltrerHistorique() {
        LocalDate selectedDate = historiqueDatePicker.getValue();

        if (selectedDate != null) {
            filteredHistorique.setPredicate(signalement -> {
                if (signalement.getDateSignalement() == null) return false;
                return signalement.getDateSignalement().toLocalDate().equals(selectedDate);
            });
            showMessage("Filtre appliqué", true);
        } else {
            filteredHistorique.setPredicate(s -> true);
        }
    }

    // ==================== PROFIL ====================

    @FXML
    private void handleModifierProfil() {
        String nouveauNom = profilNomField.getText().trim();
        String nouvelEmail = profilEmailField.getText().trim();

        if (nouveauNom.isEmpty() || nouvelEmail.isEmpty()) {
            showMessage("Les champs ne peuvent pas être vides", false);
            return;
        }

        currentUser.setNom(nouveauNom);
        currentUser.setEmail(nouvelEmail);

        if (utilisateurService.updateUtilisateur(currentUser)) {
            SessionManager.setUtilisateurConnecte(currentUser);
            agentNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            showMessage("Profil mis à jour avec succès", true);
        } else {
            showMessage("Erreur lors de la mise à jour", false);
        }
    }

    @FXML
    private void handleModifierMotPasse() {
        // Créer une boîte de dialogue personnalisée
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changer le mot de passe");
        dialog.setHeaderText("Modification du mot de passe");

        ButtonType confirmerButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField ancienMdp = new PasswordField();
        PasswordField nouveauMdp = new PasswordField();
        PasswordField confirmerMdp = new PasswordField();

        grid.add(new Label("Ancien mot de passe:"), 0, 0);
        grid.add(ancienMdp, 1, 0);
        grid.add(new Label("Nouveau mot de passe:"), 0, 1);
        grid.add(nouveauMdp, 1, 1);
        grid.add(new Label("Confirmer:"), 0, 2);
        grid.add(confirmerMdp, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmerButtonType) {
                if (!nouveauMdp.getText().equals(confirmerMdp.getText())) {
                    showMessage("Les mots de passe ne correspondent pas", false);
                    return null;
                }
                return nouveauMdp.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauMotDePasse -> {
            if (utilisateurService.updateMotDePasse(currentUser.getIdUser(), nouveauMotDePasse)) {
                showMessage("Mot de passe modifié avec succès", true);
            } else {
                showMessage("Erreur lors de la modification", false);
            }
        });
    }

    private void chargerProfil() {
        profilNomField.setText(currentUser.getNom());
        profilEmailField.setText(currentUser.getEmail());
        profilZoneField.setText(currentUser.getLocalite());
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleShowAgentDashboard() {
        showPage(pageAgentDashboard, btnAgentDashboard);
        chargerDonnees();
    }

    @FXML
    private void handleShowMesMissions() {
        showPage(pageMesMissions, btnMesMissions);
        chargerDonnees();
    }

    @FXML
    private void handleShowCarteZones() {
        showPage(pageCarteZones, btnCarteZones);
        // Afficher toutes les missions sur la carte
        if (!missionsList.isEmpty()) {
            afficherMissionSurCarte(missionsList.get(0));
        }
    }

    @FXML
    private void handleShowHistorique() {
        showPage(pageHistorique, btnHistorique);
        filteredHistorique.setPredicate(s -> true);
        historiqueDatePicker.setValue(null);
    }

    @FXML
    private void handleShowMonProfil() {
        showPage(pageMonProfil, btnMonProfil);
        chargerProfil();
    }

    private void showPage(VBox pageToShow, Button activeButton) {
        // Cacher toutes les pages
        pageAgentDashboard.setVisible(false);
        pageAgentDashboard.setManaged(false);
        pageMesMissions.setVisible(false);
        pageMesMissions.setManaged(false);
        pageCarteZones.setVisible(false);
        pageCarteZones.setManaged(false);
        pageHistorique.setVisible(false);
        pageHistorique.setManaged(false);
        pageMonProfil.setVisible(false);
        pageMonProfil.setManaged(false);

        // Afficher la page sélectionnée
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);

        // Réinitialiser les styles des boutons
        resetButtonStyles();

        // Styler le bouton actif
        activeButton.getStyleClass().add("active-button");
    }

    private void resetButtonStyles() {
        btnAgentDashboard.getStyleClass().remove("active-button");
        btnMesMissions.getStyleClass().remove("active-button");
        btnCarteZones.getStyleClass().remove("active-button");
        btnHistorique.getStyleClass().remove("active-button");
        btnMonProfil.getStyleClass().remove("active-button");
    }

    // ==================== THÈME ====================

    @FXML
    private void handleToggleTheme() {
        // Implémenter le changement de thème
        Pane rootPane = new Pane();
        if (themeToggleButton.getText().equals("Mode Sombre")) {
            themeToggleButton.setText("Mode Clair");
            // Appliquer le thème sombre
            rootPane.getScene().getStylesheets().clear();
            rootPane.getScene().getStylesheets().add(getClass().getResource("/css/dashboard-dark.css").toExternalForm());
        } else {
            themeToggleButton.setText("Mode Sombre");
            // Appliquer le thème clair
            rootPane.getScene().getStylesheets().clear();
            rootPane.getScene().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
        }
    }

    // ==================== DÉCONNEXION ====================

    @FXML
    private void handleDeconnexion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.logout();
            if (mainApp != null) {
                mainApp.showLoginScreen();
            }
        }
    }

    // ==================== UTILITAIRES ====================

    private String traduireStatut(String statut) {
        if (statut == null) return "";
        switch (statut) {
            case "EN_ATTENTE": return "En attente";
            case "EN_COURS": return "En cours";
            case "COLLECTE": return "Collecté";
            default: return statut;
        }
    }

    private String traduireCategorie(String categorie) {
        if (categorie == null) return "";
        switch (categorie) {
            case "PLASTIQUE": return "Plastique";
            case "PAPIER": return "Papier";
            case "ORGANIQUE": return "Organique";
            case "VERRE": return "Verre";
            default: return categorie;
        }
    }

    private void showMessage(String message, boolean success) {
        agentMessageLabel.setText(message);
        agentMessageLabel.setVisible(true);
        agentMessageLabel.setManaged(true);

        if (success) {
            agentMessageLabel.getStyleClass().remove("error-message");
            agentMessageLabel.getStyleClass().add("success-message");
        } else {
            agentMessageLabel.getStyleClass().remove("success-message");
            agentMessageLabel.getStyleClass().add("error-message");
        }

        // Faire disparaître le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    agentMessageLabel.setVisible(false);
                    agentMessageLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                // Ignorer
            }
        }).start();
    }
}