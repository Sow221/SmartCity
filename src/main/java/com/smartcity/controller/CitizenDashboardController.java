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

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label citizenNameLabel;
    @FXML
    private Label citizenMessageLabel;
    @FXML
    private Button themeToggleButton;

    @FXML
    private Button btnCitizenDashboard;
    @FXML
    private Button btnAjouterSignalement;
    @FXML
    private Button btnMesSignalements;
    @FXML
    private Button btnMonProfil;

    @FXML
    private VBox pageCitizenDashboard;
    @FXML
    private VBox pageAjouterSignalement;
    @FXML
    private VBox pageMesSignalements;
    @FXML
    private VBox pageMonProfil;

    @FXML
    private Label citizenCardTotal;
    @FXML
    private Label citizenCardAttente;
    @FXML
    private Label citizenCardEnCours;
    @FXML
    private Label citizenCardCollectes;
    @FXML
    private PieChart citizenPieChart;

    @FXML
    private TextArea descriptionSignalementArea;
    @FXML
    private ComboBox<String> categorieSignalementCombo;
    @FXML
    private ComboBox<String> zoneSignalementCombo;
    @FXML
    private TextField photoSignalementField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private WebView mapWebView;

    @FXML
    private TableView<Signalement> tableMesSignalements;
    @FXML
    private TableColumn<Signalement, Integer> colMesId;
    @FXML
    private TableColumn<Signalement, String> colMesDescription;
    @FXML
    private TableColumn<Signalement, String> colMesCategorie;
    @FXML
    private TableColumn<Signalement, String> colMesZone;
    @FXML
    private TableColumn<Signalement, String> colMesDate;
    @FXML
    private TableColumn<Signalement, String> colMesStatut;

    @FXML
    private TextField profilNomField;
    @FXML
    private TextField profilEmailField;
    @FXML
    private ComboBox<String> profilZoneCombo;
    @FXML
    private TextField profilTelephoneField;

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

        categorieSignalementCombo
                .setItems(FXCollections.observableArrayList("Plastique", "Papier", "Organique", "Verre"));
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
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        // Démarrer dans le dossier Images par défaut
        File userHome = new File(System.getProperty("user.home"));
        File picturesDir = new File(userHome, "Pictures");
        if (picturesDir.exists()) {
            chooser.setInitialDirectory(picturesDir);
        }

        File selected = chooser.showOpenDialog(MainApp.getPrimaryStage());
        if (selected != null) {
            photoSignalementField.setText(selected.getName()); // Juste le nom, pas le chemin complet
            showCitizenMessage("✅ Photo sélectionnée: " + selected.getName(), true);
        }
    }

    @FXML
    private void handleEnregistrerSignalement() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide. Reconnectez-vous.", false);
            return;
        }

        // ✅ Validation améliorée
        if (descriptionSignalementArea.getText().isBlank()) {
            showCitizenMessage("❌ Description requise", false);
            descriptionSignalementArea.requestFocus();
            return;
        }

        if (categorieSignalementCombo.getValue() == null) {
            showCitizenMessage("❌ Veuillez choisir une catégorie", false);
            categorieSignalementCombo.requestFocus();
            return;
        }

        if (zoneSignalementCombo.getValue() == null) {
            showCitizenMessage("❌ Veuillez choisir une zone", false);
            zoneSignalementCombo.requestFocus();
            return;
        }

        // ✅ Coordonnées automatiques si pas saisies
        double latitude = 0.0;
        double longitude = 0.0;

        try {
            if (!latitudeField.getText().isBlank()) {
                latitude = Double.parseDouble(latitudeField.getText().trim());
            } else {
                // Coordonnées par défaut selon la zone
                if (zoneSignalementCombo.getValue().equals("Guediawaye")) {
                    latitude = 14.7765;
                    longitude = -17.4047;
                } else {
                    latitude = 14.7646;
                    longitude = -17.3920;
                }
            }

            if (!longitudeField.getText().isBlank()) {
                longitude = Double.parseDouble(longitudeField.getText().trim());
            }
        } catch (NumberFormatException e) {
            showCitizenMessage("❌ Coordonnées invalides. Laissez vides pour utiliser la position par défaut.", false);
            return;
        }

        Signalement signalement = new Signalement();
        signalement.setDescription(descriptionSignalementArea.getText().trim());
        signalement.setCategorie(categorieSignalementCombo.getValue());
        int idZone = zoneSignalementCombo.getValue().equals("Pikine") ? 1 : 2;
        signalement.setIdZone(idZone);
        signalement.setLatitude(latitude);
        signalement.setLongitude(longitude);
        signalement.setPhoto(photoSignalementField.getText().isBlank() ? null : photoSignalementField.getText().trim());
        signalement.setIdUser(current.getIdUser());

        if (signalementService.ajouterSignalement(signalement)) {
            showCitizenMessage("🎉 Signalement enregistré avec succès !", true);
            // Nettoyer le formulaire
            clearForm();
            chargerDonnees();
            showPage(pageMesSignalements, btnMesSignalements);
        } else {
            showCitizenMessage("❌ Erreur lors de l'enregistrement. Réessayez.", false);
        }
    }

    @FXML
    private void handleAnnulerSignalement() {
        clearForm();
        showCitizenMessage("📝 Formulaire remis à zéro", true);
    }

    /**
     * Nettoie le formulaire de signalement
     */
    private void clearForm() {
        descriptionSignalementArea.clear();
        categorieSignalementCombo.setValue(null);
        zoneSignalementCombo.setValue(null);
        photoSignalementField.clear();
        latitudeField.clear();
        longitudeField.clear();

        if (mapWebView.isVisible()) {
            mapWebView.setVisible(false);
            mapWebView.setManaged(false);
        }
    }

    @FXML
    private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide.", false);
            return;
        }

        if (profilNomField.getText().isBlank() || profilEmailField.getText().isBlank()
                || profilZoneCombo.getValue() == null) {
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
            showCitizenMessage("🗺️ Cliquez sur la carte pour choisir la position", true);
            loadInteractiveMap();
        } else {
            showCitizenMessage("📍 Carte fermée - coordonnées conservées", true);
        }
    }

    /**
     * Charge la carte interactive avec un meilleur design
     */
    private void loadInteractiveMap() {
        String selectedZone = zoneSignalementCombo.getValue();
        double defaultLat = selectedZone != null && selectedZone.equals("Guediawaye") ? 14.7765 : 14.7646;
        double defaultLon = selectedZone != null && selectedZone.equals("Guediawaye") ? -17.4047 : -17.3920;

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>📍 Choisir l'emplacement</title>\n" +
                "    <meta charset='utf-8' />\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />\n" +
                "    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; font-family: Arial, sans-serif; }\n" +
                "        #map { height: 100vh; cursor: crosshair; }\n" +
                "        .info-panel {\n" +
                "            position: absolute; top: 10px; right: 10px; z-index: 1000;\n" +
                "            background: white; padding: 15px; border-radius: 8px;\n" +
                "            box-shadow: 0 2px 10px rgba(0,0,0,0.2); max-width: 200px;\n" +
                "        }\n" +
                "        .coordinates { font-size: 12px; color: #666; }\n" +
                "        .geoloc-btn {\n" +
                "            display: inline-block; margin-top: 10px; padding: 6px 12px; background: #2196F3; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; transition: background 0.2s;\n"
                +
                "        }\n" +
                "        .geoloc-btn:hover { background: #1976D2; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='info-panel'>\n" +
                "        <h4 style='margin: 0 0 10px 0; color: #2196F3;'>📍 Position</h4>\n" +
                "        <div id='coords' class='coordinates'>Cliquez sur la carte</div>\n" +
                "        <button class='geoloc-btn' onclick='useGeolocation()'>Utiliser ma position</button>\n" +
                "        <small style='color: #888;'>Zone: "
                + (selectedZone != null ? selectedZone : "Non sélectionnée") + "</small>\n" +
                "    </div>\n" +
                "    <div id='map'></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([" + defaultLat + ", " + defaultLon + "], 14);\n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "            attribution: '© OpenStreetMap contributors'\n" +
                "        }).addTo(map);\n" +
                "        \n" +
                "        // Marqueur par défaut\n" +
                "        var marker = L.marker([" + defaultLat + ", " + defaultLon + "]).addTo(map)\n" +
                "            .bindPopup('📍 Position par défaut - Cliquez ailleurs pour modifier')\n" +
                "            .openPopup();\n" +
                "        \n" +
                "        // Cercle de zone\n" +
                "        L.circle([" + defaultLat + ", " + defaultLon + "], {\n" +
                "            radius: 1000, fillColor: '#2196F3', color: '#1976D2', fillOpacity: 0.1\n" +
                "        }).addTo(map);\n" +
                "        \n" +
                "        function updateCoords(lat, lng) {\n" +
                "            document.getElementById('coords').innerHTML = \n" +
                "                'Lat: ' + lat.toFixed(6) + '<br/>Lng: ' + lng.toFixed(6);\n" +
                "            if (window.javafx) {\n" +
                "                window.javafx.setLocation(lat, lng);\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Initialiser coordonnées\n" +
                "        updateCoords(" + defaultLat + ", " + defaultLon + ");\n" +
                "        \n" +
                "        map.on('click', function(e) {\n" +
                "            map.removeLayer(marker);\n" +
                "            marker = L.marker(e.latlng).addTo(map)\n" +
                "                .bindPopup('📍 Position sélectionnée')\n" +
                "                .openPopup();\n" +
                "            updateCoords(e.latlng.lat, e.latlng.lng);\n" +
                "        });\n" +
                "        function useGeolocation() {\n" +
                "            if (navigator.geolocation) {\n" +
                "                navigator.geolocation.getCurrentPosition(function(position) {\n" +
                "                    var lat = position.coords.latitude;\n" +
                "                    var lng = position.coords.longitude;\n" +
                "                    map.setView([lat, lng], 16);\n" +
                "                    map.removeLayer(marker);\n" +
                "                    marker = L.marker([lat, lng]).addTo(map)\n" +
                "                        .bindPopup('📍 Ma position actuelle')\n" +
                "                        .openPopup();\n" +
                "                    updateCoords(lat, lng);\n" +
                "                }, function(error) {\n" +
                "                    alert('Impossible d\'obtenir la position : ' + error.message);\n" +
                "                });\n" +
                "            } else {\n" +
                "                alert('La géolocalisation n\'est pas supportée par ce navigateur.');\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        mapWebView.getEngine().loadContent(html);

        // Configuration du pont JavaScript
        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) mapWebView.getEngine().executeScript("window");
                window.setMember("javafx", new JSBridge());
            }
        });
    }

    public class JSBridge {
        public void setLocation(double lat, double lng) {
            // ✅ Mise à jour avec formatage décent
            latitudeField.setText(String.format("%.6f", lat));
            longitudeField.setText(String.format("%.6f", lng));
            showCitizenMessage(String.format("📍 Position mise à jour: %.4f, %.4f", lat, lng), true);
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
                new PieChart.Data("Collecte", collectes)));
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
                cell.getValue().getDateSignalement() == null ? ""
                        : cell.getValue().getDateSignalement().format(DATE_FORMATTER)));
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
        VBox[] pages = { pageCitizenDashboard, pageAjouterSignalement, pageMesSignalements, pageMonProfil };
        for (VBox page : pages) {
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }

        Button[] buttons = { btnCitizenDashboard, btnAjouterSignalement, btnMesSignalements, btnMonProfil };
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
        citizenMessageLabel.setStyle(success
                ? "-fx-background-color: rgba(67,160,71,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;"
                : "-fx-background-color: rgba(239,83,80,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");
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