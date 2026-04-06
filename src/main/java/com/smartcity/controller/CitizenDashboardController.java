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
    private Label positionLabel;
    @FXML
    private WebView mapWebView;

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

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

    @FXML private Button btnSupprimerSignalement;

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
        zoneSignalementCombo.setItems(FXCollections.observableArrayList("Pikine", "Guédiawaye"));
        profilZoneCombo.setItems(FXCollections.observableArrayList("Pikine", "Guédiawaye"));

        configureMesSignalementsTable();
        tableMesSignalements.setItems(mesSignalements);
        if (btnSupprimerSignalement != null)
            btnSupprimerSignalement.disableProperty().bind(tableMesSignalements.getSelectionModel().selectedItemProperty().isNull());

        // Charger la carte dès l'init
        loadInteractiveMap();

        // Tooltips navigation
        btnCitizenDashboard.setTooltip(new Tooltip("Vue générale de vos signalements"));
        btnAjouterSignalement.setTooltip(new Tooltip("Signaler un déchet dans votre zone"));
        btnMesSignalements.setTooltip(new Tooltip("Consulter et suivre vos signalements"));
        btnMonProfil.setTooltip(new Tooltip("Modifier votre profil et mot de passe"));

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
            showCitizenMessage("❌ Placez le marqueur sur la carte pour indiquer la position exacte", false);
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
            showCitizenMessage("🎉 Signalement enregistré avec succès !", true);
            clearForm();
            chargerDonnees();
            showPage(pageMesSignalements, btnMesSignalements);
        } else {
            showCitizenMessage("❌ Erreur lors de l'enregistrement. Réessayez.", false);
        }
    }

    @FXML
    private void handleSupprimerSignalement() {
        Signalement selected = tableMesSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!"En attente".equalsIgnoreCase(selected.getStatut())) {
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
        positionLabel.setText("⚠️ Cliquez sur la carte pour placer le déchet");
        positionLabel.setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
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

        Utilisateur updated = new Utilisateur();
        updated.setIdUser(current.getIdUser());
        updated.setPrenom(current.getPrenom());
        updated.setNom(profilNomField.getText().trim());
        updated.setEmail(profilEmailField.getText().trim());
        updated.setAge(current.getAge());
        updated.setLocalite(current.getLocalite());
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
            showCitizenMessage("Profil mis a jour avec succes.", true);
        } else {
            showCitizenMessage("Echec de la mise a jour du profil.", false);
        }
    }

    @FXML
    private void handleModifierMotPasse() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau mot de passe :");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        current.setMotDePasse(result.get());
        if (utilisateurService.updateUtilisateurAdmin(current)) {
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

    private void loadInteractiveMap() {
        String selectedZone = zoneSignalementCombo.getValue();
        double centerLat = (selectedZone != null && selectedZone.equals("Guédiawaye")) ? 14.7765 : 14.7646;
        double centerLon = (selectedZone != null && selectedZone.equals("Guédiawaye")) ? -17.4047 : -17.3920;

        // Construire le HTML sans marqueur par défaut - le citoyen DOIT cliquer
        String html = "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            + "<style>"
            + "html,body,#map{height:100%;margin:0;}"
            + "#map{cursor:crosshair;}"
            + "#hint{position:absolute;top:10px;left:50%;transform:translateX(-50%);z-index:1000;"
            + "background:rgba(255,255,255,0.93);padding:8px 18px;border-radius:20px;"
            + "font-size:13px;color:#1565C0;font-weight:bold;"
            + "box-shadow:0 2px 8px rgba(0,0,0,0.18);pointer-events:none;}"
            + "</style></head><body>"
            + "<div id='hint'>&#128205; Cliquez sur la carte pour placer le d&eacute;chet</div>"
            + "<div id='map'></div>"
            + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
            + "<script>"
            + "var map = L.map('map').setView([" + centerLat + "," + centerLon + "],15);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',"
            + "{maxZoom:19,attribution:'&copy; OpenStreetMap'}).addTo(map);"
            + "var marker = null;"
            + "map.on('click', function(e) {"
            + "  if (marker) map.removeLayer(marker);"
            + "  marker = L.marker(e.latlng).addTo(map)"
            + "    .bindPopup('&#128205; Position s&eacute;lectionn&eacute;e<br/>'"
            + "      + e.latlng.lat.toFixed(5) + ', ' + e.latlng.lng.toFixed(5)).openPopup();"
            + "  document.getElementById('hint').style.color='#2E7D32';"
            + "  document.getElementById('hint').innerHTML='&#9989; Position enregistr&eacute;e';"
            + "  if (window.javafx) window.javafx.setLocation(e.latlng.lat, e.latlng.lng);"
            + "});"
            + "</script></body></html>";

        mapWebView.getEngine().loadContent(html);
        // Enregistrer le pont JS après chargement complet
        mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject win = (JSObject) mapWebView.getEngine().executeScript("window");
                win.setMember("javafx", new JSBridge());
            }
        });
    }

    public class JSBridge {
        public void setLocation(double lat, double lng) {
            selectedLatitude = lat;
            selectedLongitude = lng;
            positionLabel.setText(String.format("✅ Position: %.5f, %.5f", lat, lng));
            positionLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            showCitizenMessage(String.format("📍 Position enregistrée: %.5f, %.5f", lat, lng), true);
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
        int collectes = signalementService.countByStatutAndUtilisateur("Terminé", current.getIdUser());

        com.smartcity.utils.AnimationUtils.animateCounter(citizenCardTotal, 0, total).play();
        com.smartcity.utils.AnimationUtils.animateCounter(citizenCardAttente, 0, attente).play();
        com.smartcity.utils.AnimationUtils.animateCounter(citizenCardEnCours, 0, enCours).play();
        com.smartcity.utils.AnimationUtils.animateCounter(citizenCardCollectes, 0, collectes).play();

        if (total > 0) {
            citizenPieChart.setAnimated(true);
            citizenPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("En attente (" + attente + ")", attente),
                new PieChart.Data("En cours (" + enCours + ")", enCours),
                new PieChart.Data("Terminé (" + collectes + ")", collectes)));
        }
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
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                    return;
                }
                setText(item);
                String color = "#F57C00";
                if ("En cours".equalsIgnoreCase(item)) {
                    color = "#1565C0";
                } else if ("Terminé".equalsIgnoreCase(item)) {
                    color = "#2E7D32";
                } else if ("Affecté".equalsIgnoreCase(item)) {
                    color = "#7B1FA2";
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
        pageToShow.setOpacity(0);
        javafx.animation.FadeTransition fade =
            new javafx.animation.FadeTransition(javafx.util.Duration.millis(180), pageToShow);
        fade.setFromValue(0.2); fade.setToValue(1.0); fade.play();

        Button[] buttons = { btnCitizenDashboard, btnAjouterSignalement, btnMesSignalements, btnMonProfil };
        for (Button btn : buttons) {
            btn.getStyleClass().remove("sidebar-button-active");
            if (btn == activeButton) {
                btn.getStyleClass().add("sidebar-button-active");
            }
        }
    }

    private javafx.animation.PauseTransition citizenMsgDelay =
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));

    private void showCitizenMessage(String message, boolean success) {
        if (citizenMessageLabel == null) return;
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
}