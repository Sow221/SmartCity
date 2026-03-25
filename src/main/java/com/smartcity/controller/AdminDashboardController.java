package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.model.Zone;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {

    // ==================== COMPOSANTS FXML ====================

    @FXML private VBox pageAdminDashboard;
    @FXML private VBox pageGestionSignalements;
    @FXML private VBox pageGestionUtilisateurs;
    @FXML private VBox pageGestionAgents;
    @FXML private VBox pageGestionZones;
    @FXML private VBox pageStatistiques;
    @FXML private VBox pageParametres;

    @FXML private Label adminNameLabel;
    @FXML private Label pageTitle;
    @FXML private Label adminMessageLabel;
    @FXML private Label lastSyncLabel;

    // Boutons de navigation
    @FXML private Button btnAdminDashboard;
    @FXML private Button btnGestionSignalements;
    @FXML private Button btnGestionUtilisateurs;
    @FXML private Button btnGestionAgents;
    @FXML private Button btnGestionZones;
    @FXML private Button btnStatistiques;
    @FXML private Button btnParametres;
    @FXML private Button themeToggleButton;
    @FXML private Button logoutButton;

    // Cartes de statistiques
    @FXML private Label adminCardTotalSignalements;
    @FXML private Label adminCardEnAttente;
    @FXML private Label adminCardEnCours;
    @FXML private Label adminCardCollectes;
    @FXML private Label adminCardTotalUtilisateurs;

    // Graphiques
    @FXML private PieChart statsPieChart;
    @FXML private BarChart<String, Number> statsBarChart;
    @FXML private PieChart statsPieChartCategories;
    @FXML private BarChart<String, Number> statsBarChartEvolution;
    @FXML private BarChart<String, Number> statsBarChartLocalites;
    @FXML private BarChart<String, Number> statsBarChartAgents;

    // Filtres
    @FXML private ComboBox<Zone> filterZoneCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> filterCategorieCombo;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;
    @FXML private ComboBox<String> filterRoleCombo;

    // Boutons d'action pour les signalements
    @FXML private Button btnModifierSignalement;
    @FXML private Button btnSupprimerSignalement;
    @FXML private Button btnAffecterAgent;

    // Boutons d'action pour les utilisateurs
    @FXML private Button btnModifierUtilisateur;
    @FXML private Button btnSupprimerUtilisateur;

    // Boutons d'action pour les agents
    @FXML private Button btnModifierAgent;
    @FXML private Button btnSupprimerAgent;

    // Boutons d'action pour les zones
    @FXML private Button btnModifierZone;
    @FXML private Button btnSupprimerZone;

    // Paramètres
    @FXML private CheckBox darkModeCheck;
    @FXML private CheckBox notificationsCheck;
    @FXML private ComboBox<String> languageCombo;

    // Tableaux
    @FXML private TableView<Signalement> recentSignalementsTable;
    @FXML private TableView<Signalement> tableSignalements;
    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableView<Utilisateur> tableAgents;
    @FXML private TableView<Zone> tableZones;

    // Colonnes du tableau des signalements récents
    @FXML private TableColumn<Signalement, Integer> colRecentId;
    @FXML private TableColumn<Signalement, String> colRecentDescription;
    @FXML private TableColumn<Signalement, String> colRecentCategorie;
    @FXML private TableColumn<Signalement, String> colRecentZone;
    @FXML private TableColumn<Signalement, String> colRecentStatut;
    @FXML private TableColumn<Signalement, String> colRecentDate;
    @FXML private TableColumn<Signalement, String> colRecentUtilisateur;

    // Colonnes du tableau des signalements
    @FXML private TableColumn<Signalement, Integer> colSignalementId;
    @FXML private TableColumn<Signalement, String> colSignalementDescription;
    @FXML private TableColumn<Signalement, String> colSignalementCategorie;
    @FXML private TableColumn<Signalement, String> colSignalementZone;
    @FXML private TableColumn<Signalement, String> colSignalementDate;
    @FXML private TableColumn<Signalement, String> colSignalementStatut;
    @FXML private TableColumn<Signalement, String> colSignalementUtilisateur;
    @FXML private TableColumn<Signalement, String> colSignalementAgent;

    // Colonnes du tableau des utilisateurs
    @FXML private TableColumn<Utilisateur, Integer> colUserId;
    @FXML private TableColumn<Utilisateur, String> colUserNom;
    @FXML private TableColumn<Utilisateur, String> colUserPrenom;
    @FXML private TableColumn<Utilisateur, String> colUserEmail;
    @FXML private TableColumn<Utilisateur, Integer> colUserAge;
    @FXML private TableColumn<Utilisateur, String> colUserRole;
    @FXML private TableColumn<Utilisateur, String> colUserLocalite;
    @FXML private TableColumn<Utilisateur, String> colUserPhoto;

    // Colonnes du tableau des agents
    @FXML private TableColumn<Utilisateur, Integer> colAgentId;
    @FXML private TableColumn<Utilisateur, String> colAgentNom;
    @FXML private TableColumn<Utilisateur, String> colAgentEmail;
    @FXML private TableColumn<Utilisateur, String> colAgentZone;
    @FXML private TableColumn<Utilisateur, Integer> colAgentEnCours;
    @FXML private TableColumn<Utilisateur, Integer> colAgentTraites;
    @FXML private TableColumn<Utilisateur, Integer> colAgentTotal;

    // Colonnes du tableau des zones
    @FXML private TableColumn<Zone, Integer> colZoneId;
    @FXML private TableColumn<Zone, String> colZoneNom;
    @FXML private TableColumn<Zone, Integer> colZoneSignalements;

    // ==================== SERVICES ====================

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    // ==================== OBSERVABLE LISTS ====================

    private final ObservableList<Signalement> signalementsList = FXCollections.observableArrayList();
    private final ObservableList<Signalement> recentSignalementsList = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> utilisateursList = FXCollections.observableArrayList();
    private final ObservableList<Utilisateur> agentsList = FXCollections.observableArrayList();
    private final ObservableList<Zone> zonesList = FXCollections.observableArrayList();

    // Filtered lists
    private final FilteredList<Signalement> filteredSignalements = new FilteredList<>(signalementsList, p -> true);
    private final FilteredList<Utilisateur> filteredUtilisateurs = new FilteredList<>(utilisateursList, p -> true);

    // ==================== FORMATTERS ====================

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== MAIN APP REFERENCE ====================

    private MainApp mainApp;

    // ==================== INITIALIZATION ====================

    @FXML
    private void initialize() {
        // Vérifier que l'utilisateur est connecté
        Utilisateur currentUser = SessionManager.getUtilisateurConnecte();
        if (currentUser == null) {
            return;
        }

        // Afficher le nom de l'admin
        adminNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        // Charger les données initiales
        chargerZones();
        chargerStatuts();
        chargerCategories();
        chargerRoles();
        chargerLangues();

        // Configurer les tableaux
        configurerTableSignalementsRecents();
        configurerTableSignalements();
        configurerTableUtilisateurs();
        configurerTableAgents();
        configurerTableZones();

        // Configurer les ComboBox
        configurerComboBoxes();

        // Charger toutes les données
        chargerDonnees();

        // Afficher la page dashboard par défaut
        showPage(pageAdminDashboard, btnAdminDashboard);
    }

    /**
     * Définit l'application principale
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // ==================== CHARGEMENT DES DONNÉES ====================

    private void chargerZones() {
        List<Zone> zones = zoneService.getAllZones();
        zonesList.setAll(zones);

        // Pour les filtres
        ObservableList<Zone> zonesWithAll = FXCollections.observableArrayList();
        Zone toutesZones = new Zone(0, "Toutes les zones");
        zonesWithAll.add(toutesZones);
        zonesWithAll.addAll(zones);

        filterZoneCombo.setItems(zonesWithAll);
        filterZoneCombo.setValue(toutesZones);
    }

    private void chargerStatuts() {
        ObservableList<String> statuts = FXCollections.observableArrayList(
                "Tous", "EN_ATTENTE", "EN_COURS", "COLLECTE"
        );
        filterStatutCombo.setItems(statuts);
        filterStatutCombo.setValue("Tous");
    }

    private void chargerCategories() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Toutes", "PLASTIQUE", "PAPIER", "ORGANIQUE", "VERRE"
        );
        filterCategorieCombo.setItems(categories);
        filterCategorieCombo.setValue("Toutes");
    }

    private void chargerRoles() {
        ObservableList<String> roles = FXCollections.observableArrayList(
                "Tous", "ADMIN", "AGENT", "CITOYEN"
        );
        filterRoleCombo.setItems(roles);
        filterRoleCombo.setValue("Tous");
    }

    private void chargerLangues() {
        ObservableList<String> langues = FXCollections.observableArrayList(
                "Français", "English", "Wolof"
        );
        languageCombo.setItems(langues);
        languageCombo.setValue("Français");
    }

    private void configurerComboBoxes() {
        // Configuration de l'affichage des zones dans les ComboBox
        Callback<ListView<Zone>, ListCell<Zone>> cellFactory = new Callback<>() {
            @Override
            public ListCell<Zone> call(ListView<Zone> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Zone item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getNomZone());
                        }
                    }
                };
            }
        };

        filterZoneCombo.setCellFactory(cellFactory);
        filterZoneCombo.setButtonCell(cellFactory.call(null));

        // Converter pour afficher le nom de la zone
        filterZoneCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Zone zone) {
                return zone == null ? null : zone.getNomZone();
            }

            @Override
            public Zone fromString(String string) {
                return null;
            }
        });

        // Configurer le filtre de rôle
        filterRoleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filtrerUtilisateursParRole();
            }
        });
    }

    @FXML
    public void chargerDonnees() {
        chargerSignalements();
        chargerUtilisateurs();
        chargerAgents();
        chargerZonesList();
        mettreAJourStatistiques();
        showMessage("Données actualisées", true);
    }

    private void chargerSignalements() {
        List<Signalement> signalements = signalementService.getAllSignalements();
        signalementsList.setAll(signalements);

        // 10 derniers signalements
        recentSignalementsList.setAll(
                signalements.stream()
                        .limit(10)
                        .collect(Collectors.toList())
        );

        tableSignalements.setItems(filteredSignalements);
        recentSignalementsTable.setItems(recentSignalementsList);
    }

    private void chargerUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        utilisateursList.setAll(utilisateurs);
        tableUtilisateurs.setItems(filteredUtilisateurs);
    }

    private void chargerAgents() {
        List<Utilisateur> agents = utilisateurService.getAgents();
        agentsList.setAll(agents);
        tableAgents.setItems(agentsList);
    }

    private void chargerZonesList() {
        tableZones.setItems(zonesList);
    }

    // ==================== CONFIGURATION DES TABLEAUX ====================

    private void configurerTableSignalementsRecents() {
        colRecentId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colRecentDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRecentCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        colRecentZone.setCellValueFactory(cellData -> {
            Zone zone = zoneService.getZoneById(cellData.getValue().getIdZone());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Zone #" + cellData.getValue().getIdZone());
        });

        colRecentStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireStatut(cellData.getValue().getStatut())));

        colRecentDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateSignalement();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        colRecentUtilisateur.setCellValueFactory(cellData -> {
            Utilisateur user = utilisateurService.getUtilisateurById(cellData.getValue().getIdUser());
            return new SimpleStringProperty(user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur #" + cellData.getValue().getIdUser());
        });
    }

    private void configurerTableSignalements() {
        colSignalementId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colSignalementDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSignalementCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        colSignalementZone.setCellValueFactory(cellData -> {
            Zone zone = zoneService.getZoneById(cellData.getValue().getIdZone());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Zone #" + cellData.getValue().getIdZone());
        });

        colSignalementDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateSignalement();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        colSignalementStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(traduireStatut(cellData.getValue().getStatut())));

        colSignalementUtilisateur.setCellValueFactory(cellData -> {
            Utilisateur user = utilisateurService.getUtilisateurById(cellData.getValue().getIdUser());
            return new SimpleStringProperty(user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur #" + cellData.getValue().getIdUser());
        });

        colSignalementAgent.setCellValueFactory(cellData -> {
            // À implémenter avec AffectationService
            return new SimpleStringProperty("Non affecté");
        });

        // Ajouter un menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem voirDetails = new MenuItem("Voir détails");
        MenuItem modifier = new MenuItem("Modifier");
        MenuItem supprimer = new MenuItem("Supprimer");
        MenuItem affecter = new MenuItem("Affecter à un agent");

        voirDetails.setOnAction(e -> voirDetailsSignalement());
        modifier.setOnAction(e -> modifierSignalement());
        supprimer.setOnAction(e -> supprimerSignalement());
        affecter.setOnAction(e -> affecterAgent());

        contextMenu.getItems().addAll(voirDetails, modifier, affecter, new SeparatorMenuItem(), supprimer);
        tableSignalements.setContextMenu(contextMenu);

        // Double-clic pour voir les détails
        tableSignalements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                voirDetailsSignalement();
            }
        });
    }

    private void configurerTableUtilisateurs() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserLocalite.setCellValueFactory(new PropertyValueFactory<>("localite"));
        colUserPhoto.setCellValueFactory(new PropertyValueFactory<>("photoProfil"));

        // Formater l'affichage de la photo
        colUserPhoto.setCellValueFactory(cellData -> {
            String photo = cellData.getValue().getPhotoProfil();
            return new SimpleStringProperty(photo != null ? "📷" : "❌");
        });

        // Menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem modifier = new MenuItem("Modifier");
        MenuItem supprimer = new MenuItem("Supprimer");
        MenuItem changerRole = new MenuItem("Changer le rôle");

        modifier.setOnAction(e -> modifierUtilisateur());
        supprimer.setOnAction(e -> supprimerUtilisateur());
        changerRole.setOnAction(e -> changerRoleUtilisateur());

        contextMenu.getItems().addAll(modifier, changerRole, new SeparatorMenuItem(), supprimer);
        tableUtilisateurs.setContextMenu(contextMenu);
    }

    private void configurerTableAgents() {
        colAgentId.setCellValueFactory(new PropertyValueFactory<>("idUser"));

        colAgentNom.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrenom() + " " + cellData.getValue().getNom()));

        colAgentEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAgentZone.setCellValueFactory(new PropertyValueFactory<>("localite"));

        colAgentEnCours.setCellValueFactory(cellData -> {
            int count = signalementService.countEnCoursByAgent(cellData.getValue().getIdUser());
            return new SimpleIntegerProperty(count).asObject();
        });

        colAgentTraites.setCellValueFactory(cellData -> {
            int count = signalementService.countTraitesByAgent(cellData.getValue().getIdUser());
            return new SimpleIntegerProperty(count).asObject();
        });

        colAgentTotal.setCellValueFactory(cellData -> {
            int total = signalementService.countByUtilisateur(cellData.getValue().getIdUser());
            return new SimpleIntegerProperty(total).asObject();
        });

        // Menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem modifier = new MenuItem("Modifier");
        MenuItem supprimer = new MenuItem("Supprimer");
        MenuItem voirStatistiques = new MenuItem("Voir statistiques");

        modifier.setOnAction(e -> modifierAgent());
        supprimer.setOnAction(e -> supprimerAgent());
        voirStatistiques.setOnAction(e -> voirStatistiquesAgent());

        contextMenu.getItems().addAll(modifier, voirStatistiques, new SeparatorMenuItem(), supprimer);
        tableAgents.setContextMenu(contextMenu);
    }

    private void configurerTableZones() {
        colZoneId.setCellValueFactory(new PropertyValueFactory<>("idZone"));
        colZoneNom.setCellValueFactory(new PropertyValueFactory<>("nomZone"));

        colZoneSignalements.setCellValueFactory(cellData -> {
            int count = signalementService.countByZone(cellData.getValue().getIdZone());
            return new SimpleIntegerProperty(count).asObject();
        });

        // Menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem modifier = new MenuItem("Modifier");
        MenuItem supprimer = new MenuItem("Supprimer");
        MenuItem voirSignalements = new MenuItem("Voir les signalements");

        modifier.setOnAction(e -> modifierZone());
        supprimer.setOnAction(e -> supprimerZone());
        voirSignalements.setOnAction(e -> voirSignalementsZone());

        contextMenu.getItems().addAll(modifier, voirSignalements, new SeparatorMenuItem(), supprimer);
        tableZones.setContextMenu(contextMenu);
    }

    // ==================== FILTRES ====================

    @FXML
    private void handleFiltrerSignalements() {
        filteredSignalements.setPredicate(signalement -> {
            boolean match = true;

            // Filtre par zone
            Zone selectedZone = filterZoneCombo.getValue();
            if (selectedZone != null && selectedZone.getIdZone() > 0) {
                match = match && signalement.getIdZone() == selectedZone.getIdZone();
            }

            // Filtre par statut
            String selectedStatut = filterStatutCombo.getValue();
            if (selectedStatut != null && !"Tous".equals(selectedStatut)) {
                match = match && selectedStatut.equals(signalement.getStatut());
            }

            // Filtre par catégorie
            String selectedCategorie = filterCategorieCombo.getValue();
            if (selectedCategorie != null && !"Toutes".equals(selectedCategorie)) {
                match = match && selectedCategorie.equals(signalement.getCategorie());
            }

            // Filtre par date
            if (filterDateDebut.getValue() != null) {
                LocalDate dateDebut = filterDateDebut.getValue();
                LocalDateTime signalementDate = signalement.getDateSignalement();
                if (signalementDate != null) {
                    match = match && !signalementDate.toLocalDate().isBefore(dateDebut);
                }
            }

            if (filterDateFin.getValue() != null) {
                LocalDate dateFin = filterDateFin.getValue();
                LocalDateTime signalementDate = signalement.getDateSignalement();
                if (signalementDate != null) {
                    match = match && !signalementDate.toLocalDate().isAfter(dateFin);
                }
            }

            return match;
        });

        showMessage("Filtres appliqués", true);
    }

    @FXML
    private void handleReinitialiserFiltres() {
        filterZoneCombo.setValue(zonesList.isEmpty() ? null : zonesList.get(0));
        filterStatutCombo.setValue("Tous");
        filterCategorieCombo.setValue("Toutes");
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);

        filteredSignalements.setPredicate(s -> true);
        showMessage("Filtres réinitialisés", true);
    }

    private void filtrerUtilisateursParRole() {
        String roleSelectionne = filterRoleCombo.getValue();

        filteredUtilisateurs.setPredicate(utilisateur -> {
            if (roleSelectionne == null || "Tous".equals(roleSelectionne)) {
                return true;
            }
            return roleSelectionne.equals(utilisateur.getRole());
        });
    }

    // ==================== ACTIONS SUR LES SIGNALEMENTS ====================

    private void voirDetailsSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un signalement", false);
            return;
        }

        Zone zone = zoneService.getZoneById(selected.getIdZone());
        Utilisateur user = utilisateurService.getUtilisateurById(selected.getIdUser());

        String zoneNom = zone != null ? zone.getNomZone() : "Zone #" + selected.getIdZone();
        String userNom = user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur #" + selected.getIdUser();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du signalement");
        alert.setHeaderText("Signalement #" + selected.getIdSignalement());

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
                selected.getDateSignalement() != null ? selected.getDateSignalement().format(DATE_FORMATTER) : "N/A",
                selected.getPhotoDepot() != null ? selected.getPhotoDepot() : "Non spécifiée",
                selected.getLatitude() != null ? selected.getLatitude() : 0,
                selected.getLongitude() != null ? selected.getLongitude() : 0
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAjouterSignalement() {
        // Ouvrir le formulaire d'ajout de signalement
        showMessage("Fonctionnalité d'ajout de signalement en cours de développement", true);
    }

    @FXML
    private void handleModifierSignalement() {
        modifierSignalement();
    }

    private void modifierSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un signalement", false);
            return;
        }

        // Ici, ouvrir une boîte de dialogue de modification
        showMessage("Fonctionnalité de modification en cours de développement", true);
    }

    @FXML
    private void handleSupprimerSignalement() {
        supprimerSignalement();
    }

    private void supprimerSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un signalement", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le signalement #" + selected.getIdSignalement());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce signalement ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
                chargerDonnees();
                showMessage("Signalement supprimé avec succès", true);
            } else {
                showMessage("Erreur lors de la suppression", false);
            }
        }
    }

    @FXML
    private void handleAffecterAgent() {
        affecterAgent();
    }

    private void affecterAgent() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un signalement", false);
            return;
        }

        List<Utilisateur> agents = utilisateurService.getAgents();
        if (agents.isEmpty()) {
            showMessage("Aucun agent disponible", false);
            return;
        }

        ChoiceDialog<Utilisateur> dialog = new ChoiceDialog<>(agents.get(0), agents);
        dialog.setTitle("Affecter un agent");
        dialog.setHeaderText("Signalement #" + selected.getIdSignalement());
        dialog.setContentText("Choisir un agent:");

        // Personnaliser l'affichage
        dialog.getDialogPane().lookup(".choice-box").setStyle("-fx-font-size: 14px;");

        Optional<Utilisateur> result = dialog.showAndWait();
        result.ifPresent(agent -> {
            // À implémenter avec AffectationService
            showMessage("Agent " + agent.getPrenom() + " " + agent.getNom() + " affecté avec succès", true);
        });
    }

    // ==================== ACTIONS SUR LES UTILISATEURS ====================

    @FXML
    private void handleAjouterUtilisateur() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Création d'un nouvel utilisateur");
        dialog.setContentText("Email de l'utilisateur:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            showMessage("Fonctionnalité d'ajout d'utilisateur en cours de développement", true);
        });
    }

    @FXML
    private void handleModifierUtilisateur() {
        modifierUtilisateur();
    }

    private void modifierUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un utilisateur", false);
            return;
        }

        // Ici, ouvrir une boîte de dialogue de modification
        showMessage("Fonctionnalité de modification en cours de développement", true);
    }

    @FXML
    private void handleSupprimerUtilisateur() {
        supprimerUtilisateur();
    }

    private void supprimerUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un utilisateur", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur " + selected.getPrenom() + " " + selected.getNom());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (utilisateurService.deleteUtilisateur(selected.getIdUser())) {
                chargerDonnees();
                showMessage("Utilisateur supprimé avec succès", true);
            } else {
                showMessage("Erreur lors de la suppression", false);
            }
        }
    }

    private void changerRoleUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un utilisateur", false);
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getRole(), "ADMIN", "AGENT", "CITOYEN");
        dialog.setTitle("Changer le rôle");
        dialog.setHeaderText("Utilisateur: " + selected.getPrenom() + " " + selected.getNom());
        dialog.setContentText("Nouveau rôle:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauRole -> {
            selected.setRole(nouveauRole);
            if (utilisateurService.updateUtilisateurAdmin(selected)) {
                chargerDonnees();
                showMessage("Rôle modifié avec succès", true);
            } else {
                showMessage("Erreur lors de la modification du rôle", false);
            }
        });
    }

    // ==================== ACTIONS SUR LES AGENTS ====================

    @FXML
    private void handleAjouterAgent() {
        showMessage("Fonctionnalité d'ajout d'agent en cours de développement", true);
    }

    @FXML
    private void handleModifierAgent() {
        modifierAgent();
    }

    private void modifierAgent() {
        Utilisateur selected = tableAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un agent", false);
            return;
        }
        showMessage("Fonctionnalité de modification en cours de développement", true);
    }

    @FXML
    private void handleSupprimerAgent() {
        supprimerAgent();
    }

    private void supprimerAgent() {
        Utilisateur selected = tableAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un agent", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'agent " + selected.getPrenom() + " " + selected.getNom());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet agent ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (utilisateurService.deleteUtilisateur(selected.getIdUser())) {
                chargerDonnees();
                showMessage("Agent supprimé avec succès", true);
            } else {
                showMessage("Erreur lors de la suppression", false);
            }
        }
    }

    private void voirStatistiquesAgent() {
        Utilisateur selected = tableAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner un agent", false);
            return;
        }

        int enCours = signalementService.countEnCoursByAgent(selected.getIdUser());
        int traites = signalementService.countTraitesByAgent(selected.getIdUser());
        int total = signalementService.countByUtilisateur(selected.getIdUser());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques de l'agent");
        alert.setHeaderText(selected.getPrenom() + " " + selected.getNom());

        String content = String.format(
                "Total des signalements: %d\n" +
                        "En cours: %d\n" +
                        "Traités: %d\n" +
                        "Zone: %s",
                total, enCours, traites, selected.getLocalite()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== ACTIONS SUR LES ZONES ====================

    @FXML
    private void handleAjouterZone() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter une zone");
        dialog.setHeaderText("Création d'une nouvelle zone");
        dialog.setContentText("Nom de la zone:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nomZone -> {
            if (!nomZone.trim().isEmpty()) {
                Zone zone = new Zone();
                zone.setNomZone(nomZone.trim());

                if (zoneService.ajouterZone(zone)) {
                    chargerZones();
                    chargerZonesList();
                    showMessage("Zone ajoutée avec succès", true);
                } else {
                    showMessage("Erreur lors de l'ajout de la zone", false);
                }
            }
        });
    }

    @FXML
    private void handleModifierZone() {
        modifierZone();
    }

    private void modifierZone() {
        Zone selected = tableZones.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner une zone", false);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getNomZone());
        dialog.setTitle("Modifier une zone");
        dialog.setHeaderText("Modification de la zone");
        dialog.setContentText("Nouveau nom:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauNom -> {
            if (!nouveauNom.trim().isEmpty()) {
                selected.setNomZone(nouveauNom.trim());
                if (zoneService.updateZone(selected)) {
                    chargerZones();
                    chargerZonesList();
                    showMessage("Zone modifiée avec succès", true);
                } else {
                    showMessage("Erreur lors de la modification", false);
                }
            }
        });
    }

    @FXML
    private void handleSupprimerZone() {
        supprimerZone();
    }

    private void supprimerZone() {
        Zone selected = tableZones.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner une zone", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la zone " + selected.getNomZone());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette zone ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (zoneService.deleteZone(selected.getIdZone())) {
                chargerZones();
                chargerZonesList();
                showMessage("Zone supprimée avec succès", true);
            } else {
                showMessage("Erreur lors de la suppression", false);
            }
        }
    }

    private void voirSignalementsZone() {
        Zone selected = tableZones.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Veuillez sélectionner une zone", false);
            return;
        }

        // Filtrer les signalements par cette zone
        filterZoneCombo.setValue(selected);
        handleFiltrerSignalements();

        // Aller à la page des signalements
        showPage(pageGestionSignalements, btnGestionSignalements);
    }

    // ==================== STATISTIQUES ====================

    private void mettreAJourStatistiques() {
        // Mettre à jour les cartes
        int total = signalementService.countAll();
        int enAttente = signalementService.countByStatut("EN_ATTENTE");
        int enCours = signalementService.countByStatut("EN_COURS");
        int collectes = signalementService.countByStatut("COLLECTE");
        int totalUsers = utilisateurService.countAll();

        adminCardTotalSignalements.setText(String.valueOf(total));
        adminCardEnAttente.setText(String.valueOf(enAttente));
        adminCardEnCours.setText(String.valueOf(enCours));
        adminCardCollectes.setText(String.valueOf(collectes));
        adminCardTotalUtilisateurs.setText(String.valueOf(totalUsers));

        // Mettre à jour le PieChart des statuts
        if (total > 0) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("En attente (" + enAttente + ")", enAttente),
                    new PieChart.Data("En cours (" + enCours + ")", enCours),
                    new PieChart.Data("Collecté (" + collectes + ")", collectes)
            );
            statsPieChart.setData(pieData);
        }

        // Mettre à jour le BarChart des zones
        statsBarChart.getData().clear();
        XYChart.Series<String, Number> seriesZones = new XYChart.Series<>();
        seriesZones.setName("Signalements par zone");

        for (Zone zone : zonesList) {
            int count = signalementService.countByZone(zone.getIdZone());
            seriesZones.getData().add(new XYChart.Data<>(zone.getNomZone(), count));
        }
        statsBarChart.getData().add(seriesZones);

        // Mettre à jour les statistiques avancées
        mettreAJourStatistiquesAvancees();
    }

    private void mettreAJourStatistiquesAvancees() {
        // PieChart des catégories
        statsPieChartCategories.getData().clear();
        int plastique = signalementService.countByCategorie("PLASTIQUE");
        int papier = signalementService.countByCategorie("PAPIER");
        int organique = signalementService.countByCategorie("ORGANIQUE");
        int verre = signalementService.countByCategorie("VERRE");

        if (plastique + papier + organique + verre > 0) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Plastique (" + plastique + ")", plastique),
                    new PieChart.Data("Papier (" + papier + ")", papier),
                    new PieChart.Data("Organique (" + organique + ")", organique),
                    new PieChart.Data("Verre (" + verre + ")", verre)
            );
            statsPieChartCategories.setData(pieData);
        }

        // BarChart des agents
        statsBarChartAgents.getData().clear();
        XYChart.Series<String, Number> seriesAgents = new XYChart.Series<>();
        seriesAgents.setName("Signalements traités");

        for (Utilisateur agent : agentsList) {
            int traites = signalementService.countTraitesByAgent(agent.getIdUser());
            seriesAgents.getData().add(new XYChart.Data<>(agent.getPrenom(), traites));
        }
        statsBarChartAgents.getData().add(seriesAgents);

        // BarChart d'évolution (simulé)
        statsBarChartEvolution.getData().clear();
        XYChart.Series<String, Number> seriesEvolution = new XYChart.Series<>();
        seriesEvolution.setName("Évolution mensuelle");

        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthName = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
            // À implémenter avec une vraie requête par mois
            seriesEvolution.getData().add(new XYChart.Data<>(monthName, (int)(Math.random() * 20)));
        }
        statsBarChartEvolution.getData().add(seriesEvolution);

        // BarChart des localités
        statsBarChartLocalites.getData().clear();
        XYChart.Series<String, Number> seriesLocalites = new XYChart.Series<>();
        seriesLocalites.setName("Signalements par localité");

        // Grouper par localité
        utilisateursList.stream()
                .collect(Collectors.groupingBy(Utilisateur::getLocalite, Collectors.counting()))
                .forEach((localite, count) -> {
                    seriesLocalites.getData().add(new XYChart.Data<>(localite != null ? localite : "Inconnue", count));
                });
        statsBarChartLocalites.getData().add(seriesLocalites);
    }

    // ==================== PARAMÈTRES ====================

    @FXML
    private void handleToggleTheme() {
        // Implémenter le changement de thème
        if (darkModeCheck.isSelected()) {
            // Appliquer le thème sombre
            showMessage("Mode sombre activé", true);
        } else {
            // Appliquer le thème clair
            showMessage("Mode clair activé", true);
        }
    }

    @FXML
    private void handleExporterDonnees() {
        showMessage("Fonctionnalité d'export en cours de développement", true);
    }

    @FXML
    private void handleImporterDonnees() {
        showMessage("Fonctionnalité d'import en cours de développement", true);
    }

    @FXML
    private void handleNettoyerBase() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Nettoyer la base de données");
        confirm.setContentText("Cette action est irréversible. Voulez-vous vraiment nettoyer la base ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showMessage("Base de données nettoyée", true);
        }
    }

    @FXML
    private void handleSynchroniser() {
        lastSyncLabel.setText(LocalDateTime.now().format(DATE_FORMATTER));
        chargerDonnees();
        showMessage("Synchronisation terminée", true);
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleShowAdminDashboard() {
        showPage(pageAdminDashboard, btnAdminDashboard);
        pageTitle.setText("Tableau de bord administrateur");
        chargerDonnees();
    }

    @FXML
    private void handleShowGestionSignalements() {
        showPage(pageGestionSignalements, btnGestionSignalements);
        pageTitle.setText("Gestion des signalements");
    }

    @FXML
    private void handleShowGestionUtilisateurs() {
        showPage(pageGestionUtilisateurs, btnGestionUtilisateurs);
        pageTitle.setText("Gestion des utilisateurs");
    }

    @FXML
    private void handleShowGestionAgents() {
        showPage(pageGestionAgents, btnGestionAgents);
        pageTitle.setText("Gestion des agents");
        chargerAgents();
    }

    @FXML
    private void handleShowGestionZones() {
        showPage(pageGestionZones, btnGestionZones);
        pageTitle.setText("Gestion des zones");
    }

    @FXML
    private void handleShowStatistiques() {
        showPage(pageStatistiques, btnStatistiques);
        pageTitle.setText("Statistiques détaillées");
        mettreAJourStatistiquesAvancees();
    }

    @FXML
    private void handleShowParametres() {
        showPage(pageParametres, btnParametres);
        pageTitle.setText("Paramètres");
    }

    private void showPage(VBox pageToShow, Button activeButton) {
        // Cacher toutes les pages
        pageAdminDashboard.setVisible(false);
        pageAdminDashboard.setManaged(false);
        pageGestionSignalements.setVisible(false);
        pageGestionSignalements.setManaged(false);
        pageGestionUtilisateurs.setVisible(false);
        pageGestionUtilisateurs.setManaged(false);
        pageGestionAgents.setVisible(false);
        pageGestionAgents.setManaged(false);
        pageGestionZones.setVisible(false);
        pageGestionZones.setManaged(false);
        pageStatistiques.setVisible(false);
        pageStatistiques.setManaged(false);
        pageParametres.setVisible(false);
        pageParametres.setManaged(false);

        // Afficher la page sélectionnée
        pageToShow.setVisible(true);
        pageToShow.setManaged(true);

        // Réinitialiser les styles des boutons
        resetButtonStyles();

        // Styler le bouton actif
        activeButton.getStyleClass().add("active-button");
    }

    private void resetButtonStyles() {
        btnAdminDashboard.getStyleClass().remove("active-button");
        btnGestionSignalements.getStyleClass().remove("active-button");
        btnGestionUtilisateurs.getStyleClass().remove("active-button");
        btnGestionAgents.getStyleClass().remove("active-button");
        btnGestionZones.getStyleClass().remove("active-button");
        btnStatistiques.getStyleClass().remove("active-button");
        btnParametres.getStyleClass().remove("active-button");
    }

    // ==================== DÉCONNEXION ====================

    @FXML
    private void handleDeconnexion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirm.setContentText("Vous serez redirigé vers l'écran de connexion.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.logout();
            if (mainApp != null) {
                mainApp.showLoginScreen();
            }
        }
    }

    // ==================== UTILITAIRES ====================

    private String traduireStatut(String statut) {
        if (statut == null) return "";
        switch (statut) {
            case "EN_ATTENTE": return "En attente";
            case "EN_COURS": return "En cours";
            case "COLLECTE": return "Collecté";
            default: return statut;
        }
    }

    private void showMessage(String message, boolean success) {
        adminMessageLabel.setText(message);
        adminMessageLabel.setVisible(true);
        adminMessageLabel.setManaged(true);

        if (success) {
            adminMessageLabel.getStyleClass().remove("error-message");
            adminMessageLabel.getStyleClass().add("success-message");
        } else {
            adminMessageLabel.getStyleClass().remove("success-message");
            adminMessageLabel.getStyleClass().add("error-message");
        }

        // Faire disparaître le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    adminMessageLabel.setVisible(false);
                    adminMessageLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                // Ignorer
            }
        }).start();
    }
}