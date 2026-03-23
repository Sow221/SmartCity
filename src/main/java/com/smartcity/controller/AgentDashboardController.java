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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 🎯 CONTRÔLEUR AGENT AVEC GPS TEMPS RÉEL
 * Interface moderne avec navigation dynamique
 */
public class AgentDashboardController implements RealTimeGPSService.LocationUpdateListener {

    @FXML private BorderPane rootPane;
    @FXML private Label agentNameLabel;
    @FXML private Label agentMessageLabel;
    @FXML private Button themeToggleButton;

    @FXML private Button btnAgentDashboard;
    @FXML private Button btnMesMissions;
    @FXML private Button btnCarteZones;
    @FXML private Button btnHistorique;
    @FXML private Button btnMonProfil;
    
    // Nouveaux boutons d'actions rapides GPS
    @FXML private Button btnActionDemarrerProchaine;
    @FXML private Button btnActionVoirEnCours;
    @FXML private Button btnActionItineraireRapide;
    @FXML private Button btnActionActualiser;
    @FXML private Label lblMissionsCount;
    @FXML private Label lblPositionActuelle;

    @FXML private VBox pageAgentDashboard;
    @FXML private VBox pageMesMissions;
    @FXML private VBox pageCarteZones;
    @FXML private VBox pageHistorique;
    @FXML private VBox pageMonProfil;

    @FXML private Label agentCardTotal;
    @FXML private Label agentCardEnAttente;
    @FXML private Label agentCardEnCours;
    @FXML private Label agentCardTerminees;

    @FXML private TableView<Signalement> tableMissionsUrgentes;
    @FXML private TableColumn<Signalement, Integer> colUrgId;
    @FXML private TableColumn<Signalement, String> colUrgZone;
    @FXML private TableColumn<Signalement, String> colUrgAdresse;
    @FXML private TableColumn<Signalement, String> colUrgType;
    @FXML private TableColumn<Signalement, String> colUrgDate;
    @FXML private TableColumn<Signalement, String> colUrgStatut;

    @FXML private TableView<Signalement> tableMesMissions;
    @FXML private TableColumn<Signalement, Integer> colMissionId;
    @FXML private TableColumn<Signalement, String> colMissionZone;
    @FXML private TableColumn<Signalement, String> colMissionAdresse;
    @FXML private TableColumn<Signalement, String> colMissionType;
    @FXML private TableColumn<Signalement, String> colMissionDate;
    @FXML private TableColumn<Signalement, String> colMissionStatut;
    @FXML private TableColumn<Signalement, String> colMissionDistance;
    @FXML private TableColumn<Signalement, Void> colMissionAction;

// Labels pour la carte et GPS
@FXML private Label mapDistanceLabel;
@FXML private Label mapTempsLabel;
@FXML private WebView mapWebView;
    @FXML private Label mapMissionIdLabel;
    @FXML private Label mapMissionZoneLabel;
    @FXML private Label mapMissionAdresseLabel;
    @FXML private Label mapMissionStatutLabel;

    @FXML private DatePicker historiqueDatePicker;
    @FXML private TableView<Signalement> tableHistorique;
    @FXML private TableColumn<Signalement, Integer> colHistId;
    @FXML private TableColumn<Signalement, String> colHistZone;
    @FXML private TableColumn<Signalement, String> colHistAdresse;
    @FXML private TableColumn<Signalement, String> colHistType;
    @FXML private TableColumn<Signalement, String> colHistDate;
    @FXML private TableColumn<Signalement, String> colHistStatut;

    @FXML private TextField profilNomField;
    @FXML private TextField profilEmailField;
    @FXML private TextField profilZoneField;

    // Services
    private final GeolocationService geolocationService = new GeolocationService();
    private final RealTimeGPSService gpsService = new RealTimeGPSService();
    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    // Data
    private final ObservableList<Signalement> missions = FXCollections.observableArrayList();
    private final ObservableList<Signalement> missionsUrgentes = FXCollections.observableArrayList();
    private final ObservableList<Signalement> historique = FXCollections.observableArrayList();
    private List<RealTimeGPSService.MissionWithDistance> currentDistances = List.of();

