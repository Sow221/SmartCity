# 🔧 PLAN DE FIXES CONCRETS - PAGE STATISTIQUE

**Status**: À Implémenter  
**Priorité**: 4 fixes critiques + 3 améliorations rapides  
**Temps Estimé**: 6-8 heures développement

---

## 🥇 FIX 1: Initialiser activeAlerts ObservableList [CRITIQUE]

### Problème
```java
// AdminDashboardController.java - ligne ~150
private List<AlertRow> activeAlerts;  // ← Jamais initialisée!

// Puis ligne ~1015:
if (tableAlerts != null) {
    // ...
    activeAlerts.setAll(rows);  // ← NullPointerException!
}
```

### Solution
**Fichier**: `AdminDashboardController.java`

```java
// AVANT (ligne ~150):
@FXML private TableView<AlertRow> tableAlerts;
private List<AlertRow> activeAlerts;

// APRÈS:
@FXML private TableView<AlertRow> tableAlerts;
private ObservableList<AlertRow> activeAlerts;  // ← Changé type

// NOUVEAU - dans initialize() ou @FXML private void initialize():
private void initialize() {
    // ... autres initialisations ...
    
    // AJOUTER:
    activeAlerts = FXCollections.observableArrayList();
    if (tableAlerts != null) {
        tableAlerts.setItems(activeAlerts);
        tableAlerts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
```

**Vérification**: Après fix, table alertes doit afficher 0-5 alertes au démarrage.

---

## 🥈 FIX 2: Null-Check sur AnalysisSnapshot [CRITIQUE]

### Problème
```java
// AdminDashboardController.java - refreshAnalysisCompactMetrics() ligne ~1010
AnalyticsRulesService.AnalysisSnapshot snapshot = analyticsRulesService.computeAnalysisSnapshot();
if (statCardTourneesActives != null) {
    statCardTourneesActives.setText(String.valueOf(snapshot.chargeOperationnelleActive));
    // ↑ snapshot peut être null → NPE
}
```

### Solution
**Fichier**: `AdminDashboardController.java`

```java
// AVANT:
private void refreshAnalysisCompactMetrics() {
    AnalyticsRulesService.AnalysisSnapshot snapshot = analyticsRulesService.computeAnalysisSnapshot();
    if (statCardTourneesActives != null) {
        statCardTourneesActives.setText(String.valueOf(snapshot.chargeOperationnelleActive));
        // ...
    }
}

// APRÈS:
private void refreshAnalysisCompactMetrics() {
    try {
        AnalyticsRulesService.AnalysisSnapshot snapshot = analyticsRulesService.computeAnalysisSnapshot();
        
        // ← AJOUTER NULL-CHECK:
        if (snapshot == null) {
            logger.warn("Snapshot null, utilisant valeurs par défaut");
            snapshot = new AnalyticsRulesService.AnalysisSnapshot(0, 0, 0.0, Collections.emptyList(), Collections.emptyList());
        }
        
        if (statCardTourneesActives != null) {
            statCardTourneesActives.setText(String.valueOf(snapshot.chargeOperationnelleActive));
        }
        if (statCardCritiques72h != null) {
            statCardCritiques72h.setText(String.valueOf(snapshot.retardsCritiquesGlobaux));
        }
        if (statCardTauxResolution != null) {
            statCardTauxResolution.setText(String.format(java.util.Locale.US, "%.1f%%", snapshot.tauxTraitementGlobal));
        }
        // ...
    } catch (Exception e) {
        logger.error("Erreur refresh metrics", e);
        showAdminMessage("Erreur actualisation statistiques", false);
    }
}
```

**Vérification**: Pas de crash même si service échoue.

---

## 🥉 FIX 3: Arrêter Timer Quand Page Cachée [CRITIQUE]

### Problème
```java
// AdminDashboardController.java
private void startLiveStatistiquesAutoRefresh() {
    // Démarre timer
    liveStatistiquesRefreshTimer.scheduleAtFixedRate(..., 30000, 30000);
}

// ← Jamais arrêté si utilisateur navigue ailleurs!
// Fuite mémoire + CPU inutile
```

