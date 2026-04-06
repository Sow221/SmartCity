# -*- coding: utf-8 -*-
import sys, re
sys.stdout.reconfigure(encoding='utf-8')

f = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
c = open(f, encoding='utf-8').read()

fixes = []

# 1. Imports nécessaires
if 'import com.smartcity.service.AffectationService;' not in c:
    c = c.replace(
        'import com.smartcity.service.GeolocationService;',
        'import com.smartcity.service.AffectationService;\nimport com.smartcity.service.GeolocationService;'
    )
    fixes.append('AffectationService import')

if 'import javafx.collections.transformation.FilteredList;' not in c:
    c = c.replace(
        'import javafx.collections.ObservableList;',
        'import javafx.collections.ObservableList;\nimport javafx.collections.transformation.FilteredList;'
    )
    fixes.append('FilteredList import')

if 'import javafx.scene.chart.PieChart;' not in c:
    c = c.replace(
        'import javafx.scene.layout.BorderPane;',
        'import javafx.scene.chart.PieChart;\nimport javafx.scene.layout.BorderPane;'
    )
    fixes.append('PieChart import')

# 2. Statut Collecte -> Terminé
c = c.replace('"Collecte"', '"Terminé"')
fixes.append('Collecte->Terminé')

# 3. Ajouter AffectationService dans les services
if 'affectationService' not in c:
    c = c.replace(
        'private SignalementService signalementService = new SignalementService();',
        'private SignalementService signalementService = new SignalementService();\n    private AffectationService affectationService = new AffectationService();'
    )
    fixes.append('affectationService field')

# 4. Ajouter missionsZone ObservableList
if 'missionsZone' not in c:
    c = c.replace(
        'private ObservableList<Signalement> historique = FXCollections.observableArrayList();',
        'private ObservableList<Signalement> historique = FXCollections.observableArrayList();\n    private ObservableList<Signalement> missionsZone = FXCollections.observableArrayList();'
    )
    fixes.append('missionsZone field')

# 5. Ajouter FilteredList field
if 'filteredMissions' not in c:
    c = c.replace(
        'private ObservableList<Signalement> missionsZone = FXCollections.observableArrayList();',
        'private ObservableList<Signalement> missionsZone = FXCollections.observableArrayList();\n    private FilteredList<Signalement> filteredMissions;'
    )
    fixes.append('filteredMissions field')

# 6. Ajouter bienvenuAffiche flag
if 'bienvenuAffiche' not in c:
    c = c.replace(
        'private MainApp mainApp;',
        'private MainApp mainApp;\n    private boolean bienvenuAffiche = false;'
    )
    fixes.append('bienvenuAffiche flag')

# 7. Ajouter PieChart @FXML
if 'agentPieChart' not in c:
    c = c.replace(
        '@FXML private WebView mapWebView;',
        '@FXML private WebView mapWebView;\n    @FXML private PieChart agentPieChart;\n    @FXML private ComboBox<String> filtreMissionsCombo;\n    @FXML private Label lblMissionsFiltre;\n    @FXML private Label agentCardAujourdhui;\n    @FXML private Label agentCardTaux;\n    @FXML private Label histCardTotal, histCardCeMois, histCardAujourdhui, histCardCategoriePrincipale;\n    @FXML private Label profilMissionsTraitees, profilMissionsActives;\n    @FXML private Button btnDemarrerDepuisCarte, btnNavigationGPS;\n    @FXML private Label mapMissionCategorieLabel;'
    )
    fixes.append('PieChart and new @FXML fields')

# 8. Corriger chargerDonnees pour utiliser getSignalementsByAgent
old_charge = 'List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());\n        missions.setAll(missionList);'
new_charge = '''List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());
        List<Signalement> mesMissions = signalementService.getSignalementsByAgent(current.getIdUser());
        missions.setAll(mesMissions);
        missionsZone.setAll(missionList);
        if (!bienvenuAffiche) {
            bienvenuAffiche = true;
            long nbAttente = mesMissions.stream().filter(s -> "Affecté".equalsIgnoreCase(s.getStatut()) || "En attente".equalsIgnoreCase(s.getStatut())).count();
            long nbEnCours = mesMissions.stream().filter(s -> "En cours".equalsIgnoreCase(s.getStatut())).count();
            showAgentMessage("Bonjour " + current.getNom() + " \uD83D\uDC4B  " + nbAttente + " mission(s) à traiter  •  " + nbEnCours + " en cours", true);
        }'''
if old_charge in c:
    c = c.replace(old_charge, new_charge, 1)
    fixes.append('chargerDonnees getSignalementsByAgent')

# 9. Corriger refreshCards pour utiliser missionsZone
old_cards_start = '    private void refreshCards() {\n        int total = missions.size();\n        int attente = (int) missions.stream().filter(s -> "En attente"'
new_cards_start = '    private void refreshCards() {\n        int total = missionsZone.isEmpty() ? missions.size() : missionsZone.size();\n        ObservableList<Signalement> src = missionsZone.isEmpty() ? missions : missionsZone;\n        int attente = (int) src.stream().filter(s -> "En attente"'
if old_cards_start in c:
    c = c.replace(old_cards_start, new_cards_start, 1)
    fixes.append('refreshCards uses missionsZone')

# 10. Corriger isTermine - utiliser "Terminé" uniquement
old_is = '''    private boolean isTermine(Signalement signalement) {
        String statut = signalement.getStatut();
        if (statut == null) return false;
        String s = statut.toLowerCase().trim();
        return s.equals("collecte") || s.equals("termine") || s.equals("terminé") || s.startsWith("collect");
    }'''
new_is = '''    private boolean isTermine(Signalement signalement) {
        return "Terminé".equalsIgnoreCase(signalement.getStatut());
    }'''
