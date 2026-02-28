package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.utils.SessionManager;
import javafx.animation.FadeTransition;
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
 * Controleur du dashboard agent de collecte.
 */
public class AgentDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label agentNameLabel;
    @FXML private Label agentMessageLabel;
    @FXML private Button themeToggleButton;

    @FXML private Button btnAgentDashboard;
    @FXML private Button btnMesMissions;
    @FXML private Button btnCarteZones;
    @FXML private Button btnHistorique;
    @FXML private Button btnMonProfil;

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
    @FXML private TableColumn<Signalement, Void> colMissionAction;

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

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    private final ObservableList<Signalement> missions = FXCollections.observableArrayList();
    private final ObservableList<Signalement> missionsUrgentes = FXCollections.observableArrayList();
    private final ObservableList<Signalement> historique = FXCollections.observableArrayList();
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
            showAgentMessage("Acces reserve aux agents.", false);
            return;
        }

        agentNameLabel.setText(current.getNom());
        installTooltips();
        configureDashboardTable();
        configureMissionsTable();
        configureHistoriqueTable();

        tableMissionsUrgentes.setItems(missionsUrgentes);
        tableMesMissions.setItems(missions);
        tableHistorique.setItems(historique);

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
            updateMapDetails(newValue);
            refreshMap(newValue, false);
        });

        historiqueDatePicker.setValue(LocalDate.now());
        applyTheme();
        showAgentDashboardPage();
        chargerDonnees();
    }

    @FXML
    public void chargerDonnees() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        List<Signalement> missionList = signalementService.getSignalementsByZone(current.getZone());
        missions.setAll(missionList);

        missionsUrgentes.setAll(missionList.stream()
                .filter(s -> "En attente".equalsIgnoreCase(s.getStatut()))
                .collect(Collectors.toList()));

        refreshCards();
        refreshHistorique();
        refreshProfil();

        if (!missions.isEmpty() && tableMesMissions.getSelectionModel().getSelectedItem() == null) {
            tableMesMissions.getSelectionModel().selectFirst();
        }

        refreshMap(tableMesMissions.getSelectionModel().getSelectedItem(), false);
    }

    @FXML
    private void handleDeconnexion() {
        SessionManager.logout();
        if (mainApp != null) {
            mainApp.showLoginScreen();
        }
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
        Signalement selected = tableMesMissions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAgentMessage("Selectionnez une mission pour calculer l'itineraire.", false);
            return;
        }
        refreshMap(selected, true);
        showAgentMessage("Itineraire optimal affiche pour la mission #" + selected.getIdSignalement() + ".", true);
    }

    @FXML
    private void handleFiltrerHistorique() {
        refreshHistorique();
        showAgentMessage("Historique filtre applique.", true);
    }

    @FXML
    private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank()) {
            showAgentMessage("Nom et email sont obligatoires.", false);
            return;
        }

        Utilisateur updated = new Utilisateur();
        updated.setIdUser(current.getIdUser());
        updated.setNom(profilNomField.getText().trim());
        updated.setEmail(profilEmailField.getText().trim());
        updated.setZone(current.getZone());
        updated.setTelephone(current.getTelephone());

        if (utilisateurService.updateUtilisateur(updated)) {
            current.setNom(updated.getNom());
            current.setEmail(updated.getEmail());
            agentNameLabel.setText(updated.getNom());
            showAgentMessage("Profil mis a jour.", true);
        } else {
            showAgentMessage("Echec de mise a jour du profil.", false);
        }
    }

    @FXML
    private void handleModifierMotPasse() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau mot de passe:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            return;
        }

        current.setMotPasse(result.get());
        if (utilisateurService.updateUtilisateurAdmin(current)) {
            showAgentMessage("Mot de passe mis a jour.", true);
        } else {
            showAgentMessage("Echec de mise a jour du mot de passe.", false);
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
    }

    private void refreshHistorique() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        LocalDate date = historiqueDatePicker.getValue();
        if (date == null) {
            historique.setAll(missions.stream().filter(this::isTermine).collect(Collectors.toList()));
            return;
        }

        historique.setAll(signalementService.getSignalementsByZoneAndDate(current.getZone(), date)
                .stream().filter(this::isTermine).collect(Collectors.toList()));
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        profilNomField.setText(current.getNom());
        profilEmailField.setText(current.getEmail());
        profilZoneField.setText(current.getZone());
    }

    private void configureDashboardTable() {
        colUrgId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colUrgZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colUrgAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colUrgType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colUrgDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colUrgStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));

        setCommonColumnStyles(colUrgId, colUrgZone, colUrgAdresse, colUrgType, colUrgDate, colUrgStatut);
        applyStatutColorCell(colUrgStatut);
    }

    private void configureMissionsTable() {
        colMissionId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMissionZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colMissionAdresse.setCellValueFactory(cell -> new SimpleStringProperty(getAdresse(cell.getValue())));
        colMissionType.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colMissionDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue())));
        colMissionStatut.setCellValueFactory(cell -> new SimpleStringProperty(getDisplayStatut(cell.getValue().getStatut())));

        setCommonColumnStyles(colMissionId, colMissionZone, colMissionAdresse, colMissionType, colMissionDate, colMissionStatut);
        applyStatutColorCell(colMissionStatut);

        colMissionAction.setCellFactory(col -> new TableCell<>() {
            private final Button startButton = new Button("Demarrer mission");
            private final Button doneButton = new Button("Marquer termine");
            private final HBox box = new HBox(10, startButton, doneButton);

            {
                startButton.getStyleClass().add("button-agent");
                doneButton.getStyleClass().add("button-agent-success");
                startButton.getStyleClass().add("mission-primary");
                doneButton.getStyleClass().add("mission-primary");
                startButton.setPrefSize(160, 50);
                doneButton.setPrefSize(160, 50);

                startButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    demarrerMission(mission);
                });

                doneButton.setOnAction(evt -> {
                    Signalement mission = getTableView().getItems().get(getIndex());
                    marquerTermine(mission);
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
                startButton.setDisable(terminee || "En cours".equalsIgnoreCase(mission.getStatut()));
                doneButton.setDisable(terminee);
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

        setCommonColumnStyles(colHistId, colHistZone, colHistAdresse, colHistType, colHistDate, colHistStatut);
        applyStatutColorCell(colHistStatut);
    }

    @SafeVarargs
    private final void setCommonColumnStyles(TableColumn<Signalement, ?>... columns) {
        for (TableColumn<Signalement, ?> col : columns) {
            col.setStyle("-fx-alignment: CENTER;");
        }
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
                if ("En cours".equalsIgnoreCase(item)) {
                    color = "#1565C0";
                } else if ("Termine".equalsIgnoreCase(item)) {
                    color = "#2E7D32";
                }
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
    }

    private void demarrerMission(Signalement mission) {
        if (mission == null || isTermine(mission)) {
            return;
        }

        if (signalementService.updateStatut(mission.getIdSignalement(), "En cours")) {
            showAgentMessage("Mission #" + mission.getIdSignalement() + " demarree.", true);
            chargerDonnees();
        } else {
            showAgentMessage("Echec du demarrage de mission.", false);
        }
    }

    private void marquerTermine(Signalement mission) {
        if (mission == null || isTermine(mission)) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Confirmer la mission #" + mission.getIdSignalement() + " comme terminee ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (signalementService.updateStatut(mission.getIdSignalement(), "Collecte")) {
            showAgentMessage("Mission #" + mission.getIdSignalement() + " marquee terminee.", true);
            chargerDonnees();
        } else {
            showAgentMessage("Echec de mise a jour du statut.", false);
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
        mapMissionZoneLabel.setText(mission.getZone());
        mapMissionAdresseLabel.setText(getAdresse(mission));
        mapMissionStatutLabel.setText(getDisplayStatut(mission.getStatut()));
    }

    private void refreshMap(Signalement selectedMission, boolean showRoute) {
        if (mapWebView == null) {
            return;
        }
        mapWebView.getEngine().loadContent(buildLeafletHtml(selectedMission, showRoute));
    }

    private String buildLeafletHtml(Signalement selectedMission, boolean showRoute) {
        Point center = zoneCenter(getAgentZone());
        if (selectedMission != null) {
            center = missionPoint(selectedMission, indexOfMission(selectedMission));
        }

        StringBuilder markers = new StringBuilder();
        for (int i = 0; i < missions.size(); i++) {
            Signalement mission = missions.get(i);
            Point point = missionPoint(mission, i);
            String markerColor = statusColorHex(getDisplayStatut(mission.getStatut()));
            String popup = "<div class=\"popup-card\"><div class=\"popup-title\">Zone: "
                    + jsEscape(mission.getZone()) + "</div>"
                    + "<div class=\"popup-item\">Mission: #" + mission.getIdSignalement() + "</div>"
                    + "<div class=\"popup-item\">Type: " + jsEscape(mission.getCategorie()) + "</div>"
                    + "<div class=\"popup-item\">Niveau: " + jsEscape(getDisplayStatut(mission.getStatut())) + "</div>"
                    + "<div class=\"popup-item\">Adresse: " + jsEscape(getAdresse(mission)) + "</div></div>";
            markers.append("L.circleMarker([")
                    .append(fmt(point.lat))
                    .append(",")
                    .append(fmt(point.lon))
                    .append("],{radius:9,color:'")
                    .append(markerColor)
                    .append("',fillColor:'")
                    .append(markerColor)
                    .append("',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('")
                    .append(popup)
                    .append("');");
        }

        Point zoneCenter = zoneCenter(getAgentZone());
        int zoneCount = missions.size();
        String criticalJs = "L.circle([" + fmt(zoneCenter.lat) + "," + fmt(zoneCenter.lon) + "],{radius:"
                + Math.max(700, zoneCount * 120)
                + ",color:'#FFC107',fillColor:'#FFC107',fillOpacity:0.12,weight:2}).addTo(map);";

        String routeJs = "";
        if (showRoute && selectedMission != null) {
            Point start = zoneCenter(getAgentZone());
            Point end = missionPoint(selectedMission, indexOfMission(selectedMission));
            routeJs = "L.polyline([[" + fmt(start.lat) + "," + fmt(start.lon) + "],[" + fmt(end.lat) + "," + fmt(end.lon) + "]],"
                    + "{color:'#1565C0',weight:5,dashArray:'8 6'}).addTo(map);";
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<style>"
                + "html,body,#map{height:100%;margin:0;}"
                + ".leaflet-popup-content-wrapper{border-radius:14px;box-shadow:0 8px 22px rgba(0,0,0,.18);}"
                + ".leaflet-popup-content{margin:10px 12px;font-family:Segoe UI,Arial,sans-serif;font-size:12px;line-height:1.35;}"
                + ".popup-card{min-width:170px;}"
                + ".popup-title{font-size:13px;font-weight:700;color:#1B5E20;margin-bottom:6px;}"
                + ".popup-item{color:#334155;margin-bottom:2px;}"
                + "</style>"
                + "</head><body><div id='map'></div>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script>"
                + "var map=L.map('map').setView([" + fmt(center.lat) + "," + fmt(center.lon) + "],12);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
                + criticalJs
                + markers
                + routeJs
                + "</script></body></html>";
    }

    private int indexOfMission(Signalement mission) {
        for (int i = 0; i < missions.size(); i++) {
            if (missions.get(i).getIdSignalement() == mission.getIdSignalement()) {
                return i;
            }
        }
        return 0;
    }

    private Point missionPoint(Signalement mission, int index) {
        Point base = zoneCenter(mission.getZone());
        double latOffset = (index % 5) * 0.004 + 0.001;
        double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
        return new Point(base.lat + latOffset, base.lon + lonOffset);
    }

    private Point zoneCenter(String zone) {
        if (zone != null && zone.equalsIgnoreCase("Guediawaye")) {
            return new Point(14.7765, -17.4047);
        }
        return new Point(14.7646, -17.3920);
    }

    private String getAgentZone() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        return current != null ? current.getZone() : "Pikine";
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.6f", value);
    }

    private String jsEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ");
    }

    private String statusColorHex(String statut) {
        if ("En cours".equalsIgnoreCase(statut)) {
            return "#1E88E5";
        }
        if ("Termine".equalsIgnoreCase(statut)) {
            return "#43A047";
        }
        return "#FFC107";
    }

    private String getAdresse(Signalement signalement) {
        return (signalement.getDescription() == null || signalement.getDescription().isBlank())
                ? "Adresse non precisee"
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
        if (statut == null) {
            return "";
        }
        return "Collecte".equalsIgnoreCase(statut) ? "Termine" : statut;
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
        btnAgentDashboard.setTooltip(new Tooltip("Vue globale des missions du jour"));
        btnMesMissions.setTooltip(new Tooltip("Missions assignees et actions"));
        btnCarteZones.setTooltip(new Tooltip("Carte interactive des zones"));
        btnHistorique.setTooltip(new Tooltip("Historique des missions terminees"));
        btnMonProfil.setTooltip(new Tooltip("Informations du compte agent"));
    }

    private void showAgentMessage(String message, boolean success) {
        agentMessageLabel.setText(message);
        agentMessageLabel.setStyle(success
                ? "-fx-background-color: rgba(30,136,229,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;"
                : "-fx-background-color: rgba(239,83,80,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");

        messageClearDelay.stop();
        messageClearDelay.setOnFinished(evt -> agentMessageLabel.setText(""));
        messageClearDelay.playFromStart();
    }

    private void applyTheme() {
        if (rootPane == null) {
            return;
        }
        rootPane.getStyleClass().remove("dark-mode");
        if (SessionManager.isDarkMode()) {
            rootPane.getStyleClass().add("dark-mode");
            if (themeToggleButton != null) {
                themeToggleButton.setText("Mode Clair");
            }
        } else if (themeToggleButton != null) {
            themeToggleButton.setText("Mode Sombre");
        }
    }

    private static class Point {
        private final double lat;
        private final double lon;

        private Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}