### Solution
**Fichier**: `AdminDashboardController.java`

```java
// MODIFIER: showPage() pour arrêter timer si pageStatistiques cachée

private void showPage(javafx.scene.control.ScrollPane pageToShow, javafx.scene.control.Button buttonToHighlight) {
    // ... code existant ...
    
    // AJOUTER:
    // Arrêter auto-refresh si on quitte page statistiques
    if (pageToShow != pageStatistiques) {
        stopLiveStatistiquesAutoRefresh();  // ← AJOUTER CET APPEL
    } else {
        // On rentre sur page statistiques, démarrer refresh
        startLiveStatistiquesAutoRefresh();
    }
    
    // ... reste du code ...
}

// S'assurer que stopLiveStatistiquesAutoRefresh() existe et fonctionne:
private void stopLiveStatistiquesAutoRefresh() {
    synchronized (timerLock) {
        if (liveStatistiquesRefreshTimer != null) {
            liveStatistiquesRefreshTimer.cancel();
            liveStatistiquesRefreshTimer = null;
            logger.info("Live statistiques auto-refresh arrêté");
        }
    }
}
```

**Vérification**: Naviguer entre pages → pas de fuite mémoire, timer arrêté.

---

## 🔶 FIX 4: Ajouter Feedback UI sur Exports [CRITIQUE]

### Problème
```java
// ReportsController.java - exportReport()
private void exportReport() {
    FileChooser fileChooser = new FileChooser();
    // ...
    File file = fileChooser.showSaveDialog(...);
    if (file != null) {
        try {
            if (file.getName().endsWith(".csv")) {
                exportCsvFile(file);
            } else if (file.getName().endsWith(".xlsx")) {
                exportExcelXlsx(file);
            }
            // ...
        } catch (IOException e) {
            logger.error("Erreur export", e);  // ← Seulement logué!
            // Utilisateur ne voit rien → frustration
        }
    }
}
```

### Solution
**Fichier**: `ReportsController.java`

```java
// MODIFIER exportReport():
private void exportReport() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Exporter le rapport");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"),
        new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
        new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"),
        new FileChooser.ExtensionFilter("Texte (*.txt)", "*.txt")
    );
    
    File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
    if (file != null) {
        try {
            if (file.getName().endsWith(".csv")) {
                exportCsvFile(file);
                setExportStatus("✅ CSV exporté: " + file.getName(), true);
            } else if (file.getName().endsWith(".xlsx")) {
                exportExcelXlsx(file);
                setExportStatus("✅ Excel exporté: " + file.getName(), true);
            } else if (file.getName().endsWith(".pdf")) {
                exportPdf(file);
                setExportStatus("✅ PDF exporté: " + file.getName(), true);
            } else if (file.getName().endsWith(".txt")) {
                exportReportFile(file);
                setExportStatus("✅ Texte exporté: " + file.getName(), true);
            } else {
                setExportStatus("❌ Format non reconnu: " + file.getName(), false);
            }
        } catch (IOException e) {
            logger.error("Erreur export", e);
            setExportStatus("❌ Erreur: " + e.getMessage(), false);  // ← UI FEEDBACK!
            showAlert("Erreur Export", "Impossible d'exporter: " + e.getMessage());
        }
    }
}

// S'assurer que setExportStatus() affiche le message correctement:
private void setExportStatus(String message, boolean success) {
    if (exportStatusLabel != null) {
        exportStatusLabel.setText(message);
        exportStatusLabel.setStyle(success 
            ? "-fx-text-fill: #059669; -fx-font-weight: bold;"
            : "-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        exportStatusLabel.setVisible(true);
        
        // Auto-hide après 5 secondes
        javafx.application.Platform.runLater(() -> {
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> 
                        exportStatusLabel.setVisible(false));
                }
            }, 5000);
        });
    }
}
```

**Vérification**: Après export, message "✅ Excel exporté: rapport.xlsx" affiché 5 secondes.

---

## ✨ AMÉLIORATIONS RAPIDES (Bonus)

### AMÉLIORATION 1: Afficher "Dernière Actualisation"

**Fichier**: `ReportsController.java`