if old_is in c:
    c = c.replace(old_is, new_is, 1)
    fixes.append('isTermine simplified')

# 11. Corriger getDisplayStatut
old_disp = '''    private String getDisplayStatut(String statut) {
        if (statut == null) return "";
        if (statut.toLowerCase().startsWith("collect") || "Terminé".equalsIgnoreCase(statut)) return "Terminé";
        return statut;
    }'''
new_disp = '''    private String getDisplayStatut(String statut) {
        return statut == null ? "" : statut;
    }'''
if old_disp in c:
    c = c.replace(old_disp, new_disp, 1)
    fixes.append('getDisplayStatut simplified')

# 12. Corriger zoneCenter pour utiliser gpsService
old_zone = '''    private Point zoneCenter(String zone) {
        if (zone != null && zone.equalsIgnoreCase("Guediawaye")) return new Point(14.7765, -17.4047);
        return new Point(14.7646, -17.3920);
    }'''
new_zone = '''    private Point zoneCenter(String zone) {
        com.smartcity.service.RealTimeGPSService.Coordinates coords = gpsService.getZoneCenter(zone);
        return new Point(coords.lat, coords.lon);
    }'''
if old_zone in c:
    c = c.replace(old_zone, new_zone, 1)
    fixes.append('zoneCenter uses gpsService')

# 13. Corriger getAgentZone null check
old_agent_zone = '''    private String getAgentZone() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        return current != null ? zoneService.getZoneById(current.getIdZone()).getNomZone() : "Pikine";
    }'''
new_agent_zone = '''    private String getAgentZone() {
        Utilisateur current = SessionManager.getUtilisateurConnecte();
        if (current == null) return "Pikine";
        com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());
        return zone != null ? zone.getNomZone() : "Pikine";
    }'''
if old_agent_zone in c:
    c = c.replace(old_agent_zone, new_agent_zone, 1)
    fixes.append('getAgentZone null check')

# 14. Corriger refreshProfil null check
old_profil = '        profilZoneField.setText(zoneService.getZoneById(current.getIdZone()).getNomZone());'
new_profil = '        com.smartcity.model.Zone zone = zoneService.getZoneById(current.getIdZone());\n        profilZoneField.setText(zone != null ? zone.getNomZone() : "");'
if old_profil in c:
    c = c.replace(old_profil, new_profil, 1)
    fixes.append('refreshProfil null check')

# 15. Ajouter commentaire clôture dans marquerTermineAvecAnimation
old_term = 'if (signalementService.updateStatut(mission.getIdSignalement(), "Terminé")) {'
new_term = '''// Commentaire optionnel
        javafx.scene.control.TextArea commentArea = new javafx.scene.control.TextArea();
        commentArea.setPromptText("Commentaire optionnel (ex: accès difficile, dépôt récurrent...)");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        javafx.scene.control.Dialog<ButtonType> commentDialog = new javafx.scene.control.Dialog<>();
        commentDialog.setTitle("Commentaire de clôture");
        commentDialog.setHeaderText("Mission #" + mission.getIdSignalement());
        commentDialog.getDialogPane().setContent(commentArea);
        commentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> cr = commentDialog.showAndWait();
        if (cr.isEmpty() || cr.get() == ButtonType.CANCEL) { button.setDisable(false); return; }
        String commentaire = commentArea.getText().trim();

        if (signalementService.updateStatut(mission.getIdSignalement(), "Terminé")) {
            if (!commentaire.isBlank()) affectationService.sauvegarderCommentaire(mission.getIdSignalement(), commentaire);'''

# Trouver le bon endroit - après le bouton disable
idx = c.find('button.setDisable(true);\n        if (signalementService.updateStatut(mission.getIdSignalement(), "Terminé"))')
if idx >= 0:
    old_block = 'button.setDisable(true);\n        if (signalementService.updateStatut(mission.getIdSignalement(), "Terminé")) {'
    new_block = 'button.setDisable(true);\n        ' + new_term
    c = c.replace(old_block, new_block, 1)
    fixes.append('commentaire clôture')

# 16. Corriger showAgentMessage pour utiliser CSS classes
old_msg = '''    private void showAgentMessage(String message, boolean success) {
        String emoji = success ? "🎉" : "⚠️";
        String fullMessage = emoji + " " + message;
        agentMessageLabel.setText(fullMessage);
        String baseStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13px;";
        if (success) agentMessageLabel.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049); " + baseStyle);
        else agentMessageLabel.setStyle("-fx-background-color: linear-gradient(to right, #f44336, #da190b); " + baseStyle);'''
new_msg = '''    private void showAgentMessage(String message, boolean success) {
        agentMessageLabel.setText((success ? "\uD83C\uDF89 " : "\u26A0\uFE0F ") + message);
        agentMessageLabel.getStyleClass().removeAll("message-success", "message-error");
        agentMessageLabel.getStyleClass().add(success ? "message-success" : "message-error");'''
if old_msg in c:
    c = c.replace(old_msg, new_msg, 1)
    fixes.append('showAgentMessage CSS classes')

# Sauvegarder
with open(f, 'w', encoding='utf-8') as fw:
    fw.write(c)

print('FIXES APPLIED:')
for fix in fixes:
    print(' -', fix)

# Vérification
c2 = open(f, encoding='utf-8').read()
print('\nVERIFICATION:')
print('size:', len(c2))
print('package OK:', c2.startswith('package com.smartcity'))
print('AffectationService:', 'affectationService' in c2)
print('Collecte remaining:', '"Collecte"' in c2)
print('isTermine simplified:', 'equalsIgnoreCase(signalement.getStatut())' in c2)
print('missionsZone:', 'missionsZone' in c2)
