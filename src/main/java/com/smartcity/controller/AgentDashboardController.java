package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.GeolocationService;
import com.smartcity.service.RealTimeGPSService;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgentDashboardController {

    // ========================= CHAMPS FXML =========================
    @FXML
    private TableView<Signalement> tableMesMissions;
    @FXML
    private TableView<Signalement> tableMissionsUrgentes;
    @FXML
    private TableView<Signalement> tableHistorique;
    @FXML
    private Button btnActionDemarrerProchaine;
    @FXML
    private Button btnActionVoirEnCours;
    @FXML
    private Button btnActionItineraireRapide;
    @FXML
    private Button btnActionActualiser;
    @FXML
    private Label lblPositionActuelle;

    @FXML
    private TableColumn<Signalement, String> colUrgId;
    @FXML
    private TableColumn<Signalement, String> colUrgZone;
    @FXML
    private TableColumn<Signalement, String> colUrgAdresse;
    @FXML
    private TableColumn<Signalement, String> colUrgType;
    @FXML
    private TableColumn<Signalement, String> colUrgDate;
    @FXML
    private TableColumn<Signalement, String> colUrgStatut;

    @FXML
    private TableColumn<Signalement, String> colMissionId;
    @FXML
    private TableColumn<Signalement, String> colMissionZone;
    @FXML
    private TableColumn<Signalement, String> colMissionAdresse;
    @FXML
    private TableColumn<Signalement, String> colMissionType;
    @FXML
    private TableColumn<Signalement, String> colMissionDate;
    @FXML
    private TableColumn<Signalement, String> colMissionStatut;
    @FXML
    private TableColumn<Signalement, Void> colMissionAction;

    @FXML
    private TableColumn<Signalement, String> colHistId;
    @FXML
    private TableColumn<Signalement, String> colHistZone;
    @FXML
    private TableColumn<Signalement, String> colHistAdresse;
    @FXML
    private TableColumn<Signalement, String> colHistType;
    @FXML
    private TableColumn<Signalement, String> colHistDate;
    @FXML
    private TableColumn<Signalement, String> colHistStatut;

    @FXML
    private Label mapDistanceLabel, mapTempsLabel, agentCardTotal, agentCardEnAttente, agentCardEnCours,
            agentCardTerminees, agentMessageLabel, lblMissionsCount, mapMissionIdLabel, mapMissionZoneLabel,
            mapMissionAdresseLabel, mapMissionStatutLabel, agentNameLabel;
    @FXML
    private TextField profilNomField, profilEmailField, profilZoneField;
    @FXML
    private DatePicker historiqueDatePicker;
    @FXML
    private VBox pageAgentDashboard, pageMesMissions, pageCarteZones, pageHistorique, pageMonProfil;
    @FXML
    private Button btnAgentDashboard, btnMesMissions, btnCarteZones, btnHistorique, btnMonProfil, themeToggleButton;
    @FXML
    private BorderPane rootPane;
    @FXML
    private WebView mapWebView;

    // ========================= SERVICES =========================
    private GeolocationService geolocationService = new GeolocationService();
    private RealTimeGPSService gpsService = new RealTimeGPSService();
    private SignalementService signalementService = new SignalementService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private ZoneService zoneService = new ZoneService();

    // ========================= DONNÉES =========================
    private ObservableList<Signalement> missions = FXCollections.observableArrayList();
    private ObservableList<Signalement> missionsUrgentes = FXCollections.observableArrayList();
    private ObservableList<Signalement> historique = FXCollections.observableArrayList();

    // ========================= AUTRES =========================
    private MainApp mainApp;
    private PauseTransition messageClearDelay = new PauseTransition(Duration.seconds(3));
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy");
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AgentDashboardController.class);
    private javafx.scene.media.AudioClip nearbyBeep;
    private javafx.animation.Timeline autoRefreshTimeline;

    // ========================= MÉTHODES PUBLIQUES =========================
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // colonne GPS distance dans "Mes Missions"
    @FXML
    private TableColumn<Signalement, String> colMissionDistance;

    @FXML
    public void initialize() {
        configureDashboardTable();
        configureMissionsTable();
        configureHistoriqueTable();

        tableMesMissions.setItems(missions);
        tableMissionsUrgentes.setItems(missionsUrgentes);
        tableHistorique.setItems(historique);

        initRealTimeGPS();
        startAutoRefresh();
        installTooltips();
        applyTheme();
        // showPage sans chargerDonnees ici — les données sont chargées par
        // chargerDonnees() appelé depuis MainApp
        VBox[] pages = { pageAgentDashboard, pageMesMissions, pageCarteZones, pageHistorique, pageMonProfil };
        for (VBox page : pages) {
            page.setVisible(page == pageAgentDashboard);
            page.setManaged(page == pageAgentDashboard);
        }
        if (btnAgentDashboard != null)
            btnAgentDashboard.getStyleClass().add("sidebar-agent-button-active");
    }

    private void initRealTimeGPS() {
        // Son notification proximité avec null-check
        java.net.URL beepUrl = getClass().getResource("/sounds/beep.mp3");
        if (beepUrl != null) {
            nearbyBeep = new javafx.scene.media.AudioClip(beepUrl.toExternalForm());
        } else {
            logger.warn("🔊 Son beep.mp3 non trouvé - notifications silencieuses");
        }

        // Démarrer GPS tracking
        gpsService.startRealTimeTracking();
        gpsService.addLocationUpdateListener(new RealTimeGPSService.LocationUpdateListener() {
            @Override
            public void onLocationUpdate(RealTimeGPSService.Coordinates pos) {
                updatePositionLabel(pos);
            }

            @Override
            public void onMissionsDistanceUpdate(java.util.List<RealTimeGPSService.MissionWithDistance> distances) {
                updateLiveDistances(distances);
                checkNearbyMissions(distances);
            }
        });

        logger.info("🗺️ GPS RealTime initialisé");
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> chargerDonnees()));
        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoRefreshTimeline.play();
        logger.info("🔄 Auto-refresh 30s démarré");
    }

    private void updatePositionLabel(RealTimeGPSService.Coordinates pos) {
        Platform.runLater(() -> {
            if (lblPositionActuelle != null) {
                lblPositionActuelle.setText(String.format("📍 %.4f, %.4f (Live)", pos.lat, pos.lon));
            }
        });
    }

    private void updateLiveDistances(java.util.List<RealTimeGPSService.MissionWithDistance> distances) {
        Platform.runLater(() -> {
            if (!distances.isEmpty()) {
                RealTimeGPSService.MissionWithDistance closest = distances.get(0);
                mapDistanceLabel.setText(String.format("%.2f km", closest.distanceKm));
                mapTempsLabel.setText(String.format("%d min", closest.estimatedMinutes));

                // Update mission sélectionnée si correspond
                Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    updateMapDetails(selected);
                }
            }
        });
    }

    private void checkNearbyMissions(java.util.List<RealTimeGPSService.MissionWithDistance> distances) {
        boolean hasNearby = distances.stream().anyMatch(d -> d.isNearby);
        if (hasNearby && nearbyBeep != null) {
            nearbyBeep.play();
            showAgentMessage("🚨 MISSION À PROXIMITÉ! Vérifiez la carte", true);
        }
    }

    @FXML
    public void chargerDonnees() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null)
            return;

        if (agentNameLabel != null)
            agentNameLabel.setText(current.getNom());

        List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());
        missions.setAll(missionList);

        // Dashboard : toutes les missions actives (En attente + En cours + Affecté)
        missionsUrgentes.setAll(missionList.stream()
                .filter(s -> !isTermine(s) && !"Archivé".equalsIgnoreCase(s.getStatut()))
                .collect(Collectors.toList()));

        refreshCards();
        refreshHistorique();
        refreshProfil();
        gpsService.updateMissions(missionList);

        if (!missions.isEmpty() && tableMesMissions.getSelectionModel().getSelectedItem() == null) {
            tableMesMissions.getSelectionModel().selectFirst();
        }
        refreshMap(tableMesMissions.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    private void handleDeconnexion() {
        // Stopper les timers avant de quitter
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        gpsService.stopRealTimeTracking();
        SessionManager.logout();
        if (mainApp != null)
            mainApp.showLoginScreen();
    }

    @FXML
    private void handleShowAgentDashboard() {
        showPage(pageAgentDashboard, btnAgentDashboard);
    }

    @FXML
    private void handleShowMesMissions() {
        showPage(pageMesMissions, btnMesMissions);
    }

    @FXML
    private void handleShowCarteZones() {
        showPage(pageCarteZones, btnCarteZones);
        refreshMap(tableMesMissions.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    private void handleShowHistorique() {
        showPage(pageHistorique, btnHistorique);
    }

    @FXML
    private void handleShowMonProfil() {
        showPage(pageMonProfil, btnMonProfil);
    }

    @FXML
    private void handleToggleTheme() {
        SessionManager.toggleDarkMode();
        applyTheme();
    }

    @FXML
    private void handleItineraireOptimal() {
        List<Signalement> selectedMissions = missions.stream()
                .filter(s -> "En attente".equalsIgnoreCase(s.getStatut()) || "En cours".equalsIgnoreCase(s.getStatut()))
                .collect(Collectors.toList());

        if (selectedMissions.isEmpty()) {
            showAgentMessage("Aucune mission active pour optimiser l'itinéraire.", false);
            return;
        }

        GeolocationService.Coordinates startPoint = geolocationService.getZoneCenter(getAgentZone());
        List<Signalement> optimizedRoute = geolocationService.optimizeCollectionRoute(selectedMissions, startPoint);
        refreshMapWithOptimizedRoute(optimizedRoute);

        String routeUrl = geolocationService.getMultiPointRouteUrl(optimizedRoute);
        StringBuilder routeInfo = new StringBuilder();
        routeInfo.append("📍 ITINÉRAIRE OPTIMISÉ (").append(optimizedRoute.size()).append(" missions)\n\n");
        double totalDistance = 0;
        GeolocationService.Coordinates currentPos = startPoint;
        for (int i = 0; i < optimizedRoute.size(); i++) {
            Signalement mission = optimizedRoute.get(i);
            double distance = geolocationService.calculateDistance(currentPos.lat, currentPos.lon,
                    mission.getLatitude(), mission.getLongitude());
            totalDistance += distance;
            routeInfo
                    .append(String.format("%d. Mission #%d - %s\n", i + 1, mission.getIdSignalement(),
                            mission.getCategorie()))
                    .append(String.format("   📍 Distance: %.2f km\n", distance))
                    .append(String.format("   🗺️ Zone: %s\n\n", mission.getZoneNom()));
            currentPos = new GeolocationService.Coordinates(mission.getLatitude(), mission.getLongitude());
        }
        routeInfo.append(String.format("🚗 Distance totale estimée: %.2f km\n", totalDistance))
                .append(String.format("⏱️ Temps estimé: %.0f minutes\n\n", totalDistance * 3))
                .append("🔗 Lien Google Maps:\n").append(routeUrl);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Itinéraire Optimisé");
        alert.setHeaderText("Itinéraire de collecte optimisé");
        TextArea textArea = new TextArea(routeInfo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(50);
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();

        showAgentMessage(String.format("Itinéraire optimisé pour %d missions (%.1f km total).", optimizedRoute.size(),
                totalDistance), true);
    }

    @FXML
    private void handleFiltrerHistorique() {
        refreshHistorique();
        showAgentMessage("Historique filtré appliqué.", true);
    }

    @FXML
    private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null)
            return;
        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank()) {
            showAgentMessage("Nom et email sont obligatoires.", false);
            return;
        }
        Utilisateur updated = new Utilisateur();
        updated.setIdUser(current.getIdUser());
        updated.setNom(profilNomField.getText().trim());
        updated.setEmail(profilEmailField.getText().trim());
        if (utilisateurService.updateUtilisateur(updated)) {
            current.setNom(updated.getNom());
            current.setEmail(updated.getEmail());
            agentNameLabel.setText(updated.getNom());
            showAgentMessage("Profil mis à jour.", true);
        } else {
            showAgentMessage("Échec de mise à jour du profil.", false);
        }
    }

    @FXML
    private void handleModifierMotPasse() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null)
            return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau mot de passe:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank())
            return;
        current.setMotDePasse(result.get());
        if (utilisateurService.updateUtilisateurAdmin(current)) {
            showAgentMessage("Mot de passe mis à jour.", true);
        } else {
            showAgentMessage("Échec de mise à jour du mot de passe.", false);
        }
    }

    // ========================= MÉTHODES PRIVÉES =========================
    private void showPage(VBox pageToShow, Button activeButton) {
        VBox[] pages = { pageAgentDashboard, pageMesMissions, pageCarteZones, pageHistorique, pageMonProfil };
        for (VBox page : pages) {
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }

        FadeTransition fade = new FadeTransition(Duration.millis(180), pageToShow);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        fade.play();

        Button[] buttons = { btnAgentDashboard, btnMesMissions, btnCarteZones, btnHistorique, btnMonProfil };
        for (Button btn : buttons) {
            btn.getStyleClass().remove("sidebar-agent-button-active");
            if (btn == activeButton) {
                btn.getStyleClass().add("sidebar-agent-button-active");
            }
        }

        if (pageToShow == pageAgentDashboard || pageToShow == pageMesMissions) {
            chargerDonnees();
        } else if (pageToShow == pageHistorique) {
            refreshHistorique();
        } else if (pageToShow == pageMonProfil) {
            refreshProfil();
        }
    }

    private void refreshCards() {
        int total = missions.size();
        int attente = (int) missions.stream().filter(s -> "En attente".equalsIgnoreCase(s.getStatut())).count();
        int enCours = (int) missions.stream().filter(s -> "En cours".equalsIgnoreCase(s.getStatut())).count();
        int terminees = (int) missions.stream().filter(this::isTermine).count();
        agentCardTotal.setText(String.valueOf(total));
        agentCardEnAttente.setText(String.valueOf(attente));
        agentCardEnCours.setText(String.valueOf(enCours));
        agentCardTerminees.setText(String.valueOf(terminees));
        updateMissionsCount();
    }

    private void refreshHistorique() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null)
            return;
        LocalDate date = historiqueDatePicker != null ? historiqueDatePicker.getValue() : null;
        if (date == null) {
            // Toutes les missions terminées de la zone
            historique.setAll(missions.stream().filter(this::isTermine).collect(Collectors.toList()));
        } else {
            // Missions terminées filtrées par date
            historique.setAll(
                    signalementService.getSignalementsByZoneAndDate(current.getIdZone(), date)
                            .stream().filter(this::isTermine).collect(Collectors.toList()));
        }
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null)
            return;
        profilNomField.setText(current.getNom());
        profilEmailField.setText(current.getEmail());
        profilZoneField.setText(zoneService.getZoneById(current.getIdZone()).getNomZone());
    }

    private void configureDashboardTable() {
        colUrgId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colUrgZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        colUrgAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colUrgType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colUrgDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colUrgStatut
                .setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        setCommonColumnStyles(colUrgId, colUrgZone, colUrgAdresse, colUrgType, colUrgDate, colUrgStatut);
        applyStatutColorCell(colUrgStatut);
    }

    private void configureMissionsTable() {
        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMissionZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        colMissionAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colMissionType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colMissionDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colMissionStatut
                .setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        // Colonne distance GPS : calculée en temps réel depuis la position agent
        colMissionDistance.setCellValueFactory(cell -> {
            RealTimeGPSService.Coordinates pos = gpsService.getCurrentPosition();
            if (pos == null || cell.getValue().getLatitude() == 0.0)
                return new SimpleStringProperty("-");
            double d = geolocationService.calculateDistance(pos.lat, pos.lon,
                    cell.getValue().getLatitude(), cell.getValue().getLongitude());
            return new SimpleStringProperty(String.format("%.2f km", d));
        });
        setCommonColumnStyles(colMissionId, colMissionZone, colMissionAdresse, colMissionType, colMissionDate,
                colMissionStatut, colMissionDistance);
        applyStatutColorCell(colMissionStatut);

        colMissionAction.setCellFactory(col -> new TableCell<Signalement, Void>() {
            private final Button startButton = new Button("🚀 Démarrer");
            private final Button doneButton = new Button("✅ Terminé");
            private final HBox box = new HBox(5, startButton, doneButton);
            {
                startButton.getStyleClass().addAll("btn-mission-start", "btn-modern");
                doneButton.getStyleClass().addAll("btn-mission-done", "btn-modern");
                startButton.setStyle(
                        "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                doneButton.setStyle(
                        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");
                startButton.setPrefSize(120, 35);
                doneButton.setPrefSize(120, 35);
                startButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    demarrerMissionAvecAnimation(mission, startButton);
                });
                doneButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    marquerTermineAvecAnimation(mission, doneButton);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Signalement mission = getTableView().getItems().get(getIndex());
                boolean terminee = isTermine(mission);
                boolean enCours = "En cours".equalsIgnoreCase(mission.getStatut());
                if (terminee) {
                    startButton.setText("✓ Fini");
                    startButton
                            .setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 8;");
                    doneButton.setText("✅ FAIT");
                    doneButton
                            .setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
                } else if (enCours) {
                    startButton.setText("⏳ En cours");
                    startButton
                            .setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 8;");
                    doneButton.setText("✅ FINIR");
                    doneButton.setStyle(
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #4CAF50, 5, 0, 0, 0);");
                } else {
                    startButton.setText("🚀 Démarrer");
                    startButton
                            .setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");
                    doneButton.setText("⏸ Attendre");
                    doneButton
                            .setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 8;");
                }
                startButton.setDisable(terminee);
                doneButton.setDisable(terminee || (!enCours && !"En attente".equalsIgnoreCase(mission.getStatut())));
                setGraphic(box);
            }
        });
    }

    private void configureHistoriqueTable() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colHistZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        colHistAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colHistType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colHistStatut
                .setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        setCommonColumnStyles(colHistId, colHistZone, colHistAdresse, colHistType, colHistDate, colHistStatut);
        applyStatutColorCell(colHistStatut);
    }

    @SafeVarargs
    private final void setCommonColumnStyles(TableColumn<Signalement, ?>... columns) {
        for (TableColumn<Signalement, ?> col : columns)
            col.setStyle("-fx-alignment: CENTER;");
    }

    private void applyStatutColorCell(TableColumn<Signalement, String> statutColumn) {
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                    return;
                }
                setText(item);
                String color = "#F57C00";
                if ("En cours".equalsIgnoreCase(item))
                    color = "#1565C0";
                else if ("Termine".equalsIgnoreCase(item))
                    color = "#2E7D32";
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
    }

    private void demarrerMissionAvecAnimation(Signalement mission, Button button) {
        if (mission == null || isTermine(mission))
            return;
        animateButton(button, 1.2, true);
        button.setText("Demarrage...");
        button.setDisable(true);
        if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {
            button.setText("Demarree!");
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
            showAgentMessage("Mission #" + mission.getIdSignalement() + " DEMARREE avec succes!", true);
            chargerDonnees();
        } else {
            button.setText("Erreur");
            button.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8;");
            animateButton(button, 0.9, false);
            showAgentMessage("Erreur lors du demarrage.", false);
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                button.setText("Demarrer");
                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");
                button.setDisable(false);
            });
            reset.play();
        }
    }

    private void marquerTermineAvecAnimation(Signalement mission, Button button) {
        if (mission == null || isTermine(mission))
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("✅ Confirmer Mission Terminée");
        alert.setHeaderText("Mission #" + mission.getIdSignalement());
        alert.setContentText("🎯 Confirmer que cette mission est TERMINÉE?\n\n📍 Zone: " + mission.getZoneNom()
                + "\n📋 Type: " + mission.getCategorie());
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setText("✅ TERMINÉ");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES)
            return;
        animateButton(button, 1.3, true);
        button.setText("⏳ Finalisation...");
        button.setDisable(true);
        if (signalementService.updateStatut(mission.getIdSignalement(), "Collecte")) {
            button.setText("🎉 TERMINÉE!");
            button.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #4CAF50, 8, 0, 0, 0);");
            showAgentMessage("🎉 MISSION TERMINÉE! #" + mission.getIdSignalement() + " - Excellent travail!", true);
            chargerDonnees();
        } else {
            button.setText("❌ Erreur");
            button.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8;");
            animateButton(button, 0.8, false);
            showAgentMessage("❌ Erreur lors de la finalisation.", false);
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                button.setText("✅ Terminé");
                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
                button.setDisable(false);
            });
            reset.play();
        }
    }

    private void animateButton(Button button, double scaleToValue, boolean isSuccess) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(scaleToValue);
        scale.setToY(scaleToValue);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        if (isSuccess) {
            scale.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(100), button);
                fade.setFromValue(0.7);
                fade.setToValue(1.0);
                fade.play();
            });
        }
        scale.play();
    }

    private void updateMissionsCount() {
        if (lblMissionsCount != null) {
            int actives = (int) missions.stream().filter(
                    s -> "En attente".equalsIgnoreCase(s.getStatut()) || "En cours".equalsIgnoreCase(s.getStatut()))
                    .count();
            lblMissionsCount
                    .setText(actives + " mission" + (actives > 1 ? "s" : "") + " active" + (actives > 1 ? "s" : ""));
        }
    }

    @FXML
    private void handleDemarrerProchaineMission() {
        Optional<Signalement> prochaineMission = missions.stream()
                .filter(s -> "En attente".equalsIgnoreCase(s.getStatut())).findFirst();
        if (prochaineMission.isPresent()) {
            Signalement mission = prochaineMission.get();
            if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {
                showAgentMessage("🚀 Mission #" + mission.getIdSignalement() + " démarrée automatiquement!", true);
                chargerDonnees();
                tableMesMissions.getSelectionModel().select(mission);
                showPage(pageMesMissions, btnMesMissions);
            } else {
                showAgentMessage("❌ Erreur lors du démarrage automatique.", false);
            }
        } else {
            showAgentMessage("ℹ️ Aucune mission en attente à démarrer.", false);
        }
    }

    @FXML
    private void handleVoirMissionsEnCours() {
        List<Signalement> enCours = missions.stream().filter(s -> "En cours".equalsIgnoreCase(s.getStatut()))
                .collect(Collectors.toList());
        if (enCours.isEmpty()) {
            showAgentMessage("ℹ️ Aucune mission actuellement en cours.", false);
        } else {
            showPage(pageMesMissions, btnMesMissions);
            tableMesMissions.getSelectionModel().select(enCours.get(0));
            showAgentMessage("📋 " + enCours.size() + " mission(s) en cours affichée(s).", true);
        }
    }

    @FXML
    private void handleItineraireRapide() {
        handleItineraireOptimal();
    }

    private void updateDistanceAndTime() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        RealTimeGPSService.Coordinates pos = gpsService.getCurrentPosition();
        if (selected != null && pos != null) {
            double d = geolocationService.calculateDistance(pos.lat, pos.lon, selected.getLatitude(),
                    selected.getLongitude());
            if (mapDistanceLabel != null)
                mapDistanceLabel.setText(String.format("%.2f km", d));
            if (mapTempsLabel != null)
                mapTempsLabel.setText(String.format("%d min", (int) Math.ceil(d * 4)));
        } else {
            if (mapDistanceLabel != null)
                mapDistanceLabel.setText("-");
            if (mapTempsLabel != null)
                mapTempsLabel.setText("-");
        }
    }

    private void updateMapDetails(Signalement mission) {
        if (mission == null) {
            mapMissionIdLabel.setText("-");
            mapMissionZoneLabel.setText("-");
            mapMissionAdresseLabel.setText("-");
            mapMissionStatutLabel.setText("-");
            return;
        }
        mapMissionIdLabel.setText(String.valueOf(mission.getIdSignalement()));
        mapMissionZoneLabel.setText(mission.getZoneNom());
        mapMissionAdresseLabel.setText(getAdresse(mission));
        mapMissionStatutLabel.setText(getDisplayStatut(mission.getStatut()));
    }

    private void refreshMap(Signalement selectedMission, boolean showRoute) {
        if (mapWebView == null)
            return;

        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='smart-gps-map.js'></script>" +
                "<style>html,body,#gps-map{height:100%;margin:0;}</style>" +
                "</head><body><div id='gps-map'></div>" +
                "<script>" +
                "window.javaAgent = {};" +
                "window.updateAgentPosition = (lat, lon) => { if(window.javaAgent && window.javaAgent.setAgentPosition) javaAgent.setAgentPosition(lat, lon); };"
                +
                "</script></body></html>";

        mapWebView.getEngine().loadContent(html);

        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    netscape.javascript.JSObject win = (netscape.javascript.JSObject) mapWebView.getEngine()
                            .executeScript("window");
                    win.setMember("javaAgent", new JSBridgeAgent());
                } catch (Exception e) {
                    logger.warn("Erreur bridge carte GPS", e);
                }
            }
        });
    }

    @FXML
    private void handleDefinirMaPosition() {
        if (mapWebView == null)
            return;
        showPage(pageCarteZones, btnCarteZones);
        showAgentMessage("📍 Cliquez sur la carte pour définir votre position exacte", true);
        // Activer le mode sélection de position dans la carte
        mapWebView.getEngine().executeScript("activerModePosition && activerModePosition()");
    }

    /** Pont JavaScript → Java pour recevoir la position cliquée sur la carte */
    public class JSBridgeAgent {
        public void setAgentPosition(double lat, double lon) {
            Platform.runLater(() -> {
                gpsService.setManualPosition(lat, lon);
                if (lblPositionActuelle != null) {
                    lblPositionActuelle.setText(String.format("📍 %.5f, %.5f (Manuel)", lat, lon));
                }
                showAgentMessage(String.format("✅ Position définie: %.4f, %.4f", lat, lon), true);
                updateDistanceAndTime();
            });
        }
    }

    private void refreshMapWithOptimizedRoute(List<Signalement> optimizedRoute) {
        if (mapWebView == null || optimizedRoute.isEmpty())
            return;
        mapWebView.getEngine().loadContent(buildOptimizedRouteHtml(optimizedRoute));
    }

    private String buildOptimizedRouteHtml(List<Signalement> optimizedRoute) {
        GeolocationService.Coordinates center = geolocationService.getZoneCenter(getAgentZone());
        StringBuilder markers = new StringBuilder();
        StringBuilder routePoints = new StringBuilder();
        markers.append(String.format(
                "L.marker([%.6f,%.6f],{icon:L.divIcon({className:'start-marker',html:'🏠',iconSize:[25,25]})}).addTo(map).bindPopup('Point de départ - %s');",
                center.lat, center.lon, getAgentZone()));
        routePoints.append(String.format("[%.6f,%.6f]", center.lat, center.lon));
        for (int i = 0; i < optimizedRoute.size(); i++) {
            Signalement mission = optimizedRoute.get(i);
            String color = i == 0 ? "#4CAF50" : (i == optimizedRoute.size() - 1 ? "#F44336" : "#2196F3");
            markers.append(String.format(
                    "L.circleMarker([%.6f,%.6f],{radius:12,color:'%s',fillColor:'%s',fillOpacity:0.9,weight:3}).addTo(map).bindPopup('%s');",
                    mission.getLatitude(), mission.getLongitude(), color, color,
                    String.format("<b>Étape %d</b><br/>Mission #%d<br/>%s<br/>%s", i + 1, mission.getIdSignalement(),
                            mission.getCategorie(), mission.getZoneNom())));
            routePoints.append(String.format(",[%.6f,%.6f]", mission.getLatitude(), mission.getLongitude()));
        }
        String routeLine = String.format(
                "L.polyline([%s],{color:'#FF5722',weight:4,dashArray:'10 5',opacity:0.8}).addTo(map);",
                routePoints.toString());
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/><style>html,body,#map{height:100%;margin:0;}.leaflet-popup-content-wrapper{border-radius:12px;box-shadow:0 6px 20px rgba(0,0,0,.15);}.start-marker{background:transparent;border:none;font-size:20px;text-align:center;}</style></head><body><div id='map'></div><script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script><script>var map=L.map('map').setView(["
                + center.lat + "," + center.lon
                + "],13);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
                + routeLine + markers.toString() + "</script></body></html>";
    }

    private String buildLeafletHtml(Signalement selectedMission, boolean showRoute) {
        Point center = zoneCenter(getAgentZone());
        if (selectedMission != null)
            center = missionPoint(selectedMission, indexOfMission(selectedMission));
        StringBuilder markers = new StringBuilder();
        for (int i = 0; i < missions.size(); i++) {
            Signalement mission = missions.get(i);
            Point point = missionPoint(mission, i);
            String markerColor = statusColorHex(getDisplayStatut(mission.getStatut()));
            String popup = "<div class=\"popup-card\"><div class=\"popup-title\">Zone: "
                    + jsEscape(mission.getZoneNom()) + "</div><div class=\"popup-item\">Mission: #"
                    + mission.getIdSignalement() + "</div><div class=\"popup-item\">Type: "
                    + jsEscape(mission.getCategorie()) + "</div><div class=\"popup-item\">Niveau: "
                    + jsEscape(getDisplayStatut(mission.getStatut())) + "</div><div class=\"popup-item\">Adresse: "
                    + jsEscape(getAdresse(mission)) + "</div></div>";
            markers.append("L.circleMarker([").append(fmt(point.lat)).append(",").append(fmt(point.lon))
                    .append("],{radius:9,color:'").append(markerColor).append("',fillColor:'").append(markerColor)
                    .append("',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('").append(popup).append("');");
        }

        // Marqueur position agent actuelle - sera ajouté via agentPosJs plus bas
        com.smartcity.service.RealTimeGPSService.Coordinates agentPos = gpsService.getCurrentPosition();

        Point zoneCenter = zoneCenter(getAgentZone());
        int zoneCount = missions.size();
        String criticalJs = "L.circle([" + fmt(zoneCenter.lat) + "," + fmt(zoneCenter.lon) + "],{radius:"
                + Math.max(700, zoneCount * 120)
                + ",color:'#FFC107',fillColor:'#FFC107',fillOpacity:0.12,weight:2}).addTo(map);";
        String routeJs = "";
        if (showRoute && selectedMission != null) {
            Point start = zoneCenter(getAgentZone());
            Point end = missionPoint(selectedMission, indexOfMission(selectedMission));
            routeJs = "L.polyline([[" + fmt(start.lat) + "," + fmt(start.lon) + "],[" + fmt(end.lat) + ","
                    + fmt(end.lon) + "]],{color:'#1565C0',weight:5,dashArray:'8 6'}).addTo(map);";
        }

        // Bouton "Ma Position" + mode clic sur carte
        String positionModeJs = "var positionMode = false;" +
                "var myPositionMarker = null;" +
                "function activerModePosition() {" +
                "  positionMode = true;" +
                "  map.getContainer().style.cursor = 'crosshair';" +
                "  document.getElementById('btnMaPosition').style.background = '#F44336';" +
                "  document.getElementById('btnMaPosition').innerText = 'Annuler';" +
                "}" +
                "function desactiverModePosition() {" +
                "  positionMode = false;" +
                "  map.getContainer().style.cursor = '';" +
                "  document.getElementById('btnMaPosition').style.background = '#2196F3';" +
                "  document.getElementById('btnMaPosition').innerText = 'Ma Position';" +
                "}" +
                "document.getElementById('btnMaPosition').addEventListener('click', function() {" +
                "  if (positionMode) { desactiverModePosition(); }" +
                "  else { activerModePosition(); }" +
                "});" +
                "map.on('click', function(e) {" +
                "  if (!positionMode) return;" +
                "  var lat = e.latlng.lat; var lon = e.latlng.lng;" +
                "  if (myPositionMarker) map.removeLayer(myPositionMarker);" +
                "  myPositionMarker = L.circleMarker([lat,lon],{radius:12,color:'#2196F3',fillColor:'#2196F3',fillOpacity:0.9,weight:3}).addTo(map).bindPopup('<b>Votre position</b>').openPopup();"
                +
                "  if (window.javaAgent) { window.javaAgent.setAgentPosition(lat, lon); }" +
                "  desactiverModePosition();" +
                "});";

        String agentPosJs = "";
        if (agentPos != null) {
            agentPosJs = String.format(
                    "L.circleMarker([%.6f,%.6f],{radius:12,color:'#2196F3',fillColor:'#2196F3',fillOpacity:0.9,weight:3}).addTo(map).bindPopup('<b>Votre position actuelle</b><br/>%.5f, %.5f');",
                    agentPos.lat, agentPos.lon, agentPos.lat, agentPos.lon);
        }

        String btnHtml = "<button id='btnMaPosition' style='position:absolute;top:10px;left:50px;z-index:1000;background:#2196F3;color:white;border:none;padding:10px 16px;border-radius:8px;font-size:14px;font-weight:bold;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,0.3);'>Ma Position</button>";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<style>html,body,#map{height:100%;margin:0;position:relative;}</style>" +
                "</head><body>" +
                btnHtml +
                "<div id='map'></div>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<script>" +
                "var map=L.map('map').setView([" + fmt(center.lat) + "," + fmt(center.lon) + "],13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
                +
                criticalJs + markers + routeJs + agentPosJs + positionModeJs +
                "</script></body></html>";
    }

    private int indexOfMission(Signalement mission) {
        for (int i = 0; i < missions.size(); i++)
            if (missions.get(i).getIdSignalement() == mission.getIdSignalement())
                return i;
        return 0;
    }

    private Point missionPoint(Signalement mission, int index) {
        Point base = zoneCenter(mission.getZoneNom());
        double latOffset = (index % 5) * 0.004 + 0.001;
        double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
        return new Point(base.lat + latOffset, base.lon + lonOffset);
    }

    private Point zoneCenter(String zone) {
        if (zone != null && zone.equalsIgnoreCase("Guediawaye"))
            return new Point(14.7765, -17.4047);

        GeolocationService.Coordinates c = zoneService.getCenter(zone);
        return new Point(c.lat, c.lon);
    }

    private String getAgentZone() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        return current != null ? zoneService.getZoneById(current.getIdZone()).getNomZone() : "Pikine";
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private String jsEscape(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ");
    }

    private String statusColorHex(String statut) {
        if ("En cours".equalsIgnoreCase(statut))
            return "#1E88E5";
        if ("Termine".equalsIgnoreCase(statut))
            return "#43A047";
        return "#FFC107";
    }

    private String getAdresse(Signalement signalement) {
        return (signalement.getDescription() == null || signalement.getDescription().isBlank()) ? "Adresse non précisée"
                : signalement.getDescription();
    }

    private String formatDate(Signalement signalement) {
        return signalement.getDateSignalement() == null ? "" : signalement.getDateSignalement().format(DATE_FORMATTER);
    }

    private boolean isTermine(Signalement signalement) {
        String statut = signalement.getStatut();
        if (statut == null)
            return false;
        String s = statut.toLowerCase().trim();
        return s.equals("collecte") || s.equals("termine") || s.equals("termin\u00e9") || s.startsWith("collect");
    }

    private String getDisplayStatut(String statut) {
        if (statut == null)
            return "";
        if (statut.toLowerCase().startsWith("collect") || "Termin\u00e9".equalsIgnoreCase(statut))
            return "Termin\u00e9";
        return statut;
    }

    private void showAgentDashboardPage() {
        showPage(pageAgentDashboard, btnAgentDashboard);
    }

    private void installTooltips() {
        btnAgentDashboard.setTooltip(new Tooltip("Vue globale des missions du jour"));
        btnMesMissions.setTooltip(new Tooltip("Missions assignées et actions"));
        btnCarteZones.setTooltip(new Tooltip("Carte interactive des zones"));
        btnHistorique.setTooltip(new Tooltip("Historique des missions terminées"));
        btnMonProfil.setTooltip(new Tooltip("Informations du compte agent"));
    }

    private void showAgentMessage(String message, boolean success) {
        String emoji = success ? "🎉" : "⚠️";
        String fullMessage = emoji + " " + message;
        agentMessageLabel.setText(fullMessage);
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13px;";
        if (success)
            agentMessageLabel
                    .setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049); " + baseStyle);
        else
            agentMessageLabel
                    .setStyle("-fx-background-color: linear-gradient(to right, #f44336, #da190b); " + baseStyle);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), agentMessageLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        messageClearDelay.stop();
        messageClearDelay.setOnFinished(evt -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), agentMessageLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> agentMessageLabel.setText(""));
            fadeOut.play();
        });
        messageClearDelay.playFromStart();
    }

    private static class Point {
        private final double lat, lon;

        private Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    private void applyTheme() {
        if (rootPane == null)
            return;
        rootPane.getStyleClass().remove("dark-mode");
        if (SessionManager.isDarkMode()) {
            rootPane.getStyleClass().add("dark-mode");
            if (themeToggleButton != null)
                themeToggleButton.setText("Mode Clair");
        } else if (themeToggleButton != null)
            themeToggleButton.setText("Mode Sombre");
    }
}