```java
// AJOUTER dans generateReport() après succès:
private void updateLastRefresh() {
    LocalDateTime now = LocalDateTime.now();
    String formatted = now.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    if (lastRefreshLabel != null) {
        lastRefreshLabel.setText("Dernière actualisation: " + formatted);
    }
}

// APPELER après Platform.runLater() dans generateRealTimeDashboard():
javafx.application.Platform.runLater(() -> {
    // ... mise à jour graphiques ...
    updateLastRefresh();  // ← AJOUTER
});
```

### AMÉLIORATION 2: Couleurs Alertes par Sévérité

**Fichier**: `admin_dashboard.fxml` ou CSS

```xml
<!-- Dans admin_dashboard.fxml, modifier colAlertSeverity: -->
<TableColumn fx:id="colAlertSeverity" text="Niveau" prefWidth="110">
    <cellValueFactory>
        <PropertyValueFactory property="severity"/>
    </cellValueFactory>
    <cellFactory>
        <!-- Ajouter cellFactory pour coloriser -->
        <TableCell>
            <text>${item}</text>
            <!-- CSS classe dynamique selon severity -->
        </TableCell>
    </cellFactory>
</TableColumn>
```

**Fichier**: `design-system.css`

```css
/* Ajouter: */
.alert-critical { -fx-text-fill: #dc2626; -fx-font-weight: bold; }
.alert-warning { -fx-text-fill: #f59e0b; -fx-font-weight: bold; }
.alert-info { -fx-text-fill: #3b82f6; -fx-font-weight: bold; }
```

### AMÉLIORATION 3: Pagination Table Top 10

**Fichier**: `ReportsController.java`

```java
// AVANT:
private void updateTopPriorities() {
    // ...
    .limit(10)  // ← Dur-codé
    .toList()
}

// APRÈS: Ajouter pagination
private int prioritiesPageIndex = 0;
private final int PRIORITIES_PER_PAGE = 10;

private void updateTopPriorities() {
    List<Signalement> all = signalementService.getAllSignalements();
    // ... filtres ...
    List<PriorityRow> allRows = all.stream()
        // ... filtres et tri ...
        .toList();  // ← PAS de .limit(10)!
    
    // Pagination:
    int totalPages = (int) Math.ceil(allRows.size() / (double) PRIORITIES_PER_PAGE);
    int fromIndex = prioritiesPageIndex * PRIORITIES_PER_PAGE;
    int toIndex = Math.min(fromIndex + PRIORITIES_PER_PAGE, allRows.size());
    
    List<PriorityRow> pageRows = allRows.subList(fromIndex, toIndex);
    prioritiesTable.setItems(FXCollections.observableArrayList(pageRows));
    
    // Afficher: "Priorités 1-10 sur 47"
    if (prioritiesCountLabel != null) {
        prioritiesCountLabel.setText(
            String.format("Priorités %d-%d sur %d", 
                fromIndex+1, toIndex, allRows.size())
        );
    }
    
    // Boutons pagination:
    if (btnPriorityPrevious != null) {
        btnPriorityPrevious.setDisable(prioritiesPageIndex == 0);
    }
    if (btnPriorityNext != null) {
        btnPriorityNext.setDisable(toIndex >= allRows.size());
    }
}

// Ajouter méthodes:
@FXML private void handlePriorityPagePrevious() {
    if (prioritiesPageIndex > 0) {
        prioritiesPageIndex--;
        updateTopPriorities();
    }
}

@FXML private void handlePriorityPageNext() {
    prioritiesPageIndex++;
    updateTopPriorities();
}
```

---

## 📋 CHECKLIST IMPLÉMENTATION

### FIX 1: activeAlerts ObservableList
- [ ] Importer `FXCollections` et `ObservableList`
- [ ] Changer type de `activeAlerts` en `ObservableList<AlertRow>`
- [ ] Initialiser dans `initialize()`
- [ ] Ajouter `tableAlerts.setItems(activeAlerts)`
- [ ] Tester: table doit afficher alertes

### FIX 2: Null-Check AnalysisSnapshot
- [ ] Entourer `computeAnalysisSnapshot()` d'un try-catch
- [ ] Créer snapshot vide si null
- [ ] Tester: pas de crash même si service échoue

