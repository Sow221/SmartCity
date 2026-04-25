package com.smartcity.controller;





import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.GeolocationService;
import com.smartcity.service.GpsApiServer;
import com.smartcity.service.RealTimeGPSService;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;





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


    private Button btnGpsActiver;


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


    private TableColumn<Signalement, String> colHistDuree;


    @FXML


    private Label lblHistoriqueCount;





    @FXML


    private Label mapDistanceLabel, mapTempsLabel, mapCommentaireLabel, agentCardTotal, agentCardEnAttente, agentCardEnCours,


            agentCardTerminees, agentMessageLabel, lblMissionsCount, mapMissionIdLabel, mapMissionZoneLabel,


            mapMissionAdresseLabel, mapMissionStatutLabel, agentNameLabel,


            tourneeDistanceLabel, tourneeProchaineLabel, tourneeTempsLabel, qrUrlLabel,


            agentAvatarLabel, agentProfilAvatarLabel, agentProfilNomDisplay, lblGpsStatus;


    @FXML


    private TextField profilNomField, profilEmailField, profilZoneField;


    @FXML


    private DatePicker dashboardDatePicker, historiqueDatePicker;


    @FXML


    private ScrollPane pageAgentDashboard, pageMesMissions, pageHistorique;


    


    @FXML


    private ScrollPane pageCarteZonesScroll;


    @FXML


    private VBox pageCarteZones;


    @FXML


    private ScrollPane pageMonProfil;


    @FXML


    private Button btnAgentDashboard, btnMesMissions, btnCarteZones, btnHistorique, btnMonProfil, themeToggleButton;
    @FXML private javafx.scene.control.ComboBox<String> filterMissionsStatutCombo;


    @FXML


    private BorderPane rootPane;


    @FXML


    private WebView mapWebView;


    @FXML


    private javafx.scene.image.ImageView qrCodeImageViewCarte;


    @FXML


    private Label qrUrlLabelCarte;


    @FXML


    private Label profilStatMissionsLabel;


    @FXML


    private Label profilStatScoreLabel;





    // ========================= SERVICES =========================


    private GeolocationService geolocationService = new GeolocationService();


    private RealTimeGPSService gpsService = new RealTimeGPSService();


    private SignalementService signalementService = new SignalementService();


    private UtilisateurService utilisateurService = new UtilisateurService();


    private ZoneService zoneService = new ZoneService();


    private com.smartcity.service.AffectationService affectationService = new com.smartcity.service.AffectationService();





    // ========================= DONNÃƒÆ’Ã¢â‚¬Â°ES =========================


    private ObservableList<Signalement> missions = FXCollections.observableArrayList();


    private ObservableList<Signalement> missionsUrgentes = FXCollections.observableArrayList();


    private ObservableList<Signalement> historique = FXCollections.observableArrayList();





    // ========================= AUTRES =========================


    private MainApp mainApp;


    private PauseTransition messageClearDelay = new PauseTransition(Duration.seconds(3));


    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter


            .ofPattern("dd/MM/yyyy");


    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AgentDashboardController.class);
    
    // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Contenu embarquÃƒÆ’Ã‚Â© des ressources JS statiques
    private static String SMART_GPS_MAP_JS = null;
    
    static {
        loadEmbeddedResources();
    }
    
    private static void loadEmbeddedResources() {
        try {
            // Charger smart-gps-map.js une seule fois au dÃƒÆ’Ã‚Â©marrage
            try (var is = AgentDashboardController.class.getClassLoader().getResourceAsStream("js/smart-gps-map.js")) {
                if (is != null) {
                    SMART_GPS_MAP_JS = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    logger.info("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ smart-gps-map.js chargÃƒÆ’Ã‚Â© en mÃƒÆ’Ã‚Â©moire ({} bytes)", SMART_GPS_MAP_JS.length());
                } else {
                    logger.error("ÃƒÂ¢Ã‚ÂÃ…â€™ smart-gps-map.js not found in classpath");
                    SMART_GPS_MAP_JS = "";
                }
            }
        } catch (Exception e) {
            logger.error("ÃƒÂ¢Ã‚ÂÃ…â€™ Erreur chargement ressources embarquÃƒÆ’Ã‚Â©es", e);
            SMART_GPS_MAP_JS = "";
        }
    }


    private javafx.scene.media.AudioClip nearbyBeep;


    private javafx.animation.Timeline autoRefreshTimeline;


    private boolean mapFallbackHandlersInstalled;


    // Snapshot ids missions pour detecter les nouvelles affectations


    private final java.util.Set<Integer> missionsSnapshot = new java.util.HashSet<>();





    // ========================= MÃƒÆ’Ã¢â‚¬Â°THODES PUBLIQUES =========================


    public void setMainApp(MainApp mainApp) {


        this.mainApp = mainApp;


    }





    public void cleanup() {


        if (gpsService != null) {


            gpsService.stopRealTimeTracking();


        }


        if (autoRefreshTimeline != null) {


            autoRefreshTimeline.stop();


        }


    }





    // colonne GPS distance dans "Mes Missions"


    @FXML


    private TableColumn<Signalement, String> colMissionDistance;





    @FXML


    public void initialize() {


        configureDashboardTable();


        configureMissionsTable();


        configureHistoriqueTable();





        // Initialiser les labels avatar et nom profil
        Utilisateur agentInit = SessionManager.getUtilisateurConnecte();
        if (agentInit != null) {
            if (agentAvatarLabel != null)
                agentAvatarLabel.setText(agentInit.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (agentProfilAvatarLabel != null)
                agentProfilAvatarLabel.setText(agentInit.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (agentProfilNomDisplay != null)
                agentProfilNomDisplay.setText(agentInit.getNom());
        }

        tableMesMissions.setItems(missions);
        if (filterMissionsStatutCombo != null) {
            filterMissionsStatutCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Tous", "En attente", "AffectÃƒÆ’Ã‚Â©", "En cours", "TerminÃƒÆ’Ã‚Â©"));
            filterMissionsStatutCombo.setValue("Tous");
            filterMissionsStatutCombo.valueProperty().addListener((obs, old, val) -> {
                if (val == null || "Tous".equals(val)) {
                    tableMesMissions.setItems(missions);
                } else {
                    tableMesMissions.setItems(javafx.collections.FXCollections.observableArrayList(
                        missions.stream().filter(s -> com.smartcity.model.SignalementStatut.fromAny(val)
                            == com.smartcity.model.SignalementStatut.fromAny(s.getStatut()))
                        .collect(java.util.stream.Collectors.toList())));
                }
            });
            missions.addListener((javafx.collections.ListChangeListener<com.smartcity.model.Signalement>) c -> {
                String current = filterMissionsStatutCombo.getValue();
                if (current != null && !"Tous".equals(current)) {
                    filterMissionsStatutCombo.valueProperty().set(null);
                    filterMissionsStatutCombo.setValue(current);
                }
            });
        }


        tableMissionsUrgentes.setItems(missionsUrgentes);


        tableHistorique.setItems(historique);





        initRealTimeGPS();


        startAutoRefresh();


        installTooltips();


        applyTheme();


        


        // Initialisation visibilitÃƒÆ’Ã‚Â© pages


        javafx.scene.Node[] pages = { pageAgentDashboard, pageMesMissions, pageCarteZonesScroll, pageHistorique, pageMonProfil };


        for (javafx.scene.Node page : pages) {


            boolean visible = page == pageAgentDashboard;


            page.setVisible(visible);


            page.setManaged(visible);


        }


        if (btnAgentDashboard != null)


            btnAgentDashboard.getStyleClass().add("sidebar-agent-button-active");





        Platform.runLater(this::chargerDonnees);


    }





    private void initRealTimeGPS() {


        // Son notification proximitÃƒÆ’Ã‚Â© avec null-check


        java.net.URL beepUrl = getClass().getResource("/sounds/beep.mp3");


        if (beepUrl != null) {


            nearbyBeep = new javafx.scene.media.AudioClip(beepUrl.toExternalForm());


        } else {


            logger.warn("ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ…Â  Son beep.mp3 non trouvÃƒÆ’Ã‚Â© - notifications silencieuses");


        }





        // DÃƒÆ’Ã‚Â©marrer GPS tracking


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





        logger.info("ÃƒÂ¯Ã‚Â¿Ã‚Â½-ÃƒÂ¯Ã‚Â¿Ã‚Â½ÃƒÂ¯Ã‚Â¸Ã‚Â GPS RealTime initialisÃƒÆ’Ã‚Â©");


    }





    private void startAutoRefresh() {


        autoRefreshTimeline = new javafx.animation.Timeline(


                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> chargerDonnees()));


        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);


        autoRefreshTimeline.play();


        logger.info("ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ¢â‚¬Å¾ Auto-refresh 30s dÃƒÆ’Ã‚Â©marrÃƒÆ’Ã‚Â©");


    }





    private void updatePositionLabel(RealTimeGPSService.Coordinates pos) {


        Platform.runLater(() -> {


            if (lblPositionActuelle != null)


                lblPositionActuelle.setText(String.format("ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â %.4f, %.4f (Live)", pos.lat, pos.lon));


            if (lblGpsStatus != null)


                lblGpsStatus.setText("GPS actif ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â");


        });


    }





    private void updateLiveDistances(java.util.List<RealTimeGPSService.MissionWithDistance> distances) {


        Platform.runLater(() -> {


            refreshTourneeSummary();
            if (tableMesMissions != null) tableMesMissions.refresh(); // ? Refresh distances GPS


            if (!distances.isEmpty()) {


                RealTimeGPSService.MissionWithDistance closest = distances.get(0);


                mapDistanceLabel.setText(String.format("%.2f km", closest.distanceKm));


                mapTempsLabel.setText(String.format("%d min", closest.estimatedMinutes));





                // Update mission sÃƒÆ’Ã‚Â©lectionnÃƒÆ’Ã‚Â©e si correspond


                Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();


                if (selected != null) {


                    updateMapDetails(selected);


                }


                if (pageCarteZonesScroll != null && pageCarteZonesScroll.isVisible()) {


                    refreshTourneeMap();


                }


            }


        });


    }





    private void checkNearbyMissions(java.util.List<RealTimeGPSService.MissionWithDistance> distances) {


        boolean hasNearby = distances.stream().anyMatch(d -> d.isNearby);


        if (hasNearby && nearbyBeep != null) {


            nearbyBeep.play();


            showAgentMessage("ÃƒÂ°Ã…Â¸Ã…Â¡Ã‚Â¨ MISSION ÃƒÆ’Ã¢â€šÂ¬ PROXIMITÃƒÆ’Ã¢â‚¬Â°! VÃƒÆ’Ã‚Â©rifiez la carte", true);


        }


    }





    @FXML


    public void chargerDonnees() {


        chargerDonneesFiltrees(null);


    }





    private void chargerDonneesFiltrees(java.time.LocalDate dateFiltree) {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        if (agentNameLabel != null) agentNameLabel.setText(current.getNom());


        int idAgent = current.getIdUser();





        javafx.concurrent.Task<List<Signalement>> task = new javafx.concurrent.Task<>() {


            @Override


            protected List<Signalement> call() {


                List<Signalement> all = affectationService.getSignalementsByAgent(idAgent);


                if (dateFiltree == null) return all;


                return all.stream()


                    .filter(s -> s.getDateSignalement() != null


                        && dateFiltree.equals(s.getDateSignalement().toLocalDate()))


                    .collect(Collectors.toList());


            }


        };


        task.setOnSucceeded(e -> {


            List<Signalement> missionList = task.getValue();


            // Detecter nouvelles missions depuis le dernier refresh


            if (!missionsSnapshot.isEmpty()) {


                long nouvelles = missionList.stream()


                    .filter(s -> !isTermine(s) && !missionsSnapshot.contains(s.getIdSignalement()))


                    .count();


                if (nouvelles > 0) {


                    showAgentMessage(nouvelles + " nouvelle" + (nouvelles > 1 ? "s" : "") + " mission" + (nouvelles > 1 ? "s" : "") + " assignÃƒÆ’Ã‚Â©e" + (nouvelles > 1 ? "s" : "") + " !", true);


                    if (nearbyBeep != null) nearbyBeep.play();


                }


            }


            // Mettre a jour le snapshot


            missionsSnapshot.clear();


            missionList.forEach(s -> missionsSnapshot.add(s.getIdSignalement()));


            String currentFilter = (filterMissionsStatutCombo != null) ? filterMissionsStatutCombo.getValue() : "Tous";
            missions.setAll(missionList);


            // Missions actives = tout ce qui n'est pas termine


            // Les urgentes (>24h en attente) sont mises en evidence visuellement


            java.time.LocalDateTime seuil24h = java.time.LocalDateTime.now().minusHours(24);


            missionsUrgentes.setAll(missionList.stream()


                    .filter(s -> !isTermine(s))


                    .collect(Collectors.toList()));


            refreshCards();
            // Reapply filter after refresh
            if (!"Tous".equals(currentFilter) && filterMissionsStatutCombo != null) filterMissionsStatutCombo.setValue(currentFilter);


            refreshTourneeSummary();


            refreshHistorique();


            refreshProfil();


            gpsService.updateMissions(missionList);


            if (!missions.isEmpty() && tableMesMissions.getSelectionModel().getSelectedItem() == null)


                tableMesMissions.getSelectionModel().selectFirst();


            if (pageCarteZonesScroll != null && pageCarteZonesScroll.isVisible())


                refreshTourneeMap();


            else


                refreshMap(tableMesMissions.getSelectionModel().getSelectedItem(), false);


        });


        task.setOnFailed(e -> logger.error("Erreur chargement missions agent", task.getException()));


        Thread t = new Thread(task, "agent-load"); t.setDaemon(true); t.start();


    }





    @FXML


    private void handleDeconnexion() {


        // Stopper les timers avant de quitter


        if (autoRefreshTimeline != null) {


            autoRefreshTimeline.stop();


        }


        gpsService.stopRealTimeTracking();


        // NETTOYER TOUS LES LISTENERS POUR EVITER MEMORY LEAK


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


        showPage(pageCarteZonesScroll, btnCarteZones);


        refreshTourneeMap();


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        String gpsUrl = GpsApiServer.getGpsPageUrl(current.getIdUser());


        if (qrUrlLabelCarte != null) qrUrlLabelCarte.setText(gpsUrl);


        if (qrCodeImageViewCarte != null) {


            // Generation QR code async pour ne pas bloquer le thread UI


            javafx.concurrent.Task<javafx.scene.image.Image> qrTask = new javafx.concurrent.Task<>() {


                @Override protected javafx.scene.image.Image call() {


                    return com.smartcity.utils.QrCodeUtils.generateQrCode(gpsUrl, 180);


                }


            };


            qrTask.setOnSucceeded(e -> {


                if (qrTask.getValue() != null) qrCodeImageViewCarte.setImage(qrTask.getValue());


            });


            Thread t = new Thread(qrTask, "qr-gen"); t.setDaemon(true); t.start();


        }


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


    private void handleFiltrerDashboard() {


        java.time.LocalDate date = dashboardDatePicker != null ? dashboardDatePicker.getValue() : null;


        chargerDonneesFiltrees(date);


        showAgentMessage(date != null


            ? "Missions du " + date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " affich\u00e9es."


            : "Toutes les missions affich\u00e9es.", true);


    }





    @FXML


    private void handleResetDashboard() {


        if (dashboardDatePicker != null) dashboardDatePicker.setValue(null);


        chargerDonnees();


        showAgentMessage("Toutes les missions affich\u00e9es.", true);


    }





    @FXML


    private void handleToggleTheme() {


        SessionManager.toggleDarkMode();


        applyTheme();


    }





    @FXML


    private void handleItineraireOptimal() {


        List<Signalement> selectedMissions = getActiveMissions();





        if (selectedMissions.isEmpty()) {


            showAgentMessage("Aucune mission active pour optimiser l'itinÃƒÆ’Ã‚Â©raire.", false);


            return;


        }





        GeolocationService.Coordinates startPoint = getTourneeStartPoint();


        List<Signalement> optimizedRoute = geolocationService.optimizeCollectionRoute(selectedMissions, startPoint);


        refreshMapWithOptimizedRoute(optimizedRoute);





        String routeUrl = geolocationService.getMultiPointRouteUrl(optimizedRoute);


        StringBuilder routeInfo = new StringBuilder();


        routeInfo.append("ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â ITINÃƒÆ’Ã¢â‚¬Â°RAIRE OPTIMISÃƒÆ’Ã¢â‚¬Â° (").append(optimizedRoute.size()).append(" missions)\n\n");


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


                    .append(String.format("   ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â Distance: %.2f km\n", distance))


                    .append(String.format("   ÃƒÂ¯Ã‚Â¿Ã‚Â½-ÃƒÂ¯Ã‚Â¿Ã‚Â½ÃƒÂ¯Ã‚Â¸Ã‚Â Zone: %s\n\n", mission.getZoneNom()));


            currentPos = new GeolocationService.Coordinates(mission.getLatitude(), mission.getLongitude());


        }


        routeInfo.append(String.format("ÃƒÂ¯Ã‚Â¿Ã‚Â½- Distance totale estimÃƒÆ’Ã‚Â©e: %.2f km\n", totalDistance))


                .append(String.format("ÃƒÂ¢Ã‚ÂÃ‚Â±ÃƒÂ¯Ã‚Â¸Ã‚Â Temps estimÃƒÆ’Ã‚Â©: %.0f minutes\n\n", totalDistance * 3))


                .append("ÃƒÂ¯Ã‚Â¿Ã‚Â½- Lien Google Maps:\n").append(routeUrl);





        Alert alert = new Alert(Alert.AlertType.INFORMATION);


        alert.setTitle("ItinÃƒÆ’Ã‚Â©raire OptimisÃƒÆ’Ã‚Â©");


        alert.setHeaderText("ItinÃƒÆ’Ã‚Â©raire de collecte optimisÃƒÆ’Ã‚Â©");


        TextArea textArea = new TextArea(routeInfo.toString());


        textArea.setEditable(false);


        textArea.setWrapText(true);


        textArea.setPrefRowCount(15);


        textArea.setPrefColumnCount(50);


        alert.getDialogPane().setContent(textArea);


        alert.showAndWait();





        showAgentMessage(String.format("ItinÃƒÆ’Ã‚Â©raire optimisÃƒÆ’Ã‚Â© pour %d missions (%.1f km total).", optimizedRoute.size(),


                totalDistance), true);


    }





    @FXML


    private void handleFiltrerHistorique() {


        refreshHistorique();


        showAgentMessage("Historique filtrÃƒÆ’Ã‚Â© appliquÃƒÆ’Ã‚Â©.", true);


    }





    @FXML


    private void handleResetHistorique() {


        if (historiqueDatePicker != null) {


            historiqueDatePicker.setValue(null);


        }


        refreshHistorique();


        showAgentMessage("Historique rÃƒÆ’Ã‚Â©initialisÃƒÆ’Ã‚Â©.", true);


    }





    @FXML


    private void handleModifierProfil() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank()) {


            showAgentMessage("Nom et email sont obligatoires.", false);


            return;


        }


        String newNom = profilNomField.getText().trim();


        String newEmail = profilEmailField.getText().trim().toLowerCase(java.util.Locale.ROOT);


        if (!newEmail.equalsIgnoreCase(current.getEmail()) && utilisateurService.emailExiste(newEmail)) {


            showAgentMessage("Cet email est dÃƒÆ’Ã‚Â©jÃƒÆ’Ã‚Â  utilisÃƒÆ’Ã‚Â© par un autre compte.", false);


            return;


        }


        Utilisateur updated = new Utilisateur();


        updated.setIdUser(current.getIdUser());


        updated.setPrenom(current.getPrenom());


        updated.setNom(newNom);


        updated.setEmail(newEmail);


        updated.setRole(current.getRole());


        updated.setAge(current.getAge());


        updated.setLocalite(current.getLocalite());


        updated.setPhotoProfil(current.getPhotoProfil());


        updated.setIdZone(current.getIdZone());


        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {


            @Override protected Boolean call() {


                return utilisateurService.updateUtilisateur(updated);


            }


        };


        task.setOnSucceeded(e -> {


            if (Boolean.TRUE.equals(task.getValue())) {


                current.setNom(newNom);


                current.setEmail(newEmail);


                agentNameLabel.setText(newNom);


                if (agentAvatarLabel != null)


                    agentAvatarLabel.setText(newNom.substring(0, 1).toUpperCase(java.util.Locale.ROOT));


                if (agentProfilAvatarLabel != null)


                    agentProfilAvatarLabel.setText(newNom.substring(0, 1).toUpperCase(java.util.Locale.ROOT));


                if (agentProfilNomDisplay != null)


                    agentProfilNomDisplay.setText(newNom);


                showAgentMessage("Profil mis ÃƒÆ’Ã‚Â  jour.", true);


            } else {


                showAgentMessage("ÃƒÆ’Ã¢â‚¬Â°chec de mise ÃƒÆ’Ã‚Â  jour du profil.", false);


            }


        });


        task.setOnFailed(e -> showAgentMessage("ÃƒÆ’Ã¢â‚¬Â°chec de mise ÃƒÆ’Ã‚Â  jour du profil.", false));


        Thread t = new Thread(task, "profil-update"); t.setDaemon(true); t.start();


    }





    @FXML


    private void handleModifierMotPasse() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null)


            return;


        Dialog<String> dialog = new Dialog<>();


        dialog.setTitle("Mot de passe");


        dialog.setHeaderText("Choisissez un nouveau mot de passe");


        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);


        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);


        PasswordField passwordField = new PasswordField();


        passwordField.setPromptText("Nouveau mot de passe");


        PasswordField confirmField = new PasswordField();


        confirmField.setPromptText("Confirmer le mot de passe");


        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();


        grid.setHgap(10);


        grid.setVgap(10);


        grid.add(new Label("Nouveau mot de passe"), 0, 0);


        grid.add(passwordField, 1, 0);


        grid.add(new Label("Confirmation"), 0, 1);


        grid.add(confirmField, 1, 1);


        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(buttonType -> buttonType == saveType ? passwordField.getText() : null);


        Optional<String> result = dialog.showAndWait();


        if (result.isEmpty() || result.get().isBlank())


            return;


        if (!passwordField.getText().equals(confirmField.getText())) {


            showAgentMessage("Les mots de passe ne correspondent pas.", false);


            return;


        }


        com.smartcity.utils.ValidationUtils.ValidationResult check =


            com.smartcity.utils.ValidationUtils.validatePassword(result.get());


        if (!check.isValid()) {


            showAgentMessage(check.getMessage(), false);


            return;


        }


        if (utilisateurService.updateMotDePasse(current.getIdUser(), result.get())) {


            showAgentMessage("Mot de passe mis ÃƒÆ’Ã‚Â  jour.", true);


        } else {


            showAgentMessage("ÃƒÆ’Ã¢â‚¬Â°chec de mise ÃƒÆ’Ã‚Â  jour du mot de passe.", false);


        }


    }





    // ========================= MÃƒÆ’Ã¢â‚¬Â°THODES PRIVÃƒÆ’Ã¢â‚¬Â°ES =========================


    private void showPage(javafx.scene.Node pageToShow, Button activeButton) {


        javafx.scene.Node[] pages = { pageAgentDashboard, pageMesMissions, pageCarteZonesScroll, pageHistorique, pageMonProfil };


        for (javafx.scene.Node page : pages) {


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


        } else if (pageToShow == pageCarteZonesScroll) {


            refreshTourneeMap();


        }


    }





    private void refreshCards() {


        int total    = missions.size();


        int attente  = (int) missions.stream().filter(s -> "En attente".equalsIgnoreCase(s.getStatut())).count();


        int enCours  = (int) missions.stream().filter(s -> "En cours".equalsIgnoreCase(s.getStatut())).count();


        int terminees = (int) missions.stream().filter(this::isTermine).count();


        agentCardTotal.setText(String.valueOf(total));


        agentCardEnAttente.setText(String.valueOf(attente));


        agentCardEnCours.setText(String.valueOf(enCours));


        agentCardTerminees.setText(String.valueOf(terminees));


        // Score de performance agent


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        // Score affichÃƒÆ’Ã‚Â© uniquement dans les cards, pas dans le message flottant


        updateMissionsCount();


    }





    private void refreshTourneeSummary() {


        List<RealTimeGPSService.MissionWithDistance> activeDistances = gpsService.calculateMissionsDistances().stream()


            .filter(m -> !isTermine(m.mission))


            .collect(Collectors.toList());





        if (activeDistances.isEmpty()) {


            if (tourneeDistanceLabel != null) tourneeDistanceLabel.setText("0 km");


            if (tourneeTempsLabel != null) tourneeTempsLabel.setText("0 min");


            if (tourneeProchaineLabel != null) tourneeProchaineLabel.setText("Aucune");


            return;


        }





        double totalDistance = activeDistances.stream().mapToDouble(m -> m.distanceKm).sum();


        int totalMinutes = activeDistances.stream().mapToInt(m -> m.estimatedMinutes).sum();


        RealTimeGPSService.MissionWithDistance next = activeDistances.get(0);





        if (tourneeDistanceLabel != null) {


            tourneeDistanceLabel.setText(String.format(Locale.US, "%.1f km", totalDistance));


        }


        if (tourneeTempsLabel != null) {


            tourneeTempsLabel.setText(totalMinutes + " min");


        }


        if (tourneeProchaineLabel != null) {


            tourneeProchaineLabel.setText("#" + next.mission.getIdSignalement());


        }


    }





    private void refreshHistorique() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        LocalDate date = historiqueDatePicker != null ? historiqueDatePicker.getValue() : null;


        if (date == null) {


            historique.setAll(missions.stream().filter(this::isTermine).collect(Collectors.toList()));


        } else {


            historique.setAll(


                    affectationService.getSignalementsByAgent(current.getIdUser())


                            .stream()


                            .filter(this::isTermine)


                            .filter(s -> s.getDateCollecte() != null && date.equals(s.getDateCollecte().toLocalDate()))


                            .collect(Collectors.toList()));


        }


        if (lblHistoriqueCount != null)


            lblHistoriqueCount.setText(historique.size() + " mission" + (historique.size() > 1 ? "s" : ""));


    }





    private void refreshProfil() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        profilNomField.setText(current.getNom());


        profilEmailField.setText(current.getEmail());


        if (agentProfilAvatarLabel != null)


            agentProfilAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));


        if (agentProfilNomDisplay != null)


            agentProfilNomDisplay.setText(current.getNom());


        try {


            com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());


            profilZoneField.setText(zone != null ? zone.getNomZone() : "");


        } catch (Exception e) {


            logger.warn("Zone introuvable pour l'agent id={}", current.getIdZone());


            profilZoneField.setText("");


        }


        // Stats de performance async


        int idAgent = current.getIdUser();


        javafx.concurrent.Task<int[]> statsTask = new javafx.concurrent.Task<>() {


            @Override protected int[] call() {


                int traites = signalementService.countTraitesByAgent(idAgent);


                int actives = signalementService.countMissionsActivesParAgent(idAgent);


                return new int[]{traites, actives};


            }


        };


        statsTask.setOnSucceeded(e -> {


            int[] d = statsTask.getValue();


            int traites = d[0], actives = d[1];


            int total = traites + actives;


            if (profilStatMissionsLabel != null)


                profilStatMissionsLabel.setText(String.valueOf(traites));


            if (profilStatScoreLabel != null)


                profilStatScoreLabel.setText(total > 0


                    ? (int) Math.round(traites * 100.0 / total) + "%"


                    : "-");


        });


        Thread t = new Thread(statsTask, "profil-stats"); t.setDaemon(true); t.start();


    }





    private void configureDashboardTable() {


        colUrgId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));


        colUrgZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));


        colUrgAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));


        colUrgType.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        colUrgDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));


        colUrgStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));


        setCommonColumnStyles(colUrgId, colUrgZone, colUrgAdresse, colUrgType, colUrgDate, colUrgStatut);


        applyStatutColorCell(colUrgStatut);


        // Ligne rouge si urgente (En attente > 24h)


        tableMissionsUrgentes.setRowFactory(tv -> new javafx.scene.control.TableRow<Signalement>() {


            @Override


            protected void updateItem(Signalement item, boolean empty) {


                super.updateItem(item, empty);


                if (empty || item == null) {


                    setStyle("");


                    return;


                }


                boolean urgente = "En attente".equalsIgnoreCase(item.getStatut())


                    && item.getDateSignalement() != null


                    && item.getDateSignalement().isBefore(java.time.LocalDateTime.now().minusHours(24));


                setStyle(urgente ? "-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; -fx-border-width: 0 0 0 3;" : "");


            }


        });


    }





    private void configureMissionsTable() {


        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));


        colMissionZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));


        colMissionAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));


        colMissionType.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        colMissionDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));


        colMissionStatut


                .setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));


        // Colonne distance GPS : calculÃƒÆ’Ã‚Â©e en temps rÃƒÆ’Ã‚Â©el depuis la position agent


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

        tableMesMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateMapDetails(newVal);
            updateDistanceAndTime();
        });





        colMissionAction.setCellFactory(col -> new TableCell<Signalement, Void>() {


            private final Button startButton = new Button("ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ DÃƒÆ’Ã‚Â©marrer");


            private final Button doneButton = new Button("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ TerminÃƒÆ’Ã‚Â©");


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


                    startButton.setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Å“ Fini");


                    startButton


                            .setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ FAIT");


                    doneButton


                            .setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


                } else if (enCours) {


                    startButton.setText("ÃƒÂ¢Ã‚ÂÃ‚Â³ En cours");


                    startButton


                            .setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ FINIR");


                    doneButton.setStyle(


                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #4CAF50, 5, 0, 0, 0);");


                } else {


                    startButton.setText("ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ DÃƒÆ’Ã‚Â©marrer");


                    startButton


                            .setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("ÃƒÂ¢Ã‚ÂÃ‚Â¸ Attendre");


                    doneButton


                            .setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 8;");


                }


                startButton.setDisable(terminee);


                // FUNC-04: l'agent doit avoir demarre (En cours) pour pouvoir terminer


                doneButton.setDisable(terminee || !enCours);


                setGraphic(box);


            }


        });


    }





    private void configureHistoriqueTable() {


        colHistId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));


        colHistZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));


        colHistAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));


        colHistType.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(formatHistoriqueDate(cell.getValue())));


        colHistStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));


        // Colonne DurÃƒÆ’Ã‚Â©e : dateSignalement ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ dateCollecte


        if (colHistDuree != null) {


            colHistDuree.setCellValueFactory(cell -> {


                Signalement s = cell.getValue();


                if (s.getDateSignalement() == null || s.getDateCollecte() == null)


                    return new SimpleStringProperty("-");


                long heures = java.time.Duration.between(s.getDateSignalement(), s.getDateCollecte()).toHours();


                if (heures < 1) return new SimpleStringProperty("< 1h");


                if (heures < 24) return new SimpleStringProperty(heures + "h");


                return new SimpleStringProperty((heures / 24) + "j " + (heures % 24) + "h");


            });


            colHistDuree.setStyle("-fx-alignment: CENTER;");


        }


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


                else if ("Termin\u00e9".equalsIgnoreCase(item) || "Termine".equalsIgnoreCase(item))


                    color = "#2E7D32";


                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");


            }


        });


    }





    private void demarrerMissionAvecAnimation(Signalement mission, Button button) {


        if (mission == null || isTermine(mission)) return;


        animateButton(button, 1.2, true);


        button.setText("DÃƒÆ’Ã‚Â©marrage...");


        button.setDisable(true);


        int idMission = mission.getIdSignalement();


        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {


            @Override protected Boolean call() {


                return signalementService.updateStatut(idMission, "En cours");


            }


        };


        task.setOnSucceeded(e -> {


            if (Boolean.TRUE.equals(task.getValue())) {


                showAgentMessage("Mission #" + idMission + " dÃƒÆ’Ã‚Â©marrÃƒÆ’Ã‚Â©e !", true);


                rechargerEtReselectionner(idMission);


            } else {


                button.setText("ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ DÃƒÆ’Ã‚Â©marrer");


                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


                button.setDisable(false);


                showAgentMessage("Erreur lors du dÃƒÆ’Ã‚Â©marrage.", false);


            }


        });


        task.setOnFailed(e -> {


            button.setText("ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ DÃƒÆ’Ã‚Â©marrer");


            button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


            button.setDisable(false);


            showAgentMessage("Erreur lors du dÃƒÆ’Ã‚Â©marrage.", false);


        });


        Thread t = new Thread(task, "mission-start"); t.setDaemon(true); t.start();


    }





    private void marquerTermineAvecAnimation(Signalement mission, Button button) {


        if (mission == null || isTermine(mission)) return;


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);


        alert.setTitle("Confirmer Mission TerminÃƒÆ’Ã‚Â©e");


        alert.setHeaderText("Mission #" + mission.getIdSignalement());


        alert.setContentText("Confirmer que cette mission est TERMINÃƒÆ’Ã¢â‚¬Â°E ?\nZone: " + mission.getZoneNom()


                + "\nType: " + mission.getCategorie());


        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);


        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ TERMINÃƒÆ’Ã¢â‚¬Â°");


        Optional<ButtonType> result = alert.showAndWait();


        if (result.isEmpty() || result.get() != ButtonType.YES) return;


        animateButton(button, 1.3, true);


        button.setText("ÃƒÂ¢Ã‚ÂÃ‚Â³ Finalisation...");


        button.setDisable(true);


        int idMission = mission.getIdSignalement();


        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {


            @Override protected Boolean call() {


                return signalementService.updateStatut(idMission, "TerminÃƒÆ’Ã‚Â©");


            }


        };


        task.setOnSucceeded(e -> {


            if (Boolean.TRUE.equals(task.getValue())) {


                showAgentMessage("ÃƒÂ°Ã…Â¸Ã…Â½Ã¢â‚¬Â° Mission #" + idMission + " terminÃƒÆ’Ã‚Â©e ! Excellent travail !", true);


                rechargerEtReselectionner(idMission);


            } else {


                button.setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Terminer");


                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


                button.setDisable(false);


                showAgentMessage("Erreur lors de la finalisation.", false);


            }


        });


        task.setOnFailed(e -> {


            button.setText("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Terminer");


            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


            button.setDisable(false);


            showAgentMessage("Erreur lors de la finalisation.", false);


        });


        Thread t = new Thread(task, "mission-done"); t.setDaemon(true); t.start();


    }





    /** Recharge les missions et resÃƒÆ’Ã‚Â©lectionne la ligne par id aprÃƒÆ’Ã‚Â¨s une action. */


    private void rechargerEtReselectionner(int idMission) {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        int idAgent = current.getIdUser();


        javafx.concurrent.Task<List<Signalement>> task = new javafx.concurrent.Task<>() {


            @Override protected List<Signalement> call() {


                return affectationService.getSignalementsByAgent(idAgent);


            }


        };


        task.setOnSucceeded(e -> {


            List<Signalement> missionList = task.getValue();


            missions.setAll(missionList);


            java.time.LocalDateTime seuil24h = java.time.LocalDateTime.now().minusHours(24);


            missionsUrgentes.setAll(missionList.stream()


                .filter(s -> !isTermine(s))


                .collect(Collectors.toList()));


            refreshCards();


            refreshTourneeSummary();


            gpsService.updateMissions(missionList);


            missionList.stream()


                .filter(m -> m.getIdSignalement() == idMission)


                .findFirst()


                .ifPresent(m -> {


                    tableMesMissions.getSelectionModel().select(m);


                    tableMesMissions.scrollTo(m);


                });


        });


        task.setOnFailed(e -> logger.error("Erreur rechargement missions", task.getException()));


        Thread t = new Thread(task, "mission-reload"); t.setDaemon(true); t.start();


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


                showAgentMessage("ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ Mission #" + mission.getIdSignalement() + " dÃƒÆ’Ã‚Â©marrÃƒÆ’Ã‚Â©e automatiquement!", true);


                chargerDonnees();


                tableMesMissions.getSelectionModel().select(mission);


                showPage(pageMesMissions, btnMesMissions);


            } else {


                showAgentMessage("ÃƒÂ¢Ã‚ÂÃ…â€™ Erreur lors du dÃƒÆ’Ã‚Â©marrage automatique.", false);


            }


        } else {


            showAgentMessage("ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¹ÃƒÂ¯Ã‚Â¸Ã‚Â Aucune mission en attente ÃƒÆ’Ã‚Â  dÃƒÆ’Ã‚Â©marrer.", false);


        }


    }





    @FXML


    private void handleVoirMissionsEnCours() {


        List<Signalement> enCours = missions.stream().filter(s -> "En cours".equalsIgnoreCase(s.getStatut()))


                .collect(Collectors.toList());


        if (enCours.isEmpty()) {


            showAgentMessage("ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¹ÃƒÂ¯Ã‚Â¸Ã‚Â Aucune mission actuellement en cours.", false);


        } else {


            showPage(pageMesMissions, btnMesMissions);


            tableMesMissions.getSelectionModel().select(enCours.get(0));


            showAgentMessage("ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã¢â‚¬Â¹ " + enCours.size() + " mission(s) en cours affichÃƒÆ’Ã‚Â©e(s).", true);


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
        if (mapCommentaireLabel != null) {
            String commentaire = affectationService.getCommentaireBySignalement(mission.getIdSignalement());
            mapCommentaireLabel.setText(commentaire != null && !commentaire.isBlank() ? commentaire : "-");
        }
    }





    private void refreshMap(Signalement selectedMission, boolean showRoute) {
        if (mapWebView == null) return;
        installMapFallbackHandlers(mapWebView.getEngine(), "Carte agent indisponible.");
        String html = buildLeafletHtml(selectedMission, showRoute);
        // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Charger SEULEMENT la premiÃƒÆ’Ã‚Â¨re fois
        if (mapWebView.getEngine().getLocation() == null || mapWebView.getEngine().getLocation().isEmpty()) {
            mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        netscape.javascript.JSObject win = (netscape.javascript.JSObject)
                            mapWebView.getEngine().executeScript("window");
                        win.setMember("javaAgent", new JSBridgeAgent());
                    } catch (Exception e) {
                        logger.warn("Erreur bridge carte GPS", e);
                    }
                }
            });
            mapWebView.getEngine().loadContent(html);
        }
    }





    @FXML


    private void handleDefinirMaPosition() {


        if (mapWebView == null)


            return;


        showPage(pageCarteZonesScroll, btnCarteZones);


        showAgentMessage("ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â Cliquez sur la carte pour dÃƒÆ’Ã‚Â©finir votre position exacte", true);


        // Activer le mode sÃƒÆ’Ã‚Â©lection de position dans la carte


        mapWebView.getEngine().executeScript("if(typeof activerModePosition==='function')activerModePosition()");


    }





    /** Pont JavaScript ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Java pour recevoir la position cliquÃƒÆ’Ã‚Â©e sur la carte */


    public class JSBridgeAgent {


        public void setAgentPosition(double lat, double lon) {


            Platform.runLater(() -> {


                gpsService.setManualPosition(lat, lon);


                if (lblPositionActuelle != null) {


                    lblPositionActuelle.setText(String.format("ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â %.5f, %.5f (Manuel)", lat, lon));


                }


                showAgentMessage(String.format("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ Position dÃƒÆ’Ã‚Â©finie: %.4f, %.4f", lat, lon), true);


                updateDistanceAndTime();


            });


        }

        public void selectMission(int id) {
            Platform.runLater(() -> {
                missions.stream()
                    .filter(m -> m.getIdSignalement() == id)
                    .findFirst()
                    .ifPresent(m -> {
                        tableMesMissions.getSelectionModel().select(m);
                        updateMapDetails(m);
                        updateDistanceAndTime();
                    });
            });
        }


    }





    private void refreshMapWithOptimizedRoute(List<Signalement> optimizedRoute) {


        if (mapWebView == null)


            return;

        // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ SI PREMIÃƒÆ’Ã‹â€ RE FOIS: charger l'HTML complet
        if (mapWebView.getEngine().getLocation() == null || mapWebView.getEngine().getLocation().isEmpty()) {
            installMapFallbackHandlers(mapWebView.getEngine(), "Itineraire indisponible. Les donnees restent accessibles dans la liste des missions.");
            String html = buildLeafletHtml(null, false);
            mapWebView.getEngine().loadContent(html);
            return; // Attendre que la carte soit chargÃƒÆ’Ã‚Â©e
        }

        // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ MISE ÃƒÆ’Ã¢â€šÂ¬ JOUR DYNAMIQUE VIA executeScript (pas de clignotement)
        try {
            // Nettoyer anciens marqueurs
            mapWebView.getEngine().executeScript("if (typeof window.clearMissions === 'function') window.clearMissions();");

            // Ajouter nouvelles missions
            for (Signalement m : optimizedRoute) {
                Point p = missionPoint(m, indexOfMission(m));
                String markerColor = statusColorHex(getDisplayStatut(m.getStatut()));
                String popup = "Mission #" + m.getIdSignalement() + " - " + getDisplayStatut(m.getStatut());
                String jsAddMarker = "L.circleMarker([" + fmt(p.lat) + "," + fmt(p.lon) + "],{radius:9,color:'" + markerColor + 
                    "',fillColor:'" + markerColor + "',fillOpacity:0.88}).addTo(map).bindPopup('" + popup.replace("'", "\\'") + "');";
                mapWebView.getEngine().executeScript(jsAddMarker);
            }

            // Tracer la route dynamiquement
            if (!optimizedRoute.isEmpty()) {
                GeolocationService.Coordinates startPoint = getTourneeStartPoint();
                StringBuilder routePoints = new StringBuilder("[");
                routePoints.append(String.format("[%.6f,%.6f]", startPoint.lat, startPoint.lon));
                for (Signalement m : optimizedRoute) {
                    Point p = missionPoint(m, indexOfMission(m));
                    routePoints.append(String.format(",[%.6f,%.6f]", p.lat, p.lon));
                }
                routePoints.append("]");
                String routeLine = "L.polyline(" + routePoints + ",{color:'#FF5722',weight:4,dashArray:'10 5',opacity:0.8}).addTo(map);";
                mapWebView.getEngine().executeScript(routeLine);
            }
        } catch (Exception e) {
            logger.error("ÃƒÂ¢Ã‚ÂÃ…â€™ Erreur mise ÃƒÆ’Ã‚Â  jour carte temps-rÃƒÆ’Ã‚Â©el", e);
            // Fallback: recharger complÃƒÆ’Ã‚Â¨tement si executeScript ÃƒÆ’Ã‚Â©choue
            installMapFallbackHandlers(mapWebView.getEngine(), "Itineraire indisponible.");
            String html = buildLeafletHtml(null, false);
            mapWebView.getEngine().loadContent(html);
        }


    }





    private void installMapFallbackHandlers(WebEngine engine, String message) {


        if (engine == null || mapFallbackHandlersInstalled) {


            return;


        }


        mapFallbackHandlersInstalled = true;


        engine.getLoadWorker().exceptionProperty().addListener((obs, oldValue, error) -> {


            if (error != null) {


                showAgentMessage(message, false);


                engine.loadContent(buildMapFallbackHtml(message));


            }


        });


        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {


            if (newState == javafx.concurrent.Worker.State.FAILED) {


                showAgentMessage(message, false);


                engine.loadContent(buildMapFallbackHtml(message));


            }


        });


    }





    private String buildMapFallbackHtml(String message) {


        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"


            + "body{font-family:Arial,sans-serif;background:#f6f8fb;color:#1f2937;display:flex;"


            + "align-items:center;justify-content:center;height:100%;margin:0;padding:24px;text-align:center;}"


            + ".card{max-width:440px;background:white;border-radius:16px;padding:24px;"


            + "box-shadow:0 8px 24px rgba(15,23,42,0.12);}"


            + ".title{font-size:18px;font-weight:700;margin-bottom:8px;color:#b45309;}"


            + "</style></head><body><div class='card'><div class='title'>Carte indisponible</div><div>"


            + message + "</div></div></body></html>";


    }





    /** ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ INJECTER LE JS EMBARQUÃƒÆ’Ã¢â‚¬Â° AU LIEU D'UNE URL EXTERNE */
    private static String getSmartGpsMapJsInline() {
        return SMART_GPS_MAP_JS != null ? SMART_GPS_MAP_JS : "";
    }

    /** ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ CRÃƒÆ’Ã¢â‚¬Â°ER LE CLIENT WEBSOCKET POUR MISES ÃƒÆ’Ã¢â€šÂ¬ JOUR TEMPS RÃƒÆ’Ã¢â‚¬Â°EL */
    private static String getWebSocketClientJs() {
        return """
            // ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ…â€™ WebSocket Client - Mises ÃƒÆ’Ã‚Â  jour temps rÃƒÆ’Ã‚Â©el des missions et positions
            let ws = null;
            let wsConnectAttempts = 0;
            
            function connectWebSocket() {
                try {
                    let protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
                    let wsUrl = protocol + '//' + location.hostname + ':' + (window._wsPort || 8888) + '/ws/agent-missions';
                    ws = new WebSocket(wsUrl);
                    
                    ws.onopen = function() {
                        console.log('ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ WebSocket connectÃƒÆ’Ã‚Â© pour mises ÃƒÆ’Ã‚Â  jour temps rÃƒÆ’Ã‚Â©el');
                        wsConnectAttempts = 0; // Reset counter on success
                    };
                    
                    ws.onmessage = function(event) {
                        try {
                            let data = JSON.parse(event.data);
                            
                            // Position d'agent en temps rÃƒÆ’Ã‚Â©el
                            if (data.type === 'position' && data.lat && data.lon) {
                                console.log('ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‚Â Position agent reÃƒÆ’Ã‚Â§ue:', data.lat, data.lon);
                                if (typeof updateAgentPosition === 'function') {
                                    updateAgentPosition(data.lat, data.lon);
                                }
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                            
                            // Nouvelle mission
                            if (data.type === 'mission') {
                                console.log('ÃƒÂ°Ã…Â¸Ã…Â½Ã‚Â¯ Nouvelle mission reÃƒÆ’Ã‚Â§ue');
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                        } catch(parseErr) {
                            console.debug('WebSocket message parse error (non-JSON):', event.data);
                        }
                    };
                    
                    ws.onerror = function(err) {
                        console.error('ÃƒÂ¢Ã‚ÂÃ…â€™ WebSocket error:', err);
                    };
                    
                    ws.onclose = function() {
                        console.warn('ÃƒÂ¢Ã…Â¡Ã‚Â ÃƒÂ¯Ã‚Â¸Ã‚Â WebSocket fermÃƒÆ’Ã‚Â©');
                        // Reconnexion aprÃƒÆ’Ã‚Â¨s dÃƒÆ’Ã‚Â©lai
                        wsConnectAttempts++;
                        let delay = Math.min(300000, 5000 * Math.pow(1.5, wsConnectAttempts));
                        console.log('ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ¢â‚¬Å¾ Reconnexion dans', Math.round(delay/1000), 's');
                        setTimeout(connectWebSocket, delay);
                    };
                } catch(err) {
                    console.error('WebSocket setup error:', err);
                    setTimeout(connectWebSocket, 5000);
                }
            }
            
            // Connecter au chargement de la page
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', connectWebSocket);
            } else {
                connectWebSocket();
            }
            """;
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


                    .append("',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('").append(popup)
                    .append("').on('click',function(){if(window.javaAgent)window.javaAgent.selectMission(")
                    .append(mission.getIdSignalement())
                    .append(");});");


        }





        // Marqueur position agent actuelle - sera ajoutÃƒÆ’Ã‚Â© via agentPosJs plus bas


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


                "</script>" +

                "<script>window._wsPort=" + com.smartcity.config.GeoConfig.getWebSocketPort() + ";</script>" +
                "<script>" + getSmartGpsMapJsInline() + "</script>" +
                "<script>" + getWebSocketClientJs() + "</script>" +

                "</body></html>";




    }


    private int indexOfMission(Signalement mission) {


        for (int i = 0; i < missions.size(); i++)


            if (missions.get(i).getIdSignalement() == mission.getIdSignalement())


                return i;


        return 0;


    }





    private Point missionPoint(Signalement mission, int index) {
        // ÃƒÂ°Ã…Â¸Ã…Â½Ã‚Â¯ UTILISER D'ABORD LES VRAIES COORDONNÃƒÆ’Ã¢â‚¬Â°ES GPS SI DISPONIBLES
        if (mission.getLatitude() != 0.0 || mission.getLongitude() != 0.0) {
            return new Point(mission.getLatitude(), mission.getLongitude());
        }
        
        // SINON: Placer autour du centre de zone avec offset pour ÃƒÆ’Ã‚Â©viter superposition
        Point base = zoneCenter(mission.getZoneNom());
        double latOffset = (index % 5) * 0.004 + 0.001;  // Ãƒâ€šÃ‚Â±0.004Ãƒâ€šÃ‚Â° = ~400m
        double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
        return new Point(base.lat + latOffset, base.lon + lonOffset);
    }





    private Point zoneCenter(String zone) {
        // ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ DÃƒÆ’Ã¢â‚¬Â°LÃƒÆ’Ã¢â‚¬Â°GUER ENTIÃƒÆ’Ã‹â€ REMENT AU SERVICE (Pas de hardcode)
        GeolocationService.Coordinates c = zoneService.getCenter(zone);
        return new Point(c.lat, c.lon);
    }





    private String getAgentZone() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return "Pikine";


        try {


            com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());


            return zone != null ? zone.getNomZone() : "Pikine";


        } catch (Exception e) {


            return "Pikine";


        }


    }





    private GeolocationService.Coordinates getTourneeStartPoint() {


        RealTimeGPSService.Coordinates currentPosition = gpsService.getCurrentPosition();


        if (currentPosition != null) {


            return new GeolocationService.Coordinates(currentPosition.lat, currentPosition.lon);


        }


        return geolocationService.getZoneCenter(getAgentZone());


    }





    private List<Signalement> getActiveMissions() {


        return missions.stream()


            .filter(s -> !isTermine(s))


            .collect(Collectors.toList());


    }





    private void refreshTourneeMap() {


        List<Signalement> activeMissions = getActiveMissions();


        if (activeMissions.isEmpty()) {


            refreshMap(tableMesMissions.getSelectionModel().getSelectedItem(), false);


            return;


        }


        GeolocationService.Coordinates startPoint = getTourneeStartPoint();


        List<Signalement> optimizedRoute = geolocationService.optimizeCollectionRoute(activeMissions, startPoint);


        refreshMapWithOptimizedRoute(optimizedRoute);


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


        return (signalement.getDescription() == null || signalement.getDescription().isBlank()) ? "Adresse non prÃƒÆ’Ã‚Â©cisÃƒÆ’Ã‚Â©e"


                : signalement.getDescription();


    }





    private String formatDate(Signalement signalement) {


        return signalement.getDateSignalement() == null ? "" : signalement.getDateSignalement().format(DATE_FORMATTER);


    }





    private String formatHistoriqueDate(Signalement signalement) {


        if (signalement == null) {


            return "";


        }


        if (signalement.getDateCollecte() != null) {


            return signalement.getDateCollecte().format(DATE_FORMATTER);


        }


        return formatDate(signalement);


    }





    private boolean isTermine(Signalement signalement) {


        return signalement != null && SignalementStatut.isCompleted(signalement.getStatut());


    }





    private String getDisplayStatut(String statut) {


        return SignalementStatut.toLabel(statut, statut == null ? "" : statut);


    }





    private void showAgentDashboardPage() {


        showPage(pageAgentDashboard, btnAgentDashboard);


    }





    private void installTooltips() {


        btnAgentDashboard.setTooltip(new Tooltip("Vue globale des missions du jour"));


        btnMesMissions.setTooltip(new Tooltip("Missions assignÃƒÆ’Ã‚Â©es et actions"));


        btnCarteZones.setTooltip(new Tooltip("Carte interactive des zones"));


        btnHistorique.setTooltip(new Tooltip("Historique des missions terminÃƒÆ’Ã‚Â©es"));


        btnMonProfil.setTooltip(new Tooltip("Informations du compte agent"));


    }





    private void showAgentMessage(String message, boolean success) {


        String emoji = success ? "ÃƒÂ°Ã…Â¸Ã…Â½Ã¢â‚¬Â°" : "ÃƒÂ¢Ã…Â¡Ã‚Â ÃƒÂ¯Ã‚Â¸Ã‚Â";


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


    @FXML


    private void handleToggleGps() {


        RealTimeGPSService.Coordinates pos = gpsService.getCurrentPosition();


        if (pos != null) {


            showAgentMessage(String.format("\ud83d\udccd GPS actif ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â position : %.4f, %.4f", pos.lat, pos.lon), true);


        } else {


            showAgentMessage("\ud83d\udccd GPS actif ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â en attente de position...", true);


        }


        if (lblPositionActuelle != null && pos != null) {


            lblPositionActuelle.setText(String.format("\ud83d\udccd %.4f, %.4f (Live)", pos.lat, pos.lon));


        }


    }





    @FXML


    private void handleCopierUrlGps() {


        if (qrUrlLabel == null || qrUrlLabel.getText() == null || qrUrlLabel.getText().isEmpty()) {


            showAgentMessage("ÃƒÂ¢Ã‚ÂÃ…â€™ URL GPS non disponible", false);


            return;


        }


        


        String urlGps = qrUrlLabel.getText();


        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();


        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();


        content.putString(urlGps);


        clipboard.setContent(content);


        


        showAgentMessage("ÃƒÂ¢Ã…â€œÃ¢â‚¬Â¦ URL GPS copiÃƒÆ’Ã‚Â©e dans le presse-papiers: " + urlGps, true);


    }


}


