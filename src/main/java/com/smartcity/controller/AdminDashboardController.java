package com.smartcity.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.smartcity.app.MainApp;
import com.smartcity.model.Signalement;
import com.smartcity.model.Utilisateur;
import com.smartcity.service.AnalyticsRulesService;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controleur du dashboard administrateur.
 */
public class AdminDashboardController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label adminNameLabel;
    @FXML
    private Label adminAvatarLabel;
    @FXML
    private Label adminMessageLabel;
    @FXML
    private Button themeToggleButton;

    @FXML
    private Button btnAdminDashboard;
    @FXML
    private Button btnGestionUtilisateurs;
    @FXML
    private Button btnGestionAgents;
    @FXML
    private Button btnGestionSignalements;
    @FXML
    private Button btnStatistiques;
    @FXML
    private Button btnAdminProfil;
    @FXML
    private Button btnParametres;

    @FXML
    private javafx.scene.control.ScrollPane pageAdminDashboard;
    @FXML
    private javafx.scene.control.ScrollPane pageAdminUsers;
    @FXML
    private javafx.scene.control.ScrollPane pageAdminAgents;
    @FXML
    private javafx.scene.control.ScrollPane pageAdminSignalements;
    @FXML
    private javafx.scene.control.ScrollPane pageStatistiques;
    @FXML
    private VBox pageAdminProfil;
    @FXML
    private javafx.scene.control.ScrollPane pageParametres;

    @FXML
    private Label adminCardTotalSignalements;
    @FXML
    private Label adminCardEnAttente;
    @FXML
    private Label adminCardEnCours;
    @FXML
    private Label adminCardCollectes;
    @FXML
    private Label adminCardTotalUtilisateurs;

    @FXML
    private PieChart statsPieChart;
    @FXML
    private BarChart<String, Number> statsBarChart;

    // Page Statistiques
    @FXML
    private Label statCardTauxResolution;
    @FXML
    private Label statCardCritiques72h;
    @FXML
    private Label statCardTourneesActives;
    @FXML
    private javafx.scene.web.WebView agentLiveMapWebView;
    @FXML
    private TableView<AlertRow> tableAlerts;
    @FXML
    private TableColumn<AlertRow, String> colAlertSeverity;
    @FXML
    private TableColumn<AlertRow, String> colAlertTitle;
    @FXML
    private TableColumn<AlertRow, String> colAlertDetail;
    @FXML
    private TableColumn<AlertRow, Void> colAlertAck;

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;
    @FXML
    private TableColumn<Utilisateur, Integer> colUserId;
    @FXML
    private TableColumn<Utilisateur, String> colUserNom;
    @FXML
    private TableColumn<Utilisateur, String> colUserEmail;
    @FXML
    private TableColumn<Utilisateur, String> colUserRole;
    @FXML
    private TableColumn<Utilisateur, String> colUserZone;
    @FXML
    private Button btnModifierUtilisateur;
    @FXML
    private Button btnSupprimerUtilisateur;

    @FXML
    private TableView<AgentStatsRow> tableAgents;
    @FXML
    private TableColumn<AgentStatsRow, Integer> colAgentId;
    @FXML
    private TableColumn<AgentStatsRow, String> colAgentNom;
    @FXML
    private TableColumn<AgentStatsRow, String> colAgentZone;
    @FXML
    private TableColumn<AgentStatsRow, Integer> colAgentTraites;
    @FXML
    private TableColumn<AgentStatsRow, String> colAgentEmail;
    @FXML
    private TableColumn<AgentStatsRow, Void> colAgentStatut;
    @FXML
    private javafx.scene.control.TextField searchUserField;
    @FXML
    private javafx.scene.control.TextField searchAgentField;
    @FXML
    private javafx.scene.control.ComboBox<String> filterAgentZoneCombo;
    @FXML
    private Button btnModifierAgent;
    @FXML
    private Button btnSupprimerAgent;

    @FXML
    private Button btnModifierStatutSignalement;
    @FXML
    private Button btnReaffecterSignalement;
    @FXML
    private Button btnSupprimerSignalement;

    @FXML
    private ComboBox<String> filterZoneCombo;
    @FXML
    private ComboBox<String> filterStatutCombo;
    @FXML
    private ComboBox<String> filterPrioriteCombo;
    @FXML
    private ComboBox<String> filterAffectationCombo;
    @FXML
    private ComboBox<String> filterCategorieCombo;
    @FXML
    private javafx.scene.control.DatePicker filterDateDebutPicker;
    @FXML
    private javafx.scene.control.DatePicker filterDateFinPicker;
    @FXML
    private Label adminCardUrgents;
    @FXML
    private javafx.scene.control.ProgressIndicator loadingSpinner;
    @FXML
    private TableView<Signalement> tableSignalements;
    @FXML
    private TableColumn<Signalement, Integer> colSignalementId;
    @FXML
    private TableColumn<Signalement, String> colSignalementDescription;
    @FXML
    private TableColumn<Signalement, String> colSignalementCategorie;
    @FXML
    private TableColumn<Signalement, String> colSignalementZone;
    @FXML
    private TableColumn<Signalement, String> colSignalementDate;
    @FXML
    private TableColumn<Signalement, String> colSignalementStatut;
    @FXML
    private TableColumn<Signalement, String> colSignalementUtilisateur;
    @FXML
    private TableColumn<Signalement, String> colSignalementCommentaire;

    @FXML
    private Label adminProfilAvatarLabel;
    @FXML
    private Label adminProfilNomDisplay;
    @FXML
    private TextField adminProfilNomField;
    @FXML
    private TextField adminProfilEmailField;
    @FXML
    private Label adminProfilRoleField;
    @FXML
    private Label adminProfilDateLabel;
    @FXML
    private TextField nouvelleZoneField;
    @FXML
    private Label zonesListLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final SignalementService signalementService = new SignalementService();
    private final ZoneService zoneService = new ZoneService();
    private final com.smartcity.service.AffectationService affectationService = new com.smartcity.service.AffectationService();
    private final com.smartcity.service.SuiviService suiviService = new com.smartcity.service.SuiviService();
    private final AnalyticsRulesService analyticsRulesService = new AnalyticsRulesService();
    private final ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private final ObservableList<AgentStatsRow> agents = FXCollections.observableArrayList();
    private final ObservableList<Signalement> signalements = FXCollections.observableArrayList();
    private final ObservableList<AlertRow> activeAlerts = FXCollections.observableArrayList();
    private final Set<String> acknowledgedAlerts = new HashSet<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",
            java.util.Locale.FRANCE);
    private static final String ALL_ZONES = "Toutes";
    private static final String ALL_STATUTS = "Tous";
    private static final String ALL_CATEGORIES = "Toutes";

    private static final int PAGE_SIZE = 50;
    private final ObservableList<Signalement> tousLesSignalements = FXCollections.observableArrayList();
    private int currentPage = 0;
    private boolean paginationUpdating;

    private volatile boolean isLoading = false;
    private MainApp mainApp;
    private boolean liveMapFallbackHandlersInstalled;
    private javafx.animation.Timeline liveTrackingRefreshTimeline;
    private java.util.Timer liveStatistiquesRefreshTimer;
    private final Object timerLock = new Object();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null || !SessionManager.isAdmin()) {
            showAdminMessage("Acces reserve au role Administrateur.", false);
            if (mainApp != null)
                javafx.application.Platform.runLater(() -> mainApp.showLoginScreen());
            return;
        }

        adminNameLabel.setText(current.getNom());
        if (adminAvatarLabel != null)
            adminAvatarLabel.setText(current.getNom().substring(0, 1).toUpperCase(java.util.Locale.ROOT));
        configureUsersTable();
        configureAgentsTable();
        configureSignalementsTable();
        tableSignalements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Signalement sel = tableSignalements.getSelectionModel().getSelectedItem();
                if (sel != null)
                    afficherDetailSignalement(sel);
            }
        });
        configureAlertsTable();
        configureFilters();
        refreshZoneSettings();

        btnModifierUtilisateur.disableProperty()
                .bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerUtilisateur.disableProperty()
                .bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnModifierAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnModifierStatutSignalement.disableProperty()
                .bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerSignalement.disableProperty()
                .bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());
        if (btnReaffecterSignalement != null)
            btnReaffecterSignalement.disableProperty()
                    .bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());

        tableUtilisateurs.setItems(utilisateurs);
        tableUtilisateurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Recherche temps reel utilisateurs
        if (searchUserField != null) {
            searchUserField.textProperty().addListener((obs, old, val) -> {
                if (val == null || val.isBlank()) {
                    tableUtilisateurs.setItems(utilisateurs);
                    return;
                }
                String lower = val.toLowerCase(java.util.Locale.ROOT);
                tableUtilisateurs.setItems(javafx.collections.FXCollections.observableArrayList(
                        utilisateurs.stream().filter(u -> u.getNom().toLowerCase(java.util.Locale.ROOT).contains(lower)
                                || u.getEmail().toLowerCase(java.util.Locale.ROOT).contains(lower))
                                .collect(java.util.stream.Collectors.toList())));
            });
        }
        tableAgents.setItems(agents);
        tableAgents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Filtre zone + recherche agents
        if (filterAgentZoneCombo != null) {
            filterAgentZoneCombo.getItems().clear();
            filterAgentZoneCombo.getItems().add("Toutes les zones");
            zoneService.getAllZones().forEach(z -> filterAgentZoneCombo.getItems().add(z.getNomZone()));
            filterAgentZoneCombo.setValue("Toutes les zones");
            filterAgentZoneCombo.valueProperty().addListener((obs, old, val) -> filtrerAgents());
        }
        if (searchAgentField != null) {
            searchAgentField.textProperty().addListener((obs, old, val) -> filtrerAgents());
        }
        tableSignalements.setItems(signalements);
        tableSignalements.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        if (tableAlerts != null)
            tableAlerts.setItems(activeAlerts);
        // Pagination : boutons Précédent/Suivant ajoutés dynamiquement sous la table
        ajouterPagination();

        applyTheme();
        showAdminDashboardPage();
        rootPane.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.F5)
                chargerDonnees();
        });
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
        if (isLoading)
            return;
        isLoading = true;
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(true);
            loadingSpinner.setManaged(true);
        }
        final javafx.scene.Node btnActualiser = rootPane != null ? rootPane.lookup("#btnActualiser") : null;
        if (btnActualiser != null)
            btnActualiser.setDisable(true);
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                int total = signalementService.countAll();
                int attente = signalementService.countByStatut("En attente");
                int encours = signalementService.countByStatut("En cours")
                        + signalementService.countByStatut("Affect\u00e9");
                int collectes = signalementService.countByStatut("Termin\u00e9");
                int urgents = signalementService.countUrgents();
                List<Utilisateur> users = utilisateurService.getUtilisateursByRole("Citoyen");
                List<Signalement> sigs = signalementService.getAllSignalements();
                // Fix N+1 agents : 1 seule requête SQL avec JOIN
                List<UtilisateurService.AgentStats> agentStats = utilisateurService.getAgentsWithStats();
                // Fix N+1 colonne agent : 1 seule requête JOIN
                java.util.Map<Integer, String> agentParSig = affectationService.getAgentNomParSignalement();
                javafx.application.Platform.runLater(() -> {
                    try {
                        adminCardTotalSignalements.setText(String.valueOf(total));
                        adminCardEnAttente.setText(String.valueOf(attente));
                        adminCardEnCours.setText(String.valueOf(encours));
                        adminCardCollectes.setText(String.valueOf(collectes));
                        adminCardTotalUtilisateurs.setText(String.valueOf(users.size()));
                        if (adminCardUrgents != null) {
                            adminCardUrgents.setText(String.valueOf(urgents));
                            adminCardUrgents.setStyle(urgents > 0
                                    ? "-fx-text-fill: #D32F2F; -fx-font-weight: bold;"
                                    : "-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        }
                        utilisateurs.setAll(users);
                        signalements.setAll(sigs);
                        agents.clear();
                        agentStats.forEach(a -> agents.add(new AgentStatsRow(a.idUser(), a.nom(), a.email(),
                                a.zoneNom(), a.traites(), a.actif())));
                        // Appliquer la map agent sans requetes supplementaires
                        if (colSignalementCommentaire != null) {
                            colSignalementCommentaire.setCellValueFactory(cell -> new SimpleStringProperty(
                                    agentParSig.getOrDefault(cell.getValue().getIdSignalement(), "-")));
                        }
                        refreshLiveTracking();
                        refreshDashboardCharts();
                        if (pageStatistiques != null && pageStatistiques.isVisible())
                            refreshAnalysisCompactMetrics();
                    } catch (Exception ex) {
                        showAdminMessage("Erreur lors de la mise a jour de l'interface.", false);
                    } finally {
                        if (loadingSpinner != null) {
                            loadingSpinner.setVisible(false);
                            loadingSpinner.setManaged(false);
                        }
                        if (btnActualiser != null)
                            btnActualiser.setDisable(false);
                        isLoading = false;
                    }
                });
                return null;
            }
        };
        task.setOnFailed(e -> javafx.application.Platform.runLater(() -> {
            isLoading = false;
            if (loadingSpinner != null) {
                loadingSpinner.setVisible(false);
                loadingSpinner.setManaged(false);
            }
            if (btnActualiser != null)
                btnActualiser.setDisable(false);
            showAdminMessage("Erreur lors du chargement des donn\u00e9es.", false);
        }));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleDeconnexion() {
        if (!confirm("Déconnexion", "Voulez-vous vraiment vous déconnecter ?"))
            return;
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
        refreshAnalysisCompactMetrics();
        refreshLiveTracking();
        // Auto-refresh toutes les 30 secondes
        startLiveStatistiquesAutoRefresh();
    }

    private void startLiveStatistiquesAutoRefresh() {
        synchronized (timerLock) {
            // Arreter le timer precedent s'il existe
            if (liveStatistiquesRefreshTimer != null) {
                liveStatistiquesRefreshTimer.cancel();
                liveStatistiquesRefreshTimer = null;
            }
            // Demarrer un nouveau timer
            liveStatistiquesRefreshTimer = new java.util.Timer("AdminStatistiquesAutoRefresh", true);
            liveStatistiquesRefreshTimer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    if (pageStatistiques != null && pageStatistiques.isVisible()) {
                        javafx.application.Platform.runLater(() -> {
                            refreshAnalysisCompactMetrics();
                            refreshLiveTracking();
                        });
                    }
                }
            }, 30000, 30000); // Refresh apres 30s et puis tous les 30s
        }
    }

    private void stopLiveStatistiquesAutoRefresh() {
        synchronized (timerLock) {
            if (liveStatistiquesRefreshTimer != null) {
                liveStatistiquesRefreshTimer.cancel();
                liveStatistiquesRefreshTimer = null;
            }
        }
    }

    @FXML
    private void handleShowAdminProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current != null) {
            adminProfilNomField.setText(current.getNom());
            adminProfilEmailField.setText(current.getEmail());
            adminProfilRoleField.setText(current.getRole());
            if (adminProfilDateLabel != null && current.getDateInscription() != null)
                adminProfilDateLabel.setText(current.getDateInscription()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
        if (current == null)
            return;
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
        if (current == null)
            return;
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
        if (result.isEmpty() || result.get().isBlank())
            return;
        com.smartcity.utils.ValidationUtils.ValidationResult check = com.smartcity.utils.ValidationUtils
                .validatePassword(result.get());
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
        com.smartcity.service.GeolocationService.Coordinates fallbackCenter = zoneService.getCenterById(1);
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
        if (file == null)
            return;
        try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            fw.write('\uFEFF');
            writeCsvLine(fw, "Zone", "Total", "En attente", "En cours", "Termine", "Taux resolution");
            for (com.smartcity.service.SuiviService.KpiZone k : suiviService.getKpiParZone()) {
                String taux = String.format(java.util.Locale.US, "%.1f%%", k.tauxResolution);
                writeCsvLine(fw,
                        k.nomZone,
                        String.valueOf(k.total),
                        String.valueOf(k.enAttente),
                        String.valueOf(k.enCours),
                        String.valueOf(k.termines),
                        taux);
            }
            showAdminMessage("Export statistiques reussi : " + file.getName(), true);
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
        if (file == null)
            return;
        try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            fw.write('\uFEFF');
            writeCsvLine(fw, "ID", "Description", "Categorie", "Zone", "Date", "Statut", "Citoyen");
            List<Signalement> source = tousLesSignalements.isEmpty() ? signalements : tousLesSignalements;
            for (Signalement s : source) {
                writeCsvLine(fw,
                        String.valueOf(s.getIdSignalement()),
                        s.getDescription(),
                        s.getCategorie(),
                        s.getZoneNom(),
                        s.getDateSignalement() != null ? s.getDateSignalement().format(DATE_FORMATTER) : "",
                        s.getStatut(),
                        valueOrDash(s.getUtilisateurNom()));
            }
            showAdminMessage("Export CSV reussi : " + file.getName() + " (" + source.size() + " lignes)", true);
        } catch (java.io.IOException e) {
            showAdminMessage("Erreur export CSV.", false);
        }
    }

    @FXML
    private void handleAjouterUtilisateur() {
        showAdminMessage("Creation citoyen desactivee. Utiliser l'inscription citoyenne.", false);
    }

    @FXML
    private void handleModifierUtilisateur() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Optional<Utilisateur> result = openUtilisateurDialog("Modifier citoyen", selected, "Citoyen");
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

        if (!confirm("Confirmer", "Désactiver le compte de " + selected.getNom() + " ?")) {
            return;
        }

        if (utilisateurService.deleteUtilisateur(selected.getIdUser())) {
            showAdminMessage("Compte désactivé avec succès.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la désactivation du compte.", false);
        }
    }

    @FXML
    private void handleAjouterAgent() {
        Optional<Utilisateur> result = openUtilisateurDialog("Ajouter agent", null, "Agent");
        if (result.isEmpty()) {
            return;
        }

        Utilisateur agent = result.get();

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

        Optional<Utilisateur> result = openUtilisateurDialog("Modifier agent", base, "Agent");
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

        if (!confirm("Confirmer", "Désactiver le compte de l'agent " + selected.nom() + " ?")) {
            return;
        }

        if (utilisateurService.deleteUtilisateur(selected.id())) {
            showAdminMessage("Agent désactivé avec succès.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la désactivation de l'agent.", false);
        }
    }

    @FXML
    private void handleModifierStatutSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        // Seule action manuelle pertinente : clôturer (Terminé) un signalement déjà
        // affecté
        if (!affectationService.estDejaAffecte(selected.getIdSignalement())) {
            showAdminMessage("Ce signalement n'est pas encore affecté à un agent.", false);
            return;
        }
        if (com.smartcity.model.SignalementStatut.TERMINE.matches(selected.getStatut())) {
            showAdminMessage("Ce signalement est déjà terminé.", false);
            return;
        }
        if (!confirm("Clôturer", "Marquer le signalement #" + selected.getIdSignalement() + " comme Terminé ?"))
            return;
        if (signalementService.updateStatut(selected.getIdSignalement(),
                com.smartcity.model.SignalementStatut.TERMINE.label())) {
            showAdminMessage("Signalement clôturé.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la clôture.", false);
        }
    }

    @FXML
    private void handleReaffecterSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;
        if (!confirm("R\u00e9affecter",
                "R\u00e9affecter le signalement #" + selected.getIdSignalement() + " \u00e0 un autre agent ?"))
            return;
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
        if (selected == null)
            return;
        if ("En cours".equalsIgnoreCase(selected.getStatut())) {
            showAdminMessage("Impossible de supprimer un signalement en cours de traitement.", false);
            return;
        }
        if (!confirm("Confirmer", "Supprimer le signalement #" + selected.getIdSignalement() + " ?"))
            return;
        if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
            showAdminMessage("Signalement supprimé.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la suppression.", false);
        }
    }

    @FXML
    private void handleResetFiltresSignalements() {
        if (filterZoneCombo != null)
            filterZoneCombo.setValue(ALL_ZONES);
        if (filterStatutCombo != null)
            filterStatutCombo.setValue(ALL_STATUTS);
        if (filterCategorieCombo != null)
            filterCategorieCombo.setValue(ALL_CATEGORIES);
        if (filterPrioriteCombo != null)
            filterPrioriteCombo.setValue("Toutes");
        if (filterDateDebutPicker != null)
            filterDateDebutPicker.setValue(null);
        if (filterDateFinPicker != null)
            filterDateFinPicker.setValue(null);
        signalements.setAll(signalementService.getAllSignalements());
        showAdminMessage("Filtres réinitialisés.", true);
    }

    @FXML
    private void handleFiltrerSignalements() {
        String zone = getFilterValue(filterZoneCombo, ALL_ZONES);
        String statut = getFilterValue(filterStatutCombo, ALL_STATUTS);
        String categorie = getFilterValue(filterCategorieCombo, ALL_CATEGORIES);
        String priorite = getFilterValue(filterPrioriteCombo, "Toutes");
        String affectation = null;
        java.time.LocalDate dateDebut = filterDateDebutPicker != null ? filterDateDebutPicker.getValue() : null;
        java.time.LocalDate dateFin = filterDateFinPicker != null ? filterDateFinPicker.getValue() : null;
        signalements.setAll(signalementService.getSignalementsFiltres(zone, statut, categorie, dateDebut, dateFin,
                priorite, affectation));
        showAdminMessage("Filtres appliqu\u00e9s.", true);
    }

    private void refreshDashboardCharts() {
        if (statsPieChart == null && statsBarChart == null)
            return;
        javafx.concurrent.Task<int[]> task = new javafx.concurrent.Task<>() {
            @Override
            protected int[] call() {
                return new int[] {
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
                        new PieChart.Data("Affect\u00e9 (" + affecte + ")", Math.max(affecte, 0.01)),
                        new PieChart.Data("En cours (" + enCours + ")", Math.max(enCours, 0.01)),
                        new PieChart.Data("Termin\u00e9 (" + termines + ")", Math.max(termines, 0.01)));
                statsPieChart.setData(pieData);
                statsPieChart.setLabelsVisible(true);
                statsPieChart.setLegendVisible(true);
            }
            if (statsBarChart != null) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Signalements");
                zoneService.getAllZones().forEach(z -> series.getData()
                        .add(new XYChart.Data<>(z.getNomZone(), signalementService.countByZone(z.getNomZone()))));
                statsBarChart.getData().clear();
                statsBarChart.getData().add(series);
                statsBarChart.setLegendVisible(true);
            }
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void refreshLiveTracking() {
        if (agentLiveMapWebView == null) {
            refreshAnalysisCompactMetrics();
            return;
        }

        List<com.smartcity.service.AffectationService.AgentLiveStatus> statuses = affectationService
                .getAgentLiveStatuses();

        if (agentLiveMapWebView != null) {
            installLiveMapFallbackHandlers("Carte live agents indisponible. Le tableau reste disponible.");
            agentLiveMapWebView.getEngine().loadContent(buildLiveTrackingMapHtml(statuses));
        }
        refreshAnalysisCompactMetrics();
    }

    private void refreshAnalysisCompactMetrics() {
        AnalyticsRulesService.AnalysisSnapshot snapshot = analyticsRulesService.computeAnalysisSnapshot();
        if (statCardTourneesActives != null) {
            statCardTourneesActives.setText(String.valueOf(snapshot.chargeOperationnelleActive));
        }
        if (statCardCritiques72h != null) {
            statCardCritiques72h.setText(String.valueOf(snapshot.retardsCritiquesGlobaux));
        }
        if (statCardTauxResolution != null) {
            statCardTauxResolution.setText(String.format(java.util.Locale.US, "%.1f%%", snapshot.tauxTraitementGlobal));
        }
        if (tableAlerts != null) {
            List<AlertRow> rows = snapshot.alerts.stream()
                    .filter(a -> !acknowledgedAlerts.contains(a.key))
                    .limit(5)
                    .map(a -> new AlertRow(a.key, a.severity.name(), a.title, a.detail))
                    .collect(java.util.stream.Collectors.toList());
            activeAlerts.setAll(rows);
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

    private boolean isGpsFresh(com.smartcity.service.AffectationService.AgentLiveStatus status) {
        return status.dernierePosition != null
                && status.dernierePosition.isAfter(java.time.LocalDateTime.now().minusMinutes(15));
    }

    private String formatFraicheur(java.time.LocalDateTime updatedAt) {
        long minutes = java.time.Duration.between(updatedAt, java.time.LocalDateTime.now()).toMinutes();
        if (minutes <= 1)
            return "Live";
        if (minutes < 60)
            return minutes + " min";
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
                    + "<br/>GPS: "
                    + (status.dernierePosition == null ? "Hors ligne" : formatFraicheur(status.dernierePosition));
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

    private Optional<Utilisateur> openUtilisateurDialog(String title, Utilisateur initial, String forcedRole) {
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
        ComboBox<String> roleCombo = new ComboBox<>(
                FXCollections.observableArrayList("Citoyen", "Agent", "Administrateur"));
        roleCombo.setValue(initial != null ? valueOrDash(initial.getRole()) : "Citoyen");
        ComboBox<String> zoneCombo = new ComboBox<>();
        zoneService.getAllZones().forEach(z -> zoneCombo.getItems().add(z.getNomZone()));
        com.smartcity.model.Zone zoneInitial = initial != null ? zoneService.getZoneById(initial.getIdZone()) : null;
        zoneCombo.setValue(zoneInitial != null ? zoneInitial.getNomZone()
                : (zoneCombo.getItems().isEmpty() ? "Pikine" : zoneCombo.getItems().get(0)));

        if (forcedRole != null && !forcedRole.isBlank()) {
            roleCombo.setValue(forcedRole);
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

    private void filtrerAgents() {
        String zone = filterAgentZoneCombo != null ? filterAgentZoneCombo.getValue() : null;
        String search = searchAgentField != null ? searchAgentField.getText() : null;
        boolean allZones = zone == null || "Toutes les zones".equals(zone);
        boolean noSearch = search == null || search.isBlank();
        if (allZones && noSearch) {
            tableAgents.setItems(agents);
            return;
        }
        String lowerSearch = noSearch ? "" : search.toLowerCase(java.util.Locale.ROOT);
        tableAgents.setItems(javafx.collections.FXCollections.observableArrayList(
                agents.stream().filter(a -> (allZones || zone.equals(a.zone())) &&
                        (noSearch || a.nom().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch)
                                || a.email().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch)))
                        .collect(java.util.stream.Collectors.toList())));
    }

    private void configureUsersTable() {
        if (tableUtilisateurs != null) {
            tableUtilisateurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        // Pré-charger toutes les zones en une seule requête pour éviter N+1
        java.util.Map<Integer, String> zonesCache = new java.util.HashMap<>();
        zoneService.getAllZones().forEach(z -> zonesCache.put(z.getIdZone(), z.getNomZone()));
        colUserZone.setCellValueFactory(cell -> new SimpleStringProperty(
                zonesCache.getOrDefault(cell.getValue().getIdZone(), "Zone inconnue")));

        centerColumn(colUserId);
        centerColumn(colUserNom);
        centerColumn(colUserEmail);
        centerColumn(colUserRole);
        centerColumn(colUserZone);
    }

    private void configureAgentsTable() {
        colAgentId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().id()).asObject());
        colAgentNom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nom()));
        if (colAgentEmail != null)
            colAgentEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().email()));
        colAgentZone.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().zone()));
        colAgentTraites
                .setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().nombreTraites()).asObject());
        if (colAgentStatut != null) {
            // Required for a custom status cell on a Void column.
            colAgentStatut.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(null));
            colAgentStatut.setCellFactory(col -> new javafx.scene.control.TableCell<AgentStatsRow, Void>() {
                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setGraphic(null);
                        return;
                    }
                    AgentStatsRow row = getTableView().getItems().get(getIndex());
                    boolean actif = row.actif();
                    javafx.scene.control.Label lbl = new javafx.scene.control.Label(
                            actif ? "\u2705 Actif" : "\u274c Inactif");
                    lbl.setStyle(actif ? "-fx-text-fill:#2E7D32;-fx-font-weight:bold;"
                            : "-fx-text-fill:#C62828;-fx-font-weight:bold;");
                    setGraphic(lbl);
                }
            });
        }

        centerColumn(colAgentId);
        centerColumn(colAgentNom);
        centerColumn(colAgentZone);
        centerColumn(colAgentTraites);
    }

    private void configureAlertsTable() {
        if (tableAlerts == null) {
            return;
        }
        colAlertSeverity.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().severity()));
        colAlertTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().title()));
        colAlertDetail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().detail()));
        colAlertAck.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(null));
        colAlertAck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Acquitter");
            {
                btn.getStyleClass().add("btn-outline-sm");
                btn.setOnAction(e -> {
                    AlertRow row = getTableView().getItems().get(getIndex());
                    acknowledgedAlerts.add(row.key());
                    refreshAnalysisCompactMetrics();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void configureSignalementsTable() {
        colSignalementId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colSignalementDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSignalementDescription.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }
                setText(item);
                setStyle("-fx-alignment: CENTER;");
                setTooltip(new Tooltip(item));
            }
        });
        colSignalementCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colSignalementZone.setCellValueFactory(new PropertyValueFactory<>("zoneNom"));
        colSignalementDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDateSignalement() == null ? ""
                        : cell.getValue().getDateSignalement().format(DATE_FORMATTER)));
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
        if (colSignalementCommentaire != null)
            colSignalementCommentaire.setSortable(false);

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
        if (!(tableSignalements.getParent() instanceof VBox vbox))
            return;
        int idx = vbox.getChildren().indexOf(tableSignalements);
        if (idx < 0)
            return;
        Label pageLabel = new Label("Page 1 / 1  (0 total)");
        pageLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");
        Button btnPrev = new Button("\u25c4 Pr\u00e9c.");
        Button btnNext = new Button("Suiv. \u25ba");
        String btnStyle = "-fx-background-color:#1565C0;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:4 10;";
        btnPrev.setStyle(btnStyle);
        btnNext.setStyle(btnStyle);
        btnPrev.setDisable(true);
        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                afficherPage(pageLabel, btnPrev, btnNext);
            }
        });
        btnNext.setOnAction(e -> {
            int maxPage = (int) Math.ceil((double) tousLesSignalements.size() / PAGE_SIZE) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                afficherPage(pageLabel, btnPrev, btnNext);
            }
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
        int to = Math.min(from + PAGE_SIZE, tousLesSignalements.size());
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
                                .map(com.smartcity.model.SignalementStatut::label))
                        .collect(java.util.stream.Collectors.toList())));
        filterStatutCombo.setValue(ALL_STATUTS);

        if (filterPrioriteCombo != null) {
            filterPrioriteCombo.setItems(FXCollections.observableArrayList("Toutes", "A traiter aujourd'hui",
                    "Urgent >24h", "Critique >72h"));
            filterPrioriteCombo.setValue("Toutes");
        }

        // Filtre affectation retire de l'interface admin (non pertinent au flux
        // principal).

        filterCategorieCombo.setItems(FXCollections.observableArrayList(
                java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(ALL_CATEGORIES),
                        com.smartcity.model.SignalementStatut.CATEGORIES.stream())
                        .collect(java.util.stream.Collectors.toList())));
        filterCategorieCombo.setValue(ALL_CATEGORIES);
    }

    private void afficherDetailSignalement(Signalement sig) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Signalement #" + sig.getIdSignalement());
        dialog.setHeaderText(sig.getCategorie() + " \u2014 " + sig.getZoneNom());
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(14));
        content.setPrefWidth(500);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        addAdminDetailRow(content, "Zone", sig.getZoneNom());
        addAdminDetailRow(content, "Cat\u00e9gorie", sig.getCategorie());
        addAdminDetailRow(content, "Description", sig.getDescription() != null ? sig.getDescription() : "-");
        addAdminDetailRow(content, "Statut", sig.getStatut());
        addAdminDetailRow(content, "Citoyen", sig.getUtilisateurNom() != null ? sig.getUtilisateurNom() : "-");
        addAdminDetailRow(content, "Date signalement",
                sig.getDateSignalement() != null ? sig.getDateSignalement().format(fmt) : "-");
        if (sig.getDateCollecte() != null)
            addAdminDetailRow(content, "Date collecte", sig.getDateCollecte().format(fmt));
        com.smartcity.model.Affectation aff = affectationService.getAffectationBySignalement(sig.getIdSignalement());
        if (aff != null) {
            com.smartcity.model.Utilisateur agent = utilisateurService.getUtilisateurById(aff.getIdAgent());
            addAdminDetailRow(content, "Agent affect\u00e9",
                    agent != null ? agent.getNom() : "Agent #" + aff.getIdAgent());
            if (aff.getCommentaire() != null && !aff.getCommentaire().isBlank())
                addAdminDetailRow(content, "Commentaire", aff.getCommentaire());
        }
        com.smartcity.service.SuiviService.EvaluationEntry eval = suiviService
                .getEvaluationBySignalement(sig.getIdSignalement());
        if (eval != null) {
            addAdminDetailRow(content, "Note citoyen",
                    "\u2605".repeat(eval.note) + "\u2606".repeat(5 - eval.note) + " (" + eval.note + "/5)");
            if (eval.commentaire != null && !eval.commentaire.isBlank())
                addAdminDetailRow(content, "Avis citoyen", eval.commentaire);
        }
        dialog.getDialogPane().setContent(new javafx.scene.control.ScrollPane(content));
        dialog.showAndWait();
    }

    private void addAdminDetailRow(javafx.scene.layout.VBox parent, String label, String value) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(label + " :");
        lbl.setStyle("-fx-font-weight:bold;-fx-min-width:150px;-fx-text-fill:#374151;");
        javafx.scene.control.Label val = new javafx.scene.control.Label(value);
        val.setWrapText(true);
        val.setStyle("-fx-text-fill:#1f2937;");
        row.getChildren().addAll(lbl, val);
        parent.getChildren().add(row);
    }

    private void showAdminDashboardPage() {
        showPage(pageAdminDashboard, btnAdminDashboard);
    }

    private void showPage(javafx.scene.Node pageToShow, Button activeButton) {
        // Arreter le timer auto-refresh si on quitte la page Statistiques
        if (pageToShow != pageStatistiques) {
            stopLiveStatistiquesAutoRefresh();
        }
        com.smartcity.utils.NavigationUtils.showPage(
                pageToShow,
                new javafx.scene.Node[] { pageAdminDashboard, pageAdminAgents, pageAdminUsers, pageAdminSignalements,
                        pageStatistiques, pageAdminProfil, pageParametres },
                activeButton,
                new Button[] { btnAdminDashboard, btnGestionAgents, btnGestionUtilisateurs, btnGestionSignalements,
                        btnStatistiques, btnAdminProfil, btnParametres },
                "sidebar-button-active");
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

    private void writeCsvLine(java.io.OutputStreamWriter writer, String... values) throws java.io.IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escapeCsv(values[i]));
        }
        writer.write('\n');
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String normalized = value
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }

    private final javafx.animation.PauseTransition adminMsgDelay = new javafx.animation.PauseTransition(
            javafx.util.Duration.seconds(4));

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
        if (adminMessageLabel == null)
            return;
        adminMessageLabel.setVisible(true);
        adminMessageLabel.setManaged(true);
        adminMessageLabel.setText(message);
        adminMessageLabel.setStyle(success
                ? "-fx-background-color: rgba(67,160,71,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;"
                : "-fx-background-color: rgba(239,83,80,0.95); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 14 10 14;");
        adminMessageLabel.setOpacity(1);
        adminMsgDelay.stop();
        adminMsgDelay.setOnFinished(e -> {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300),
                    adminMessageLabel);
            fade.setFromValue(1);
            fade.setToValue(0);
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

    public static class AlertRow {
        private final String key;
        private final String severity;
        private final String title;
        private final String detail;

        public AlertRow(String key, String severity, String title, String detail) {
            this.key = key;
            this.severity = severity;
            this.title = title;
            this.detail = detail;
        }

        public String key() {
            return key;
        }

        public String severity() {
            return severity;
        }

        public String title() {
            return title;
        }

        public String detail() {
            return detail;
        }
    }

    public static class AgentStatsRow {
        private final int id;
        private final String nom;
        private final String email;
        private final String zone;
        private final int nombreTraites;
        private final boolean actif;

        public AgentStatsRow(int id, String nom, String email, String zone, int nombreTraites, boolean actif) {
            this.id = id;
            this.nom = nom;
            this.email = email;
            this.zone = zone;
            this.nombreTraites = nombreTraites;
            this.actif = actif;
        }

        public String email() {
            return email;
        }

        public boolean actif() {
            return actif;
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

}
