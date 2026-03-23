package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controleur du dashboard citoyen.
 */
public class CitizenDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label citizenNameLabel;
    @FXML private Label citizenMessageLabel;
    @FXML private Button themeToggleButton;

    @FXML private Button btnCitizenDashboard;
    @FXML private Button btnAjouterSignalement;
    @FXML private Button btnMesSignalements;
    @FXML private Button btnMonProfil;

    @FXML private VBox pageCitizenDashboard;
    @FXML private VBox pageAjouterSignalement;
    @FXML private VBox pageMesSignalements;
    @FXML private VBox pageMonProfil;

    @FXML private Label citizenCardTotal;
    @FXML private Label citizenCardAttente;
    @FXML private Label citizenCardEnCours;
    @FXML private Label citizenCardCollectes;
    @FXML private PieChart citizenPieChart;

    @FXML private TextArea descriptionSignalementArea;
    @FXML private ComboBox<String> categorieSignalementCombo;
    @FXML private ComboBox<String> zoneSignalementCombo;
    @FXML private TextField photoSignalementField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private WebView mapWebView;

    @FXML private TableView<Signalement> tableMesSignalements;
    @FXML private TableColumn<Signalement, Integer> colMesId;
    @FXML private TableColumn<Signalement, String> colMesDescription;
    @FXML private TableColumn<Signalement, String> colMesCategorie;
    @FXML private TableColumn<Signalement, String> colMesZone;
    @FXML private TableColumn<Signalement, String> colMesDate;
    @FXML private TableColumn<Signalement, String> colMesStatut;

    @FXML private TextField profilNomField;
    @FXML private TextField profilEmailField;
    @FXML private ComboBox<String> profilZoneCombo;
    @FXML private TextField profilTelephoneField;

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();
    private final ObservableList<Signalement> mesSignalements = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null || !SessionManager.isCitoyen()) {
            showCitizenMessage("Acces reserve au role Citoyen.", false);
            return;
        }

        citizenNameLabel.setText(current.getNom());

        categorieSignalementCombo.setItems(FXCollections.observableArrayList("Plastique", "Papier", "Organique", "Verre"));
        zoneSignalementCombo.setItems(FXCollections.observableArrayList("Pikine", "Guediawaye"));
        profilZoneCombo.setItems(FXCollections.observableArrayList("Pikine", "Guediawaye"));

        configureMesSignalementsTable();
        tableMesSignalements.setItems(mesSignalements);

        applyTheme();
        showCitizenDashboardPage();
        chargerDonnees();
    }

    @FXML
    private void handleToggleTheme() {
        SessionManager.toggleDarkMode();
        applyTheme();
    }

    @FXML
    public void chargerDonnees() {
        refreshCards();
        refreshMesSignalements();
        refreshProfil();
    }

    @FXML
    private void handleDeconnexion() {
        SessionManager.logout();
        if (mainApp != null) {
            mainApp.showLoginScreen();
        }
    }

    @FXML
    private void handleShowCitizenDashboard() {
        showCitizenDashboardPage();
    }

    @FXML
    private void handleShowAjouterSignalement() {
        showPage(pageAjouterSignalement, btnAjouterSignalement);
    }

    @FXML
    private void handleShowMesSignalements() {
        showPage(pageMesSignalements, btnMesSignalements);
    }

    @FXML
    private void handleShowMonProfil() {
        showPage(pageMonProfil, btnMonProfil);
    }

    @FXML
    private void handleChoisirPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selected = chooser.showOpenDialog(MainApp.getPrimaryStage());
        if (selected != null) {
            photoSignalementField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleEnregistrerSignalement() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide. Reconnectez-vous.", false);
            return;
        }

        if (descriptionSignalementArea.getText().isBlank() ||
                categorieSignalementCombo.getValue() == null ||
                zoneSignalementCombo.getValue() == null) {
            showCitizenMessage("Description, categorie et zone sont obligatoires.", false);
            return;
        }

        Signalement signalement = new Signalement();
        signalement.setDescription(descriptionSignalementArea.getText().trim());
        signalement.setCategorie(categorieSignalementCombo.getValue());
        int idZone = zoneSignalementCombo.getValue().equals("Pikine") ? 1 : 2;
        signalement.setIdZone(idZone);
        signalement.setLatitude(latitudeField.getText().isBlank() ? 0.0 : Double.parseDouble(latitudeField.getText().trim()));
        signalement.setLongitude(longitudeField.getText().isBlank() ? 0.0 : Double.parseDouble(longitudeField.getText().trim()));
        signalement.setPhoto(photoSignalementField.getText().isBlank() ? null : photoSignalementField.getText().trim());
        signalement.setIdUser(current.getIdUser());

        if (signalementService.ajouterSignalement(signalement)) {
            showCitizenMessage("Signalement enregistre avec succes.", true);
            descriptionSignalementArea.clear();
            categorieSignalementCombo.setValue(null);
            zoneSignalementCombo.setValue(null);
            photoSignalementField.clear();
            latitudeField.clear();
            longitudeField.clear();
            chargerDonnees();
            showPage(pageMesSignalements, btnMesSignalements);
        } else {
            showCitizenMessage("Echec de l'enregistrement du signalement.", false);
        }
    }

    @FXML
    private void handleAnnulerSignalement() {
        descriptionSignalementArea.clear();
        categorieSignalementCombo.setValue(null);
        zoneSignalementCombo.setValue(null);
        photoSignalementField.clear();
        latitudeField.clear();
        longitudeField.clear();
        mapWebView.setVisible(false);
        mapWebView.setManaged(false);
    }

    @FXML
    private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide.", false);
            return;
        }

        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank() || profilZoneCombo.getValue() == null) {
            showCitizenMessage("Nom, email et zone sont obligatoires.", false);
            return;
        }

        Utilisateur updated = new Utilisateur();
        updated.setIdUser(current.getIdUser());
        updated.setNom(profilNomField.getText().trim());
        updated.setEmail(profilEmailField.getText().trim());
        int idZone = profilZoneCombo.getValue().equals("Pikine") ? 1 : 2;
        updated.setIdZone(idZone);

        if (utilisateurService.updateUtilisateur(updated)) {
            current.setNom(updated.getNom());
            current.setEmail(updated.getEmail());
            current.setIdZone(updated.getIdZone());
            citizenNameLabel.setText(current.getNom());
            showCitizenMessage("Profil mis a jour avec succes.", true);
        } else {
            showCitizenMessage("Echec de la mise a jour du profil.", false);
        }
    }

    @FXML
    private void handleSupprimerCompte() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide.", false);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer votre compte ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        if (utilisateurService.deleteUtilisateur(current.getIdUser())) {
            showCitizenMessage("Compte supprime. Deconnexion en cours.", true);
            SessionManager.logout();
            if (mainApp != null) {
                mainApp.showLoginScreen();
            }
        } else {
            showCitizenMessage("Echec de la suppression du compte.", false);
        }
    }

    @FXML
    private void handleToggleMap() {
        boolean visible = mapWebView.isVisible();
        mapWebView.setVisible(!visible);
        mapWebView.setManaged(!visible);
        if (!visible) {
            // Load interactive map with Leaflet
            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Selection de localisation</title>\n" +
                    "    <meta charset='utf-8' />\n" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                    "    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />\n" +
                    "    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
                    "    <style>\n" +
                    "        #map { height: 100vh; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div id='map'></div>\n" +
                    "    <script>\n" +
                    "        var map = L.map('map').setView([-14.7, -17.4], 10); // Pikine/Guediawaye\n" +
                    "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                    "            attribution: '© OpenStreetMap contributors'\n" +
                    "        }).addTo(map);\n" +
                    "        var marker;\n" +
                    "        map.on('click', function(e) {\n" +
                    "            if (marker) {\n" +
                    "                map.removeLayer(marker);\n" +
                    "            }\n" +
                    "            marker = L.marker(e.latlng).addTo(map);\n" +
                    "            // Send to JavaFX\n" +
                    "            if (window.javafx) {\n" +
                    "                window.javafx.setLocation(e.latlng.lat, e.latlng.lng);\n" +
                    "            }\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
            mapWebView.getEngine().loadContent(html);
            // Set up JavaScript bridge
            mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) mapWebView.getEngine().executeScript("window");
                    window.setMember("javafx", new JSBridge());
                }
            });
        }
    }

    public class JSBridge {
        public void setLocation(double lat, double lng) {
            latitudeField.setText(String.valueOf(lat));
            longitudeField.setText(String.valueOf(lng));
        }
    }

    private void refreshCards() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        int total = signalementService.countByUtilisateur(current.getIdUser());
        int attente = signalementService.countByStatutAndUtilisateur("En attente", current.getIdUser());
        int enCours = signalementService.countByStatutAndUtilisateur("En cours", current.getIdUser());
        int collectes = signalementService.countByStatutAndUtilisateur("Collecte", current.getIdUser());

        citizenCardTotal.setText(String.valueOf(total));
        citizenCardAttente.setText(String.valueOf(attente));
        citizenCardEnCours.setText(String.valueOf(enCours));
        citizenCardCollectes.setText(String.valueOf(collectes));

        citizenPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("En attente", attente),
                new PieChart.Data("En cours", enCours),
                new PieChart.Data("Collecte", collectes)
        ));
    }

    private void refreshMesSignalements() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }
        mesSignalements.setAll(signalementService.getSignalementsByUtilisateur(current.getIdUser()));
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            return;
        }

        profilNomField.setText(current.getNom());
        profilEmailField.setText(current.getEmail());
        profilZoneCombo.setValue(zoneService.getZoneById(current.getIdZone()).getNomZone());
    }

    private void configureMesSignalementsTable() {
        colMesId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMesDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMesCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colMesZone.setCellValueFactory(new PropertyValueFactory<>("zone"));
        colMesDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDateSignalement() == null ? "" : cell.getValue().getDateSignalement().format(DATE_FORMATTER)));
        colMesStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colMesId.setStyle("-fx-alignment: CENTER;");
        colMesDescription.setStyle("-fx-alignment: CENTER;");
        colMesCategorie.setStyle("-fx-alignment: CENTER;");
        colMesZone.setStyle("-fx-alignment: CENTER;");
        colMesDate.setStyle("-fx-alignment: CENTER;");
        colMesStatut.setStyle("-fx-alignment: CENTER;");

        colMesStatut.setCellFactory(col -> new TableCell<>() {
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
                } else if (item.toLowerCase().startsWith("collect")) {
                    color = "#2E7D32";
                }
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
    }

    private void showCitizenDashboardPage() {
        showPage(pageCitizenDashboard, btnCitizenDashboard);
    }

    private void showPage(VBox pageToShow, Button activeButton) {
        VBox[] pages = {pageCitizenDashboard, pageAjouterSignalement, pageMesSignalements, pageMonProfil};
        for (VBox page : pages) {
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }

        Button[] buttons = {btnCitizenDashboard, btnAjouterSignalement, btnMesSignalements, btnMonProfil};
        for (Button btn : buttons) {
            btn.getStyleClass().remove("sidebar-button-active");
            if (btn == activeButton) {
                btn.getStyleClass().add("sidebar-button-active");
            }
        }
    }

    private void showCitizenMessage(String message, boolean success) {
        if (citizenMessageLabel == null) {
            return;
        }
        citizenMessageLabel.setText(message);
        citizenMessageLabel.setStyle(success ? "-fx-background-color: rgba(67,160,71,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;" : "-fx-background-color: rgba(239,83,80,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");
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
}