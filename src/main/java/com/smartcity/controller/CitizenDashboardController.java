package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.model.Zone;
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

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CitizenDashboardController {

    @FXML private BorderPane rootPane;

    @FXML private Label citizenNameLabel;
    @FXML private Label citizenMessageLabel;

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
    @FXML private ComboBox<Zone> zoneSignalementCombo;
    @FXML private TextField photoSignalementField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;

    @FXML private TableView<Signalement> tableMesSignalements;

    @FXML private TableColumn<Signalement,Integer> colMesId;
    @FXML private TableColumn<Signalement,String> colMesDescription;
    @FXML private TableColumn<Signalement,String> colMesCategorie;
    @FXML private TableColumn<Signalement,String> colMesZone;
    @FXML private TableColumn<Signalement,String> colMesDate;
    @FXML private TableColumn<Signalement,String> colMesStatut;

    @FXML private TextField profilNomField;
    @FXML private TextField profilPrenomField;
    @FXML private TextField profilEmailField;
    @FXML private TextField profilAgeField;
    @FXML private ComboBox<Zone> profilZoneCombo;
    @FXML private TextField profilPhotoField;

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    private final ObservableList<Signalement> mesSignalements = FXCollections.observableArrayList();
    private final ObservableList<Zone> zones = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();

        if (current == null) {
            showCitizenMessage("Utilisateur non connecté", false);
            return;
        }

        citizenNameLabel.setText(current.getPrenom() + " " + current.getNom());

        // Charger les zones depuis la base de données
        chargerZones();

        // Configurer les ComboBox de zones avec affichage du nom
        zoneSignalementCombo.setItems(zones);
        zoneSignalementCombo.setCellFactory(param -> new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                if (empty || zone == null) {
                    setText(null);
                } else {
                    setText(zone.getNomZone());
                }
            }
        });
        zoneSignalementCombo.setButtonCell(new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                if (empty || zone == null) {
                    setText(null);
                } else {
                    setText(zone.getNomZone());
                }
            }
        });

        profilZoneCombo.setItems(zones);
        profilZoneCombo.setCellFactory(param -> new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                if (empty || zone == null) {
                    setText(null);
                } else {
                    setText(zone.getNomZone());
                }
            }
        });
        profilZoneCombo.setButtonCell(new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                if (empty || zone == null) {
                    setText(null);
                } else {
                    setText(zone.getNomZone());
                }
            }
        });

        // Configurer la ComboBox des catégories
        categorieSignalementCombo.setItems(FXCollections.observableArrayList(
                "PLASTIQUE", "PAPIER", "ORGANIQUE", "VERRE"
        ));

        configureMesSignalementsTable();
        tableMesSignalements.setItems(mesSignalements);

        showCitizenDashboardPage();
        chargerDonnees();
    }

    private void chargerZones() {
        List<Zone> listeZones = zoneService.getAllZones();
        zones.setAll(listeZones);
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
    private void handleChoisirPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selected = chooser.showOpenDialog(MainApp.getPrimaryStage());

        if (selected != null) {
            photoSignalementField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleChoisirPhotoProfil() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selected = chooser.showOpenDialog(MainApp.getPrimaryStage());

        if (selected != null) {
            profilPhotoField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleEnregistrerSignalement() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();

        if (current == null) {
            showCitizenMessage("Session invalide", false);
            return;
        }

        // Validation des champs obligatoires
        if (descriptionSignalementArea.getText().isBlank()
                || categorieSignalementCombo.getValue() == null
                || zoneSignalementCombo.getValue() == null) {

            showCitizenMessage("Description, catégorie et zone obligatoires", false);
            return;
        }

        // Validation des coordonnées (optionnelles mais recommandées)
        Double latitude = null;
        Double longitude = null;

        try {
            if (!latitudeField.getText().isBlank()) {
                latitude = Double.parseDouble(latitudeField.getText().trim());
            }
            if (!longitudeField.getText().isBlank()) {
                longitude = Double.parseDouble(longitudeField.getText().trim());
            }
        } catch (NumberFormatException e) {
            showCitizenMessage("Format de latitude/longitude invalide", false);
            return;
        }

        Signalement s = new Signalement();
        s.setDescription(descriptionSignalementArea.getText().trim());
        s.setCategorie(categorieSignalementCombo.getValue());

        // Récupérer l'ID de la zone sélectionnée
        Zone zoneSelectionnee = zoneSignalementCombo.getValue();
        s.setIdZone(zoneSelectionnee.getIdZone());

        s.setIdUser(current.getIdUser());
        s.setPhotoDepot(photoSignalementField.getText().isBlank() ? null : photoSignalementField.getText().trim());
        s.setLatitude(latitude);
        s.setLongitude(longitude);
        // Le statut est automatiquement "EN_ATTENTE" dans le service
        // La date est automatiquement ajoutée dans le service

        if (signalementService.ajouterSignalement(s)) {
            showCitizenMessage("Signalement enregistré avec succès", true);

            // Réinitialiser le formulaire
            descriptionSignalementArea.clear();
            categorieSignalementCombo.setValue(null);
            zoneSignalementCombo.setValue(null);
            photoSignalementField.clear();
            latitudeField.clear();
            longitudeField.clear();

            chargerDonnees();
        } else {
            showCitizenMessage("Erreur lors de l'enregistrement", false);
        }
    }

    @FXML
    private void handleModifierProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();

        if (current == null) {
            showCitizenMessage("Session invalide", false);
            return;
        }

        // Validation des champs
        if (profilNomField.getText().isBlank() || profilPrenomField.getText().isBlank()
                || profilEmailField.getText().isBlank()) {
            showCitizenMessage("Nom, prénom et email obligatoires", false);
            return;
        }

        // Validation de l'âge
        Integer age = null;
        try {
            if (!profilAgeField.getText().isBlank()) {
                age = Integer.parseInt(profilAgeField.getText().trim());
                if (age < 0 || age > 150) {
                    showCitizenMessage("Âge invalide (doit être entre 0 et 150)", false);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showCitizenMessage("Format d'âge invalide", false);
            return;
        }

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!profilEmailField.getText().equals(current.getEmail())) {
            Utilisateur existingUser = utilisateurService.getUtilisateurByEmail(profilEmailField.getText().trim());
            if (existingUser != null && existingUser.getIdUser() != current.getIdUser()) {
                showCitizenMessage("Cet email est déjà utilisé", false);
                return;
            }
        }

        // Mettre à jour les informations
        current.setNom(profilNomField.getText().trim());
        current.setPrenom(profilPrenomField.getText().trim());
        current.setEmail(profilEmailField.getText().trim());
        current.setAge(age);

        Zone zoneSelectionnee = profilZoneCombo.getValue();
        if (zoneSelectionnee != null) {
            current.setLocalite(zoneSelectionnee.getNomZone()); // Ou stocker l'ID selon votre modèle
        }

        if (!profilPhotoField.getText().isBlank()) {
            current.setPhotoProfil(profilPhotoField.getText().trim());
        }

        if (utilisateurService.updateUtilisateur(current)) {
            showCitizenMessage("Profil mis à jour avec succès", true);
            SessionManager.setUtilisateurConnecte(current);
            citizenNameLabel.setText(current.getPrenom() + " " + current.getNom());
        } else {
            showCitizenMessage("Erreur lors de la mise à jour du profil", false);
        }
    }

    @FXML
    private void handleVoirDetailsSignalement() {
        Signalement selected = tableMesSignalements.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Afficher les détails du signalement (à implémenter selon vos besoins)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails du signalement");
            alert.setHeaderText("Signalement #" + selected.getIdSignalement());

            String zoneNom = selected.getZoneNom() != null ? selected.getZoneNom() : "Zone #" + selected.getIdZone();

            String content = String.format(
                    "Description: %s\nCatégorie: %s\nZone: %s\nStatut: %s\nDate: %s",
                    selected.getDescription(),
                    selected.getCategorie(),
                    zoneNom,
                    selected.getStatut(),
                    selected.getDateSignalement() != null ? selected.getDateSignalement().format(DATE_FORMATTER) : "N/A"
            );

            alert.setContentText(content);
            alert.showAndWait();
        }
    }

    private void refreshCards() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        int total = signalementService.countByUtilisateur(current.getIdUser());
        int attente = signalementService.countByStatutAndUtilisateur("EN_ATTENTE", current.getIdUser());
        int enCours = signalementService.countByStatutAndUtilisateur("EN_COURS", current.getIdUser());
        int collectes = signalementService.countByStatutAndUtilisateur("COLLECTE", current.getIdUser());

        citizenCardTotal.setText(String.valueOf(total));
        citizenCardAttente.setText(String.valueOf(attente));
        citizenCardEnCours.setText(String.valueOf(enCours));
        citizenCardCollectes.setText(String.valueOf(collectes));

        // Mettre à jour le graphique uniquement s'il y a des données
        if (total > 0) {
            citizenPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("En attente", attente),
                    new PieChart.Data("En cours", enCours),
                    new PieChart.Data("Collecté", collectes)
            ));
        } else {
            citizenPieChart.setData(FXCollections.observableArrayList());
        }
    }

    private void refreshMesSignalements() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        mesSignalements.setAll(signalementService.getSignalementsByUtilisateur(current.getIdUser()));
    }

    private void refreshProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;

        profilNomField.setText(current.getNom() != null ? current.getNom() : "");
        profilPrenomField.setText(current.getPrenom() != null ? current.getPrenom() : "");
        profilEmailField.setText(current.getEmail() != null ? current.getEmail() : "");
       // profilAgeField.setText(current.getAge() != null ? String.valueOf(current.getAge()) : "");

        // Sélectionner la zone correspondante dans la ComboBox
        if (current.getLocalite() != null) {
            for (Zone zone : zones) {
                if (zone.getNomZone().equals(current.getLocalite())) {
                    profilZoneCombo.setValue(zone);
                    break;
                }
            }
        }

        profilPhotoField.setText(current.getPhotoProfil() != null ? current.getPhotoProfil() : "");
    }

    private void configureMesSignalementsTable() {
        colMesId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colMesDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMesCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        colMesZone.setCellValueFactory(cellData -> {
            Signalement signalement = cellData.getValue();
            String zoneNom = signalement.getZoneNom();
            if (zoneNom == null && signalement.getIdZone() > 0) {
                zoneNom = "Zone #" + signalement.getIdZone();
            }
            return new SimpleStringProperty(zoneNom);
        });

        colMesDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateSignalement() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(cellData.getValue().getDateSignalement().format(DATE_FORMATTER));
        });

        colMesStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            if (statut != null) {
                // Traduire les statuts pour l'affichage
                switch (statut) {
                    case "EN_ATTENTE": return new SimpleStringProperty("En attente");
                    case "EN_COURS": return new SimpleStringProperty("En cours");
                    case "COLLECTE": return new SimpleStringProperty("Collecté");
                    default: return new SimpleStringProperty(statut);
                }
            }
            return new SimpleStringProperty("");
        });

        // Ajouter un menu contextuel pour voir les détails
        ContextMenu contextMenu = new ContextMenu();
        MenuItem voirDetailsItem = new MenuItem("Voir détails");
        voirDetailsItem.setOnAction(event -> handleVoirDetailsSignalement());
        contextMenu.getItems().add(voirDetailsItem);

        tableMesSignalements.setContextMenu(contextMenu);

        // Double-clic pour voir les détails
        tableMesSignalements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleVoirDetailsSignalement();
            }
        });
    }

    @FXML
    private void showCitizenDashboardPage() {
        showPage(pageCitizenDashboard, btnCitizenDashboard);
    }

    @FXML
    private void showAjouterSignalementPage() {
        showPage(pageAjouterSignalement, btnAjouterSignalement);
    }

    @FXML
    private void showMesSignalementsPage() {
        showPage(pageMesSignalements, btnMesSignalements);
    }

    @FXML
    private void showMonProfilPage() {
        showPage(pageMonProfil, btnMonProfil);
    }

    private void showPage(VBox page, Button activeButton) {
        VBox[] pages = { pageCitizenDashboard, pageAjouterSignalement, pageMesSignalements, pageMonProfil };
        Button[] buttons = { btnCitizenDashboard, btnAjouterSignalement, btnMesSignalements, btnMonProfil };

        for (int i = 0; i < pages.length; i++) {
            boolean isActivePage = pages[i] == page;
            pages[i].setVisible(isActivePage);
            pages[i].setManaged(isActivePage);

            // Styler le bouton actif
            if (isActivePage) {
                buttons[i].getStyleClass().add("active-button");
            } else {
                buttons[i].getStyleClass().remove("active-button");
            }
        }
    }

    private void showCitizenMessage(String msg, boolean success) {
        citizenMessageLabel.setText(msg);
        citizenMessageLabel.setStyle(success
                ? "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 3;"
                : "-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 5; -fx-background-radius: 3;");

        // Faire disparaître le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() ->
                        citizenMessageLabel.setText("")
                );
            } catch (InterruptedException e) {
                // Ignorer
            }
        }).start();
    }
}