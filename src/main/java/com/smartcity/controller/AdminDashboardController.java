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
    @FXML private PieChart statPieCategorie;
    @FXML private BarChart<String, Number> statBarZone;
    @FXML private javafx.scene.web.WebView heatmapWebView;

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
    @FXML private ComboBox<String> filterCategorieCombo;
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
    private final ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();
    private final ObservableList<AgentStatsRow> agents = FXCollections.observableArrayList();
    private final ObservableList<Signalement> signalements = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String ALL_ZONES = "Toutes";
    private static final String ALL_STATUTS = "Tous";
    private static final String ALL_CATEGORIES = "Toutes";

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null || !SessionManager.isAdmin()) {
            showAdminMessage("Acces reserve au role Administrateur.", false);
            return;
        }

        adminNameLabel.setText(current.getNom());
        configureUsersTable();
        configureAgentsTable();
        configureSignalementsTable();
        configureFilters();

        btnModifierUtilisateur.disableProperty().bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerUtilisateur.disableProperty().bind(tableUtilisateurs.getSelectionModel().selectedItemProperty().isNull());
        btnModifierAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerAgent.disableProperty().bind(tableAgents.getSelectionModel().selectedItemProperty().isNull());
        btnModifierStatutSignalement.disableProperty().bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());
        btnSupprimerSignalement.disableProperty().bind(tableSignalements.getSelectionModel().selectedItemProperty().isNull());

        tableUtilisateurs.setItems(utilisateurs);
        tableAgents.setItems(agents);
        tableSignalements.setItems(signalements);

        applyTheme();
        showAdminDashboardPage();
        
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
        refreshCards();
        refreshUtilisateurs();
        refreshAgents();
        refreshSignalements();
        refreshCharts();
    }

    @FXML
    private void handleDeconnexion() {
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
        refreshCharts();
    }

    @FXML
    private void handleShowAdminProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current != null) {
            adminProfilNomField.setText(current.getNom());
            adminProfilEmailField.setText(current.getEmail());
            adminProfilRoleField.setText(current.getRole());
        }
        showPage(pageAdminProfil, null);
    }

    @FXML
    private void handleModifierAdminProfil() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return;
        if (adminProfilNomField.getText().isBlank() || adminProfilEmailField.getText().isBlank()) {
            showAdminMessage("Nom et email obligatoires.", false);
            return;
        }
        current.setNom(adminProfilNomField.getText().trim());
        current.setEmail(adminProfilEmailField.getText().trim());
        if (utilisateurService.updateUtilisateur(current)) {
            adminNameLabel.setText(current.getNom());
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
        showAdminMessage("Rapports disponibles dans la page Statistiques.", true);
        showPage(pageStatistiques, btnStatistiques);
    }

    @FXML
    private void handleShowParametres() {
        showPage(pageParametres, null);
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
        showAdminMessage("Gestion des zones : fonctionnalite a venir.", true);
    }

    @FXML
    private void handleExporterCSVStats() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Exporter statistiques");
        chooser.setInitialFileName("statistiques.csv");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV", "*.csv"));
        java.io.File file = chooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write("Zone,Total,En attente,En cours,Terminé,Taux resolution\n");
            for (com.smartcity.model.Zone z : zoneService.getAllZones()) {
                int tot = signalementService.countByZone(z.getNomZone());
                int att = signalementService.countByStatutAndZone("En attente", z.getNomZone());
                int enc = signalementService.countByStatutAndZone("En cours", z.getNomZone());
                int ter = signalementService.countByStatutAndZone("Terminé", z.getNomZone());
                String taux = tot == 0 ? "0%" : (int) Math.round(ter * 100.0 / tot) + "%";
                fw.write(String.format("%s,%d,%d,%d,%d,%s\n", z.getNomZone(), tot, att, enc, ter, taux));
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
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
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

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatut(),
            "En attente", "Affecté", "En cours", "Terminé");
        dialog.setTitle("Modifier statut");
        dialog.setHeaderText("Signalement #" + selected.getIdSignalement());
        dialog.setContentText("Nouveau statut :");
        dialog.showAndWait().ifPresent(statut -> {
            if (signalementService.updateStatut(selected.getIdSignalement(), statut)) {
                showAdminMessage("Statut mis à jour : " + statut, true);
                chargerDonnees();
            } else {
                showAdminMessage("Echec de la mise à jour du statut.", false);
            }
        });
    }

    @FXML
    private void handleReaffecterSignalement() {
        Signalement selected = tableSignalements.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        new com.smartcity.service.AffectationService().supprimerAffectationParSignalement(selected.getIdSignalement());
        if (signalementService.updateStatut(selected.getIdSignalement(), "En attente")) {
            new com.smartcity.service.AffectationService().affecterAgentParZone(selected.getIdSignalement(), selected.getIdZone());
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
        if (!confirm("Confirmer", "Supprimer le signalement #" + selected.getIdSignalement() + " ?")) return;
        if (signalementService.supprimerSignalement(selected.getIdSignalement())) {
            showAdminMessage("Signalement supprimé.", true);
            chargerDonnees();
        } else {
            showAdminMessage("Echec de la suppression.", false);
        }
    }

    @FXML
    private void handleFiltrerSignalements() {
        String zone = getFilterValue(filterZoneCombo, ALL_ZONES);
        String statut = getFilterValue(filterStatutCombo, ALL_STATUTS);
        String categorie = getFilterValue(filterCategorieCombo, ALL_CATEGORIES);

        signalements.setAll(signalementService.getSignalementsFiltres(zone, statut, categorie));
        showAdminMessage("Filtres appliques.", true);
    }

    private void refreshCards() {
        int total = signalementService.countAll();
        int attente = signalementService.countByStatut("En attente");
        int encours = signalementService.countByStatut("En cours");
        int collectes = countCollectesGlobal();
        int totalUsers = utilisateurService.countAllActifs();

        adminCardTotalSignalements.setText(String.valueOf(total));
        adminCardEnAttente.setText(String.valueOf(attente));
        adminCardEnCours.setText(String.valueOf(encours));
        adminCardCollectes.setText(String.valueOf(collectes));
        adminCardTotalUtilisateurs.setText(String.valueOf(totalUsers));
    }

    private void refreshUtilisateurs() {
        utilisateurs.setAll(utilisateurService.getAllUtilisateurs());
    }

    private void refreshAgents() {
        List<Utilisateur> utilisateursAgents = utilisateurService.getUtilisateursByRole("Agent");
        agents.clear();
        for (Utilisateur u : utilisateursAgents) {
            int traites = signalementService.countTraitesByAgent(u.getIdUser());
            String zoneNom = zoneService.getZoneById(u.getIdZone()) != null
                    ? zoneService.getZoneById(u.getIdZone()).getNomZone() : "Zone inconnue";
            agents.add(new AgentStatsRow(u.getIdUser(), u.getNom(), zoneNom, traites));
        }
    }

    private void refreshSignalements() {
        signalements.setAll(signalementService.getAllSignalements());
    }

    private void refreshCharts() {
        int total = signalementService.countAll();
        int termines = signalementService.countByStatut("Terminé");

        // --- Dashboard charts ---
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("En attente", signalementService.countByStatut("En attente")),
                new PieChart.Data("En cours", signalementService.countByStatut("En cours")),
                new PieChart.Data("Terminé", termines)
        );
        if (statsPieChart != null) statsPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Signalements");
        zoneService.getAllZones().forEach(z ->
            zoneSeries.getData().add(new XYChart.Data<>(z.getNomZone(), signalementService.countByZone(z.getNomZone()))));
        if (statsBarChart != null) { statsBarChart.getData().clear(); statsBarChart.getData().add(zoneSeries); }

        // --- Page Statistiques ---
        if (statCardTauxResolution != null) {
            int pct = total == 0 ? 0 : (int) Math.round(termines * 100.0 / total);
            statCardTauxResolution.setText(pct + "%");
        }
        if (statCardTauxPikine != null) {
            int totalPikine = signalementService.countByZone("Pikine");
            int terminesPikine = signalementService.countByStatutAndZone("Terminé", "Pikine");
            statCardTauxPikine.setText(totalPikine == 0 ? "0%" : (int) Math.round(terminesPikine * 100.0 / totalPikine) + "%");
        }
        if (statCardTauxGuediawaye != null) {
            int totalG = signalementService.countByZone("Guédiawaye");
            int terminesG = signalementService.countByStatutAndZone("Terminé", "Guédiawaye");
            statCardTauxGuediawaye.setText(totalG == 0 ? "0%" : (int) Math.round(terminesG * 100.0 / totalG) + "%");
        }
        if (statCardMoyenneJour != null) {
            int moyJour = signalementService.countMoyenneParJour();
            statCardMoyenneJour.setText(String.valueOf(moyJour));
        }
        if (statPieCategorie != null) {
            ObservableList<PieChart.Data> catData = FXCollections.observableArrayList();
            for (String cat : new String[]{"Plastique", "Papier", "Organique", "Verre"}) {
                int n = signalementService.countByCategorie(cat);
                if (n > 0) catData.add(new PieChart.Data(cat + " (" + n + ")", n));
            }
            statPieCategorie.setData(catData);
        }
        if (statBarZone != null) {
            statBarZone.getData().clear();
            for (String statut : new String[]{"En attente", "En cours", "Terminé"}) {
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName(statut);
                zoneService.getAllZones().forEach(z ->
                    s.getData().add(new XYChart.Data<>(z.getNomZone(),
                        signalementService.countByStatutAndZone(statut, z.getNomZone()))));
                statBarZone.getData().add(s);
            }
        }
        if (heatmapWebView != null) refreshHeatmap();
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
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
            + "<script src='https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js'></script>"
            + "<style>html,body,#map{height:100%;margin:0;}</style></head><body>"
            + "<div id='map'></div><script>"
            + "var map=L.map('map').setView([14.7646,-17.3920],12);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19}).addTo(map);"
            + "var pts=[" + points + "];"
            + "if(pts.length>0)L.heatLayer(pts,{radius:25,blur:15,maxZoom:17}).addTo(map);"
            + "</script></body></html>";
        heatmapWebView.getEngine().loadContent(html);
    }

    private Optional<Utilisateur> openUtilisateurDialog(String title, Utilisateur initial, boolean roleLockedAgent) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(initial != null ? initial.getNom() : "");
        TextField emailField = new TextField(initial != null ? initial.getEmail() : "");
        PasswordField motPasseField = new PasswordField();
        motPasseField.setText(initial != null ? valueOrDash(initial.getMotDePasse()).equals("-") ? "" : initial.getMotDePasse() : "");
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
                    motPasseField.getText().isBlank() || roleCombo.getValue() == null || zoneCombo.getValue() == null) {
                showAdminMessage("Tous les champs obligatoires doivent etre renseignes.", false);
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
            u.setMotDePasse(motPasseField.getText());
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
        colUserZone.setCellValueFactory(cell -> {
            com.smartcity.model.Zone z = zoneService.getZoneById(cell.getValue().getIdZone());
            return new SimpleStringProperty(z != null ? z.getNomZone() : "Zone inconnue");
        });

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

    private void configureSignalementsTable() {
        colSignalementId.setCellValueFactory(new PropertyValueFactory<>("idSignalement"));
        colSignalementDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
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
            colSignalementCommentaire.setCellValueFactory(cell -> {
                Affectation aff = affectationService.getAffectationBySignalement(cell.getValue().getIdSignalement());
                if (aff == null) return new SimpleStringProperty("-");
                Utilisateur agent = utilisateurService.getUtilisateurById(aff.getIdAgent());
                return new SimpleStringProperty(agent != null ? agent.getNom() : "Agent #" + aff.getIdAgent());
            });
            colSignalementCommentaire.setText("Agent affecté");
            centerColumn(colSignalementCommentaire);
        }

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
                String color = "#F57C00";
                if ("En cours".equalsIgnoreCase(item)) {
                    color = "#1565C0";
                } else if ("Termin\u00e9".equalsIgnoreCase(item)) {
                    color = "#2E7D32";
                } else if ("Affect\u00e9".equalsIgnoreCase(item)) {
                    color = "#7B1FA2";
                }
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
    }

    private void configureFilters() {
        List<String> zoneNames = new java.util.ArrayList<>();
        zoneNames.add(ALL_ZONES);
        zoneService.getAllZones().forEach(z -> zoneNames.add(z.getNomZone()));
        filterZoneCombo.setItems(FXCollections.observableArrayList(zoneNames));
        filterZoneCombo.setValue(ALL_ZONES);

        filterStatutCombo.setItems(FXCollections.observableArrayList(ALL_STATUTS, "En attente", "Affecté", "En cours", "Terminé"));
        filterStatutCombo.setValue(ALL_STATUTS);

        filterCategorieCombo.setItems(FXCollections.observableArrayList(
                ALL_CATEGORIES, "Plastique", "Papier", "Verre", "Métal", "Organique", "Autre"));
        filterCategorieCombo.setValue(ALL_CATEGORIES);
    }

    private void showAdminDashboardPage() {
        showPage(pageAdminDashboard, btnAdminDashboard);
    }

    private void showPage(javafx.scene.Node pageToShow, Button activeButton) {
        javafx.scene.Node[] pages = {pageAdminDashboard, pageAdminUsers, pageAdminAgents, pageAdminSignalements, pageStatistiques, pageAdminProfil, pageParametres};
        for (javafx.scene.Node page : pages) {
            if (page == null) continue;
            boolean visible = page == pageToShow;
            page.setVisible(visible);
            page.setManaged(visible);
        }

        Button[] buttons = {btnAdminDashboard, btnGestionUtilisateurs, btnGestionAgents, btnGestionSignalements, btnStatistiques};
        for (Button btn : buttons) {
            btn.getStyleClass().remove("sidebar-button-active");
            if (btn == activeButton) {
                btn.getStyleClass().add("sidebar-button-active");
            }
        }
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

    private int countCollectesGlobal() {
        return signalementService.countByStatut("Terminé");
    }

    private final javafx.animation.PauseTransition adminMsgDelay =
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(4));

    private void showAdminMessage(String message, boolean success) {
        if (adminMessageLabel == null) return;
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
}