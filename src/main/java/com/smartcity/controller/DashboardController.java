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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le tableau de bord
 */
public class DashboardController {

    @FXML private BorderPane rootPane;

    // Header
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label dashboardTitle;

    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnSignalements;
    @FXML private Button btnCarte;
    @FXML private Button btnStats;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnParametres;

    // Pages
    @FXML private VBox pageDashboard;
    @FXML private VBox pageSignalements;
    @FXML private VBox pageCarte;
    @FXML private VBox pageStats;
    @FXML private VBox pageUtilisateurs;
    @FXML private VBox pageParametres;

    // Dashboard page components
    @FXML private Label totalSignalementsLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label collectesLabel;

    @FXML private PieChart pieChartStatuts;
    @FXML private BarChart<String, Number> barChartZones;
    @FXML private TableView<Signalement> recentSignalementsTable;

    // Signalements page components
    @FXML private TableView<Signalement> tableSignalements;
    @FXML private TableColumn<Signalement, Integer> colId;
    @FXML private TableColumn<Signalement, String> colDescription;
    @FXML private TableColumn<Signalement, String> colCategorie;
    @FXML private TableColumn<Signalement, String> colZone;
    @FXML private TableColumn<Signalement, String> colStatut;
    @FXML private TableColumn<Signalement, String> colDate;
    @FXML private TableColumn<Signalement, String> colUtilisateur;

    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<Zone> filterZoneCombo;
    @FXML private ComboBox<String> filterCategorieCombo;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;

    // Stats page components
    @FXML private PieChart pieChartCategories;
    @FXML private BarChart<String, Number> barChartEvolution;

    // Utilisateurs page components (pour admin)
    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colUserId;
    @FXML private TableColumn<Utilisateur, String> colUserNom;
    @FXML private TableColumn<Utilisateur, String> colUserPrenom;
    @FXML private TableColumn<Utilisateur, String> colUserEmail;
    @FXML private TableColumn<Utilisateur, String> colUserRole;
    @FXML private TableColumn<Utilisateur, String> colUserLocalite;

    // Services
    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    // Observable lists
    private final ObservableList<Signalement> listeSignalements = FXCollections.observableArrayList();
    private final ObservableList<Signalement> recentSignalements = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> listeUtilisateurs = FXCollections.observableArrayList();
    private final ObservableList<Zone> zones = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MainApp mainApp;

    public DashboardController() {
        // Initialisation par défaut
    }

    /**
     * Définit la référence à MainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initialise le contrôleur
     */
    @FXML
    private void initialize() {
        Utilisateur currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) return;

        // Afficher les infos de l'utilisateur
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        userRoleLabel.setText("Rôle: " + currentUser.getRole());
        dashboardTitle.setText("Tableau de bord - " + currentUser.getRole());

        // Charger les zones
        chargerZones();

        // Configurer les tableaux
        configurerTableSignalements();
        configurerTableRecentSignalements();
        configurerTableUtilisateurs();

        // Configurer les filtres
        configurerFiltres();

        // Afficher la page appropriée selon le rôle
        configurerNavigationParRole();

        // Charger les données initiales
        chargerDonnees();

