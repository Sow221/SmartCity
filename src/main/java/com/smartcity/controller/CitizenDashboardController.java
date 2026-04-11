package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controleur du dashboard citoyen.
 */
public class CitizenDashboardController {

    @FXML
    private BorderPane rootPane;
    @FXML private Label citizenNameLabel;
    @FXML private Label citizenAvatarLabel;
    @FXML private Label citizenMessageLabel;
    @FXML private Button themeToggleButton;

    @FXML
    private Button btnCitizenDashboard;
    @FXML
    private Button btnAjouterSignalement;
    @FXML
    private Button btnMesSignalements;
    @FXML
    private Button btnMonProfil;

    @FXML
    private ScrollPane pageCitizenDashboard;
    @FXML
    private javafx.scene.control.ScrollPane pageAjouterSignalement;
    @FXML
    private ScrollPane pageMesSignalements;
    @FXML
    private ScrollPane pageMonProfil;

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
    private Label positionLabel;

    @FXML private javafx.scene.web.WebView signalementMapView;
    @FXML private Button btnRecentrerCarte;
    @FXML private Label gpsPositionStatusLabel;
    @FXML private Label gpsReceivedIcon;

    // QR code / GPS téléphone (fallback)
    @FXML private javafx.scene.image.ImageView citizenQrCodeView;
    @FXML private Label citizenGpsUrlLabel;
    @FXML private Label gpsStatusLabel;

    private javafx.animation.Timeline gpsPollingTimeline;
    private double selectedLatitude  = 0.0;
    private double selectedLongitude = 0.0;
    private boolean mapBridgeInstalled = false;

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

    @FXML private TextField profilNomField;
    @FXML private TextField profilEmailField;
    @FXML private ComboBox<String> profilZoneCombo;
    @FXML private TextField profilTelephoneField;
    @FXML private Label citizenProfilAvatarLabel;
    @FXML private Label citizenProfilNomDisplay;
    @FXML private Button btnSupprimerSignalement;
    @FXML private Label badgeSignalements;
    @FXML private ComboBox<String> filterMesSignalementsStatut;
    @FXML private javafx.scene.control.DatePicker filterMesSignalementsDate;