### FIX 3: Arrêter Timer
- [ ] Modifier `showPage()` pour appeler `stopLiveStatistiquesAutoRefresh()`
- [ ] S'assurer que `stopLiveStatistiquesAutoRefresh()` existe
- [ ] Tester: naviguer, vérifier pas de fuite mémoire

### FIX 4: Feedback Export
- [ ] Modifier `exportReport()` pour appeler `setExportStatus()` après succès
- [ ] Ajouter `showAlert()` en cas d'erreur
- [ ] Tester: message visible après export

### AMÉLIORATION 1: Dernière Actualisation
- [ ] Ajouter `updateLastRefresh()` méthode
- [ ] Appeler après graphiques mis à jour
- [ ] Tester: label en bas montre heure

### AMÉLIORATION 2: Couleurs Alertes
- [ ] Ajouter classes CSS `.alert-critical`, `.alert-warning`, `.alert-info`
- [ ] Implémenter cellFactory dans FXML
- [ ] Tester: alertes colorées selon sévérité

### AMÉLIORATION 3: Pagination Top 10
- [ ] Ajouter variables `prioritiesPageIndex`, `PRIORITIES_PER_PAGE`
- [ ] Modifier `updateTopPriorities()` pour paginer
- [ ] Ajouter boutons Précédent/Suivant en FXML
- [ ] Implémenter `handlePriorityPagePrevious()`, `handlePriorityPageNext()`
- [ ] Tester: voir plus de 10 priorités

---

## 🧪 TESTS AVANT/APRÈS

### Test 1: Alertes S'Affichent
```
AVANT: Table vide
APRÈS: 1-5 alertes affichées
Test: Lancer app → Stats → Vérifier table alertes
```

### Test 2: Pas de Crash sur Snapshot Null
```
AVANT: Crash si service échoue
APRÈS: Valeurs par défaut (0, 0%, etc.)
Test: Arrêter BD → Stats → Vérifier pas de crash
```

### Test 3: Timer Arrêté
```
AVANT: Fuite mémoire
APRÈS: Timer cancelled quand quitter page
Test: Vérifier logs "Live statistiques auto-refresh arrêté"
```

### Test 4: Message Export Visible
```
AVANT: Silence après export
APRÈS: "✅ Excel exporté: rapport.xlsx" affiché 5s
Test: Exporter → Vérifier message
```

---

## ⏱️ TEMPS ESTIMÉ

| Fix/Amélioration | Temps | Difficulté |
|------------------|-------|------------|
| FIX 1: activeAlerts | 20 min | 🟢 Facile |
| FIX 2: Null-Check | 30 min | 🟢 Facile |
| FIX 3: Timer Stop | 30 min | 🟢 Facile |
| FIX 4: Export Feedback | 40 min | 🟠 Moyen |
| AMÉ 1: Dernière Actualisation | 15 min | 🟢 Facile |
| AMÉ 2: Couleurs Alertes | 30 min | 🟠 Moyen |
| AMÉ 3: Pagination | 60 min | 🟠 Moyen |
| **TOTAL** | **225 min = 3h45** | |

---

## 📌 COMMIT MESSAGES

```
fix: initialiser activeAlerts ObservableList dans AdminDashboardController

Fixes tableau alertes vide. activeAlerts était jamais initialisé,
causant NPE quand setAll() appelé.

fix: ajouter null-check sur AnalysisSnapshot

Prévient crash si computeAnalysisSnapshot() retourne null.

fix: arrêter auto-refresh timer quand quitter page statistiques

Évite fuite mémoire et CPU inutile.

feat: ajouter feedback UI sur export rapport

Affiche message success/error après export. Timeout 5s.

feat: afficher dernière actualisation timestamp

Label en bas montre "Dernière actualisation: 27/04 14:32:15".

feat: coloriser alertes par sévérité

Alertes CRITICAL en rouge, WARNING en orange, INFO en bleu.

feat: pagination table Top 10 priorités

Afficher > 10 priorités avec boutons Précédent/Suivant.
```

---

**Fin du plan de fixes**