    // UI
    private final PauseTransition messageClearDelay = new PauseTransition(Duration.seconds(4));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null || !SessionManager.isAgent()) {
            showAgentMessage("🚫 Accès réservé aux agents.", false);
            return;
        }

        agentNameLabel.setText(current.getNom());
        installTooltips();
        configureTables();
        setupGPSService();

        tableMissionsUrgentes.setItems(missionsUrgentes);
        tableMesMissions.setItems(missions);
        tableHistorique.setItems(historique);

        historiqueDatePicker.setValue(LocalDate.now());
        applyTheme();
        showAgentDashboardPage();
        chargerDonnees();
        
        // 🚀 Démarrer le GPS temps réel
        gpsService.startRealTimeTracking();
    }

    // === IMPLÉMENTATION GPS LISTENER ===
    
    @Override
    public void onLocationUpdate(RealTimeGPSService.Coordinates newPosition) {
        if (lblPositionActuelle != null) {
            lblPositionActuelle.setText(String.format("📍 Position: %.4f, %.4f", 
                newPosition.lat, newPosition.lon));
        }
        
        // Mettre à jour la carte avec la nouvelle position
        refreshMapWithGPS();
    }
    
    @Override
    public void onMissionsDistanceUpdate(List<RealTimeGPSService.MissionWithDistance> distances) {
        this.currentDistances = distances;
        
        // Mettre à jour le tableau avec les distances
        refreshTableWithDistances();
        
        // Mettre à jour les informations de la mission sélectionnée
        updateMapDetailsWithDistance();
        
        // Notification pour la mission la plus proche
        distances.stream()
            .filter(d -> d.isClosest && d.distanceKm < 0.2) // < 200m
            .findFirst()
            .ifPresent(closest -> 
                showAgentMessage("🎯 Mission #" + closest.mission.getIdSignalement() + 
                    " très proche (" + Math.round(closest.distanceKm * 1000) + "m)!", true));
    }

    // === ACTIONS RAPIDES GPS ===
    
    @FXML
    private void handleDemarrerProchaineMission() {
        Optional<RealTimeGPSService.MissionWithDistance> closest = currentDistances.stream()
            .filter(d -> d.isClosest && "En attente".equalsIgnoreCase(d.mission.getStatut()))
            .findFirst();
            
        if (closest.isPresent()) {
            Signalement mission = closest.get().mission;
            if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {
                showAgentMessage(String.format("🚀 Mission la plus proche démarrée! #%d (%.0fm)", 
                    mission.getIdSignalement(), closest.get().distanceKm * 1000), true);
                chargerDonnees();
                
                // Sélectionner et centrer sur la mission
                tableMesMissions.getSelectionModel().select(mission);
                showPage(pageMesMissions, btnMesMissions);
            }
        } else {
            showAgentMessage("ℹ️ Aucune mission proche à démarrer.", false);
        }
    }
    
    @FXML
    private void handleVoirMissionsEnCours() {
        List<Signalement> enCours = missions.stream()
            .filter(s -> "En cours".equalsIgnoreCase(s.getStatut()))
            .collect(Collectors.toList());
            
        if (enCours.isEmpty()) {
            showAgentMessage("ℹ️ Aucune mission en cours.", false);
        } else {
            showPage(pageMesMissions, btnMesMissions);
            tableMesMissions.getSelectionModel().select(enCours.get(0));
            showAgentMessage("📋 " + enCours.size() + " mission(s) en cours.", true);
        }
    }
    
    @FXML
    private void handleItineraireRapide() {
        generateOptimalRoute();
    }
    
    @FXML
    private void handleActionActualiser() {
        chargerDonnees();
        gpsService.updateMissions(missions);
        showAgentMessage("🔄 Données et GPS actualisés!", true);
    }

    // === CHARGEMENT DONNÉES ===
    
    @FXML
    public void chargerDonnees() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());
        missions.setAll(missionList);

        missionsUrgentes.setAll(missionList.stream()
                .filter(s -> "En attente".equalsIgnoreCase(s.getStatut()))
                .collect(Collectors.toList()));

        // Mettre à jour le GPS avec les nouvelles missions
        gpsService.updateMissions(missions);

        refreshCards();
        refreshHistorique();
        refreshProfil();

        if (!missions.isEmpty() && tableMesMissions.getSelectionModel().getSelectedItem() == null) {
            tableMesMissions.getSelectionModel().selectFirst();
        }

        refreshMapWithGPS();
    }

    // === CONFIGURATION TABLES ===
    
    private void configureTables() {
        configureDashboardTable();
        configureMissionsTable();
        configureHistoriqueTable();
        
        // Événements de sélection
        tableMissionsUrgentes.setRowFactory(tv -> {
            TableRow<Signalement> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    tableMesMissions.getSelectionModel().select(row.getItem());
                    showPage(pageMesMissions, btnMesMissions);
                }
            });
            return row;
        });

        tableMesMissions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateMapDetailsWithDistance();
            refreshMapWithGPS();
        });
    }

    private void configureDashboardTable() {
        colUrgId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colUrgZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colUrgAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colUrgType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colUrgDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colUrgStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        applyStatutColorCell(colUrgStatut);
    }

    private void configureMissionsTable() {
        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMissionZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colMissionAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colMissionType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colMissionDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colMissionStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        
        // Nouvelle colonne distance
        if (colMissionDistance != null) {
            colMissionDistance.setCellValueFactory(cell -> {
                Signalement mission = cell.getValue();
                return currentDistances.stream()
                    .filter(d -> d.mission.getIdSignalement() == mission.getIdSignalement())
                    .findFirst()
                    .map(d -> new SimpleStringProperty(d.directionText))
                    .orElse(new SimpleStringProperty("🔄 Calcul..."));
            });
        }
        
        applyStatutColorCell(colMissionStatut);
        configureMissionActionColumn();
    }

    private void configureMissionActionColumn() {
        colMissionAction.setCellFactory(col -> new TableCell<>() {
            private final Button startButton = new Button("🚀 Démarrer");
            private final Button doneButton = new Button("✅ Terminé");
            private final Button navButton = new Button("🧭 GPS");
            private final HBox box = new HBox(3, startButton, doneButton, navButton);

            {
                // Styles modernes
                startButton.getStyleClass().addAll("btn-mission-start", "btn-modern");
                doneButton.getStyleClass().addAll("btn-mission-done", "btn-modern");
                navButton.getStyleClass().addAll("btn-mission-nav", "btn-modern");
                
                startButton.setPrefSize(100, 32);
                doneButton.setPrefSize(100, 32);
                navButton.setPrefSize(80, 32);

                startButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    demarrerMissionAvecAnimation(mission, startButton);
                });

                doneButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    marquerTermineAvecAnimation(mission, doneButton);
                });
                
                navButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    ouvrirNavigationGPS(mission);
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
                
                // Adaptation des boutons selon l'état
                if (terminee) {
                    startButton.setText("✓ Fini");
                    startButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 6;");
                    doneButton.setText("✅ FAIT");
                    doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6;");
                } else if (enCours) {
                    startButton.setText("⏳ En cours");
                    startButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 6;");
                    doneButton.setText("✅ FINIR");
                    doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, #4CAF50, 3, 0, 0, 0);");
                } else {
                    startButton.setText("🚀 Démarrer");
                    startButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 6;");
                    doneButton.setText("⏸ Attendre");
                    doneButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 6;");
                }
                
                // Bouton GPS toujours disponible
                Optional<RealTimeGPSService.MissionWithDistance> distance = currentDistances.stream()
                    .filter(d -> d.mission.getIdSignalement() == mission.getIdSignalement())
                    .findFirst();
                
                if (distance.isPresent() && distance.get().isClosest) {
                    navButton.setText("⭐ GPS");
                    navButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, #FF5722, 2, 0, 0, 0);");
                } else {
                    navButton.setText("🧭 GPS");
                    navButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-background-radius: 6;");
                }
                
                startButton.setDisable(terminee);
                doneButton.setDisable(terminee || (!enCours && !"En attente".equalsIgnoreCase(mission.getStatut())));
                setGraphic(box);
            }
        });
    }

    private void configureHistoriqueTable() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colHistZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colHistAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colHistType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colHistDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colHistStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));
        applyStatutColorCell(colHistStatut);
    }

    // === ACTIONS MISSIONS ===
    
    private void demarrerMissionAvecAnimation(Signalement mission, Button button) {
        if (mission == null || isTermine(mission)) return;

        animateButton(button, 1.2, true);
        button.setText("⏳ Démarrage...");
        button.setDisable(true);

        if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {
            button.setText("✓ Démarrée!");
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6;");
            showAgentMessage("🚀 Mission #" + mission.getIdSignalement() + " DÉMARRÉE!", true);
            chargerDonnees();
        } else {
            resetButtonAfterError(button, "🚀 Démarrer", "#2196F3");
            showAgentMessage("❌ Erreur lors du démarrage.", false);
        }
    }

    private void marquerTermineAvecAnimation(Signalement mission, Button button) {
        if (mission == null || isTermine(mission)) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("✅ Mission Terminée");
        alert.setHeaderText("Mission #" + mission.getIdSignalement());
        alert.setContentText("🎯 Confirmer que cette mission est TERMINÉE?\n\n" +
            "📍 Zone: " + mission.getZoneNom() + "\n📋 Type: " + mission.getCategorie());
        
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) return;

        animateButton(button, 1.3, true);
        button.setText("⏳ Finalisation...");
        button.setDisable(true);

        if (signalementService.updateStatut(mission.getIdSignalement(), "Collecte")) {
            button.setText("🎉 TERMINÉE!");
            button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, #4CAF50, 5, 0, 0, 0);");
            showAgentMessage("🎉 MISSION TERMINÉE! #" + mission.getIdSignalement(), true);
            chargerDonnees();
        } else {
            resetButtonAfterError(button, "✅ Terminé", "#4CAF50");
            showAgentMessage("❌ Erreur lors de la finalisation.", false);
        }
    }
    
    private void ouvrirNavigationGPS(Signalement mission) {
        String url = gpsService.getNavigationUrl(mission);
        
        // Afficher les informations de navigation
        Optional<RealTimeGPSService.MissionWithDistance> distance = currentDistances.stream()
            .filter(d -> d.mission.getIdSignalement() == mission.getIdSignalement())
            .findFirst();
            
        String infoText = distance.map(d -> 
            String.format("📍 Distance: %.1f km\n⏱️ Temps estimé: %d min\n🧭 Direction: %s", 
                d.distanceKm, d.estimatedMinutes, d.directionText.replace("🎯", "").replace("📍", "").replace("🚶", "").replace("🚗", "").replace("🛣️", "").trim()))
            .orElse("📍 Calcul de la route...");
            
        Alert navAlert = new Alert(Alert.AlertType.INFORMATION);
        navAlert.setTitle("🧭 Navigation GPS");
        navAlert.setHeaderText("Mission #" + mission.getIdSignalement());
        navAlert.setContentText(infoText + "\n\n🔗 Lien Google Maps:\n" + url);
        
        TextArea urlArea = new TextArea(url);
        urlArea.setEditable(false);
        urlArea.setPrefRowCount(2);
        
        navAlert.getDialogPane().setExpandableContent(urlArea);
        navAlert.showAndWait();
        
        showAgentMessage("🧭 Navigation vers mission #" + mission.getIdSignalement(), true);
    }

    // === CARTE GPS TEMPS RÉEL ===
    
    private void setupGPSService() {
        gpsService.addLocationUpdateListener(this);
    }
    
    private void refreshMapWithGPS() {
        if (mapWebView == null) return;
        
        Signalement selectedMission = tableMesMissions.getSelectionModel().getSelectedItem();
        String mapHtml = buildGPSMapHtml(selectedMission);
        mapWebView.getEngine().loadContent(mapHtml);
    }
    
    private String buildGPSMapHtml(Signalement selectedMission) {
        RealTimeGPSService.Coordinates agentPos = gpsService.getCurrentPosition();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>")
            .append("<style>")
            .append("html,body,#map{height:100%;margin:0;}")
            .append(".agent-marker{background:#2196F3;width:18px;height:18px;border-radius:50%;border:3px solid white;box-shadow:0 0 10px rgba(33,150,243,0.7);animation:pulse 2s infinite;}")
            .append("@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(33,150,243,0.7);}70%{box-shadow:0 0 0 8px rgba(33,150,243,0);}100%{box-shadow:0 0 0 0 rgba(33,150,243,0);}}")
            .append(".mission-closest{animation:glow 1.5s ease-in-out infinite alternate;}")
            .append("@keyframes glow{from{box-shadow:0 0 5px #fff,0 0 10px #fff,0 0 15px #FF5722;}to{box-shadow:0 0 10px #fff,0 0 20px #FF5722,0 0 30px #FF5722;}}")
            .append("</style></head><body><div id='map'></div>")
            .append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>")
            .append("<script>");
        
        // Position centrale de la carte
        double centerLat = agentPos != null ? agentPos.lat : 14.7646;
        double centerLon = agentPos != null ? agentPos.lon : -17.3920;
        
        html.append(String.format("var map=L.map('map').setView([%.6f,%.6f],14);", centerLat, centerLon))
            .append("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);");
        
        // Marqueur agent (position temps réel)
        if (agentPos != null) {
            html.append(String.format(
                "L.marker([%.6f,%.6f],{icon:L.divIcon({className:'agent-marker',html:'🚶',iconSize:[25,25]})}).addTo(map).bindPopup('🎯 Votre Position<br/>Lat: %.4f<br/>Lon: %.4f<br/><small>Temps réel</small>');",
                agentPos.lat, agentPos.lon, agentPos.lat, agentPos.lon));
        }
        
        // Marqueurs missions avec distances
        for (Signalement mission : missions) {
            Optional<RealTimeGPSService.MissionWithDistance> distanceInfo = currentDistances.stream()
                .filter(d -> d.mission.getIdSignalement() == mission.getIdSignalement())
                .findFirst();
                
            String color = getStatusColor(mission.getStatut());
            String markerClass = distanceInfo.map(d -> d.isClosest ? "mission-closest" : "").orElse("");
            String distanceText = distanceInfo.map(d -> 
                String.format("<br/>📏 %.1fkm (%dmn)", d.distanceKm, d.estimatedMinutes)).orElse("");
            
            html.append(String.format(
                "L.circleMarker([%.6f,%.6f],{radius:10,color:'%s',fillColor:'%s',fillOpacity:0.8,weight:3,className:'%s'}).addTo(map).bindPopup('<b>Mission #%d</b><br/>%s<br/>%s<br/>📊 %s%s');",
                mission.getLatitude(), mission.getLongitude(), color, color, markerClass,
                mission.getIdSignalement(), mission.getZoneNom(), mission.getCategorie(), 
                getDisplayStatut(mission.getStatut()), distanceText));
        }
        
        // Route vers mission sélectionnée
        if (selectedMission != null && agentPos != null) {
            html.append(String.format(
                "L.polyline([[%.6f,%.6f],[%.6f,%.6f]],{color:'#1565C0',weight:4,dashArray:'8 6',opacity:0.8}).addTo(map);",
                agentPos.lat, agentPos.lon, selectedMission.getLatitude(), selectedMission.getLongitude()));
        }
        
        html.append("</script></body></html>");
        return html.toString();
    }
    
    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "en attente": return "#FF5722";
            case "en cours": return "#FF9800";
            case "collecte": case "terminé": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    // === AUTRES MÉTHODES ===
    
    private void refreshTableWithDistances() {
        if (colMissionDistance != null) {
            colMissionDistance.setVisible(false);
            colMissionDistance.setVisible(true); // Force refresh
        }
    }
    
    private void updateMapDetailsWithDistance() {
        Signalement mission = tableMesMissions.getSelectionModel().getSelectedItem();
        if (mission == null) {
            mapMissionIdLabel.setText("-");
            mapMissionZoneLabel.setText("-");
            mapMissionAdresseLabel.setText("-");
            mapMissionStatutLabel.setText("-");
            if (mapDistanceLabel != null) mapDistanceLabel.setText("-");
            if (mapTempsLabel != null) mapTempsLabel.setText("-");
            return;
        }

        mapMissionIdLabel.setText(String.valueOf(mission.getIdSignalement()));
        mapMissionZoneLabel.setText(mission.getZoneNom());
        mapMissionAdresseLabel.setText(getAdresse(mission));
        mapMissionStatutLabel.setText(getDisplayStatut(mission.getStatut()));
        
        // Informations GPS
        currentDistances.stream()
            .filter(d -> d.mission.getIdSignalement() == mission.getIdSignalement())
            .findFirst()
            .ifPresentOrElse(d -> {
                if (mapDistanceLabel != null) mapDistanceLabel.setText(String.format("%.1f km", d.distanceKm));
                if (mapTempsLabel != null) mapTempsLabel.setText(d.estimatedMinutes + " min");
            }, () -> {
                if (mapDistanceLabel != null) mapDistanceLabel.setText("Calcul...");
                if (mapTempsLabel != null) mapTempsLabel.setText("-");
            });
    }

    private void generateOptimalRoute() {
        List<Signalement> activeMissions = missions.stream()
            .filter(s -> "En attente".equalsIgnoreCase(s.getStatut()) || "En cours".equalsIgnoreCase(s.getStatut()))
            .collect(Collectors.toList());
            
        if (activeMissions.isEmpty()) {
            showAgentMessage("Aucune mission active pour l'itinéraire.", false);
            return;
        }
        
        RealTimeGPSService.Coordinates startPoint = gpsService.getCurrentPosition();
        GeolocationService.Coordinates geoStartPoint;
        
        if (startPoint != null) {
            geoStartPoint = new GeolocationService.Coordinates(startPoint.lat, startPoint.lon);
        } else {
            geoStartPoint = geolocationService.getZoneCenter(getAgentZone());
        }
        
        List<Signalement> optimizedRoute = geolocationService.optimizeCollectionRoute(activeMissions, geoStartPoint);
        
        // Afficher l'itinéraire optimisé
        StringBuilder routeInfo = new StringBuilder();
        routeInfo.append("🗺️ ITINÉRAIRE GPS OPTIMISÉ\n")
                 .append("📍 Départ: Position actuelle\n")
                 .append("📋 ").append(optimizedRoute.size()).append(" missions à visiter\n\n");
        
        double totalDistance = 0;
        GeolocationService.Coordinates currentPos = geoStartPoint;
        
        for (int i = 0; i < optimizedRoute.size(); i++) {
            Signalement mission = optimizedRoute.get(i);
            double distance = geolocationService.calculateDistance(
                currentPos.lat, currentPos.lon, 
                mission.getLatitude(), mission.getLongitude()
            );
            totalDistance += distance;
            
            routeInfo.append(String.format("%d. Mission #%d\n", i + 1, mission.getIdSignalement()))
                     .append(String.format("   📍 %.1f km - %s\n", distance, mission.getCategorie()))
                     .append(String.format("   🏢 %s\n\n", mission.getZoneNom()));
            
            currentPos = new GeolocationService.Coordinates(mission.getLatitude(), mission.getLongitude());
        }
        
        routeInfo.append(String.format("🚗 Distance totale: %.1f km\n", totalDistance))
                 .append(String.format("⏱️ Temps total estimé: %.0f min", totalDistance * 3));
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🗺️ Itinéraire GPS Optimisé");
        alert.setHeaderText("Navigation intelligente");
        
        TextArea textArea = new TextArea(routeInfo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(12);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
        
        showAgentMessage(String.format("🗺️ Itinéraire optimisé: %d missions (%.1f km)", 
            optimizedRoute.size(), totalDistance), true);
    }

    // === UTILITAIRES ===
    
    private void animateButton(Button button, double scale, boolean isSuccess) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();
    }
    
    private void resetButtonAfterError(Button button, String text, String color) {
        button.setText("❌ Erreur");
        button.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 6;");
        
        PauseTransition reset = new PauseTransition(Duration.seconds(2));
        reset.setOnFinished(e -> {
            button.setText(text);
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6;");
            button.setDisable(false);
        });
        reset.play();
    }
    
    private void updateMissionsCount() {
        if (lblMissionsCount != null) {
            int actives = (int) missions.stream()
                .filter(s -> "En attente".equalsIgnoreCase(s.getStatut()) || "En cours".equalsIgnoreCase(s.getStatut()))
                .count();
            lblMissionsCount.setText(actives + " mission" + (actives > 1 ? "s" : "") + " active" + (actives > 1 ? "s" : ""));
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
        if (current == null) return;

        LocalDate date = historiqueDatePicker.getValue();
        if (date == null) {
            historique.setAll(missions.stream().filter(this::isTermine).collect(Collectors.toList()));
            return;
        }

        historique.setAll(signalementService.getSignalementsByZoneAndDate(current.getIdZone(), date)
                .stream().filter(this::isTermine).collect(Collectors.toList()));
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        profilNomField.setText(current.getNom());
        profilEmailField.setText(current.getEmail());
        profilZoneField.setText(zoneService.getZoneById(current.getIdZone()).getNomZone());
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
                String color = getStatusColor(item);
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
    }

    private String getAdresse(Signalement signalement) {
        return (signalement.getDescription() == null || signalement.getDescription().isBlank())
                ? "Adresse non précisée"
                : signalement.getDescription();
    }

    private String formatDate(Signalement signalement) {
        return signalement.getDateSignalement() == null ? "" : signalement.getDateSignalement().format(DATE_FORMATTER);
    }

    private boolean isTermine(Signalement signalement) {
        String statut = signalement.getStatut();
        return statut != null && ("Collecte".equalsIgnoreCase(statut) || "Termine".equalsIgnoreCase(statut));
    }

    private String getDisplayStatut(String statut) {
        if (statut == null) return "";
        return "Collecte".equalsIgnoreCase(statut) ? "Terminé" : statut;
    }
    
    private String getAgentZone() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        return current != null ? zoneService.getZoneById(current.getIdZone()).getNomZone() : "Pikine";
    }

    // === HANDLERS NAVIGATION ===
    
    @FXML private void handleDeconnexion() {
        gpsService.stopRealTimeTracking();
        SessionManager.logout();
        if (mainApp != null) mainApp.showLoginScreen();
    }

    @FXML private void handleShowAgentDashboard() { showPage(pageAgentDashboard, btnAgentDashboard); }
    @FXML private void handleShowMesMissions() { showPage(pageMesMissions, btnMesMissions); }
    @FXML private void handleShowCarteZones() { 
        showPage(pageCarteZones, btnCarteZones); 
        refreshMapWithGPS(); 
    }
    @FXML private void handleShowHistorique() { showPage(pageHistorique, btnHistorique); }
    @FXML private void handleShowMonProfil() { showPage(pageMonProfil, btnMonProfil); }
    @FXML private void handleToggleTheme() { SessionManager.toggleDarkMode(); applyTheme(); }
    @FXML private void handleFiltrerHistorique() { refreshHistorique(); showAgentMessage("Historique filtré.", true); }
    
    @FXML private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank()) {
            showAgentMessage("Nom et email obligatoires.", false);
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
            showAgentMessage("Échec mise à jour profil.", false);
        }
    }

    private void showAgentDashboardPage() {
        showPage(pageAgentDashboard, btnAgentDashboard);
    }

    private void showPage(VBox pageToShow, Button activeButton) {
        VBox[] pages = {pageAgentDashboard, pageMesMissions, pageCarteZones, pageHistorique, pageMonProfil};
        for (VBox page : pages) {
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }

        FadeTransition fade = new FadeTransition(Duration.millis(180), pageToShow);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        fade.play();

        Button[] buttons = {btnAgentDashboard, btnMesMissions, btnCarteZones, btnHistorique, btnMonProfil};
        for (Button btn : buttons) {
            btn.getStyleClass().remove("sidebar-agent-button-active");
            if (btn == activeButton) {
                btn.getStyleClass().add("sidebar-agent-button-active");
            }
        }
    }

    private void installTooltips() {
        btnAgentDashboard.setTooltip(new Tooltip("Vue globale avec GPS temps réel"));
        btnMesMissions.setTooltip(new Tooltip("Missions avec navigation GPS"));
        btnCarteZones.setTooltip(new Tooltip("Carte interactive temps réel"));
        btnHistorique.setTooltip(new Tooltip("Historique des missions"));
        btnMonProfil.setTooltip(new Tooltip("Profil agent"));
        
        if (btnActionDemarrerProchaine != null) {
            btnActionDemarrerProchaine.setTooltip(new Tooltip("Démarrer la mission la plus proche"));
        }
        if (btnActionItineraireRapide != null) {
            btnActionItineraireRapide.setTooltip(new Tooltip("Calculer itinéraire optimal GPS"));
        }
    }

    private void showAgentMessage(String message, boolean success) {
        String emoji = success ? "🎉" : "⚠️";
        String fullMessage = emoji + " " + message;
        
        agentMessageLabel.setText(fullMessage);
        
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13px;";
        if (success) {
            agentMessageLabel.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049); " + baseStyle);
        } else {
            agentMessageLabel.setStyle("-fx-background-color: linear-gradient(to right, #f44336, #da190b); " + baseStyle);
        }

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

    private void applyTheme() {
        if (rootPane == null) return;
        rootPane.getStyleClass().remove("dark-mode");
        if (SessionManager.isDarkMode()) {
            rootPane.getStyleClass().add("dark-mode");
            if (themeToggleButton != null) themeToggleButton.setText("Mode Clair");
        } else if (themeToggleButton != null) {
            themeToggleButton.setText("Mode Sombre");
        }
    }
}