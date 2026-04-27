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
import javafx.beans.property.SimpleObjectProperty;
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


    private TextField profilNomField, profilEmailField;
    @FXML private Label profilZoneField;


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

    @FXML private javafx.scene.control.TextArea mapCommentaireField;
    @FXML private Label histStatTerminees;
    @FXML private Label profilStatNoteLabel;
    @FXML private Label profilDateInscriptionLabel;
    @FXML private Label histStatTaux;
    @FXML private Label histStatTemps;
    @FXML private Label histStatNote;
    @FXML private Button btnTerminerDepuisCarte;





    // ========================= SERVICES =========================


    private GeolocationService geolocationService = new GeolocationService();


    private RealTimeGPSService gpsService = new RealTimeGPSService();


    private SignalementService signalementService = new SignalementService();


    private UtilisateurService utilisateurService = new UtilisateurService();


    private ZoneService zoneService = new ZoneService();


    private com.smartcity.service.AffectationService affectationService = new com.smartcity.service.AffectationService();
    private final com.smartcity.service.SuiviService suiviService = new com.smartcity.service.SuiviService();





    // ========================= DONNAAaAAAasAAAES =========================


    private ObservableList<Signalement> missions = FXCollections.observableArrayList();


    private ObservableList<Signalement> missionsUrgentes = FXCollections.observableArrayList();


    private ObservableList<Signalement> historique = FXCollections.observableArrayList();





    // ========================= AUTRES =========================


    private MainApp mainApp;


    private PauseTransition messageClearDelay = new PauseTransition(Duration.seconds(3));


    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter


            .ofPattern("dd/MM/yyyy");


    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AgentDashboardController.class);
    
    // AAAAaAaAAAAasAAA Contenu embarquAAaAasAA des ressources JS statiques
    private static String SMART_GPS_MAP_JS = null;
    
    static {
        loadEmbeddedResources();
    }
    
    private static void loadEmbeddedResources() {
        try {
            // Charger smart-gps-map.js une seule fois au dAAaAasAAmarrage
            try (var is = AgentDashboardController.class.getClassLoader().getResourceAsStream("js/smart-gps-map.js")) {
                if (is != null) {
                    SMART_GPS_MAP_JS = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    logger.info("AAAAaAaAAAAasAAA smart-gps-map.js chargAAaAasAA en mAAaAasAAmoire ({} bytes)", SMART_GPS_MAP_JS.length());
                } else {
                    logger.error("AAAAasAAAaAaa smart-gps-map.js not found in classpath");
                    SMART_GPS_MAP_JS = "";
                }
            }
        } catch (Exception e) {
            logger.error("AAAAasAAAaAaa Erreur chargement ressources embarquAAaAasAAes", e);
            SMART_GPS_MAP_JS = "";
        }
    }

    private String displayAgentName(Utilisateur user) {
        if (user == null) return "";
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        if (!prenom.isEmpty() && !nom.isEmpty()) return prenom + " " + nom;
        if (!nom.isEmpty()) return nom;
        if (!prenom.isEmpty()) return prenom;
        return user.getEmail() != null ? user.getEmail() : "";
    }

    private String agentInitial(Utilisateur user) {
        String displayName = displayAgentName(user).trim();
        return displayName.isEmpty() ? "A" : displayName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }


    private javafx.scene.media.AudioClip nearbyBeep;


    private javafx.animation.Timeline autoRefreshTimeline;


    private boolean mapFallbackHandlersInstalled;
    private boolean mapBridgeInstalled;
    private boolean mapNeedsRefresh = true; // true = carte doit etre rechargee
    private long lastNearbyAlertMs = 0;


    // Snapshot ids missions pour detecter les nouvelles affectations


    private final java.util.Set<Integer> missionsSnapshot = new java.util.HashSet<>();





    // ========================= MAAaAAAasAAATHODES PUBLIQUES =========================


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
            if (agentNameLabel != null)
                agentNameLabel.setText(displayAgentName(agentInit));
            if (agentAvatarLabel != null)
                agentAvatarLabel.setText(agentInitial(agentInit));
            if (agentProfilAvatarLabel != null)
                agentProfilAvatarLabel.setText(agentInitial(agentInit));
            if (agentProfilNomDisplay != null)
                agentProfilNomDisplay.setText(displayAgentName(agentInit));
        }

        tableMesMissions.setItems(missions);
        if (filterMissionsStatutCombo != null) {
            filterMissionsStatutCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Tous", "Affecte", "En cours", "Termine"));
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


        


        // Initialisation visibilitAAaAasAA pages


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


        // Son notification proximitAAaAasAA avec null-check


        java.net.URL beepUrl = getClass().getResource("/sounds/beep.mp3");


        if (beepUrl != null) {


            nearbyBeep = new javafx.scene.media.AudioClip(beepUrl.toExternalForm());


        } else {


            logger.warn("AAAAaAAAAAasAAAAaAA Son beep.mp3 non trouvAAaAasAA - notifications silencieuses");


        }





        // DAAaAasAAmarrer GPS tracking


        gpsService.startRealTimeTracking();
        if (lblPositionActuelle != null) lblPositionActuelle.setText("Position par defaut (Pikine) - scannez le QR code");
        if (lblGpsStatus != null) lblGpsStatus.setText("GPS en attente \u23F3");


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





        logger.info("GPS RealTime initialise");


    }





    private void startAutoRefresh() {


        autoRefreshTimeline = new javafx.animation.Timeline(


                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> chargerDonnees()));


        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);


        autoRefreshTimeline.play();


        logger.info("Auto-refresh 30s demarre");


    }





    private void updatePositionLabel(RealTimeGPSService.Coordinates pos) {


        Platform.runLater(() -> {


            if (lblPositionActuelle != null)


                lblPositionActuelle.setText(String.format("Position live: %.4f, %.4f", pos.lat, pos.lon));


            if (lblGpsStatus != null)


                lblGpsStatus.setText("GPS actif");


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





                // Update mission sAAaAasAAlectionnAAaAasAAe si correspond


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


        if (hasNearby) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastNearbyAlertMs > 60000L) {
                lastNearbyAlertMs = nowMs;
                if (nearbyBeep != null) nearbyBeep.play();
                showAgentMessage("Mission a proximite ! Verifiez la carte.", true);
            }
        }
    }




    @FXML


    public void chargerDonnees() {


        chargerDonneesFiltrees(null);


    }





    private void chargerDonneesFiltrees(java.time.LocalDate dateFiltree) {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        if (agentNameLabel != null) agentNameLabel.setText(displayAgentName(current));


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


                    showAgentMessage(nouvelles + " nouvelle" + (nouvelles > 1 ? "s" : "") + " mission" + (nouvelles > 1 ? "s" : "") + " assignAAaAasAAe" + (nouvelles > 1 ? "s" : "") + " !", true);


                    if (nearbyBeep != null) nearbyBeep.play();


                }


            }


            // Mettre a jour le snapshot


            missionsSnapshot.clear();


            missionList.forEach(s -> missionsSnapshot.add(s.getIdSignalement()));


            String currentFilter = (filterMissionsStatutCombo != null) ? filterMissionsStatutCombo.getValue() : "Tous";
            missions.setAll(missionList); mapNeedsRefresh = true;


            // Missions actives = tout ce qui n'est pas termine


            // Les urgentes (>24h en attente) sont mises en evidence visuellement


            missionsUrgentes.setAll(missionList.stream()


                    .filter(s -> !isTermine(s)
                        && s.getDateSignalement() != null
                        && s.getDateSignalement().isBefore(java.time.LocalDateTime.now().minusHours(24)))


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
        javafx.concurrent.Task<Double> noteTask = new javafx.concurrent.Task<>() {
            @Override protected Double call() { return suiviService.getNoteMoyenneParAgent(idAgent); }
        };
        noteTask.setOnSucceeded(ev -> {
            double note = noteTask.getValue();
            if (profilStatNoteLabel != null)
                profilStatNoteLabel.setText(note > 0 ? String.format(java.util.Locale.US, "%.1f/5 \u2605", note) : "-");
        });
        Thread nt = new Thread(noteTask, "profil-note"); nt.setDaemon(true); nt.start();


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


        if (filterMissionsStatutCombo != null) {
            filterMissionsStatutCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                    "Tous", "Affecte", "En cours", "Termine"));
            if (filterMissionsStatutCombo.getValue() == null
                    || "En attente".equalsIgnoreCase(filterMissionsStatutCombo.getValue())) {
                filterMissionsStatutCombo.setValue("Tous");
            }
        }
        showPage(pageMesMissions, btnMesMissions);


    }


    @FXML
    private void handleActualiserDashboard() {
        java.time.LocalDate date = dashboardDatePicker != null ? dashboardDatePicker.getValue() : null;
        chargerDonneesFiltrees(date);
    }





    @FXML


    private void handleShowCarteZones() {
        mapNeedsRefresh = true; // forcer rechargement a l'ouverture de la page
        if (tableMesMissions.getSelectionModel().getSelectedItem() == null && !missions.isEmpty()) {
            Signalement first = missions.stream().filter(s -> !isTermine(s)).findFirst().orElse(missions.get(0));
            tableMesMissions.getSelectionModel().select(first);
            updateMapDetails(first);
            updateDistanceAndTime();
        }


        showPage(pageCarteZonesScroll, btnCarteZones);


        refreshTourneeMap();


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        String gpsUrl = GpsApiServer.getGpsPageUrl(current.getIdUser());


        if (qrUrlLabelCarte != null) qrUrlLabelCarte.setText(gpsUrl);
        if (qrUrlLabel != null) qrUrlLabel.setText(gpsUrl); // sync barre rapide


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


            showAgentMessage("Aucune mission active pour optimiser l'itinAAaAasAAraire.", false);


            return;


        }





        GeolocationService.Coordinates startPoint = getTourneeStartPoint();


        List<Signalement> optimizedRoute = geolocationService.optimizeCollectionRoute(selectedMissions, startPoint);


        refreshMapWithOptimizedRoute(optimizedRoute);





        String routeUrl = geolocationService.getMultiPointRouteUrl(optimizedRoute);


        StringBuilder routeInfo = new StringBuilder();


        routeInfo.append("ITINERAIRE OPTIMISE (").append(optimizedRoute.size()).append(" missions)\n\n");


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


                    .append(String.format("   Distance: %.2f km\n", distance))
                    .append(String.format("   Zone: %s\n\n", mission.getZoneNom()));


            currentPos = new GeolocationService.Coordinates(mission.getLatitude(), mission.getLongitude());


        }


        routeInfo.append(String.format("Distance totale estimee: %.2f km\n", totalDistance))
                .append(String.format("Temps estime: %.0f minutes\n\n", totalDistance * 3))
                .append("Lien Google Maps:\n").append(routeUrl);





        Alert alert = new Alert(Alert.AlertType.INFORMATION);


        alert.setTitle("Itineraire optimise");


        alert.setHeaderText("Itineraire de collecte optimise");


        TextArea textArea = new TextArea(routeInfo.toString());


        textArea.setEditable(false);


        textArea.setWrapText(true);


        textArea.setPrefRowCount(15);


        textArea.setPrefColumnCount(50);


        alert.getDialogPane().setContent(textArea);


        alert.showAndWait();





        showAgentMessage(String.format("ItinAAaAasAAraire optimisAAaAasAA pour %d missions (%.1f km total).", optimizedRoute.size(),


                totalDistance), true);


    }





    @FXML


    private void handleFiltrerHistorique() {


        refreshHistorique();


        showAgentMessage("Historique filtrAAaAasAA appliquAAaAasAA.", true);


    }





    @FXML


    private void handleResetHistorique() {


        if (historiqueDatePicker != null) {


            historiqueDatePicker.setValue(null);


        }


        refreshHistorique();


        showAgentMessage("Historique rAAaAasAAinitialisAAaAasAA.", true);


    }





    @FXML
    private void handleResetFiltresMissions() {
        if (filterMissionsStatutCombo != null) {
            filterMissionsStatutCombo.setValue("Tous");
        }
        tableMesMissions.setItems(missions);
        showAgentMessage("Filtre r\u00e9initialis\u00e9 \u2014 toutes les missions affich\u00e9es.", true);
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


            showAgentMessage("Cet email est dAAaAasAAjAAaAasAA utilisAAaAasAA par un autre compte.", false);


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


                agentNameLabel.setText(displayAgentName(current));


                if (agentAvatarLabel != null)


                    agentAvatarLabel.setText(agentInitial(current));


                if (agentProfilAvatarLabel != null)


                    agentProfilAvatarLabel.setText(agentInitial(current));


                if (agentProfilNomDisplay != null)


                    agentProfilNomDisplay.setText(displayAgentName(current));


                showAgentMessage("Profil mis AAaAasAA jour.", true);


            } else {


                showAgentMessage("AAaAAAasAAAchec de mise AAaAasAA jour du profil.", false);


            }


        });


        task.setOnFailed(e -> showAgentMessage("AAaAAAasAAAchec de mise AAaAasAA jour du profil.", false));


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


            showAgentMessage("Mot de passe mis AAaAasAA jour.", true);


        } else {


            showAgentMessage("AAaAAAasAAAchec de mise AAaAasAA jour du mot de passe.", false);


        }


    }





    // ========================= MAAaAAAasAAATHODES PRIVAAaAAAasAAAES =========================


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


        int attente  = (int) missions.stream()
            .filter(s -> SignalementStatut.AFFECTE.matches(s.getStatut()))
            .count();


        int enCours  = (int) missions.stream()
            .filter(s -> SignalementStatut.EN_COURS.matches(s.getStatut()))
            .count();


        int terminees = (int) missions.stream().filter(this::isTermine).count();


        if (agentCardTotal != null) agentCardTotal.setText(String.valueOf(total));


        if (agentCardEnAttente != null) agentCardEnAttente.setText(String.valueOf(attente));


        if (agentCardEnCours != null) agentCardEnCours.setText(String.valueOf(enCours));


        if (agentCardTerminees != null) agentCardTerminees.setText(String.valueOf(terminees));


        // Score de performance agent


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        // Score affichAAaAasAA uniquement dans les cards, pas dans le message flottant


        updateMissionsCount();


    }





    private void refreshTourneeSummary() {


        List<RealTimeGPSService.MissionWithDistance> activeDistances = gpsService.calculateMissionsDistances().stream()


            .filter(m -> !isTermine(m.mission))


            .collect(Collectors.toList());





        if (activeDistances.isEmpty()) {


            if (tourneeDistanceLabel != null) tourneeDistanceLabel.setText("0 km");


            if (tourneeTempsLabel != null) tourneeTempsLabel.setText("0 min");


            if (tourneeProchaineLabel != null) tourneeProchaineLabel.setText(gpsService.getCurrentPosition() != null ? "Aucune" : "GPS requis \u26a0");


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


                            .filter(s -> {
                        java.time.LocalDate d = s.getDateCollecte() != null
                            ? s.getDateCollecte().toLocalDate()
                            : (s.getDateSignalement() != null ? s.getDateSignalement().toLocalDate() : null);
                        return date.equals(d);
                    })


                            .collect(Collectors.toList()));


        }


        if (lblHistoriqueCount != null)


            lblHistoriqueCount.setText(historique.size() + " mission" + (historique.size() > 1 ? "s" : ""));
        refreshHistoriqueStats();


    }





    private void refreshHistoriqueStats() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        int idAgent = current.getIdUser();
        javafx.concurrent.Task<double[]> task = new javafx.concurrent.Task<>() {
            @Override protected double[] call() {
                int traites = signalementService.countTraitesByAgent(idAgent);
                int actives = signalementService.countMissionsActivesParAgent(idAgent);
                int total = traites + actives;
                double taux = total > 0 ? traites * 100.0 / total : 0;
                double tempsH = signalementService.getTempsResolutionMoyenH();
                double note = suiviService.getNoteMoyenneParAgent(idAgent);
                return new double[]{traites, taux, tempsH, note};
            }
        };
        task.setOnSucceeded(e -> {
            double[] d = task.getValue();
            if (histStatTerminees != null) histStatTerminees.setText(String.valueOf((int) d[0]));
            if (histStatTaux != null) histStatTaux.setText(d[1] > 0 ? (int) d[1] + "%" : "-");
            if (histStatTemps != null) histStatTemps.setText(d[2] > 0 ? String.format(java.util.Locale.US, "%.1fh", d[2]) : "-");
            if (histStatNote != null) histStatNote.setText(d[3] > 0 ? String.format(java.util.Locale.US, "%.1f/5 \u2605", d[3]) : "-");
        });
        Thread t = new Thread(task, "hist-stats"); t.setDaemon(true); t.start();
    }
    private void refreshProfil() {


        Utilisateur current = SessionManager.getUtilisateurConnecte();


        if (current == null) return;


        profilNomField.setText(current.getNom());


        profilEmailField.setText(current.getEmail());


        if (agentProfilAvatarLabel != null)


            agentProfilAvatarLabel.setText(agentInitial(current));


        if (agentProfilNomDisplay != null)


            agentProfilNomDisplay.setText(displayAgentName(current));


        try {


            com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());


            profilZoneField.setText(zone != null ? zone.getNomZone() : "");
        if (profilDateInscriptionLabel != null && current.getDateInscription() != null) {
            profilDateInscriptionLabel.setText(current.getDateInscription().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }


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

        if (tableMissionsUrgentes != null) {
            // Evite la colonne vide visuelle a droite en occupant toute la largeur.
            tableMissionsUrgentes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }


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


                boolean urgente = (SignalementStatut.AFFECTE.matches(item.getStatut()) || "En attente".equalsIgnoreCase(item.getStatut()))


                    && item.getDateSignalement() != null


                    && item.getDateSignalement().isBefore(java.time.LocalDateTime.now().minusHours(24));


                setStyle(urgente ? "-fx-background-color: #FFF3E0; -fx-border-color: #FF9800; -fx-border-width: 0 0 0 3;" : "");


            }


        });


    }





    private void configureMissionsTable() {

        if (tableMesMissions != null) {
            tableMesMissions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }


        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));


        colMissionZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));


        colMissionAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));


        colMissionType.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        colMissionDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));


        colMissionStatut


                .setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));


        // Colonne distance GPS : calculAAaAasAAe en temps rAAaAasAAel depuis la position agent


        colMissionDistance.setCellValueFactory(cell -> {


            RealTimeGPSService.Coordinates pos = gpsService.getCurrentPosition();


            if (pos == null) {
                return new SimpleStringProperty("GPS requis");
            }
            if (cell.getValue().getLatitude() == 0.0 && cell.getValue().getLongitude() == 0.0) {
                return new SimpleStringProperty("Coord. manquantes");
            }


            double d = geolocationService.calculateDistance(pos.lat, pos.lon,


                    cell.getValue().getLatitude(), cell.getValue().getLongitude());
            // Indiquer approximation si position par defaut (Pikine)
            boolean isDefaultPos = Math.abs(pos.lat - 14.7646) < 0.001 && Math.abs(pos.lon - (-17.3920)) < 0.001;
            return new SimpleStringProperty(isDefaultPos ? String.format("~%.1f km", d) : String.format("%.2f km", d));


        });


        setCommonColumnStyles(colMissionId, colMissionZone, colMissionAdresse, colMissionType, colMissionDate,


                colMissionStatut, colMissionDistance);


        applyStatutColorCell(colMissionStatut);

        tableMesMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateMapDetails(newVal);
            updateDistanceAndTime();
        });
        // Double-clic -> naviguer vers la carte centree sur la mission
        tableMesMissions.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
                if (selected != null && !isTermine(selected)) {
                    showPage(pageCarteZonesScroll, btnCarteZones);
                    updateMapDetails(selected);
                    updateDistanceAndTime();
                    refreshTourneeMap();
                    showAgentMessage("\uD83D\uDDFA\uFE0F Mission #" + selected.getIdSignalement() + " - voir sur la carte", true);
                }
            }
        });





        colMissionAction.setCellValueFactory(cell -> new SimpleObjectProperty<>(null));
        colMissionAction.setCellFactory(col -> new TableCell<Signalement, Void>() {


            private final Button startButton = new Button("Demarrer");


            private final Button doneButton = new Button("Terminer");


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


                    startButton.setText("Fini");


                    startButton


                            .setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("FAIT");


                    doneButton


                            .setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


                } else if (enCours) {


                    startButton.setText("En cours");


                    startButton


                            .setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("FINIR");


                    doneButton.setStyle(


                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, #4CAF50, 5, 0, 0, 0);");


                } else {


                    startButton.setText("Demarrer");


                    startButton


                            .setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


                    doneButton.setText("Attendre");


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

        if (tableHistorique != null) {
            tableHistorique.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }


        colHistId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));


        colHistZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));


        colHistAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));


        colHistType.setCellValueFactory(new PropertyValueFactory<>("categorie"));


        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(formatHistoriqueDate(cell.getValue())));


        colHistStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));


        // Colonne DurAAaAasAAe : dateSignalement AAAAAAasAAAAAAasAAazA dateCollecte


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
        // Double-clic -> dialogue detail complet de la mission terminee
        tableHistorique.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement selected = tableHistorique.getSelectionModel().getSelectedItem();
                if (selected != null) afficherDetailMissionTerminee(selected);
            }
        });


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


        button.setText("DAAaAasAAmarrage...");


        button.setDisable(true);


        int idMission = mission.getIdSignalement();


        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {


            @Override protected Boolean call() {


                return signalementService.updateStatut(idMission, "En cours");


            }


        };


        task.setOnSucceeded(e -> {


            if (Boolean.TRUE.equals(task.getValue())) {


                showAgentMessage("Mission #" + idMission + " demarree !", true);


                rechargerEtReselectionner(idMission);


            } else {


                button.setText("Demarrer");


                button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


                button.setDisable(false);


                showAgentMessage("Erreur lors du demarrage.", false);


            }


        });


        task.setOnFailed(e -> {


            button.setText("Demarrer");


            button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8;");


            button.setDisable(false);


            showAgentMessage("Erreur lors du demarrage.", false);


        });


        Thread t = new Thread(task, "mission-start"); t.setDaemon(true); t.start();


    }





    private void marquerTermineAvecAnimation(Signalement mission, Button button) {


        if (mission == null || isTermine(mission)) return;


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);


        alert.setTitle("Confirmer mission terminee");


        alert.setHeaderText("Mission #" + mission.getIdSignalement());


        alert.setContentText("Confirmer que cette mission est TERMINEE ?\nZone: " + mission.getZoneNom()


                + "\nType: " + mission.getCategorie());


        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);


        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("TERMINER");


        Optional<ButtonType> result = alert.showAndWait();


        if (result.isEmpty() || result.get() != ButtonType.YES) return;


        animateButton(button, 1.3, true);


        button.setText("Finalisation...");


        button.setDisable(true);


        int idMission = mission.getIdSignalement();


        javafx.concurrent.Task<Boolean> task = new javafx.concurrent.Task<>() {


            @Override protected Boolean call() {


                return signalementService.updateStatut(idMission, "Termine");


            }


        };


        task.setOnSucceeded(e -> {


            if (Boolean.TRUE.equals(task.getValue())) {


                showAgentMessage("Mission #" + idMission + " terminee ! Excellent travail !", true);


                rechargerEtReselectionnerEtHistorique(idMission);


            } else {


                button.setText("Terminer");


                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


                button.setDisable(false);


                showAgentMessage("Erreur lors de la finalisation.", false);


            }


        });


        task.setOnFailed(e -> {


            button.setText("Terminer");


            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");


            button.setDisable(false);


            showAgentMessage("Erreur lors de la finalisation.", false);


        });


        Thread t = new Thread(task, "mission-done"); t.setDaemon(true); t.start();


    }





    /** Recharge les missions et resAAaAasAAlectionne la ligne par id aprAAaAasAAs une action. */


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


            missions.setAll(missionList); mapNeedsRefresh = true;


            missionsUrgentes.setAll(missionList.stream()


                .filter(s -> !isTermine(s)
                        && s.getDateSignalement() != null
                        && s.getDateSignalement().isBefore(java.time.LocalDateTime.now().minusHours(24)))


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
                    if ("En cours".equalsIgnoreCase(m.getStatut())) {
                        showPage(pageCarteZonesScroll, btnCarteZones);
                        updateMapDetails(m);
                        updateDistanceAndTime();
                        refreshTourneeMap();
                        showAgentMessage("Mission #" + idMission + " demarree - voici votre itineraire !", true);
                    }


                });


        });


        task.setOnFailed(e -> logger.error("Erreur rechargement missions", task.getException()));


        Thread t = new Thread(task, "mission-reload"); t.setDaemon(true); t.start();


    }


    /** Recharge, reselectionne et navigue vers l'historique apres une mission terminee. */
    private void rechargerEtReselectionnerEtHistorique(int idMission) {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        int idAgent = current.getIdUser();
        javafx.concurrent.Task<java.util.List<Signalement>> task = new javafx.concurrent.Task<>() {
            @Override protected java.util.List<Signalement> call() {
                return affectationService.getSignalementsByAgent(idAgent);
            }
        };
        task.setOnSucceeded(e -> {
            java.util.List<Signalement> missionList = task.getValue();
            missions.setAll(missionList); mapNeedsRefresh = true;
            missionsUrgentes.setAll(missionList.stream()
                .filter(s -> !isTermine(s)
                    && s.getDateSignalement() != null
                    && s.getDateSignalement().isBefore(java.time.LocalDateTime.now().minusHours(24)))
                .collect(java.util.stream.Collectors.toList()));
            refreshCards();
            refreshTourneeSummary();
            gpsService.updateMissions(missionList);
            historique.setAll(missionList.stream().filter(this::isTermine).collect(java.util.stream.Collectors.toList()));
            if (lblHistoriqueCount != null) lblHistoriqueCount.setText(historique.size() + " mission" + (historique.size() > 1 ? "s" : ""));
            showPage(pageHistorique, btnHistorique);
            missionList.stream().filter(m -> m.getIdSignalement() == idMission).findFirst()
                .ifPresent(m -> { tableHistorique.getSelectionModel().select(m); tableHistorique.scrollTo(m); });
            showAgentMessage("\uD83C\uDF89 Mission #" + idMission + " termin\u00e9e ! Consultez votre historique.", true);
        });
        task.setOnFailed(e -> logger.error("Erreur rechargement missions", task.getException()));
        Thread t = new Thread(task, "mission-reload"); t.setDaemon(true); t.start();
    }
    private void afficherDetailMissionTerminee(Signalement mission) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Detail mission #" + mission.getIdSignalement());
        dialog.setHeaderText(mission.getCategorie() + " Aaa " + mission.getZoneNom());
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(14));
        content.setPrefWidth(460);

        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Infos principales
        addDetailRow(content, "Zone", mission.getZoneNom());
        addDetailRow(content, "Categorie", mission.getCategorie());
        addDetailRow(content, "Description", mission.getDescription() != null ? mission.getDescription() : "-");
        addDetailRow(content, "Date signalement", mission.getDateSignalement() != null ? mission.getDateSignalement().format(fmt) : "-");
        addDetailRow(content, "Date collecte", mission.getDateCollecte() != null ? mission.getDateCollecte().format(fmt) : "-");

        // Duree
        if (mission.getDateSignalement() != null && mission.getDateCollecte() != null) {
            long heures = java.time.Duration.between(mission.getDateSignalement(), mission.getDateCollecte()).toHours();
            String duree = heures < 1 ? "< 1h" : heures < 24 ? heures + "h" : (heures / 24) + "j " + (heures % 24) + "h";
            addDetailRow(content, "Duree resolution", duree);
        }

        // Commentaire agent
        String commentaire = affectationService.getCommentaireBySignalement(mission.getIdSignalement());
        addDetailRow(content, "Commentaire agent", commentaire != null && !commentaire.isBlank() ? commentaire : "Aucun");

        // Evaluation citoyen
        com.smartcity.service.SuiviService.EvaluationEntry eval = suiviService.getEvaluationBySignalement(mission.getIdSignalement());
        if (eval != null) {
            String etoiles = "\u2605".repeat(eval.note) + "\u2606".repeat(5 - eval.note);
            addDetailRow(content, "Evaluation citoyen", etoiles + " (" + eval.note + "/5)");
            if (eval.commentaire != null && !eval.commentaire.isBlank())
                addDetailRow(content, "Avis citoyen", eval.commentaire);
        } else {
            addDetailRow(content, "Evaluation citoyen", "Pas encore evaluee");
        }

        dialog.getDialogPane().setContent(new javafx.scene.control.ScrollPane(content));
        dialog.showAndWait();
    }

    private void addDetailRow(javafx.scene.layout.VBox parent, String label, String value) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(label + " :");
        lbl.setStyle("-fx-font-weight: bold; -fx-min-width: 150px; -fx-text-fill: #374151;");
        javafx.scene.control.Label val = new javafx.scene.control.Label(value);
        val.setWrapText(true);
        val.setStyle("-fx-text-fill: #1f2937;");
        row.getChildren().addAll(lbl, val);
        parent.getChildren().add(row);
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


                    s -> SignalementStatut.AFFECTE.matches(s.getStatut()) || "En cours".equalsIgnoreCase(s.getStatut()))


                    .count();


            lblMissionsCount


                    .setText(actives + " mission" + (actives > 1 ? "s" : "") + " active" + (actives > 1 ? "s" : ""));


        }


    }





    @FXML


    private void handleDemarrerProchaineMission() {


        Optional<Signalement> prochaineMission = missions.stream()


                .filter(s -> SignalementStatut.AFFECTE.matches(s.getStatut())).findFirst();


        if (prochaineMission.isPresent()) {


            Signalement mission = prochaineMission.get();


            if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {


                showAgentMessage("Mission #" + mission.getIdSignalement() + " demarree automatiquement.", true);


                chargerDonnees();


                tableMesMissions.getSelectionModel().select(mission);


                showPage(pageCarteZonesScroll, btnCarteZones);
                updateMapDetails(mission);
                updateDistanceAndTime();
                refreshTourneeMap();


            } else {


                showAgentMessage("Erreur lors du demarrage automatique.", false);


            }


        } else {


            showAgentMessage("Aucune mission en attente a demarrer.", false);


        }


    }





    @FXML


    private void handleVoirMissionsEnCours() {


        List<Signalement> enCours = missions.stream().filter(s -> SignalementStatut.EN_COURS.matches(s.getStatut()))


                .collect(Collectors.toList());


        if (enCours.isEmpty()) {


            showAgentMessage("Aucune mission actuellement en cours.", false);


        } else {


            showPage(pageMesMissions, btnMesMissions);
            tableMesMissions.setItems(javafx.collections.FXCollections.observableArrayList(enCours));
            if (filterMissionsStatutCombo != null) {
                filterMissionsStatutCombo.setValue("En cours");
            }
            tableMesMissions.getSelectionModel().select(enCours.get(0));


            showAgentMessage(enCours.size() + " mission(s) en cours affichee(s).", true);


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


            if (mapMissionAdresseLabel != null) {
                mapMissionAdresseLabel.setText("-");
            }


            mapMissionStatutLabel.setText("-");


            return;


        }


        mapMissionIdLabel.setText(String.valueOf(mission.getIdSignalement()));


        mapMissionZoneLabel.setText(mission.getZoneNom());


        if (mapMissionAdresseLabel != null) {
            mapMissionAdresseLabel.setText(getAdresse(mission));
        }


        mapMissionStatutLabel.setText(getDisplayStatut(mission.getStatut()));
        // Activer le bouton Terminer uniquement si la mission est En cours
        if (btnTerminerDepuisCarte != null)
            btnTerminerDepuisCarte.setDisable(!"En cours".equalsIgnoreCase(mission.getStatut()));
        if (mapCommentaireLabel != null) {
            String commentaire = affectationService.getCommentaireBySignalement(mission.getIdSignalement());
            mapCommentaireLabel.setText(commentaire != null && !commentaire.isBlank() ? commentaire : "-");
            if (mapCommentaireField != null)
                mapCommentaireField.setText(commentaire != null ? commentaire : "");
        }
    }





    private void refreshMap(Signalement selectedMission, boolean showRoute) {
        if (mapWebView == null) return;
        WebEngine engine = mapWebView.getEngine();
        installMapFallbackHandlers(engine, "Carte agent indisponible.");

        // Installer une seule fois le listener de cycle de chargement.
        if (!mapBridgeInstalled) {
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        netscape.javascript.JSObject win =
                                (netscape.javascript.JSObject) engine.executeScript("window");
                        win.setMember("javaAgent", new JSBridgeAgent());
                    } catch (Exception e) {
                        logger.warn("Erreur bridge carte GPS", e);
                    }
                }
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED
                        || newState == javafx.concurrent.Worker.State.FAILED) {
                    javafx.application.Platform.runLater(() -> {
                        agentMessageLabel.setVisible(false);
                        agentMessageLabel.setManaged(false);
                        agentMessageLabel.setText("");
                    });
                }
            });
            mapBridgeInstalled = true;
        }

        showAgentMessage("Chargement de la carte...", true);
        engine.loadContent(buildLeafletHtml(selectedMission, showRoute));
    }





    @FXML


    private void handleDefinirMaPosition() {
        showAgentMessage("Position manuelle desactivee. Utilisez le GPS.", false);


    }

    @FXML
    private void handleTerminerDepuisCarte() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAgentMessage("S\u00e9lectionnez une mission sur la carte d'abord.", false);
            return;
        }
        marquerTermineAvecAnimation(selected, btnTerminerDepuisCarte);
    }

    @FXML
    private void handleSauvegarderCommentaire() {
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAgentMessage("Selectionnez une mission d'abord.", false);
            return;
        }
        if (mapCommentaireField == null) return;
        String commentaire = mapCommentaireField.getText().trim();
        if (affectationService.sauvegarderCommentaire(selected.getIdSignalement(), commentaire)) {
            if (mapCommentaireLabel != null)
                mapCommentaireLabel.setText(commentaire.isBlank() ? "-" : commentaire);
            showAgentMessage("Commentaire sauvegarde.", true);
        } else {
            showAgentMessage("Erreur lors de la sauvegarde du commentaire.", false);
        }
    }





    /** Pont JavaScript AAAAAAasAAAAAAasAAazA Java pour recevoir la position cliquAAaAasAAe sur la carte */


    public class JSBridgeAgent {


        public void setAgentPosition(double lat, double lon) {


            Platform.runLater(() -> {


                gpsService.setManualPosition(lat, lon);


                if (lblPositionActuelle != null) {


                    lblPositionActuelle.setText(String.format("Position manuelle: %.5f, %.5f", lat, lon));


                }


                showAgentMessage(String.format("Position definie: %.4f, %.4f", lat, lon), true);


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

        // AAAAaAaAAAAasAAA SI PREMIAAaAaAaARE FOIS: charger l'HTML complet
        if (mapWebView.getEngine().getLocation() == null || mapWebView.getEngine().getLocation().isEmpty()) {
            installMapFallbackHandlers(mapWebView.getEngine(), "Itineraire indisponible. Les donnees restent accessibles dans la liste des missions.");
            String html = buildLeafletHtml(null, false);
            mapWebView.getEngine().loadContent(html);
            return; // Attendre que la carte soit chargAAaAasAAe
        }

        // AAAAaAaAAAAasAAA MISE AAaAAAaAAA JOUR DYNAMIQUE VIA executeScript (pas de clignotement)
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
            logger.error("AAAAasAAAaAaa Erreur mise AAaAasAA jour carte temps-rAAaAasAAel", e);
            // Fallback: recharger complAAaAasAAtement si executeScript AAaAasAAchoue
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





    /** AAAAaAaAAAAasAAA INJECTER LE JS EMBARQUAAaAAAasAAA AU LIEU D'UNE URL EXTERNE */
    private static String getSmartGpsMapJsInline() {
        return SMART_GPS_MAP_JS != null ? SMART_GPS_MAP_JS : "";
    }

    /** AAAAaAaAAAAasAAA CRAAaAAAasAAAER LE CLIENT WEBSOCKET POUR MISES AAaAAAaAAA JOUR TEMPS RAAaAAAasAAAEL */
    private static String getWebSocketClientJs() {
        return """
            // AAAAaAAAAAasAAAAaAaa WebSocket Client - Mises AAaAasAA jour temps rAAaAasAAel des missions et positions
            let ws = null;
            let wsConnectAttempts = 0;
            
            function connectWebSocket() {
                try {
                    let protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
                    let wsUrl = protocol + '//' + location.hostname + ':' + (window._wsPort || 8888) + '/ws/agent-missions';
                    ws = new WebSocket(wsUrl);
                    
                    ws.onopen = function() {
                        console.log('AAAAaAaAAAAasAAA WebSocket connectAAaAasAA pour mises AAaAasAA jour temps rAAaAasAAel');
                        wsConnectAttempts = 0; // Reset counter on success
                    };
                    
                    ws.onmessage = function(event) {
                        try {
                            let data = JSON.parse(event.data);
                            
                            // Position d'agent en temps rAAaAasAAel
                            if (data.type === 'position' && data.lat && data.lon) {
                                console.log('AAAAaAAAAAasAAaAasAA Position agent reAAaAasAAue:', data.lat, data.lon);
                                if (typeof updateAgentPosition === 'function') {
                                    updateAgentPosition(data.lat, data.lon);
                                }
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                            
                            // Nouvelle mission
                            if (data.type === 'mission') {
                                console.log('AAAAaAAAaAAAasAA Nouvelle mission reAAaAasAAue');
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                        } catch(parseErr) {
                            console.debug('WebSocket message parse error (non-JSON):', event.data);
                        }
                    };
                    
                    ws.onerror = function(err) {
                        console.error('AAAAasAAAaAaa WebSocket error:', err);
                    };
                    
                    ws.onclose = function() {
                        console.warn('AAAAaAAAasAAAAAAasAAAasAA WebSocket fermAAaAasAA');
                        // Reconnexion aprAAaAasAAs dAAaAasAAlai
                        wsConnectAttempts++;
                        let delay = Math.min(300000, 5000 * Math.pow(1.5, wsConnectAttempts));
                        console.log('AAAAaAAAAAasAAAAAAasAAA Reconnexion dans', Math.round(delay/1000), 's');
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
                    .append("').on('click',function(e){this.openPopup();if(window.javaAgent)window.javaAgent.selectMission(")
                    .append(mission.getIdSignalement())
                    .append(");});");


        }





        // Marqueur position agent actuelle - sera ajoutAAaAasAA via agentPosJs plus bas


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





        String positionModeJs = "";





        String agentPosJs = "";


        if (agentPos != null) {


            agentPosJs = String.format(


                    "L.circleMarker([%.6f,%.6f],{radius:12,color:'#2196F3',fillColor:'#2196F3',fillOpacity:0.9,weight:3}).addTo(map).bindPopup('<b>Votre position actuelle</b><br/>%.5f, %.5f');",


                    agentPos.lat, agentPos.lon, agentPos.lat, agentPos.lon);


        }





        String btnHtml = "";





        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"


                +


                "<link rel='stylesheet' href='" + com.smartcity.utils.MapResourceUtils.leafletCss() + "'/>" +


                "<style>html,body,#map{height:100%;margin:0;position:relative;}</style>" +


                "</head><body>" +


                btnHtml +


                "<div id='map'></div>" +


                "<script src='" + com.smartcity.utils.MapResourceUtils.leafletJs() + "'></script>" +


                "<script>" +


                "var map=L.map('map').setView([" + fmt(center.lat) + "," + fmt(center.lon) + "],13);" +


                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"


                +


                criticalJs + markers + routeJs + agentPosJs + positionModeJs +


                "</script>" +

                "<script>" + getSmartGpsMapJsInline() + "</script>" +
                (MainApp.isWebSocketServerRunning() ? "<script>window._wsPort=" + com.smartcity.config.GeoConfig.getWebSocketPort() + ";</script><script>" + getWebSocketClientJs() + "</script>" : "") +

                "</body></html>";




    }


    private int indexOfMission(Signalement mission) {


        for (int i = 0; i < missions.size(); i++)


            if (missions.get(i).getIdSignalement() == mission.getIdSignalement())


                return i;


        return 0;


    }





    private Point missionPoint(Signalement mission, int index) {
        // AAAAaAAAaAAAasAA UTILISER D'ABORD LES VRAIES COORDONNAAaAAAasAAAES GPS SI DISPONIBLES
        if (mission.getLatitude() != 0.0 || mission.getLongitude() != 0.0) {
            return new Point(mission.getLatitude(), mission.getLongitude());
        }
        
        // SINON: Placer autour du centre de zone avec offset pour AAaAasAAviter superposition
        Point base = zoneCenter(mission.getZoneNom());
        double latOffset = (index % 5) * 0.004 + 0.001;  // AAaAAasAA0.004AAaAAasAA = ~400m
        double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
        return new Point(base.lat + latOffset, base.lon + lonOffset);
    }





    private Point zoneCenter(String zone) {
        // AAAAaAaAAAAasAAA DAAaAAAasAAALAAaAAAasAAAGUER ENTIAAaAaAaAREMENT AU SERVICE (Pas de hardcode)
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
        if (!mapNeedsRefresh && mapWebView != null && mapWebView.getEngine().getDocument() != null) return;
        mapNeedsRefresh = false;


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


        return (signalement.getDescription() == null || signalement.getDescription().isBlank()) ? "Commentaire non precise"


                : signalement.getDescription();


    }

    private String formatLocalisation(Signalement signalement) {
        if (signalement == null) {
            return "-";
        }
        String zone = signalement.getZoneNom() == null || signalement.getZoneNom().isBlank()
            ? "Zone inconnue"
            : signalement.getZoneNom();
        if (signalement.getLatitude() == 0.0 && signalement.getLongitude() == 0.0) {
            return zone;
        }
        return String.format(java.util.Locale.US, "%s (%.5f, %.5f)",
            zone, signalement.getLatitude(), signalement.getLongitude());
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








    private void installTooltips() {


        btnAgentDashboard.setTooltip(new Tooltip("Vue globale des missions du jour"));


        btnMesMissions.setTooltip(new Tooltip("Missions assignAAaAasAAes et actions"));


        btnCarteZones.setTooltip(new Tooltip("Carte interactive des zones"));


        btnHistorique.setTooltip(new Tooltip("Historique des missions terminAAaAasAAes"));


        btnMonProfil.setTooltip(new Tooltip("Informations du compte agent"));


    }





    private void showAgentMessage(String message, boolean success) {


        String emoji = success ? "OK" : "ERREUR";


        String fullMessage = emoji + " " + message;


        agentMessageLabel.setVisible(true); agentMessageLabel.setManaged(true); agentMessageLabel.setOpacity(1); agentMessageLabel.setText(fullMessage);


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


            fadeOut.setOnFinished(e -> { agentMessageLabel.setText(""); agentMessageLabel.setVisible(false); agentMessageLabel.setManaged(false); });


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


            showAgentMessage(String.format("GPS actif - position: %.4f, %.4f", pos.lat, pos.lon), true);


        } else {


            showAgentMessage("GPS actif - en attente de position...", true);


        }


        if (lblPositionActuelle != null && pos != null) {


            lblPositionActuelle.setText(String.format("\ud83d\udccd %.4f, %.4f (Live)", pos.lat, pos.lon));


        }


    }





    @FXML


    private void handleCopierUrlGps() {


        if (qrUrlLabel == null || qrUrlLabel.getText() == null || qrUrlLabel.getText().isEmpty()) {


            showAgentMessage("URL GPS non disponible", false);


            return;


        }


        


        String urlGps = qrUrlLabel.getText();


        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();


        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();


        content.putString(urlGps);


        clipboard.setContent(content);


        


        showAgentMessage("URL GPS copiee dans le presse-papiers: " + urlGps, true);


    }


}




