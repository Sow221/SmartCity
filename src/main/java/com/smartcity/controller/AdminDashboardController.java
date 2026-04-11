package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.model.Affectation;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.SignalementService;
import com.smartcity.service.UtilisateurService;
import com.smartcity.service.ZoneService;
import com.smartcity.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controleur du dashboard administrateur.
 */
public class AdminDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label adminNameLabel;
    @FXML private Label adminAvatarLabel;
    @FXML private Label adminMessageLabel;
    @FXML private Button themeToggleButton;

    @FXML private Button btnAdminDashboard;
    @FXML private Button btnGestionUtilisateurs;
    @FXML private Button btnGestionAgents;
    @FXML private Button btnGestionSignalements;
    @FXML private Button btnStatistiques;
    @FXML private Button btnAdminProfil;
    @FXML private Button btnParametres;

    @FXML private javafx.scene.control.ScrollPane pageAdminDashboard;
    @FXML private javafx.scene.control.ScrollPane pageAdminUsers;
    @FXML private javafx.scene.control.ScrollPane pageAdminAgents;
    @FXML private javafx.scene.control.ScrollPane pageAdminSignalements;
    @FXML private javafx.scene.control.ScrollPane pageStatistiques;
    @FXML private VBox pageAdminProfil;
    @FXML private javafx.scene.control.ScrollPane pageParametres;

    @FXML private Label adminCardTotalSignalements;
    @FXML private Label adminCardEnAttente;
    @FXML private Label adminCardEnCours;
    @FXML private Label adminCardCollectes;
    @FXML private Label adminCardTotalUtilisateurs;

    @FXML private PieChart statsPieChart;
    @FXML private BarChart<String, Number> statsBarChart;

    // Page Statistiques
    @FXML private Label statCardTauxResolution;
    @FXML private Label statCardTauxPikine;
    @FXML private Label statCardTauxGuediawaye;
    @FXML private Label statCardMoyenneJour;
    @FXML private Label statCardCritiques72h;
    @FXML private Label statCardNonAssignes;
    @FXML private Label statCardAgentsGps;
    @FXML private Label statCardTourneesActives;
    @FXML private Label statCardTempsResolution;
    @FXML private Label statCardSatisfaction;
    @FXML private Label statCardAujourdhui;
    @FXML private Label statCardDelta7j;
    @FXML private PieChart statPieCategorie;
    @FXML private BarChart<String, Number> statBarZone;
    @FXML private javafx.scene.web.WebView heatmapWebView;
    @FXML private javafx.scene.web.WebView agentLiveMapWebView;
    @FXML private TableView<AgentLiveRow> tableLiveAgents;
    @FXML private TableColumn<AgentLiveRow, String> colLiveAgentNom;
    @FXML private TableColumn<AgentLiveRow, String> colLiveAgentZone;
    @FXML private TableColumn<AgentLiveRow, Integer> colLiveAgentMissions;
    @FXML private TableColumn<AgentLiveRow, Integer> colLiveAgentEnCours;
    @FXML private TableColumn<AgentLiveRow, String> colLiveAgentPosition;
    @FXML private TableColumn<AgentLiveRow, String> colLiveAgentFraicheur;

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colUserId;
    @FXML private TableColumn<Utilisateur, String> colUserNom;
    @FXML private TableColumn<Utilisateur, String> colUserEmail;
    @FXML private TableColumn<Utilisateur, String> colUserRole;
    @FXML private TableColumn<Utilisateur, String> colUserZone;
    @FXML private Button btnModifierUtilisateur;
    @FXML private Button btnSupprimerUtilisateur;

    @FXML private TableView<AgentStatsRow> tableAgents;
    @FXML private TableColumn<AgentStatsRow, Integer> colAgentId;
    @FXML private TableColumn<AgentStatsRow, String> colAgentNom;
    @FXML private TableColumn<AgentStatsRow, String> colAgentZone;
    @FXML private TableColumn<AgentStatsRow, Integer> colAgentTraites;
    @FXML private Button btnModifierAgent;
    @FXML private Button btnSupprimerAgent;

    @FXML private Button btnModifierStatutSignalement;
    @FXML private Button btnReaffecterSignalement;
    @FXML private Button btnSupprimerSignalement;

    @FXML private ComboBox<String> filterZoneCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> filterPrioriteCombo;
    @FXML private ComboBox<String> filterAffectationCombo;
    @FXML private ComboBox<String> filterCategorieCombo;
    @FXML private javafx.scene.control.DatePicker filterDateDebutPicker;
    @FXML private javafx.scene.control.DatePicker filterDateFinPicker;
    @FXML private Label adminCardUrgents;
    @FXML private javafx.scene.control.ProgressIndicator loadingSpinner;
    @FXML private TableView<Signalement> tableSignalements;
    @FXML private TableColumn<Signalement, Integer> colSignalementId;
    @FXML private TableColumn<Signalement, String> colSignalementDescription;
    @FXML private TableColumn<Signalement, String> colSignalementCategorie;
    @FXML private TableColumn<Signalement, String> colSignalementZone;
    @FXML private TableColumn<Signalement, String> colSignalementDate;
    @FXML private TableColumn<Signalement, String> colSignalementStatut;
    @FXML private TableColumn<Signalement, String> colSignalementUtilisateur;
    @FXML private TableColumn<Signalement, String> colSignalementCommentaire;

    @FXML private Label adminProfilAvatarLabel;
    @FXML private Label adminProfilNomDisplay;
    @FXML private TextField adminProfilNomField;
    @FXML private TextField adminProfilEmailField;
    @FXML private TextField adminProfilRoleField;
    @FXML private TextField nouvelleZoneField;
    @FXML private Label zonesListLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final SignalementService signalementService = new SignalementService();
    private final ZoneService zoneService = new ZoneService();
    private final com.smartcity.service.AffectationService affectationService = new com.smartcity.service.AffectationService();
    private final com.smartcity.service.SuiviService suiviService = new com.smartcity.service.SuiviService();
    private final ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private final ObservableList<AgentStatsRow> agents = FXCollections.observableArrayList();
    private final ObservableList<Signalement> signalements = FXCollections.observableArrayList();
    private final ObservableList<AgentLiveRow> liveAgents = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE);
    private static final String ALL_ZONES = "Toutes";
    private static final String ALL_STATUTS = "Tous";
    private static final String ALL_CATEGORIES = "Toutes";

    private static final int PAGE_SIZE = 50;
    private final ObservableList<Signalement> tousLesSignalements = FXCollections.observableArrayList();
    private int currentPage = 0;
    private boolean paginationUpdating;

    private volatile boolean isLoading = false;
    private MainApp mainApp;
    private boolean heatmapFallbackHandlersInstalled;
    private boolean liveMapFallbackHandlersInstalled;
    private javafx.animation.Timeline liveTrackingRefreshTimeline;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null || !SessionManager.isAdmin()) {
            showAdminMessage("Acces reserve au role Administrateur.", false);
            if (mainApp != null) javafx.application.Platform.runLater(() -> mainApp.showLoginScreen());
            return;
        }

        adminNameLabel.setText(current.getNom());
        if (adminAvatarLabel != null)
            adminAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
        configureUsersTable();
        configureAgentsTable();
        configureSignalementsTable();
        configureLiveAgentsTable();
        configureFilters();
        refreshZoneSettings();

        btnModifierUtilisateur.disableProperty().bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerUtilisateur.disableProperty().bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnModifierAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnModifierStatutSignalement.disableProperty().bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerSignalement.disableProperty().bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());
        if (btnReaffecterSignalement != null)
            btnReaffecterSignalement.disableProperty().bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());

        tableUtilisateurs.setItems(utilisateurs);
        tableAgents.setItems(agents);
        tableSignalements.setItems(signalements);
        if (tableLiveAgents != null) tableLiveAgents.setItems(liveAgents);
        // Pagination : boutons Précédent/Suivant ajoutés dynamiquement sous la table
        ajouterPagination();

        applyTheme();
        showAdminDashboardPage();
        rootPane.setOnKeyPressed(e -> { if (e.getCode() == javafx.scene.input.KeyCode.F5) chargerDonnees(); });
        startLiveTrackingAutoRefresh();
        
        // Delay data loading to prevent Hikari pool exhaustion
        javafx.application.Platform.runLater(() -> {
            chargerDonnees();
        });
    }

    @FXML
    private void handleToggleTheme() {
        SessionManager.toggleDarkMode();
        applyTheme();
    }

    @FXML
    public void chargerDonnees() {
        if (isLoading) return;
        isLoading = true;
        if (loadingSpinner != null) { loadingSpinner.setVisible(true); loadingSpinner.setManaged(true); }
        final javafx.scene.Node btnActualiser = rootPane != null ? rootPane.lookup("#btnActualiser") : null;
        if (btnActualiser != null) btnActualiser.setDisable(true);
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                int total      = signalementService.countAll();
                int attente    = signalementService.countByStatut("En attente");
                int encours    = signalementService.countByStatut("En cours");
                int collectes  = signalementService.countByStatut("Termin\u00e9");
                int urgents    = signalementService.countUrgents();
                int totalUsers = utilisateurService.countAllActifs();
                List<Utilisateur> users = utilisateurService.getAllUtilisateurs();
                List<Signalement> sigs  = signalementService.getAllSignalements();
                // Fix N+1 agents : 1 seule requête SQL avec JOIN
                List<UtilisateurService.AgentStats> agentStats = utilisateurService.getAgentsWithStats();
                // Fix N+1 colonne agent : 1 seule requête JOIN
                java.util.Map<Integer, String> agentParSig = affectationService.getAgentNomParSignalement();
                javafx.application.Platform.runLater(() -> {
                    adminCardTotalSignalements.setText(String.valueOf(total));
                    adminCardEnAttente.setText(String.valueOf(attente));
                    adminCardEnCours.setText(String.valueOf(encours));
                    adminCardCollectes.setText(String.valueOf(collectes));
                    adminCardTotalUtilisateurs.setText(String.valueOf(totalUsers));
                    if (adminCardUrgents != null) {
                        adminCardUrgents.setText(String.valueOf(urgents));
                        adminCardUrgents.setStyle(urgents > 0
                            ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold;"
                            : "-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    }
                    utilisateurs.setAll(users);
                    signalements.setAll(sigs);
                    agents.clear();
                    agentStats.forEach(a -> agents.add(new AgentStatsRow(a.idUser(), a.nom(), a.zoneNom(), a.traites())));
                    // Appliquer la map agent sans requêtes supplémentaires
                    if (colSignalementCommentaire != null)
                        colSignalementCommentaire.setCellValueFactory(cell ->
                            new SimpleStringProperty(agentParSig.getOrDefault(cell.getValue().getIdSignalement(), "-")));
                    refreshLiveTracking();
                    if (loadingSpinner != null) { loadingSpinner.setVisible(false); loadingSpinner.setManaged(false); }
                    if (btnActualiser != null) btnActualiser.setDisable(false);
                    isLoading = false;
                    refreshDashboardCharts();
                    if (pageStatistiques != null && pageStatistiques.isVisible()) refreshCharts();
                });
                return null;
            }
        };
        task.setOnFailed(e -> javafx.application.Platform.runLater(() -> {
            isLoading = false;
            if (loadingSpinner != null) { loadingSpinner.setVisible(false); loadingSpinner.setManaged(false); }
            if (btnActualiser != null) btnActualiser.setDisable(false);
            showAdminMessage("Erreur lors du chargement des donn\u00e9es.", false);
        }));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleDeconnexion() {
        if (!confirm("Déconnexion", "Voulez-vous vraiment vous déconnecter ?")) return;
        stopLiveTrackingAutoRefresh();
        SessionManager.logout();
        if (mainApp != null) {
            mainApp.showLoginScreen();
        }
    }

    @FXML
    private void handleShowAdminDashboard() {
        showAdminDashboardPage();
    }

    @FXML
    private void handleShowGestionUtilisateurs() {
        showPage(pageAdminUsers, btnGestionUtilisateurs);
    }

    @FXML
    private void handleShowGestionAgents() {
        showPage(pageAdminAgents, btnGestionAgents);
    }

    @FXML
    private void handleShowGestionSignalements() {
        showPage(pageAdminSignalements, btnGestionSignalements);
    }

    @FXML
    private void handleShowStatistiques() {
        showPage(pageStatistiques, btnStatistiques);
        refreshChartsAnimated();
    }

    @FXML
    private void handleShowAdminProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current != null) {
            adminProfilNomField.setText(current.getNom());
            adminProfilEmailField.setText(current.getEmail());
            adminProfilRoleField.setText(current.getRole());
            if (adminProfilAvatarLabel != null)
                adminProfilAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (adminProfilNomDisplay != null)
                adminProfilNomDisplay.setText(current.getNom());
        }
        showPage(pageAdminProfil, btnAdminProfil);
    }

    @FXML
    private void handleModifierAdminProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        if (adminProfilNomField.getText().isBlank() || adminProfilEmailField.getText().isBlank()) {
            showAdminMessage("Nom et email obligatoires.", false);
            return;
        }
        String newEmail = adminProfilEmailField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        if (!newEmail.equals(current.getEmail()) && utilisateurService.emailExiste(newEmail)) {
            showAdminMessage("Cet email est déjà utilisé par un autre compte.", false);
            return;
        }
        current.setNom(adminProfilNomField.getText().trim());
        current.setEmail(newEmail);
        if (utilisateurService.updateUtilisateur(current)) {
            current.setNom(adminProfilNomField.getText().trim());
            current.setEmail(newEmail);
            adminNameLabel.setText(current.getNom());
            if (adminAvatarLabel != null)
                adminAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            if (adminProfilNomDisplay != null)
                adminProfilNomDisplay.setText(current.getNom());
            if (adminProfilAvatarLabel != null)
                adminProfilAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
            showAdminMessage("Profil mis a jour.", true);
        } else {
            showAdminMessage("Echec de la mise a jour.", false);
        }
    }

    @FXML
    private void handleModifierAdminMotPasse() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mot de passe");
        dialog.setHeaderText(null);
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Nouveau mot de passe (min. 6 caractères)");
        dialog.getDialogPane().setContent(pwField);
        dialog.setResultConverter(bt -> bt == saveType ? pwField.getText() : null);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;
        com.smartcity.utils.ValidationUtils.ValidationResult check =
            com.smartcity.utils.ValidationUtils.validatePassword(result.get());
        if (!check.isValid()) {
            showAdminMessage(check.getMessage(), false);
            return;
        }
        if (utilisateurService.updateMotDePasse(current.getIdUser(), result.get())) {
            showAdminMessage("Mot de passe mis a jour.", true);
        } else {
            showAdminMessage("Echec de la mise a jour du mot de passe.", false);
        }
    }

    @FXML
    private void handleShowRapports() {
        if (mainApp != null) {
            mainApp.showReportsScreen();
            return;
        }
        showAdminMessage("Navigation indisponible pour la page Rapports.", false);
    }

    @FXML
    private void handleShowParametres() {
        showPage(pageParametres, btnParametres);
    }

    @FXML
    private void handleSetThemeClair() {
        SessionManager.setDarkMode(false);
        applyTheme();
    }

    @FXML
    private void handleSetThemeSombre() {
        SessionManager.setDarkMode(true);
        applyTheme();
    }

    @FXML
    private void handleAjouterZone() {
        if (nouvelleZoneField == null || nouvelleZoneField.getText().isBlank()) {
            showAdminMessage("Saisissez le nom de la nouvelle zone.", false);
            return;
        }
        String zoneName = nouvelleZoneField.getText().trim();
        if (zoneService.getZoneByName(zoneName) != null) {
            showAdminMessage("Cette zone existe deja.", false);
            return;
        }
        com.smartcity.model.Zone zone = new com.smartcity.model.Zone();
        zone.setNomZone(zoneName);
        com.smartcity.service.GeolocationService.Coordinates fallbackCenter =
            zoneService.getCenterById(1);
        zone.setLatitude(fallbackCenter.lat);
        zone.setLongitude(fallbackCenter.lon);
        if (zoneService.ajouterZone(zone)) {
            nouvelleZoneField.clear();
            refreshZoneSettings();
            configureFilters();
            showAdminMessage("Zone ajoutee avec succes.", true);
        } else {
            showAdminMessage("Echec de l'ajout de la zone.", false);
        }
    }

    @FXML
    private void handleExporterCSVStats() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Exporter statistiques");
        chooser.setInitialFileName("statistiques.csv");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;
        try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            fw.write('\uFEFF');
            fw.write("Zone,Total,En attente,En cours,Termin\u00e9,Taux resolution\n");
            for (com.smartcity.service.SuiviService.KpiZone k : suiviService.getKpiParZone()) {
                String taux = String.format(java.util.Locale.US, "%.1f%%", k.tauxResolution);
                fw.write(String.format("%s,%d,%d,%d,%d,%s\n",
                    k.nomZone, k.total, k.enAttente, k.enCours, k.termines, taux));
            }
            showAdminMessage("Export statistiques réussi : " + file.getName(), true);
        } catch (java.io.IOException e) {
            showAdminMessage("Erreur export statistiques.", false);
        }
    }

    @FXML
    private void handleExporterCSV() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Exporter signalements");
        chooser.setInitialFileName("signalements.csv");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;
        try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            fw.write('\uFEFF');
            fw.write("ID,Description,Categorie,Zone,Date,Statut,Citoyen\n");
            for (Signalement s : signalements) {
                fw.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    s.getIdSignalement(),
                    s.getDescription() != null ? s.getDescription().replace(",", " ") : "",
                    s.getCategorie(), s.getZoneNom(),
                    s.getDateSignalement() != null ? s.getDateSignalement().format(DATE_FORMATTER) : "",
                    s.getStatut(), valueOrDash(s.getUtilisateurNom())));
            }
            showAdminMessage("Export CSV reussi : " + file.getName(), true);
        } catch (java.io.IOException e) {
            showAdminMessage("Erreur export CSV.", false);
        }
    }

    @FXML
    private void handleAjouterUtilisateur() {
        Optional<Utilisateur> result = openUtilisateurDialog("Ajouter utilisateur", null, false);
        if (result.isEmpty()) {
            return;
        }

        Utilisateur nouveau = result.get();
        if (utilisateurService.emailExiste(nouveau.getEmail())) {
            showAdminMessage("Email deja utilise.", false);
            return;
        }

        if (utilisateurService.inscription(nouveau)) {
            showAdminMessage("Utilisateur ajoute avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de l'ajout utilisateur.", false);
        }
    }

    @FXML
    private void handleModifierUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Optional<Utilisateur> result = openUtilisateurDialog("Modifier utilisateur", selected, false);
        if (result.isEmpty()) {
            return;
        }

        Utilisateur updated = result.get();
        updated.setIdUser(selected.getIdUser());

        if (utilisateurService.updateUtilisateurAdmin(updated)) {
            showAdminMessage("Utilisateur modifie avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la modification utilisateur.", false);
        }
    }

    @FXML
    private void handleSupprimerUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (!confirm("Confirmer", "Supprimer l'utilisateur " + selected.getNom() + " ?")) {
            return;
        }

        if (utilisateurService.deleteUtilisateur(selected.getIdUser())) {
            showAdminMessage("Utilisateur desactive avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la suppression utilisateur.", false);
        }
    }

    @FXML
    private void handleAjouterAgent() {
        Optional<Utilisateur> result = openUtilisateurDialog("Ajouter agent", null, true);
        if (result.isEmpty()) {
            return;
        }

        Utilisateur agent = result.get();
        agent.setRole("Agent");

        if (utilisateurService.emailExiste(agent.getEmail())) {
            showAdminMessage("Email deja utilise.", false);
            return;
        }

        if (utilisateurService.inscription(agent)) {
            showAdminMessage("Agent ajoute avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de l'ajout agent.", false);
        }
    }

    @FXML
    private void handleModifierAgent() {
        AgentStatsRow selected = tableAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Utilisateur base = utilisateurService.getUtilisateurById(selected.id());
        if (base == null) {
            showAdminMessage("Agent introuvable.", false);
            return;
        }

        Optional<Utilisateur> result = openUtilisateurDialog("Modifier agent", base, true);
        if (result.isEmpty()) {
            return;
        }

        Utilisateur updated = result.get();
        updated.setIdUser(base.getIdUser());
        updated.setRole("Agent");

        if (utilisateurService.updateUtilisateurAdmin(updated)) {
            showAdminMessage("Agent modifie avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la modification agent.", false);
        }
    }

    @FXML
    private void handleSupprimerAgent() {
        AgentStatsRow selected = tableAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (!confirm("Confirmer", "Supprimer l'agent " + selected.nom() + " ?")) {
            return;
        }

        if (utilisateurService.deleteUtilisateur(selected.id())) {
            showAdminMessage("Agent desactive avec succes.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la suppression agent.", false);
        }
    }

    @FXML
    private void handleModifierStatutSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // Seule action manuelle pertinente : clôturer (Terminé) un signalement déjà affecté
        if (!affectationService.estDejaAffecte(selected.getIdSignalement())) {
            showAdminMessage("Ce signalement n'est pas encore affecté à un agent.", false);
            return;
        }
        if (com.smartcity.model.SignalementStatut.TERMINE.matches(selected.getStatut())) {
            showAdminMessage("Ce signalement est déjà terminé.", false);
            return;
        }
        if (!confirm("Clôturer", "Marquer le signalement #" + selected.getIdSignalement() + " comme Terminé ?")) return;
        if (signalementService.updateStatut(selected.getIdSignalement(), com.smartcity.model.SignalementStatut.TERMINE.label())) {
            showAdminMessage("Signalement clôturé.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la clôture.", false);
        }
    }

    @FXML
    private void handleReaffecterSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        affectationService.supprimerAffectationParSignalement(selected.getIdSignalement());
        if (signalementService.updateStatut(selected.getIdSignalement(), "En attente")) {
            affectationService.affecterAgentParZone(selected.getIdSignalement(), selected.getIdZone());
            showAdminMessage("Signalement réaffecté.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la réaffectation.", false);
        }
    }

    @FXML
    private void handleSupprimerSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if ("En cours".equalsIgnoreCase(selected.getStatut())) {
            showAdminMessage("Impossible de supprimer un signalement en cours de traitement.", false);
            return;
        }
        if (!confirm("Confirmer", "Supprimer le signalement #" + selected.getIdSignalement() + " ?")) return;
        if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
            showAdminMessage("Signalement supprimé.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la suppression.", false);
        }
    }

    @FXML
    private void handleResetFiltresSignalements() {
        if (filterZoneCombo != null) filterZoneCombo.setValue(ALL_ZONES);
        if (filterStatutCombo != null) filterStatutCombo.setValue(ALL_STATUTS);
        if (filterCategorieCombo != null) filterCategorieCombo.setValue(ALL_CATEGORIES);
        if (filterPrioriteCombo != null) filterPrioriteCombo.setValue("Toutes");
        if (filterAffectationCombo != null) filterAffectationCombo.setValue("Toutes");
        if (filterDateDebutPicker != null) filterDateDebutPicker.setValue(null);
        if (filterDateFinPicker != null) filterDateFinPicker.setValue(null);
        signalements.setAll(signalementService.getAllSignalements());
        showAdminMessage("Filtres réinitialisés.", true);
    }

    @FXML
    private void handleFiltrerSignalements() {
        String zone      = getFilterValue(filterZoneCombo, ALL_ZONES);
        String statut    = getFilterValue(filterStatutCombo, ALL_STATUTS);
        String categorie = getFilterValue(filterCategorieCombo, ALL_CATEGORIES);
        String priorite  = getFilterValue(filterPrioriteCombo, "Toutes");
        String affectation = getFilterValue(filterAffectationCombo, "Toutes");
        java.time.LocalDate dateDebut = filterDateDebutPicker != null ? filterDateDebutPicker.getValue() : null;
        java.time.LocalDate dateFin   = filterDateFinPicker   != null ? filterDateFinPicker.getValue()   : null;
        signalements.setAll(signalementService.getSignalementsFiltres(zone, statut, categorie, dateDebut, dateFin, priorite, affectation));
        showAdminMessage("Filtres appliques.", true);
    }

    private void refreshDashboardCharts() {
        if (statsPieChart == null && statsBarChart == null) return;
        javafx.concurrent.Task<int[]> task = new javafx.concurrent.Task<>() {
            @Override
            protected int[] call() {
                return new int[]{
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.EN_ATTENTE.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.AFFECTE.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.EN_COURS.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.TERMINE.label())
                };
            }
        };
        task.setOnSucceeded(e -> {
            int[] d = task.getValue();
            int enAttente = d[0], affecte = d[1], enCours = d[2], termines = d[3];
            if (statsPieChart != null) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("En attente (" + enAttente + ")", Math.max(enAttente, 0.01)),
                    new PieChart.Data("Affect\u00e9 (" + affecte + ")",   Math.max(affecte, 0.01)),
                    new PieChart.Data("En cours (" + enCours + ")",   Math.max(enCours, 0.01)),
                    new PieChart.Data("Termin\u00e9 (" + termines + ")",   Math.max(termines, 0.01))
                );
                statsPieChart.setData(pieData);
                statsPieChart.setLabelsVisible(true);
                statsPieChart.setLegendVisible(true);
            }
            if (statsBarChart != null) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Signalements");
                zoneService.getAllZones().forEach(z ->
                    series.getData().add(new XYChart.Data<>(z.getNomZone(), signalementService.countByZone(z.getNomZone()))));
                statsBarChart.getData().clear();
                statsBarChart.getData().add(series);
                statsBarChart.setLegendVisible(true);
            }
        });
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    private void refreshChartsAnimated() {
        com.smartcity.utils.AnimationUtils.fadeSlideIn(pageStatistiques).play();
        refreshCharts();
    }

    private void refreshCharts() {
        javafx.concurrent.Task<int[]> task = new javafx.concurrent.Task<>() {
            @Override
            protected int[] call() {
                return new int[]{
                    signalementService.countAll(),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.TERMINE.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.EN_ATTENTE.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.AFFECTE.label()),
                    signalementService.countByStatut(com.smartcity.model.SignalementStatut.EN_COURS.label()),
                    signalementService.countMoyenneParJour(),
                    signalementService.countCritiques72h(),
                    signalementService.countNonAssignes(),
                    signalementService.countTourneesActives(),
                    signalementService.countAujourdhui(),
                    signalementService.countDeltaSemaine()
                };
            }
        };
        task.setOnSucceeded(e -> {
            int[] d = task.getValue();
            int total = d[0], termines = d[1], enAttente = d[2], affecte = d[3], enCours = d[4];
            double tempsResolution = signalementService.getTempsResolutionMoyenH();
            double satisfaction = suiviService.getNoteMoyenneGlobale();
            renderCharts(total, termines, enAttente, affecte, enCours, d[5], d[6], d[7], d[8], d[9], d[10], tempsResolution, satisfaction);
        });
        task.setOnFailed(e -> showAdminMessage("Erreur chargement statistiques.", false));
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    private void renderCharts(int total, int termines, int enAttente, int affecte, int enCours,
                               int moyJour, int critiques72h, int nonAssignes, int tourneesActives,
                               int aujourdhui, int delta7j, double tempsResolutionH, double satisfaction) {
        // --- Dashboard charts (setData AVANT setLegendVisible) ---
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("En attente (" + enAttente + ")", Math.max(enAttente, 0.01)),
                new PieChart.Data("Affect\u00e9 (" + affecte + ")",   Math.max(affecte, 0.01)),
                new PieChart.Data("En cours (" + enCours + ")",   Math.max(enCours, 0.01)),
                new PieChart.Data("Termin\u00e9 (" + termines + ")",   Math.max(termines, 0.01))
        );
        if (statsPieChart != null) {
            statsPieChart.setData(pieData);
            statsPieChart.setLabelsVisible(true);
            statsPieChart.setLegendVisible(true);
        }

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Signalements");
        zoneService.getAllZones().forEach(z ->
            zoneSeries.getData().add(new XYChart.Data<>(z.getNomZone(), signalementService.countByZone(z.getNomZone()))));
        if (statsBarChart != null) {
            statsBarChart.getData().clear();
            statsBarChart.getData().add(zoneSeries);
            statsBarChart.setLegendVisible(true);
        }

        // --- Page Statistiques ---
        if (statCardTauxResolution != null) {
            int pct = total == 0 ? 0 : (int) Math.round(termines * 100.0 / total);
            animatePercent(statCardTauxResolution, pct);
        }
        if (statCardTauxPikine != null) {
            int totalPikine = signalementService.countByZone("Pikine");
            int terminesPikine = signalementService.countByStatutAndZone("Termin\u00e9", "Pikine");
            int pctP = totalPikine == 0 ? 0 : (int) Math.round(terminesPikine * 100.0 / totalPikine);
            animatePercent(statCardTauxPikine, pctP);
        }
        if (statCardTauxGuediawaye != null) {
            int totalG = signalementService.countByZone("Gu\u00e9diawaye");
            int terminesG = signalementService.countByStatutAndZone("Termin\u00e9", "Gu\u00e9diawaye");
            int pctG = totalG == 0 ? 0 : (int) Math.round(terminesG * 100.0 / totalG);
            animatePercent(statCardTauxGuediawaye, pctG);
        }
        if (statCardMoyenneJour != null)
            com.smartcity.utils.AnimationUtils.animateCounter(statCardMoyenneJour, 0, moyJour).play();
        if (statCardCritiques72h != null)
            statCardCritiques72h.setText(String.valueOf(critiques72h));
        if (statCardNonAssignes != null)
            statCardNonAssignes.setText(String.valueOf(nonAssignes));
        if (statCardTourneesActives != null)
            statCardTourneesActives.setText(String.valueOf(tourneesActives));
        if (statCardTempsResolution != null)
            statCardTempsResolution.setText(tempsResolutionH > 0
                ? String.format(java.util.Locale.US, "%.1fh", tempsResolutionH) : "-");
        if (statCardSatisfaction != null)
            statCardSatisfaction.setText(satisfaction > 0
                ? String.format(java.util.Locale.US, "%.1f/5", satisfaction) : "-");
        if (statCardAujourdhui != null)
            com.smartcity.utils.AnimationUtils.animateCounter(statCardAujourdhui, 0, aujourdhui).play();
        if (statCardDelta7j != null) {
            String deltaText = delta7j > 0 ? "+" + delta7j : String.valueOf(delta7j);
            statCardDelta7j.setText(deltaText);
            statCardDelta7j.setStyle(delta7j > 0
                ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;"
                : "-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        }

        if (statPieCategorie != null) {
            ObservableList<PieChart.Data> catData = FXCollections.observableArrayList();
            for (String cat : com.smartcity.model.SignalementStatut.CATEGORIES) {
                int n = signalementService.countByCategorie(cat);
                if (n > 0) catData.add(new PieChart.Data(cat + " (" + n + ")", n));
            }
            statPieCategorie.setData(catData);
            statPieCategorie.setLabelsVisible(true);
            statPieCategorie.setLegendVisible(true);
            statPieCategorie.setOpacity(0);
            com.smartcity.utils.AnimationUtils.fadeIn(statPieCategorie, javafx.util.Duration.millis(600)).play();
        }
        if (statBarZone != null) {
            statBarZone.getData().clear();
            for (String statut : new String[]{"En attente", "En cours", "Termin\u00e9"}) {
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName(statut);
                zoneService.getAllZones().forEach(z ->
                    s.getData().add(new XYChart.Data<>(z.getNomZone(),
                        signalementService.countByStatutAndZone(statut, z.getNomZone()))));
                statBarZone.getData().add(s);
            }
            statBarZone.setLegendVisible(true);
            statBarZone.setOpacity(0);
            com.smartcity.utils.AnimationUtils.fadeIn(statBarZone, javafx.util.Duration.millis(700)).play();
        }
        if (heatmapWebView != null) refreshHeatmap();
        refreshLiveTracking();

        // KPIs suivi politique
        java.util.List<com.smartcity.service.SuiviService.KpiZone> kpis = suiviService.getKpiParZone();
        double noteMoyenne = suiviService.getNoteMoyenneGlobale();
        if (!kpis.isEmpty()) {
            StringBuilder kpiMsg = new StringBuilder();
            kpis.forEach(k -> kpiMsg
                .append(k.nomZone).append(" : ")
                .append(String.format("%.0f%%", k.tauxResolution)).append(" r\u00e9solution, ")
                .append(String.format("%.1fh", k.tempsResolutionMoyenH)).append(" moy., ")
                .append(k.urgents).append(" urgents | "));
            if (noteMoyenne > 0)
                kpiMsg.append(String.format(" | Satisfaction : %.1f/5 \u2605", noteMoyenne));
            showAdminMessage(kpiMsg.toString().replaceAll(" \\| $", ""), true);
        }
    }

    private void refreshHeatmap() {
        List<Signalement> all = signalementService.getAllSignalements();
        StringBuilder points = new StringBuilder();
        for (Signalement s : all) {
            if (s.getLatitude() != 0.0 || s.getLongitude() != 0.0) {
                if (points.length() > 0) points.append(",");
                points.append(String.format(java.util.Locale.US, "[%.6f,%.6f,1]", s.getLatitude(), s.getLongitude()));
            }
        }
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
            + com.smartcity.utils.MapResourceUtils.leafletHeadWithHeat()
            + "<style>html,body,#map{height:100%;margin:0;}</style></head><body>"
            + "<div id='map'></div><script>"
            + "var map=L.map('map').setView([14.7646,-17.3920],12);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);"
            + "var pts=[" + points + "];"
            + "if(pts.length>0)L.heatLayer(pts,{radius:25,blur:15,maxZoom:17}).addTo(map);"
            + "</script></body></html>";
        installHeatmapFallbackHandlers("Heatmap indisponible. Les statistiques tabulaires restent disponibles.");
        heatmapWebView.getEngine().loadContent(html);
    }

    private void installHeatmapFallbackHandlers(String message) {
        if (heatmapWebView == null || heatmapFallbackHandlersInstalled) {
            return;
        }
        heatmapFallbackHandlersInstalled = true;
        heatmapWebView.getEngine().getLoadWorker().exceptionProperty().addListener((obs, oldValue, error) -> {
            if (error != null) {
                showAdminMessage(message, false);
                heatmapWebView.getEngine().loadContent(buildHeatmapFallbackHtml(message));
            }
        });
        heatmapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED) {
                showAdminMessage(message, false);
                heatmapWebView.getEngine().loadContent(buildHeatmapFallbackHtml(message));
            }
        });
    }

    private String buildHeatmapFallbackHtml(String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
            + "body{font-family:Arial,sans-serif;background:#f6f8fb;color:#1f2937;display:flex;"
            + "align-items:center;justify-content:center;height:100%;margin:0;padding:24px;text-align:center;}"
            + ".card{max-width:440px;background:white;border-radius:16px;padding:24px;"
            + "box-shadow:0 8px 24px rgba(15,23,42,0.12);}"
            + ".title{font-size:18px;font-weight:700;margin-bottom:8px;color:#b45309;}"
            + "</style></head><body><div class='card'><div class='title'>Heatmap indisponible</div><div>"
            + message + "</div></div></body></html>";
    }

    private void refreshLiveTracking() {
        if (tableLiveAgents == null && agentLiveMapWebView == null) {
            return;
        }

        List<com.smartcity.service.AffectationService.AgentLiveStatus> statuses = affectationService.getAgentLiveStatuses();
        long gpsFreshCount = statuses.stream().filter(this::isGpsFresh).count();
        if (statCardAgentsGps != null) {
            statCardAgentsGps.setText(String.valueOf(gpsFreshCount));
        }

        liveAgents.setAll(statuses.stream().map(this::toLiveRow).collect(java.util.stream.Collectors.toList()));

        if (agentLiveMapWebView != null) {
            installLiveMapFallbackHandlers("Carte live agents indisponible. Le tableau reste disponible.");
            agentLiveMapWebView.getEngine().loadContent(buildLiveTrackingMapHtml(statuses));
        }
    }

    private void installLiveMapFallbackHandlers(String message) {
        if (agentLiveMapWebView == null || liveMapFallbackHandlersInstalled) {
            return;
        }
        liveMapFallbackHandlersInstalled = true;
        agentLiveMapWebView.getEngine().getLoadWorker().exceptionProperty().addListener((obs, oldValue, error) -> {
            if (error != null) {
                showAdminMessage(message, false);
                agentLiveMapWebView.getEngine().loadContent(buildLiveMapFallbackHtml(message));
            }
        });
        agentLiveMapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED) {
                showAdminMessage(message, false);
                agentLiveMapWebView.getEngine().loadContent(buildLiveMapFallbackHtml(message));
            }
        });
    }

    private String buildLiveMapFallbackHtml(String message) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
            + "body{font-family:Arial,sans-serif;background:#f6f8fb;color:#1f2937;display:flex;"
            + "align-items:center;justify-content:center;height:100%;margin:0;padding:24px;text-align:center;}"
            + ".card{max-width:440px;background:white;border-radius:16px;padding:24px;"
            + "box-shadow:0 8px 24px rgba(15,23,42,0.12);}"
            + ".title{font-size:18px;font-weight:700;margin-bottom:8px;color:#b45309;}"
            + "</style></head><body><div class='card'><div class='title'>Carte live indisponible</div><div>"
            + message + "</div></div></body></html>";
    }

    private AgentLiveRow toLiveRow(com.smartcity.service.AffectationService.AgentLiveStatus status) {
        String position = (status.latitude != null && status.longitude != null)
            ? String.format(java.util.Locale.US, "%.4f, %.4f", status.latitude, status.longitude)
            : "Aucune";
        String fraicheur = status.dernierePosition == null
            ? "Hors ligne"
            : formatFraicheur(status.dernierePosition);
        return new AgentLiveRow(
            status.nomAgent != null ? status.nomAgent : "Agent #" + status.idAgent,
            valueOrDash(status.zoneNom),
            status.missionsActives != null ? status.missionsActives : 0,
            status.missionsEnCours != null ? status.missionsEnCours : 0,
            position,
            fraicheur
        );
    }

    private boolean isGpsFresh(com.smartcity.service.AffectationService.AgentLiveStatus status) {
        return status.dernierePosition != null
            && status.dernierePosition.isAfter(java.time.LocalDateTime.now().minusMinutes(15));
    }

    private String formatFraicheur(java.time.LocalDateTime updatedAt) {
        long minutes = java.time.Duration.between(updatedAt, java.time.LocalDateTime.now()).toMinutes();
        if (minutes <= 1) return "Live";
        if (minutes < 60) return minutes + " min";
        long hours = Math.max(1, minutes / 60);
        return hours + " h";
    }

    private String buildLiveTrackingMapHtml(List<com.smartcity.service.AffectationService.AgentLiveStatus> statuses) {
        StringBuilder markers = new StringBuilder();
        for (com.smartcity.service.AffectationService.AgentLiveStatus status : statuses) {
            if (status.latitude == null || status.longitude == null) {
                continue;
            }
            String color = isGpsFresh(status) ? "#16A34A" : "#9CA3AF";
            String popup = (status.nomAgent == null ? "Agent" : status.nomAgent)
                + "<br/>Zone: " + valueOrDash(status.zoneNom)
                + "<br/>Actives: " + (status.missionsActives == null ? 0 : status.missionsActives)
                + "<br/>En cours: " + (status.missionsEnCours == null ? 0 : status.missionsEnCours)
                + "<br/>GPS: " + (status.dernierePosition == null ? "Hors ligne" : formatFraicheur(status.dernierePosition));
            markers.append(String.format(java.util.Locale.US,
                "L.circleMarker([%.6f,%.6f],{radius:10,color:'%s',fillColor:'%s',fillOpacity:0.9,weight:2}).addTo(map).bindPopup('%s');",
                status.latitude, status.longitude, color, color, popup.replace("'", "\\'")));
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
            + com.smartcity.utils.MapResourceUtils.leafletHead()
            + "<style>html,body,#map{height:100%;margin:0;}</style></head><body>"
            + "<div id='map'></div><script>"
            + "var map=L.map('map').setView([14.7700,-17.3980],12);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);"
            + markers
            + "</script></body></html>";
    }

    private Optional<Utilisateur> openUtilisateurDialog(String title, Utilisateur initial, boolean roleLockedAgent) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/design-system.css").toExternalForm());



        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(initial != null ? initial.getNom() : "");
        TextField emailField = new TextField(initial != null ? initial.getEmail() : "");
        PasswordField motPasseField = new PasswordField();
        motPasseField.setPromptText(initial != null
            ? "Laisser vide pour conserver"
            : "Mot de passe requis");
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("Citoyen", "Agent", "Administrateur"));
        roleCombo.setValue(initial != null ? valueOrDash(initial.getRole()) : (roleLockedAgent ? "Agent" : "Citoyen"));
        ComboBox<String> zoneCombo = new ComboBox<>();
        zoneService.getAllZones().forEach(z -> zoneCombo.getItems().add(z.getNomZone()));
        com.smartcity.model.Zone zoneInitial = initial != null ? zoneService.getZoneById(initial.getIdZone()) : null;
        zoneCombo.setValue(zoneInitial != null ? zoneInitial.getNomZone() : (zoneCombo.getItems().isEmpty() ? "Pikine" : zoneCombo.getItems().get(0)));

        if (roleLockedAgent) {
            roleCombo.setValue("Agent");
            roleCombo.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nom"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Mot de passe"), 0, 2);
        grid.add(motPasseField, 1, 2);
        grid.add(new Label("Role"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Zone"), 0, 4);
        grid.add(zoneCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (nomField.getText().isBlank() || emailField.getText().isBlank() ||
                    roleCombo.getValue() == null || zoneCombo.getValue() == null) {
                showAdminMessage("Tous les champs obligatoires doivent etre renseignes.", false);
                event.consume();
            } else if (initial == null && motPasseField.getText().isBlank()) {
                showAdminMessage("Mot de passe requis pour la création.", false);
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveType) {
                return null;
            }

            Utilisateur u = new Utilisateur();
            u.setNom(nomField.getText().trim());
            u.setEmail(emailField.getText().trim());
            if (motPasseField.getText().isBlank() && initial != null) {
                u.setMotDePasse(initial.getMotDePasse());
            } else {
                u.setMotDePasse(motPasseField.getText());
            }
            u.setRole(roleCombo.getValue());
            int idZone = zoneService.getAllZones().stream()
                .filter(z -> z.getNomZone().equals(zoneCombo.getValue()))
                .mapToInt(com.smartcity.model.Zone::getIdZone)
                .findFirst().orElse(1);
            u.setIdZone(idZone);
            return u;
        });

        return dialog.showAndWait();
    }

    private void configureUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        // Pré-charger toutes les zones en une seule requête pour éviter N+1
        java.util.Map<Integer, String> zonesCache = new java.util.HashMap<>();
        zoneService.getAllZones().forEach(z -> zonesCache.put(z.getIdZone(), z.getNomZone()));
        colUserZone.setCellValueFactory(cell ->
            new SimpleStringProperty(zonesCache.getOrDefault(cell.getValue().getIdZone(), "Zone inconnue")));

        centerColumn(colUserId);
        centerColumn(colUserNom);
        centerColumn(colUserEmail);
        centerColumn(colUserRole);
        centerColumn(colUserZone);
    }

    private void configureAgentsTable() {
        colAgentId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().id()).asObject());
        colAgentNom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nom()));
        colAgentZone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().zone()));
        colAgentTraites.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().nombreTraites()).asObject());

        centerColumn(colAgentId);
        centerColumn(colAgentNom);
        centerColumn(colAgentZone);
        centerColumn(colAgentTraites);
    }

    private void configureLiveAgentsTable() {
        if (tableLiveAgents == null) {
            return;
        }
        colLiveAgentNom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nom()));
        colLiveAgentZone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().zone()));
        colLiveAgentMissions.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().missionsActives()).asObject());
        colLiveAgentEnCours.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().missionsEnCours()).asObject());
        colLiveAgentPosition.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().position()));
        colLiveAgentFraicheur.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().fraicheur()));

        centerColumn(colLiveAgentNom);
        centerColumn(colLiveAgentZone);
        centerColumn(colLiveAgentMissions);
        centerColumn(colLiveAgentEnCours);
        centerColumn(colLiveAgentPosition);
        centerColumn(colLiveAgentFraicheur);
    }

    private void configureSignalementsTable() {
        colSignalementId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colSignalementDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSignalementDescription.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setTooltip(null); return; }
                setText(item);
                setStyle("-fx-alignment: CENTER;");
                setTooltip(new Tooltip(item));
            }
        });
        colSignalementCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colSignalementZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        colSignalementDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDateSignalement() == null ? "" : cell.getValue().getDateSignalement().format(DATE_FORMATTER)));
        colSignalementStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colSignalementUtilisateur.setCellValueFactory(cell -> new SimpleStringProperty(
                valueOrDash(cell.getValue().getUtilisateurNom())));

        centerColumn(colSignalementId);
        centerColumn(colSignalementDescription);
        centerColumn(colSignalementCategorie);
        centerColumn(colSignalementZone);
        centerColumn(colSignalementDate);
        centerColumn(colSignalementStatut);
        centerColumn(colSignalementUtilisateur);
        if (colSignalementCommentaire != null) {
            centerColumn(colSignalementCommentaire);
        }
        // Tri activé sur colonnes clés
        colSignalementDate.setSortable(true);
        colSignalementStatut.setSortable(true);
        colSignalementZone.setSortable(true);
        colSignalementCategorie.setSortable(true);
        colSignalementId.setSortable(true);
        colSignalementDescription.setSortable(false);
        if (colSignalementCommentaire != null) colSignalementCommentaire.setSortable(false);

        colSignalementStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                    return;
                }
                setText(item);
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: "
                    + com.smartcity.model.SignalementStatut.couleur(item) + ";");
            }
        });
    }

    private void ajouterPagination() {
        if (!(tableSignalements.getParent() instanceof VBox vbox)) return;
        int idx = vbox.getChildren().indexOf(tableSignalements);
        if (idx < 0) return;
        Label pageLabel = new Label("Page 1 / 1  (0 total)");
        pageLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");
        Button btnPrev = new Button("\u25c4 Pr\u00e9c.");
        Button btnNext = new Button("Suiv. \u25ba");
        String btnStyle = "-fx-background-color:#1565C0;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:4 10;";
        btnPrev.setStyle(btnStyle); btnNext.setStyle(btnStyle);
        btnPrev.setDisable(true);
        btnPrev.setOnAction(e -> { if (currentPage > 0) { currentPage--; afficherPage(pageLabel, btnPrev, btnNext); } });
        btnNext.setOnAction(e -> {
            int maxPage = (int) Math.ceil((double) tousLesSignalements.size() / PAGE_SIZE) - 1;
            if (currentPage < maxPage) { currentPage++; afficherPage(pageLabel, btnPrev, btnNext); }
        });
        javafx.scene.layout.HBox bar = new javafx.scene.layout.HBox(8, btnPrev, pageLabel, btnNext);
        bar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        bar.setPadding(new javafx.geometry.Insets(4, 0, 0, 0));
        vbox.getChildren().add(idx + 1, bar);
        // Synchroniser tousLesSignalements avec signalements
        signalements.addListener((javafx.collections.ListChangeListener<Signalement>) c -> {
            if (paginationUpdating) {
                return;
            }
            tousLesSignalements.setAll(c.getList());
            currentPage = 0;
            afficherPage(pageLabel, btnPrev, btnNext);
        });
    }

    private void afficherPage(Label pageLabel, Button btnPrev, Button btnNext) {
        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, tousLesSignalements.size());
        if (from <= to) {
            paginationUpdating = true;
            try {
                signalements.setAll(tousLesSignalements.subList(from, to));
            } finally {
                paginationUpdating = false;
            }
        }
        int maxPage = Math.max(1, (int) Math.ceil((double) tousLesSignalements.size() / PAGE_SIZE));
        pageLabel.setText("Page " + (currentPage + 1) + " / " + maxPage
            + "  (" + tousLesSignalements.size() + " total)");
        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(currentPage >= maxPage - 1);
    }

    private void configureFilters() {
        List<String> zoneNames = new java.util.ArrayList<>();
        zoneNames.add(ALL_ZONES);
        zoneService.getAllZones().forEach(z -> zoneNames.add(z.getNomZone()));
        filterZoneCombo.setItems(FXCollections.observableArrayList(zoneNames));
        filterZoneCombo.setValue(ALL_ZONES);

        // Configuration canonique ci-dessous avec labels issus de SignalementStatut
        filterStatutCombo.setItems(FXCollections.observableArrayList(
            java.util.stream.Stream.concat(
                java.util.stream.Stream.of(ALL_STATUTS),
                java.util.Arrays.stream(com.smartcity.model.SignalementStatut.values())
                    .map(com.smartcity.model.SignalementStatut::label)
            ).collect(java.util.stream.Collectors.toList())));
        filterStatutCombo.setValue(ALL_STATUTS);

        if (filterPrioriteCombo != null) {
            filterPrioriteCombo.setItems(FXCollections.observableArrayList("Toutes", "A traiter aujourd'hui", "Urgent >24h", "Critique >72h"));
            filterPrioriteCombo.setValue("Toutes");
        }

        if (filterAffectationCombo != null) {
            filterAffectationCombo.setItems(FXCollections.observableArrayList("Toutes", "Assignes", "Non assignes"));
            filterAffectationCombo.setValue("Toutes");
        }

        filterCategorieCombo.setItems(FXCollections.observableArrayList(
                java.util.stream.Stream.concat(
                    java.util.stream.Stream.of(ALL_CATEGORIES),
                    com.smartcity.model.SignalementStatut.CATEGORIES.stream()
                ).collect(java.util.stream.Collectors.toList())));
        filterCategorieCombo.setValue(ALL_CATEGORIES);
    }

    private void showAdminDashboardPage() {
        showPage(pageAdminDashboard, btnAdminDashboard);
    }

    private void showPage(javafx.scene.Node pageToShow, Button activeButton) {
        com.smartcity.utils.NavigationUtils.showPage(
            pageToShow,
            new javafx.scene.Node[]{pageAdminDashboard, pageAdminUsers, pageAdminAgents, pageAdminSignalements, pageStatistiques, pageAdminProfil, pageParametres},
            activeButton,
            new Button[]{btnAdminDashboard, btnGestionUtilisateurs, btnGestionAgents, btnGestionSignalements, btnStatistiques, btnAdminProfil, btnParametres},
            "sidebar-button-active"
        );
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER;");
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    private String getFilterValue(ComboBox<String> combo, String allValue) {
        if (combo == null || combo.getValue() == null || allValue.equals(combo.getValue())) {
            return null;
        }
        return combo.getValue();
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private void animatePercent(Label label, int endPct) {
        com.smartcity.utils.AnimationUtils.animateCounter(
            label, 0, endPct,
            javafx.util.Duration.millis(800)
        ).play();
        javafx.animation.Timeline fix = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(830),
                e -> label.setText(endPct + "%")));
        fix.play();
    }

    private final javafx.animation.PauseTransition adminMsgDelay =
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(4));

    private void startLiveTrackingAutoRefresh() {
        stopLiveTrackingAutoRefresh();
        liveTrackingRefreshTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> refreshLiveTracking()));
        liveTrackingRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        liveTrackingRefreshTimeline.play();
    }

    private void stopLiveTrackingAutoRefresh() {
        if (liveTrackingRefreshTimeline != null) {
            liveTrackingRefreshTimeline.stop();
            liveTrackingRefreshTimeline = null;
        }
    }

    private void showAdminMessage(String message, boolean success) {
        if (adminMessageLabel == null) return;
        adminMessageLabel.setVisible(true);
        adminMessageLabel.setManaged(true);
        adminMessageLabel.setText(message);
        adminMessageLabel.setStyle(success
            ? "-fx-background-color: rgba(67,160,71,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;"
            : "-fx-background-color: rgba(239,83,80,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");
        adminMessageLabel.setOpacity(1);
        adminMsgDelay.stop();
        adminMsgDelay.setOnFinished(e -> {
            javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), adminMessageLabel);
            fade.setFromValue(1); fade.setToValue(0);
            fade.setOnFinished(ev -> adminMessageLabel.setText(""));
            fade.play();
        });
        adminMsgDelay.playFromStart();
    }

    private void refreshZoneSettings() {
        if (zonesListLabel == null) {
            return;
        }
        String zones = zoneService.getAllZones().stream()
            .map(com.smartcity.model.Zone::getNomZone)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(java.util.stream.Collectors.joining(" | "));
        zonesListLabel.setText(zones.isBlank() ? "Aucune zone configuree." : zones);
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

    public static class AgentStatsRow {
        private final int id;
        private final String nom;
        private final String zone;
        private final int nombreTraites;

        public AgentStatsRow(int id, String nom, String zone, int nombreTraites) {
            this.id = id;
            this.nom = nom;
            this.zone = zone;
            this.nombreTraites = nombreTraites;
        }

        public int id() {
            return id;
        }

        public String nom() {
            return nom;
        }

        public String zone() {
            return zone;
        }

        public int nombreTraites() {
            return nombreTraites;
        }
    }

    public static class AgentLiveRow {
        private final String nom;
        private final String zone;
        private final int missionsActives;
        private final int missionsEnCours;
        private final String position;
        private final String fraicheur;

        public AgentLiveRow(String nom, String zone, int missionsActives, int missionsEnCours, String position, String fraicheur) {
            this.nom = nom;
            this.zone = zone;
            this.missionsActives = missionsActives;
            this.missionsEnCours = missionsEnCours;
            this.position = position;
            this.fraicheur = fraicheur;
        }

        public String nom() {
            return nom;
        }

        public String zone() {
            return zone;
        }

        public int missionsActives() {
            return missionsActives;
        }

        public int missionsEnCours() {
            return missionsEnCours;
        }

        public String position() {
            return position;
        }

        public String fraicheur() {
            return fraicheur;
        }
    }
}