        // Afficher la page dashboard par défaut
        showPage(pageDashboard, btnDashboard);
    }

    /**
     * Charge toutes les zones depuis la base de données
     */
    private void chargerZones() {
        List<Zone> listeZones = zoneService.getAllZones();
        zones.setAll(listeZones);
    }

    /**
     * Configure les colonnes du tableau des signalements
     */
    private void configurerTableSignalements() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        // Colonne zone avec nom
        colZone.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            String zoneNom = getNomZoneForSignalement(s);
            return new SimpleStringProperty(zoneNom);
        });

        // Colonne statut avec traduction
        colStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(traduireStatut(statut));
        });

        // Colonne date formatée
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateSignalement() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateSignalement().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        // Colonne utilisateur
        colUtilisateur.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            String nomUtilisateur = getNomUtilisateurForSignalement(s);
            return new SimpleStringProperty(nomUtilisateur);
        });

        tableSignalements.setItems(listeSignalements);

        // Ajouter un menu contextuel
        ajouterMenuContextuelSignalements();
    }

    /**
     * Configure le tableau des signalements récents
     */
    private void configurerTableRecentSignalements() {
        TableColumn<Signalement, Integer> recentColId = new TableColumn<>("ID");
        recentColId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));

        TableColumn<Signalement, String> recentColDesc = new TableColumn<>("Description");
        recentColDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        recentColDesc.setPrefWidth(200);

        TableColumn<Signalement, String> recentColZone = new TableColumn<>("Zone");
        recentColZone.setCellValueFactory(cellData -> {
            Signalement s = cellData.getValue();
            return new SimpleStringProperty(getNomZoneForSignalement(s));
        });

        TableColumn<Signalement, String> recentColStatut = new TableColumn<>("Statut");
        recentColStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(traduireStatut(statut));
        });

        TableColumn<Signalement, String> recentColDate = new TableColumn<>("Date");
        recentColDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateSignalement() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateSignalement().format(DATE_ONLY_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        recentSignalementsTable.getColumns().setAll(recentColId, recentColDesc, recentColZone, recentColStatut, recentColDate);
        recentSignalementsTable.setItems(recentSignalements);
    }

    /**
     * Configure le tableau des utilisateurs (admin seulement)
     */
    private void configurerTableUtilisateurs() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserLocalite.setCellValueFactory(new PropertyValueFactory<>("localite"));

        tableUtilisateurs.setItems(listeUtilisateurs);
    }

    /**
     * Configure les filtres
     */
    private void configurerFiltres() {
        // Filtre statut
        filterStatutCombo.setItems(FXCollections.observableArrayList(
                "Tous", "EN_ATTENTE", "EN_COURS", "COLLECTE"
        ));
        filterStatutCombo.setValue("Tous");

        // Filtre zone avec affichage du nom
        filterZoneCombo.setItems(zones);
        filterZoneCombo.setCellFactory(param -> new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                setText(empty || zone == null ? null : zone.getNomZone());
            }
        });
        filterZoneCombo.setButtonCell(new ListCell<Zone>() {
            @Override
            protected void updateItem(Zone zone, boolean empty) {
                super.updateItem(zone, empty);
                setText(empty || zone == null ? "Toutes les zones" : zone.getNomZone());
            }
        });

        // Filtre catégorie
        filterCategorieCombo.setItems(FXCollections.observableArrayList(
                "Tous", "PLASTIQUE", "PAPIER", "ORGANIQUE", "VERRE"
        ));
        filterCategorieCombo.setValue("Tous");
    }

    /**
     * Configure la navigation selon le rôle
     */
    private void configurerNavigationParRole() {
        if (SessionManager.isCitoyen()) {
            // Les citoyens ne voient pas certaines pages
            btnUtilisateurs.setVisible(false);
            btnUtilisateurs.setManaged(false);
            btnStats.setVisible(false);
            btnStats.setManaged(false);
        } else if (SessionManager.isAgent()) {
            // Les agents voient presque tout sauf gestion des utilisateurs
            btnUtilisateurs.setVisible(false);
            btnUtilisateurs.setManaged(false);
        }
        // Admin voit tout
    }

    /**
     * Ajoute un menu contextuel au tableau des signalements
     */
    private void ajouterMenuContextuelSignalements() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemVoirDetails = new MenuItem("Voir détails");
        itemVoirDetails.setOnAction(e -> voirDetailsSignalement());

        MenuItem itemChangerStatut = new MenuItem("Changer statut");
        itemChangerStatut.setOnAction(e -> changerStatutSignalement());

        MenuItem itemAffecterAgent = new MenuItem("Affecter à un agent");
        itemAffecterAgent.setOnAction(e -> affecterAgent());

        MenuItem itemSupprimer = new MenuItem("Supprimer");
        itemSupprimer.setOnAction(e -> supprimerSignalement());

        contextMenu.getItems().addAll(itemVoirDetails, itemChangerStatut, itemAffecterAgent, new SeparatorMenuItem(), itemSupprimer);

        tableSignalements.setContextMenu(contextMenu);

        // Double-clic pour voir les détails
        tableSignalements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                voirDetailsSignalement();
            }
        });
    }

    /**
     * Charge les données selon le rôle de l'utilisateur connecté
     */
    @FXML
    public void chargerDonnees() {
        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
        if (utilisateur == null) return;

        List<Signalement> signalements;

        if (SessionManager.isAdmin()) {
            signalements = signalementService.getAllSignalements();
            chargerUtilisateurs();
        } else if (SessionManager.isAgent()) {
            // Les agents voient les signalements de leur zone
            Zone zoneAgent = zoneService.getZoneByNom(utilisateur.getLocalite());
            if (zoneAgent != null) {
                signalements = signalementService.getSignalementsByZone(zoneAgent.getIdZone());
            } else {
                signalements = signalementService.getAllSignalements();
            }
        } else {
            signalements = signalementService.getSignalementsByUtilisateur(utilisateur.getIdUser());
        }

        listeSignalements.clear();
        listeSignalements.addAll(signalements);

        // Mettre à jour les signalements récents (5 derniers)
        recentSignalements.clear();
        recentSignalements.addAll(signalements.stream()
                .limit(5)
                .collect(Collectors.toList()));

        mettreAJourStatistiques();
    }

    /**
     * Charge la liste des utilisateurs (admin seulement)
     */
    private void chargerUtilisateurs() {
        if (SessionManager.isAdmin()) {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            listeUtilisateurs.clear();
            listeUtilisateurs.addAll(utilisateurs);
        }
    }

    /**
     * Met à jour les statistiques et graphiques
     */
    private void mettreAJourStatistiques() {
        Utilisateur currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) return;

        int userId = currentUser.getIdUser();

        // Mettre à jour les compteurs
        if (SessionManager.isAdmin()) {
            totalSignalementsLabel.setText(String.valueOf(signalementService.countAll()));
            enAttenteLabel.setText(String.valueOf(signalementService.countByStatut("EN_ATTENTE")));
            enCoursLabel.setText(String.valueOf(signalementService.countByStatut("EN_COURS")));
            collectesLabel.setText(String.valueOf(signalementService.countByStatut("COLLECTE")));
        } else if (SessionManager.isAgent()) {
            // Pour un agent, compter seulement dans sa zone
            Zone zoneAgent = zoneService.getZoneByNom(currentUser.getLocalite());
            if (zoneAgent != null) {
                int zoneId = zoneAgent.getIdZone();
                totalSignalementsLabel.setText(String.valueOf(signalementService.countByZone(zoneId)));
                // Compter par statut dans cette zone (à implémenter si nécessaire)
            }
        } else {
            // Pour un citoyen
            totalSignalementsLabel.setText(String.valueOf(signalementService.countByUtilisateur(userId)));
            enAttenteLabel.setText(String.valueOf(signalementService.countByStatutAndUtilisateur("EN_ATTENTE", userId)));
            enCoursLabel.setText(String.valueOf(signalementService.countByStatutAndUtilisateur("EN_COURS", userId)));
            collectesLabel.setText(String.valueOf(signalementService.countByStatutAndUtilisateur("COLLECTE", userId)));
        }

        // Mettre à jour les graphiques (admin seulement)
        if (SessionManager.isAdmin()) {
            mettreAJourGraphiques();
        }
    }

    /**
     * Met à jour les graphiques (admin seulement)
     */
    private void mettreAJourGraphiques() {
        // PieChart – Signalements par statut
        if (pieChartStatuts != null) {
            int enAttente = signalementService.countByStatut("EN_ATTENTE");
            int enCours = signalementService.countByStatut("EN_COURS");
            int collecte = signalementService.countByStatut("COLLECTE");

            if (enAttente + enCours + collecte > 0) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                        new PieChart.Data("En attente (" + enAttente + ")", enAttente),
                        new PieChart.Data("En cours (" + enCours + ")", enCours),
                        new PieChart.Data("Collecté (" + collecte + ")", collecte));
                pieChartStatuts.setData(pieData);
                pieChartStatuts.setTitle("Signalements par statut");
            }
        }

        // BarChart – Signalements par zone
        if (barChartZones != null) {
            barChartZones.getData().clear();
            barChartZones.setTitle("Signalements par zone");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de signalements");

            List<Zone> toutesZones = zoneService.getAllZones();
            for (Zone zone : toutesZones) {
                int count = signalementService.countByZone(zone.getIdZone());
                series.getData().add(new XYChart.Data<>(zone.getNomZone(), count));
            }

            barChartZones.getData().add(series);
        }

        // PieChart – Signalements par catégorie
        if (pieChartCategories != null) {
            int plastique = signalementService.countByCategorie("PLASTIQUE");
            int papier = signalementService.countByCategorie("PAPIER");
            int organique = signalementService.countByCategorie("ORGANIQUE");
            int verre = signalementService.countByCategorie("VERRE");

            if (plastique + papier + organique + verre > 0) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                        new PieChart.Data("Plastique (" + plastique + ")", plastique),
                        new PieChart.Data("Papier (" + papier + ")", papier),
                        new PieChart.Data("Organique (" + organique + ")", organique),
                        new PieChart.Data("Verre (" + verre + ")", verre));
                pieChartCategories.setData(pieData);
                pieChartCategories.setTitle("Signalements par catégorie");
            }
        }
    }

    /**
     * Applique les filtres sélectionnés
     */
    @FXML
    private void appliquerFiltres() {
        Integer idZone = filterZoneCombo.getValue() != null ? filterZoneCombo.getValue().getIdZone() : null;
        String statut = "Tous".equals(filterStatutCombo.getValue()) ? null : filterStatutCombo.getValue();
        String categorie = "Tous".equals(filterCategorieCombo.getValue()) ? null : filterCategorieCombo.getValue();

        List<Signalement> filtres = signalementService.getSignalementsFiltres(idZone, statut, categorie);
        listeSignalements.clear();
        listeSignalements.addAll(filtres);
    }

    /**
     * Réinitialise les filtres
     */
    @FXML
    private void reinitialiserFiltres() {
        filterStatutCombo.setValue("Tous");
        filterZoneCombo.setValue(null);
        filterCategorieCombo.setValue("Tous");
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
        chargerDonnees();
    }

    /**
     * Affiche les détails d'un signalement
     */
    private void voirDetailsSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du signalement");
        alert.setHeaderText("Signalement #" + selected.getIdSignalement());

        String zoneNom = getNomZoneForSignalement(selected);
        String userNom = getNomUtilisateurForSignalement(selected);
        String dateFormatee = selected.getDateSignalement() != null ?
                selected.getDateSignalement().format(DATE_FORMATTER) : "Non spécifiée";

        String content = String.format(
                "Description: %s\n" +
                        "Catégorie: %s\n" +
                        "Zone: %s\n" +
                        "Signalé par: %s\n" +
                        "Statut: %s\n" +
                        "Date: %s\n" +
                        "Photo: %s\n" +
                        "Coordonnées: %.6f, %.6f",
                selected.getDescription(),
                selected.getCategorie(),
                zoneNom,
                userNom,
                traduireStatut(selected.getStatut()),
                dateFormatee,
                selected.getPhotoDepot() != null ? selected.getPhotoDepot() : "Non spécifiée",
                selected.getLatitude() != null ? selected.getLatitude() : 0,
                selected.getLongitude() != null ? selected.getLongitude() : 0
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Change le statut d'un signalement
     */
    private void changerStatutSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>("EN_COURS", "EN_ATTENTE", "EN_COURS", "COLLECTE");
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Signalement #" + selected.getIdSignalement());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(nouveauStatut -> {
            if (signalementService.updateStatut(selected.getIdSignalement(), nouveauStatut)) {
                showAlert("Succès", "Statut mis à jour avec succès", Alert.AlertType.INFORMATION);
                chargerDonnees();
            } else {
                showAlert("Erreur", "Impossible de mettre à jour le statut", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Affecte un agent à un signalement
     */
    private void affecterAgent() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!"EN_ATTENTE".equals(selected.getStatut())) {
            showAlert("Action impossible", "Seuls les signalements en attente peuvent être affectés", Alert.AlertType.WARNING);
            return;
        }

        // Récupérer la liste des agents
        List<Utilisateur> agents = utilisateurService.getAgents();
        if (agents.isEmpty()) {
            showAlert("Aucun agent", "Aucun agent disponible", Alert.AlertType.WARNING);
            return;
        }

        ChoiceDialog<Utilisateur> dialog = new ChoiceDialog<>(agents.get(0), agents);
        dialog.setTitle("Affecter un agent");
        dialog.setHeaderText("Signalement #" + selected.getIdSignalement());
        dialog.setContentText("Choisir un agent:");

        // Personnaliser l'affichage
        dialog.getDialogPane().lookup(".choice-box").setStyle("-fx-font-size: 14px;");

        dialog.showAndWait().ifPresent(agent -> {
            if (signalementService.affecterAgent(selected.getIdSignalement(), agent.getIdUser())) {
                showAlert("Succès", "Agent affecté avec succès", Alert.AlertType.INFORMATION);
                chargerDonnees();
            } else {
                showAlert("Erreur", "Impossible d'affecter l'agent", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Supprime un signalement
     */
    private void supprimerSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le signalement #" + selected.getIdSignalement());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce signalement ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
                    showAlert("Succès", "Signalement supprimé avec succès", Alert.AlertType.INFORMATION);
                    chargerDonnees();
                } else {
                    showAlert("Erreur", "Impossible de supprimer le signalement", Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Agent : prendre en charge le signalement sélectionné
     */
    @FXML
    private void handlePrendreEnCharge() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un signalement.", Alert.AlertType.WARNING);
            return;
        }
        if (!"EN_ATTENTE".equals(selected.getStatut())) {
            showAlert("Action impossible", "Ce signalement n'est pas en attente.", Alert.AlertType.WARNING);
            return;
        }

        Utilisateur currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) return;

        if (signalementService.affecterAgent(selected.getIdSignalement(), currentUser.getIdUser())) {
            showAlert("Succès", "Signalement pris en charge.", Alert.AlertType.INFORMATION);
            chargerDonnees();
        } else {
            showAlert("Erreur", "Impossible de mettre à jour le statut.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Agent : marquer le signalement sélectionné comme collecté
     */
    @FXML
    private void handleMarquerCollecte() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection requise", "Veuillez sélectionner un signalement.", Alert.AlertType.WARNING);
            return;
        }
        if ("COLLECTE".equals(selected.getStatut())) {
            showAlert("Action impossible", "Ce signalement est déjà marqué comme collecté.", Alert.AlertType.WARNING);
            return;
        }
        if (signalementService.updateStatut(selected.getIdSignalement(), "COLLECTE")) {
            showAlert("Succès", "Signalement marqué comme collecté.", Alert.AlertType.INFORMATION);
            chargerDonnees();
        } else {
            showAlert("Erreur", "Impossible de mettre à jour le statut.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Citoyen : ouvrir le formulaire de nouveau signalement
     */
    @FXML
    private void handleNouveauSignalement() {
        // Rediriger vers la page d'ajout de signalement
        if (mainApp != null) {
            mainApp.showCitizenDashboard();
        }
    }

    /**
     * Gère le clic sur le bouton Déconnexion
     */
    @FXML
    private void handleDeconnexion() {
        SessionManager.logout();
        if (mainApp != null) {
            mainApp.showLoginScreen();
        }
    }

    /**
     * Navigation entre les pages
     */
    @FXML
    private void showDashboardPage() {
        showPage(pageDashboard, btnDashboard);
        chargerDonnees();
    }

    @FXML
    private void showSignalementsPage() {
        showPage(pageSignalements, btnSignalements);
        chargerDonnees();
    }

    @FXML
    private void showCartePage() {
        showPage(pageCarte, btnCarte);
        // Initialiser la carte si nécessaire
    }

    @FXML
    private void showStatsPage() {
        showPage(pageStats, btnStats);
        mettreAJourGraphiques();
    }

    @FXML
    private void showUtilisateursPage() {
        showPage(pageUtilisateurs, btnUtilisateurs);
        chargerUtilisateurs();
    }

    @FXML
    private void showParametresPage() {
        showPage(pageParametres, btnParametres);
    }

    /**
     * Affiche une page spécifique et met à jour le bouton actif
     */
    private void showPage(VBox page, Button activeButton) {
        VBox[] pages = {pageDashboard, pageSignalements, pageCarte, pageStats, pageUtilisateurs, pageParametres};
        Button[] buttons = {btnDashboard, btnSignalements, btnCarte, btnStats, btnUtilisateurs, btnParametres};

        for (int i = 0; i < pages.length; i++) {
            boolean isActivePage = pages[i] == page;
            if (pages[i] != null) {
                pages[i].setVisible(isActivePage);
                pages[i].setManaged(isActivePage);
            }

            // Styler le bouton actif
            if (buttons[i] != null) {
                if (isActivePage) {
                    buttons[i].getStyleClass().add("active-button");
                } else {
                    buttons[i].getStyleClass().remove("active-button");
                }
            }
        }
    }

    /**
     * Obtient le nom de la zone pour un signalement
     */
    private String getNomZoneForSignalement(Signalement signalement) {
        Zone zone = zoneService.getZoneById(signalement.getIdZone());
        return zone != null ? zone.getNomZone() : "Zone #" + signalement.getIdZone();
    }

    /**
     * Obtient le nom de l'utilisateur pour un signalement
     */
    private String getNomUtilisateurForSignalement(Signalement signalement) {
        Utilisateur user = utilisateurService.getUtilisateurById(signalement.getIdUser());
        if (user != null) {
            return user.getPrenom() + " " + user.getNom();
        }
        return "Utilisateur #" + signalement.getIdUser();
    }

    /**
     * Traduit le statut pour l'affichage
     */
    private String traduireStatut(String statut) {
        if (statut == null) return "";
        switch (statut) {
            case "EN_ATTENTE": return "En attente";
            case "EN_COURS": return "En cours";
            case "COLLECTE": return "Collecté";
            default: return statut;
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}