    // Snapshot des statuts pour détecter les changements
    private final java.util.Map<Integer, String> statutsSnapshot = new java.util.HashMap<>();
    private javafx.animation.Timeline pollingTimeline;

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();
    private final com.smartcity.service.SuiviService suiviService = new com.smartcity.service.SuiviService();
    private final ObservableList<Signalement> mesSignalements = FXCollections.observableArrayList();
    private final java.util.Map<Integer, com.smartcity.service.SuiviService.SuiviSignalement> suiviCache = new java.util.HashMap<>();

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
            if (mainApp != null) javafx.application.Platform.runLater(() -> mainApp.showLoginScreen());
            return;
        }

        citizenNameLabel.setText(current.getNom());
        if (citizenAvatarLabel != null)
            citizenAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));

        categorieSignalementCombo.setItems(
                javafx.collections.FXCollections.observableArrayList(SignalementStatut.CATEGORIES));
        // Zones depuis la DB
        ObservableList<String> zonesItems = FXCollections.observableArrayList(
            zoneService.getAllZones().stream().map(z -> z.getNomZone()).collect(java.util.stream.Collectors.toList()));
        zoneSignalementCombo.setItems(zonesItems);
        profilZoneCombo.setItems(FXCollections.observableArrayList(zonesItems));

        configureMesSignalementsTable();
        tableMesSignalements.setItems(mesSignalements);
        if (btnSupprimerSignalement != null)
            btnSupprimerSignalement.disableProperty().bind(tableMesSignalements.getSelectionModel().selectedItemProperty().isNull());
        // Double-clic pour voir l'historique complet du signalement
        tableMesSignalements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement selected = tableMesSignalements.getSelectionModel().getSelectedItem();
                if (selected != null) afficherHistoriqueSignalement(selected);
            }
        });

        if (filterMesSignalementsStatut != null)
            filterMesSignalementsStatut.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Affecté", "En cours", "Terminé"));

        // Charger la carte dès l'init + recentrer sur changement de zone
        loadInteractiveMap();
        zoneSignalementCombo.valueProperty().addListener((obs, old, newZone) -> {
            if (newZone != null) loadInteractiveMap();
        });

        // Tooltips navigation
        btnCitizenDashboard.setTooltip(new Tooltip("Vue générale de vos signalements"));
        btnAjouterSignalement.setTooltip(new Tooltip("Signaler un déchet dans votre zone"));
        btnMesSignalements.setTooltip(new Tooltip("Consulter et suivre vos signalements"));
        btnMonProfil.setTooltip(new Tooltip("Modifier votre profil et mot de passe"));

        applyTheme();
        showCitizenDashboardPage();
        rootPane.setOnKeyPressed(e -> { if (e.getCode() == javafx.scene.input.KeyCode.F5) chargerDonnees(); });
        chargerDonnees();
        startPolling();
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        if (alert.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) return;
        stopPolling();
        SessionManager.logout();
        if (mainApp != null) mainApp.showLoginScreen();
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
        clearBadge();
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
            String fileName = selected.getName();
            if (!fileName.matches("[a-zA-Z0-9_\\-. ]+\\.(png|jpg|jpeg|gif|bmp)")) {
                showCitizenMessage("❌ Nom de fichier invalide.", false);
                return;
            }
            photoSignalementField.setText(fileName);
            showCitizenMessage("✅ Photo sélectionnée: " + fileName, true);
        }
    }

    @FXML
    private void handleEnregistrerSignalement() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            showCitizenMessage("Session invalide. Reconnectez-vous.", false);
            return;
        }

        if (descriptionSignalementArea.getText().isBlank()) {
            showCitizenMessage("❌ Description requise", false);
            descriptionSignalementArea.requestFocus();
            return;
        }

        if (categorieSignalementCombo.getValue() == null) {
            showCitizenMessage("❌ Veuillez choisir une catégorie", false);
            return;
        }

        if (zoneSignalementCombo.getValue() == null) {
            showCitizenMessage("❌ Veuillez choisir une zone", false);
            return;
        }

        // Bloquer si position non choisie sur la carte
        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            // Fallback : utiliser le centre de la zone sélectionnée
            com.smartcity.model.Zone z = zoneService.getAllZones().stream()
                .filter(zone -> zone.getNomZone().equals(zoneSignalementCombo.getValue()))
                .findFirst().orElse(null);
            if (z != null && (z.getLatitude() != 0.0 || z.getLongitude() != 0.0)) {
                selectedLatitude = z.getLatitude();
                selectedLongitude = z.getLongitude();
                showCitizenMessage("\u26a0\ufe0f Position GPS non re\u00e7ue \u2014 centre de zone utilis\u00e9 par d\u00e9faut.", false);
            } else {
                showCitizenMessage("❌ Cliquez sur la carte ou scannez le QR code pour obtenir votre position", false);
                return;
            }
        }

        // Validation sécurisée
        com.smartcity.utils.ValidationUtils.ValidationResult validation =
            com.smartcity.utils.ValidationUtils.validateSignalement(
                descriptionSignalementArea.getText().trim(),
                categorieSignalementCombo.getValue(),
                selectedLatitude, selectedLongitude);
        if (!validation.isValid()) {
            showCitizenMessage("❌ " + validation.getMessage(), false);
            return;
        }

        Signalement signalement = new Signalement();
        signalement.setDescription(descriptionSignalementArea.getText().trim());
        signalement.setCategorie(categorieSignalementCombo.getValue());
        int idZone = zoneService.getAllZones().stream()
            .filter(z -> z.getNomZone().equals(zoneSignalementCombo.getValue()))
            .mapToInt(com.smartcity.model.Zone::getIdZone)
            .findFirst().orElse(1);
        signalement.setIdZone(idZone);
        signalement.setLatitude(selectedLatitude);
        signalement.setLongitude(selectedLongitude);
        signalement.setPhoto(photoSignalementField.getText().isBlank() ? null : photoSignalementField.getText().trim());
        signalement.setIdUser(current.getIdUser());

        if (signalementService.ajouterSignalement(signalement)) {
            // Vérifier si le signalement a été affecté ou reste en attente
            String msg = SignalementStatut.AFFECTE.matches(signalement.getStatut())
                ? "Signalement enregistr\u00e9 et affect\u00e9 \u00e0 un agent !"
                : "Signalement enregistr\u00e9. Un agent sera assign\u00e9 d\u00e8s que possible.";
            showCitizenMessage(msg, true);
            clearForm();
            chargerDonnees();
            showPage(pageMesSignalements, btnMesSignalements);
        } else {
            showCitizenMessage("Erreur lors de l'enregistrement. R\u00e9essayez.", false);
        }
    }

    @FXML
    private void handleSupprimerSignalement() {
        Signalement selected = tableMesSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!SignalementStatut.EN_ATTENTE.matches(selected.getStatut())) {
            showCitizenMessage("❌ Seuls les signalements en attente peuvent être supprimés.", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer le signalement #" + selected.getIdSignalement() + " ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;
        if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
            showCitizenMessage("✅ Signalement supprimé.", true);
            chargerDonnees();
        } else {
            showCitizenMessage("❌ Echec de la suppression.", false);
        }
    }

    @FXML
    private void handleResetFiltresMesSignalements() {
        if (filterMesSignalementsStatut != null) filterMesSignalementsStatut.setValue(null);
        if (filterMesSignalementsDate != null) filterMesSignalementsDate.setValue(null);
        refreshMesSignalements();
    }

    @FXML
    private void handleFiltrerMesSignalements() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        String statut = (filterMesSignalementsStatut != null
            && filterMesSignalementsStatut.getValue() != null
            && !"Tous".equals(filterMesSignalementsStatut.getValue()))
            ? filterMesSignalementsStatut.getValue() : null;
        java.time.LocalDate date = filterMesSignalementsDate != null ? filterMesSignalementsDate.getValue() : null;
        java.util.List<Signalement> tous = signalementService.getSignalementsByUtilisateur(current.getIdUser());
        mesSignalements.setAll(tous.stream()
            .filter(s -> statut == null || com.smartcity.model.SignalementStatut.fromAny(statut) == com.smartcity.model.SignalementStatut.fromAny(s.getStatut()))
            .filter(s -> date == null || (s.getDateSignalement() != null && date.equals(s.getDateSignalement().toLocalDate())))
            .collect(java.util.stream.Collectors.toList()));
    }

    @FXML
    private void handleAnnulerSignalement() {
        clearForm();
        showCitizenMessage("📝 Formulaire remis à zéro", true);
    }

    private void clearForm() {
        descriptionSignalementArea.clear();
        categorieSignalementCombo.setValue(null);
        zoneSignalementCombo.setValue(null);
        photoSignalementField.clear();
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        loadInteractiveMap();
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

        String newEmail = profilEmailField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        if (!newEmail.equals(current.getEmail()) && utilisateurService.emailExiste(newEmail)) {
            showCitizenMessage("Cet email est déjà utilisé par un autre compte.", false);
            return;
        }

        Utilisateur updated = new Utilisateur();
        updated.setIdUser(current.getIdUser());
        updated.setPrenom(current.getPrenom());
        updated.setNom(profilNomField.getText().trim());
        updated.setEmail(newEmail);
        updated.setAge(current.getAge());
        // Téléphone stocké dans localite
        updated.setLocalite(profilTelephoneField != null ? profilTelephoneField.getText().trim() : current.getLocalite());
        updated.setPhotoProfil(current.getPhotoProfil());
        int idZone = zoneService.getAllZones().stream()
            .filter(z -> z.getNomZone().equals(profilZoneCombo.getValue()))
            .mapToInt(com.smartcity.model.Zone::getIdZone)
            .findFirst().orElse(1);
        updated.setIdZone(idZone);

        if (utilisateurService.updateUtilisateur(updated)) {
            current.setNom(updated.getNom());
            current.setEmail(updated.getEmail());
            current.setIdZone(updated.getIdZone());
            citizenNameLabel.setText(current.getNom());
            if (citizenAvatarLabel != null)
                citizenAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (citizenProfilAvatarLabel != null)
                citizenProfilAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (citizenProfilNomDisplay != null)
                citizenProfilNomDisplay.setText(current.getNom());
            showCitizenMessage("Profil mis a jour avec succes.", true);
        } else {
            showCitizenMessage("Echec de la mise a jour du profil.", false);
        }
    }

    @FXML
    private void handleModifierMotPasse() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mot de passe");
        dialog.setHeaderText(null);
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Nouveau mot de passe");
        dialog.getDialogPane().setContent(pwField);
        dialog.setResultConverter(bt -> bt == saveType ? pwField.getText() : null);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        com.smartcity.utils.ValidationUtils.ValidationResult check =
            com.smartcity.utils.ValidationUtils.validatePassword(result.get());
        if (!check.isValid()) {
            showCitizenMessage(check.getMessage(), false);
            return;
        }
        if (utilisateurService.updateMotDePasse(current.getIdUser(), result.get())) {
            showCitizenMessage("Mot de passe mis à jour.", true);
        } else {
            showCitizenMessage("Echec de la mise à jour du mot de passe.", false);
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

    // ── Carte interactive + GPS ──────────────────────────────────────────────

    private void loadInteractiveMap() {
        if (signalementMapView == null) return;
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        selectedLatitude  = 0.0;
        selectedLongitude = 0.0;
        updatePositionUI(false, 0, 0);

        // Centrer sur la zone sélectionnée ou zone de l'utilisateur
        String zoneChoisie = zoneSignalementCombo.getValue();
        com.smartcity.service.GeolocationService.Coordinates center;
        com.smartcity.service.ZoneService zs = zoneService;
        if (zoneChoisie != null) {
            center = zs.getCenter(zoneChoisie);
        } else {
            center = zs.getCenterById(current.getIdZone());
        }

        String html = buildSignalementMapHtml(center.lat, center.lon);
        signalementMapView.getEngine().loadContent(html);

        // Bridge Java ← JavaScript (clic sur carte)
        if (!mapBridgeInstalled) {
            mapBridgeInstalled = true;
            signalementMapView.getEngine().getLoadWorker().stateProperty().addListener((obs, o, n) -> {
                if (n == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        netscape.javascript.JSObject win =
                            (netscape.javascript.JSObject) signalementMapView.getEngine().executeScript("window");
                        win.setMember("javaCitizen", new MapBridgeCitizen());
                    } catch (Exception ex) {
                        logger.warn("Bridge carte citoyen non installe", ex);
                    }
                }
            });
        }

        // QR code téléphone (fallback — fonctionne sur même WiFi)
        int citizenId = current.getIdUser();
        String qrUrl = com.smartcity.service.GpsApiServer.getCitizenGpsPageUrl(citizenId);
        if (citizenQrCodeView != null) {
            javafx.scene.image.Image qr = com.smartcity.utils.QrCodeUtils.generateQrCode(qrUrl, 180);
            if (qr != null) citizenQrCodeView.setImage(qr);
        }
        if (citizenGpsUrlLabel != null) citizenGpsUrlLabel.setText(qrUrl);
        if (gpsStatusLabel != null) gpsStatusLabel.setText("📱 Ou scannez le QR code (même WiFi requis)");
        if (gpsReceivedIcon != null) gpsReceivedIcon.setText("⏳");

        // Polling position téléphone (toutes les 3s, silencieux)
        startGpsPolling(citizenId);
    }

    /** HTML Leaflet : clic sur carte → javaCitizen.setPosition(lat, lon) */
    private String buildSignalementMapHtml(double centerLat, double centerLon) {
        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            + "<style>html,body,#map{height:100%;margin:0;}</style></head><body>"
            + "<div id='map'></div>"
            + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
            + "<script>"
            + "var map=L.map('map').setView([" + String.format(java.util.Locale.US, "%.6f,%.6f", centerLat, centerLon) + "],14);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
            + "var marker=null;"
            + "map.on('click',function(e){"
            + "  var lat=e.latlng.lat,lon=e.latlng.lng;"
            + "  if(marker)map.removeLayer(marker);"
            + "  marker=L.marker([lat,lon]).addTo(map).bindPopup('\uD83D\uDCCD D\u00e9chet signal\u00e9').openPopup();"
            + "  if(window.javaCitizen)window.javaCitizen.setPosition(lat,lon);"
            + "});"
            + "</script></body></html>";
    }

    /** Pont JavaScript → Java pour le clic sur la carte */
    public class MapBridgeCitizen {
        public void setPosition(double lat, double lon) {
            javafx.application.Platform.runLater(() -> onPositionSelected(lat, lon, false));
        }
    }

    @FXML
    private void handleRecentrerCarte() {
        loadInteractiveMap();
    }

    private void onPositionSelected(double lat, double lon, boolean fromPhone) {
        selectedLatitude  = lat;
        selectedLongitude = lon;
        updatePositionUI(true, lat, lon);
        if (fromPhone) stopGpsPolling();
        showCitizenMessage(String.format("📍 Position %s: %.5f, %.5f",
            fromPhone ? "GPS t\u00e9l\u00e9phone" : "s\u00e9lectionn\u00e9e", lat, lon), true);
    }

    private void updatePositionUI(boolean received, double lat, double lon) {
        if (positionLabel != null) {
            if (received) {
                positionLabel.setText(String.format("✅ %.5f, %.5f", lat, lon));
                positionLabel.setStyle("-fx-text-fill:#2E7D32;-fx-font-weight:bold;");
            } else {
                positionLabel.setText("Cliquez sur la carte pour placer le marqueur");
                positionLabel.setStyle("");
            }
        }
        if (gpsReceivedIcon != null) gpsReceivedIcon.setText(received ? "📍" : "⏳");
        if (gpsPositionStatusLabel != null)
            gpsPositionStatusLabel.setText(received
                ? String.format("Position re\u00e7ue : %.5f, %.5f", lat, lon)
                : "Cliquez sur la carte pour placer le marqueur");
    }

    // Polling position téléphone (QR code fallback)
    private void startGpsPolling(int citizenId) {
        stopGpsPolling();
        String token = com.smartcity.service.GpsApiServer.getOrCreateCitizenToken(citizenId);
        String pollUrl = "http://localhost:" + com.smartcity.service.GpsApiServer.PORT
            + "/api/citizen-position?citizenId=" + citizenId + "&token=" + token;
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(1)).build();
        gpsPollingTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                // Ne pas poller si position déjà obtenue
                if (selectedLatitude != 0.0 || selectedLongitude != 0.0) return;
                try {
                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(pollUrl))
                        .timeout(java.time.Duration.ofSeconds(1)).GET().build();
                    java.net.http.HttpResponse<String> resp =
                        httpClient.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                    if (resp.statusCode() == 200) {
                        com.google.gson.JsonObject json =
                            com.google.gson.JsonParser.parseString(resp.body()).getAsJsonObject();
                        double lat = json.get("lat").getAsDouble();
                        double lon = json.get("lon").getAsDouble();
                        javafx.application.Platform.runLater(() -> {
                            onPositionSelected(lat, lon, true);
                            // Placer le marqueur sur la carte aussi
                            if (signalementMapView != null) {
                                signalementMapView.getEngine().executeScript(
                                    String.format(java.util.Locale.US,
                                        "if(marker)map.removeLayer(marker);"
                                        + "marker=L.marker([%.6f,%.6f]).addTo(map).bindPopup('\uD83D� GPS t\u00e9l\u00e9phone').openPopup();"
                                        + "map.setView([%.6f,%.6f],16);",
                                        lat, lon, lat, lon));
                            }
                        });
                    }
                } catch (Exception ex) { /* silencieux */ }
            })
        );
        gpsPollingTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        gpsPollingTimeline.play();
    }

    private void stopGpsPolling() {
        if (gpsPollingTimeline != null) {
            gpsPollingTimeline.stop();
            gpsPollingTimeline = null;
        }
    }

    private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(CitizenDashboardController.class);


    private void refreshCards() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        int total     = signalementService.countByUtilisateur(current.getIdUser());
        int attente   = signalementService.countByStatutAndUtilisateur(SignalementStatut.EN_ATTENTE.label(), current.getIdUser());
        int affecte   = signalementService.countByStatutAndUtilisateur(SignalementStatut.AFFECTE.label(), current.getIdUser());
        int enCours   = signalementService.countByStatutAndUtilisateur(SignalementStatut.EN_COURS.label(), current.getIdUser()) + affecte;
        int collectes = signalementService.countByStatutAndUtilisateur(SignalementStatut.TERMINE.label(), current.getIdUser());

        boolean dashboardVisible = pageCitizenDashboard != null && pageCitizenDashboard.isVisible();
        if (dashboardVisible) {
            com.smartcity.utils.AnimationUtils.animateCounter(citizenCardTotal,    0, total).play();
            com.smartcity.utils.AnimationUtils.animateCounter(citizenCardAttente,  0, attente).play();
            com.smartcity.utils.AnimationUtils.animateCounter(citizenCardEnCours,  0, enCours).play();
            com.smartcity.utils.AnimationUtils.animateCounter(citizenCardCollectes, 0, collectes).play();
        } else {
            citizenCardTotal.setText(String.valueOf(total));
            citizenCardAttente.setText(String.valueOf(attente));
            citizenCardEnCours.setText(String.valueOf(enCours));
            citizenCardCollectes.setText(String.valueOf(collectes));
        }

        if (total > 0) {
            citizenPieChart.setAnimated(dashboardVisible);
            citizenPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("En attente (" + attente + ")", Math.max(attente, 0.01)),
                new PieChart.Data("En cours (" + enCours + ")", Math.max(enCours, 0.01)),
                new PieChart.Data("Termin\u00e9 (" + collectes + ")", Math.max(collectes, 0.01))));
            citizenPieChart.setLabelsVisible(true);
            citizenPieChart.setLegendVisible(true);
        } else {
            citizenPieChart.setData(FXCollections.observableArrayList());
        }
    }

    private void refreshMesSignalements() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        mesSignalements.setAll(signalementService.getSignalementsByUtilisateur(current.getIdUser()));
        // Pré-charger le suivi en une seule passe pour éviter N+1
        suiviCache.clear();
        for (Signalement s : mesSignalements) {
            com.smartcity.service.SuiviService.SuiviSignalement suivi = suiviService.getSuiviComplet(s.getIdSignalement());
            if (suivi != null) suiviCache.put(s.getIdSignalement(), suivi);
        }
        refreshSuiviColumns(suiviCache);
        tableMesSignalements.refresh();
    }

    private void refreshSuiviColumns(java.util.Map<Integer, com.smartcity.service.SuiviService.SuiviSignalement> suiviMap) {
        for (TableColumn<Signalement, ?> col : tableMesSignalements.getColumns()) {
            if ("Agent".equals(col.getText())) {
                @SuppressWarnings("unchecked")
                TableColumn<Signalement, String> colAgent = (TableColumn<Signalement, String>) col;
                colAgent.setCellValueFactory(cell -> {
                    com.smartcity.service.SuiviService.SuiviSignalement suivi = suiviMap.get(cell.getValue().getIdSignalement());
                    return new SimpleStringProperty(suivi != null && suivi.nomAgent != null ? suivi.nomAgent : "-");
                });
            } else if ("D\u00e9lai".equals(col.getText())) {
                @SuppressWarnings("unchecked")
                TableColumn<Signalement, String> colDelai = (TableColumn<Signalement, String>) col;
                colDelai.setCellValueFactory(cell -> {
                    com.smartcity.service.SuiviService.SuiviSignalement suivi = suiviMap.get(cell.getValue().getIdSignalement());
                    if (suivi == null) return new SimpleStringProperty("-");
                    if (suivi.heuresResolution != null) return new SimpleStringProperty(suivi.heuresResolution + "h \u2705");
                    return new SimpleStringProperty(suivi.heuresEcoules + "h");
                });
            }
        }
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        profilNomField.setText(current.getNom());
        profilEmailField.setText(current.getEmail());
        if (citizenProfilAvatarLabel != null)
            citizenProfilAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
        if (citizenProfilNomDisplay != null)
            citizenProfilNomDisplay.setText(current.getNom());
        if (profilTelephoneField != null)
            profilTelephoneField.setText(current.getLocalite() != null ? current.getLocalite() : "");
        com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());
        if (zone != null) profilZoneCombo.setValue(zone.getNomZone());
    }

    private void configureMesSignalementsTable() {
        colMesId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMesDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMesCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colMesZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
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
                if (empty || item == null) { setText(null); setStyle("-fx-alignment: CENTER;"); return; }
                setText(item);
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: "
                    + SignalementStatut.couleur(item) + ";");
            }
        });

        // Colonne Agent affecté (suivi citoyen)
        TableColumn<Signalement, String> colAgent = new TableColumn<>("Agent");
        colAgent.setStyle("-fx-alignment: CENTER;");
        colAgent.setPrefWidth(130);
        colAgent.setCellValueFactory(cell -> {
            com.smartcity.service.SuiviService.SuiviSignalement suivi =
                suiviCache.get(cell.getValue().getIdSignalement());
            String agent = (suivi != null && suivi.nomAgent != null) ? suivi.nomAgent : "-";
            return new SimpleStringProperty(agent);
        });

        // Colonne Délai (heures écoulées)
        TableColumn<Signalement, String> colDelai = new TableColumn<>("Délai");
        colDelai.setStyle("-fx-alignment: CENTER;");
        colDelai.setPrefWidth(90);
        colDelai.setCellValueFactory(cell -> {
            com.smartcity.service.SuiviService.SuiviSignalement suivi =
                suiviCache.get(cell.getValue().getIdSignalement());
            if (suivi == null) return new SimpleStringProperty("-");
            if (suivi.heuresResolution != null)
                return new SimpleStringProperty(suivi.heuresResolution + "h ✅");
            return new SimpleStringProperty(suivi.heuresEcoules + "h");
        });

        // Colonne Évaluation (bouton pour les terminés)
        TableColumn<Signalement, Void> colEval = new TableColumn<>("★ Évaluer");
        colEval.setStyle("-fx-alignment: CENTER;");
        colEval.setPrefWidth(100);
        colEval.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("★ Évaluer");
            { btn.setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:3 8;");
              btn.setOnAction(e -> ouvrirEvaluation(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Signalement s = getTableView().getItems().get(getIndex());
                boolean termine = SignalementStatut.TERMINE.matches(s.getStatut());
                com.smartcity.service.SuiviService.EvaluationEntry eval =
                    termine ? suiviService.getEvaluationBySignalement(s.getIdSignalement()) : null;
                if (!termine) { setGraphic(null); return; }
                if (eval != null) {
                    btn.setText("\u2605".repeat(eval.note));
                    btn.setStyle("-fx-background-color:#4CAF50;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:3 8;");
                }
                setGraphic(btn);
            }
        });

        if (tableMesSignalements.getColumns().size() <= 6) {
            tableMesSignalements.getColumns().addAll(colAgent, colDelai, colEval);
        }
    }

    private void afficherHistoriqueSignalement(Signalement signalement) {
        java.util.List<com.smartcity.service.SuiviService.HistoriqueEntry> historique =
            suiviService.getHistoriqueBySignalement(signalement.getIdSignalement());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historique #" + signalement.getIdSignalement());
        dialog.setHeaderText(signalement.getCategorie() + " — " + signalement.getZoneNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8);
        content.setPadding(new javafx.geometry.Insets(10));
        content.setPrefWidth(480);

        if (historique.isEmpty()) {
            content.getChildren().add(new Label("Aucun historique disponible."));
        } else {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (com.smartcity.service.SuiviService.HistoriqueEntry e : historique) {
                String date = e.dateChangement != null ? e.dateChangement.format(fmt) : "";
                String auteur = e.nomAuteur != null ? " par " + e.nomAuteur : "";
                Label lbl = new Label(date + " — " + e.ancienStatut + " → " + e.nouveauStatut + auteur);
                lbl.setStyle("-fx-font-size:12px;");
                content.getChildren().add(lbl);
                if (e.commentaire != null && !e.commentaire.isBlank()) {
                    Label comment = new Label("   ↳ " + e.commentaire);
                    comment.setStyle("-fx-font-size:11px; -fx-text-fill:#555;");
                    content.getChildren().add(comment);
                }
            }
        }
        dialog.getDialogPane().setContent(new javafx.scene.control.ScrollPane(content));
        dialog.showAndWait();
    }

    private void ouvrirEvaluation(Signalement signalement) {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("★ Évaluer la collecte");
        dialog.setHeaderText("Signalement #" + signalement.getIdSignalement()
            + " — " + signalement.getCategorie());
        ButtonType saveType = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(12);
        content.setPadding(new javafx.geometry.Insets(10));
        Label lblNote = new Label("Note (1 = mauvais, 5 = excellent) :");
        javafx.scene.control.Slider slider = new javafx.scene.control.Slider(1, 5, 3);
        slider.setMajorTickUnit(1); slider.setMinorTickCount(0);
        slider.setSnapToTicks(true); slider.setShowTickLabels(true); slider.setShowTickMarks(true);
        Label lblValeur = new Label("★★★");
        slider.valueProperty().addListener((obs, o, n) ->
            lblValeur.setText("★".repeat(n.intValue()) + "☆".repeat(5 - n.intValue())));
        TextArea taComment = new TextArea();
        taComment.setPromptText("Commentaire optionnel...");
        taComment.setPrefRowCount(3); taComment.setWrapText(true);
        content.getChildren().addAll(lblNote, slider, lblValeur,
            new Label("Commentaire :"), taComment);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(bt -> bt == saveType
            ? new int[]{(int) slider.getValue()}
            : null);
        dialog.showAndWait().ifPresent(res -> {
            if (suiviService.ajouterEvaluation(signalement.getIdSignalement(),
                    current.getIdUser(), res[0], taComment.getText().trim())) {
                showCitizenMessage("✅ Merci pour votre évaluation !", true);
                refreshMesSignalements();
            } else {
                showCitizenMessage("❌ Erreur lors de l'envoi.", false);
            }
        });
    }

    public void cleanup() {
        stopPolling();
        stopGpsPolling();
        mapBridgeInstalled = false;
    }

    private void startPolling() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        // Snapshot initial
        signalementService.getSignalementsByUtilisateur(current.getIdUser())
            .forEach(s -> statutsSnapshot.put(s.getIdSignalement(), s.getStatut()));
        // Polling toutes les 30 secondes
        pollingTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> checkStatutChanges()));
        pollingTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pollingTimeline.play();
    }

    private void stopPolling() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
            pollingTimeline = null;
        }
    }

    private void checkStatutChanges() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) {
            stopPolling();
            javafx.application.Platform.runLater(() -> {
                if (mainApp != null) mainApp.showLoginScreen();
            });
            return;
        }
        // Requête légère : seulement id + statut, pas le signalement complet
        java.util.List<com.smartcity.model.Signalement> liste =
            signalementService.getSignalementsByUtilisateur(current.getIdUser());
        int nbChangements = 0;
        for (com.smartcity.model.Signalement s : liste) {
            String ancienStatut = statutsSnapshot.get(s.getIdSignalement());
            if (ancienStatut != null && !ancienStatut.equals(s.getStatut())) {
                nbChangements++;
            }
            statutsSnapshot.put(s.getIdSignalement(), s.getStatut());
        }
        // Stopper le pulse si la page signalements est déjà visible
        if (nbChangements > 0) {
            final int n = nbChangements;
            javafx.application.Platform.runLater(() -> {
                if (pageMesSignalements != null && pageMesSignalements.isVisible()) {
                    clearBadge();
                } else {
                    updateBadge(n);
                    showCitizenMessage(n + " signalement(s) mis \u00e0 jour !", true);
                }
            });
        }
    }

    private void updateBadge(int count) {
        if (badgeSignalements == null) return;
        if (count <= 0) {
            badgeSignalements.setVisible(false);
            badgeSignalements.setManaged(false);
        } else {
            badgeSignalements.setText(count > 9 ? "9+" : String.valueOf(count));
            badgeSignalements.setVisible(true);
            badgeSignalements.setManaged(true);
            // Animation pulse
            javafx.animation.ScaleTransition pulse =
                new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), badgeSignalements);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.3);   pulse.setToY(1.3);
            pulse.setAutoReverse(true); pulse.setCycleCount(4);
            pulse.play();
        }
    }

    private void clearBadge() {
        // Mettre à jour le snapshot avec les statuts actuels
        mesSignalements.forEach(s -> statutsSnapshot.put(s.getIdSignalement(), s.getStatut()));
        updateBadge(0);
    }

    private void showCitizenDashboardPage() {
        showPage(pageCitizenDashboard, btnCitizenDashboard);
    }

    private void showPage(javafx.scene.Node pageToShow, Button activeButton) {
        com.smartcity.utils.NavigationUtils.showPage(
            pageToShow,
            new javafx.scene.Node[]{pageCitizenDashboard, pageAjouterSignalement, pageMesSignalements, pageMonProfil},
            activeButton,
            new Button[]{btnCitizenDashboard, btnAjouterSignalement, btnMesSignalements, btnMonProfil},
            "sidebar-button-active"
        );
    }

    private final javafx.animation.PauseTransition citizenMsgDelay =
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));

    private void showCitizenMessage(String message, boolean success) {
        if (citizenMessageLabel == null) return;
        citizenMessageLabel.setVisible(true);
        citizenMessageLabel.setManaged(true);
        citizenMessageLabel.setText(message);
        citizenMessageLabel.getStyleClass().removeAll("message-success", "message-error");
        citizenMessageLabel.getStyleClass().add(success ? "message-success" : "message-error");
        citizenMessageLabel.setOpacity(1);
        citizenMsgDelay.stop();
        citizenMsgDelay.setOnFinished(e -> {
            javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), citizenMessageLabel);
            fade.setFromValue(1); fade.setToValue(0);
            fade.setOnFinished(ev -> citizenMessageLabel.setText(""));
            fade.play();
        });
        citizenMsgDelay.playFromStart();
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

    /**
     * Copie le lien GPS dans le presse-papiers (uitility button near QR code)
     */
    @FXML
    private void handleCopyGpsLink() {
        if (citizenGpsUrlLabel != null && citizenGpsUrlLabel.getText() != null) {
            String url = citizenGpsUrlLabel.getText();
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(url);
            clipboard.setContent(content);
            showCitizenMessage("✅ Lien GPS copié : " + url, true);
        }
    }
}
