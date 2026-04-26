package com.smartcity.controller;

import com.smartcity.app.MainApp;
import com.smartcity.service.ReportService;
import com.smartcity.service.SignalementService;
import com.smartcity.service.ZoneService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controleur pour les rapports et analytics.
 */
public class ReportsController {

    @FXML private BorderPane rootPane;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Button generateReportButton;
    @FXML private Button exportReportButton;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> zoneBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private CategoryAxis zoneXAxis;
    @FXML private NumberAxis zoneYAxis;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;
    @FXML private TableView<StatisticRow> statsTable;
    @FXML private TableColumn<StatisticRow, String> metricColumn;
    @FXML private TableColumn<StatisticRow, String> valueColumn;
    @FXML private TextArea reportTextArea;
    // KPI labels (FXML)
    @FXML private Label kpiTotal;
    @FXML private Label kpiTotalDelta;
    @FXML private Label kpiTotalSub;
    @FXML private Label kpiTaux;
    @FXML private Label kpiTauxDelta;
    @FXML private Label kpiTauxSub;
    @FXML private Label kpiDelai;
    @FXML private Label kpiDelaiDelta;
    @FXML private Label kpiDelaiSub;
    @FXML private Label kpiUrgents;
    @FXML private Label kpiUrgentsStatus;
    @FXML private Label kpiZone;
    @FXML private Label kpiZoneSub;
    @FXML private Label summaryAttente;
    @FXML private Label summaryEnCours;
    @FXML private Label summaryTermine;
    @FXML private Label summaryTauxLabel;
    @FXML private ProgressBar summaryProgressBar;
    @FXML private VBox recoContainer;
    @FXML private Label exportStatusLabel;
    @FXML private Label lastRefreshLabel;

    private final ReportService reportService = new ReportService();
    private final SignalementService signalementService = new SignalementService();
    private final ZoneService zoneService = new ZoneService();

    private static final String SEPARATEUR  = "=".repeat(60);
    private static final String SEPARATEUR2 = "-".repeat(60);
    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final java.util.Locale FR = java.util.Locale.FRENCH;

    private String entete(String titre, String sousTitre) {
        return SEPARATEUR + "\n"
            + "  SMARTCITY \u2014 GESTION DES D\u00c9CHETS M\u00c9NAGERS\n"
            + "  Pikine & Gu\u00e9diawaye, S\u00e9n\u00e9gal\n"
            + SEPARATEUR + "\n"
            + "  " + titre + "\n"
            + "  " + sousTitre + "\n"
            + "  G\u00e9n\u00e9r\u00e9 le : " + java.time.LocalDateTime.now().format(FMT_HEURE) + "\n"
            + SEPARATEUR + "\n\n";
    }

    private String kpi(String label, String valeur, String tendance) {
        String t = (tendance != null && !tendance.isBlank()) ? "  [" + tendance + "]" : "";
        return String.format("  %-38s %s%s%n", label + " :", valeur, t);
    }

    private String barre(int valeur, int total, int largeur) {
        if (total == 0) return "[" + " ".repeat(largeur) + "] 0%";
        int rempli = (int) Math.round((double) valeur / total * largeur);
        return "[" + "\u2588".repeat(rempli) + "\u2591".repeat(largeur - rempli) + "] "
            + String.format("%.1f%%", (double) valeur / total * 100);
    }

    private MainApp mainApp;
    private ReportService.WeeklyReport currentWeeklyReport;
    private ReportService.MonthlyReport currentMonthlyReport;
    private String reportContent = "";

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleBackToAdmin() {
        if (mainApp != null) {
            mainApp.showDashboard("Administrateur");
        }
    }

    private DateRange resolveRange(int defaultDays) {
        LocalDate end = endDatePicker.getValue();
        if (end == null) {
            end = LocalDate.now();
            endDatePicker.setValue(end);
        }
        LocalDate start = startDatePicker.getValue();
        if (start == null) {
            start = end.minusDays(Math.max(defaultDays, 1) - 1L);
            startDatePicker.setValue(start);
        }
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
            startDatePicker.setValue(start);
            endDatePicker.setValue(end);
        }
        long days = ChronoUnit.DAYS.between(start, end) + 1L;
        return new DateRange(start, end, Math.max(1L, days));
    }

    private DateRange previousRange(DateRange current) {
        LocalDate prevEnd = current.start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(current.days - 1L);
        return new DateRange(prevStart, prevEnd, current.days);
    }

    private static final DateTimeFormatter FMT_CHART_DAY = DateTimeFormatter.ofPattern("dd/MM");

    private String formatChartDay(String isoDate) {
        try {
            return LocalDate.parse(isoDate).format(FMT_CHART_DAY);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private void renderTrendChart(Map<String, Integer> current, String currentLabel,
                                  Map<String, Integer> previous, String previousLabel) {
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName(currentLabel);
        current.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> currentSeries.getData().add(
                new XYChart.Data<>(formatChartDay(e.getKey()), e.getValue())));

        XYChart.Series<String, Number> prevSeries = new XYChart.Series<>();
        prevSeries.setName(previousLabel);
        previous.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> prevSeries.getData().add(
                new XYChart.Data<>(formatChartDay(e.getKey()), e.getValue())));

        trendLineChart.getData().clear();
        trendLineChart.setData(FXCollections.observableArrayList(currentSeries, prevSeries));
        // Couleurs fixes : serie courante = vert, precedente = gris
        javafx.application.Platform.runLater(() -> {
            if (currentSeries.getNode() != null)
                currentSeries.getNode().setStyle("-fx-stroke: #16a34a; -fx-stroke-width: 2.5px;");
            if (prevSeries.getNode() != null)
                prevSeries.getNode().setStyle("-fx-stroke: #94a3b8; -fx-stroke-width: 1.5px;");
        });
    }

    private void finalizeReportText(StringBuilder report) {
        report.append("\n").append(SEPARATEUR).append("\n");
        report.append("  Rapport genere automatiquement par SmartCity Analytics\n");
        report.append(SEPARATEUR).append("\n");
        reportContent = report.toString();
        if (reportTextArea != null) {
            reportTextArea.setText(reportContent);
        }
        updateLastRefresh();
    }

    @FXML
    private void initialize() {
        setupControls();
        setupTable();
        setExportStatus("", false);
        loadDefaultData();
    }

    private void setupControls() {
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(7));

        reportTypeCombo.setItems(FXCollections.observableArrayList(
            "Rapport Hebdomadaire",
            "Rapport Mensuel",
            "Dashboard Temps R\u00e9el",
            "Performance Agents",
            "Analyse G\u00e9ographique"
        ));
        reportTypeCombo.setValue("Dashboard Temps R\u00e9el");

        statusPieChart.setTitle("R\u00e9partition par Statut");
        zoneBarChart.setTitle("Signalements par Zone");
        trendLineChart.setTitle("Tendances");

        reportTypeCombo.setOnAction(e -> generateReport());
        startDatePicker.setOnAction(e -> generateReport());
        endDatePicker.setOnAction(e -> generateReport());
    }

    private void setupTable() {
        metricColumn.setCellValueFactory(cellData -> cellData.getValue().metricProperty());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        metricColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        valueColumn.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void generateReport() {
        String reportType = reportTypeCombo.getValue();
        if (reportType == null) return;
        final DateRange selectedRange = switch (reportType) {
            case "Rapport Hebdomadaire", "Performance Agents" -> resolveRange(7);
            case "Rapport Mensuel", "Analyse G\u00e9ographique" -> resolveRange(30);
            default -> null;
        };
        clearCharts();
        generateReportButton.setDisable(true);
        generateReportButton.setText("Chargement...");

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                // Toutes les requetes SQL s'executent ici, sur le thread background
                if ("Dashboard Temps R\u00e9el".equals(reportType)) {
                    generateRealTimeDashboard();
                } else if ("Rapport Hebdomadaire".equals(reportType)) {
                    generateWeeklyReport(selectedRange);
                } else if ("Rapport Mensuel".equals(reportType)) {
                    generateMonthlyReport(selectedRange);
                } else if ("Performance Agents".equals(reportType)) {
                    generateAgentPerformanceReport(selectedRange);
                } else if ("Analyse G\u00e9ographique".equals(reportType)) {
                    generateGeographicAnalysis(selectedRange);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            generateReportButton.setDisable(false);
            generateReportButton.setText("Generer");
        });
        task.setOnFailed(e -> {
            generateReportButton.setDisable(false);
            generateReportButton.setText("Generer");
            showAlert("Erreur", "Erreur lors de la generation du rapport.");
        });
        Thread t = new Thread(task, "report-gen");
        t.setDaemon(true);
        t.start();
    }

    private void generateRealTimeDashboard() {
        // Toutes les requetes SQL ici (thread background)
        final ReportService.RealTimeDashboard dashboard = reportService.getRealTimeDashboard();
        final int nbAttente = signalementService.countByStatut("En attente");
        final int nbCours   = signalementService.countByStatut("En cours");
        final int nbTermine = signalementService.countByStatut("Termin\u00e9");
        final int nbAffecte = signalementService.countByStatut("Affect\u00e9");
        final int total0    = dashboard.totalSignalements;
        final int tauxRes0  = total0 > 0 ? (int) Math.round(nbTermine * 100.0 / total0) : 0;
        final double delaiH0 = signalementService.getTempsResolutionMoyenH();
        final String tendHebdo0 = dashboard.tendances.containsKey("hebdomadaire")
            ? String.format("%+.1f%% vs sem. pr\u00e9c.", dashboard.tendances.get("hebdomadaire")) : "";

        // Mise a jour UI sur le thread JavaFX
        javafx.application.Platform.runLater(() -> {
        final int nbAttente_ = nbAttente, nbCours_ = nbCours, nbTermine_ = nbTermine, nbAffecte_ = nbAffecte;
        final int total = total0, tauxRes = tauxRes0;
        final double delaiH = delaiH0;
        final String tendHebdo = tendHebdo0;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (nbAttente_ > 0) pieData.add(new PieChart.Data("En attente (" + nbAttente_ + ")", nbAttente_));
        if (nbCours_   > 0) pieData.add(new PieChart.Data("En cours ("   + nbCours_   + ")", nbCours_));
        if (nbTermine_ > 0) pieData.add(new PieChart.Data("Termin\u00e9 ("  + nbTermine_ + ")", nbTermine_));
        if (nbAffecte_ > 0) pieData.add(new PieChart.Data("Affect\u00e9 ("  + nbAffecte_ + ")", nbAffecte_));
        statusPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Signalements (7 derniers jours)");
        dashboard.zonesActives.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> zoneSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        zoneBarChart.getData().clear();
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries));

        int nbUrgents = dashboard.signalementsUrgents.size();

        // ── KPI cards ──────────────────────────────────────────────
        if (kpiTotal != null) kpiTotal.setText(String.valueOf(total));
        if (kpiTotalDelta != null) {
            kpiTotalDelta.setText(tendHebdo.isBlank() ? "" : tendHebdo);
            kpiTotalDelta.getStyleClass().removeAll(
                "rpt-kpi-delta-up", "rpt-kpi-delta-down", "rpt-kpi-delta-neutral");
            kpiTotalDelta.getStyleClass().add(
                tendHebdo.startsWith("+") ? "rpt-kpi-delta-down" :
                tendHebdo.startsWith("-") ? "rpt-kpi-delta-up" : "rpt-kpi-delta-neutral");
        }
        if (kpiTaux != null) kpiTaux.setText(tauxRes + "%");
        if (kpiTauxDelta != null) {
            kpiTauxDelta.getStyleClass().removeAll(
                "rpt-kpi-delta-up", "rpt-kpi-delta-down", "rpt-kpi-delta-neutral", "rpt-kpi-delta-warn");
            if (tauxRes >= 70) {
                kpiTauxDelta.setText("Objectif atteint");
                kpiTauxDelta.getStyleClass().add("rpt-kpi-delta-up");
            } else if (tauxRes >= 40) {
                kpiTauxDelta.setText("A ameliorer");
                kpiTauxDelta.getStyleClass().add("rpt-kpi-delta-warn");
            } else {
                kpiTauxDelta.setText("Insuffisant");
                kpiTauxDelta.getStyleClass().add("rpt-kpi-delta-down");
            }
        }
        if (kpiDelai != null)
            kpiDelai.setText(delaiH > 0 ? String.format(java.util.Locale.US, "%.1fh", delaiH) : "N/A");
        if (kpiDelaiDelta != null) {
            kpiDelaiDelta.getStyleClass().removeAll(
                "rpt-kpi-delta-up", "rpt-kpi-delta-down", "rpt-kpi-delta-neutral", "rpt-kpi-delta-warn");
            if (delaiH > 0 && delaiH <= 24) {
                kpiDelaiDelta.setText("Bon");
                kpiDelaiDelta.getStyleClass().add("rpt-kpi-delta-up");
            } else if (delaiH > 48) {
                kpiDelaiDelta.setText("Critique");
                kpiDelaiDelta.getStyleClass().add("rpt-kpi-delta-down");
            } else {
                kpiDelaiDelta.setText(delaiH > 0 ? "Acceptable" : "");
                kpiDelaiDelta.getStyleClass().add("rpt-kpi-delta-warn");
            }
        }
        if (kpiUrgents != null) kpiUrgents.setText(String.valueOf(nbUrgents));
        if (kpiUrgentsStatus != null) {
            kpiUrgentsStatus.getStyleClass().removeAll(
                "rpt-kpi-delta-up", "rpt-kpi-delta-down", "rpt-kpi-delta-neutral");
            kpiUrgentsStatus.setText(nbUrgents == 0 ? "Aucun" : "Action requise");
            kpiUrgentsStatus.getStyleClass().add(
                nbUrgents == 0 ? "rpt-kpi-delta-up" : "rpt-kpi-delta-down");
        }
        if (kpiZone != null && !dashboard.zonesActives.isEmpty()) {
            String topZone = dashboard.zonesActives.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
            kpiZone.setText(topZone);
            int topCount = dashboard.zonesActives.getOrDefault(topZone, 0);
            if (kpiZoneSub != null)
                kpiZoneSub.setText(topCount + " signalement" + (topCount > 1 ? "s" : "") + " (7j)");
        }

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Total signalements",    String.valueOf(total)));
        stats.add(new StatisticRow("Aujourd'hui",           String.valueOf(dashboard.signalementsAujourdhui)));
        stats.add(new StatisticRow("En attente",            String.valueOf(nbAttente_)));
        stats.add(new StatisticRow("En cours",              String.valueOf(nbCours_)));
        stats.add(new StatisticRow("Termin\u00e9s",          String.valueOf(nbTermine_)));
        stats.add(new StatisticRow("Taux de r\u00e9solution", tauxRes + "%"));
        stats.add(new StatisticRow("Urgents (>24h)",        String.valueOf(nbUrgents)));
        stats.add(new StatisticRow("Utilisateurs actifs",   String.valueOf(dashboard.utilisateursActifs)));
        if (!tendHebdo.isBlank()) stats.add(new StatisticRow("Tendance hebdo", tendHebdo));
        statsTable.setItems(stats);

        StringBuilder r = new StringBuilder();
        r.append(entete("TABLEAU DE BORD EN TEMPS R\u00c9EL",
                        "Situation instantan\u00e9e de la plateforme"));

        r.append("1. INDICATEURS CL\u00c9S\n").append(SEPARATEUR2).append("\n");
        r.append(kpi("Total signalements",    String.valueOf(total), tendHebdo));
        r.append(kpi("Signalements aujourd'hui", String.valueOf(dashboard.signalementsAujourdhui), ""));
        r.append(kpi("Taux de r\u00e9solution global", tauxRes + "%",
                     tauxRes >= 70 ? "\u2705 BON" : tauxRes >= 40 ? "\u26a0\ufe0f MOYEN" : "\u274c FAIBLE"));
        r.append(kpi("Signalements urgents (>24h)", String.valueOf(dashboard.signalementsUrgents.size()),
                     dashboard.signalementsUrgents.isEmpty() ? "\u2705 Aucun" : "\u26a0\ufe0f Action requise"));
        r.append(kpi("Utilisateurs actifs", String.valueOf(dashboard.utilisateursActifs), ""));
        r.append("\n");

        r.append("2. R\u00c9PARTITION DES STATUTS\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  En attente  %s%n", barre(nbAttente_, total, 30)));
        r.append(String.format("  En cours    %s%n", barre(nbCours_,   total, 30)));
        r.append(String.format("  Affect\u00e9    %s%n", barre(nbAffecte_, total, 30)));
        r.append(String.format("  Termin\u00e9    %s%n", barre(nbTermine_, total, 30)));
        r.append("\n");

        r.append("3. ACTIVIT\u00c9 PAR ZONE (7 derniers jours)\n").append(SEPARATEUR2).append("\n");
        int totalZones = dashboard.zonesActives.values().stream().mapToInt(Integer::intValue).sum();
        dashboard.zonesActives.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> r.append(String.format("  %-20s %s%n",
                e.getKey(), barre(e.getValue(), Math.max(totalZones, 1), 25) + "  (" + e.getValue() + " sign.)")));
        r.append("\n");

        if (!dashboard.signalementsUrgents.isEmpty()) {
            r.append("4. SIGNALEMENTS URGENTS \u2014 ACTION IMM\u00c9DIATE REQUISE\n").append(SEPARATEUR2).append("\n");
            dashboard.signalementsUrgents.forEach(s -> r.append(String.format(
                "  [#%d] %-12s | Zone: %-15s | Depuis: %s%n",
                s.getIdSignalement(), s.getCategorie(), s.getZoneNom(),
                s.getDateSignalement() != null ? s.getDateSignalement().format(FMT_DATE) : "N/A")));
            r.append("\n");
        }

        r.append("5. RECOMMANDATIONS\n").append(SEPARATEUR2).append("\n");
        List<String> recommandations = new ArrayList<>();
        if (!dashboard.signalementsUrgents.isEmpty())
            recommandations.add("Traiter en priorite les " + dashboard.signalementsUrgents.size() + " signalement(s) en attente depuis plus de 24h.");
        if (tauxRes < 50)
            recommandations.add("Taux de resolution inferieur a 50% : renforcer les equipes de collecte.");
        if (nbAttente > nbCours * 2)
            recommandations.add("File d'attente elevee : envisager des affectations supplementaires.");
        if (tauxRes >= 70 && dashboard.signalementsUrgents.isEmpty())
            recommandations.add("Situation satisfaisante. Maintenir le rythme actuel.");
        for (String reco : recommandations) {
            r.append("  ").append(reco).append('\n');
        }

        updateOperationalSummary(nbAttente_, nbCours_, nbTermine_);
        updateRecommendations(recommandations);
        finalizeReportText(r);
        }); // fin Platform.runLater
    }

    private void generateWeeklyReport(DateRange range) {
        ReportService.WeeklyReport weekly = reportService.generateWeeklyReport(range.start, range.end);
        currentWeeklyReport = weekly;

        DateRange prevRange = previousRange(range);
        ReportService.WeeklyReport prevReport = reportService.generateWeeklyReport(prevRange.start, prevRange.end);
        ReportService.MonthlyReport currentEvolution = reportService.generateMonthlyReport(range.start, range.end);
        ReportService.MonthlyReport prevEvolution = reportService.generateMonthlyReport(prevRange.start, prevRange.end);

        int total = weekly.totalSignalements;
        int prevTotal = prevReport.totalSignalements;
        double tauxRes = total > 0 ? (double) weekly.signalementsResolus / total * 100 : 0;
        double prevTauxRes = prevTotal > 0 ? (double) prevReport.signalementsResolus / prevTotal * 100 : 0;
        String evoTotal = prevTotal > 0 ? String.format("%+d (%.0f%%)", total - prevTotal, (double) (total - prevTotal) / prevTotal * 100) : "N/A";
        String evoTaux = String.format("%+.1f pts", tauxRes - prevTauxRes);

        List<String> recommandations = new ArrayList<>();
        if (total > prevTotal * 1.2) recommandations.add("Hausse significative des signalements (+20%). Renforcer les effectifs.");
        if (tauxRes < prevTauxRes - 5) recommandations.add("Baisse du taux de resolution. Identifier les blocages operationnels.");
        if (weekly.signalementsEnAttente > weekly.signalementsEnCours) recommandations.add("Plus de signalements en attente qu'en cours. Accelerer les affectations.");
        if (!weekly.agentsActifs.isEmpty() && weekly.agentsActifs.get(0).tauxReussite >= 80) {
            recommandations.add("Agent " + weekly.agentsActifs.get(0).nom + " : performance exemplaire.");
        }

        javafx.application.Platform.runLater(() -> {
            renderTrendChart(currentEvolution.evolutionQuotidienne, "Periode selectionnee",
                prevEvolution.evolutionQuotidienne, "Periode precedente");

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            if (weekly.signalementsEnAttente > 0)
                pieData.add(new PieChart.Data("En attente (" + weekly.signalementsEnAttente + ")", weekly.signalementsEnAttente));
            if (weekly.signalementsEnCours > 0)
                pieData.add(new PieChart.Data("En cours (" + weekly.signalementsEnCours + ")", weekly.signalementsEnCours));
            if (weekly.signalementsResolus > 0)
                pieData.add(new PieChart.Data("Resolus (" + weekly.signalementsResolus + ")", weekly.signalementsResolus));
            statusPieChart.setData(pieData);

            XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
            zoneSeries.setName("Periode selectionnee");
            XYChart.Series<String, Number> prevZoneSeries = new XYChart.Series<>();
            prevZoneSeries.setName("Periode precedente");
            weekly.performanceParZone.forEach((z, v) -> zoneSeries.getData().add(new XYChart.Data<>(z, v)));
            prevReport.performanceParZone.forEach((z, v) -> prevZoneSeries.getData().add(new XYChart.Data<>(z, v)));
            zoneBarChart.setData(FXCollections.observableArrayList(zoneSeries, prevZoneSeries));

            ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
            stats.add(new StatisticRow("Periode", range.start.format(FMT_DATE) + " -> " + range.end.format(FMT_DATE)));
            stats.add(new StatisticRow("Total signalements", total + "  (" + evoTotal + ")"));
            stats.add(new StatisticRow("Resolus", weekly.signalementsResolus + " / " + total));
            stats.add(new StatisticRow("En cours", String.valueOf(weekly.signalementsEnCours)));
            stats.add(new StatisticRow("En attente", String.valueOf(weekly.signalementsEnAttente)));
            stats.add(new StatisticRow("Taux de resolution", String.format("%.1f%%  (%s)", tauxRes, evoTaux)));
            statsTable.setItems(stats);
            updateOperationalSummary(weekly.signalementsEnAttente, weekly.signalementsEnCours, weekly.signalementsResolus);
            updateRecommendations(recommandations);

            StringBuilder r = new StringBuilder();
            r.append(entete("RAPPORT HEBDOMADAIRE", "Periode : " + range.start.format(FMT_DATE) + " au " + range.end.format(FMT_DATE)));

            r.append("1. RESUME EXECUTIF\n").append(SEPARATEUR2).append("\n");
            r.append(kpi("Total signalements recus", String.valueOf(total), evoTotal));
            r.append(kpi("Signalements resolus", weekly.signalementsResolus + " / " + total, ""));
            r.append(kpi("Taux de resolution", String.format("%.1f%%", tauxRes),
                tauxRes >= 70 ? "BON" : tauxRes >= 40 ? "MOYEN" : "FAIBLE"));
            r.append(kpi("Evolution vs periode precedente", evoTotal, ""));
            r.append(kpi("Evolution taux resolution", evoTaux,
                tauxRes > prevTauxRes ? "Progression" : tauxRes < prevTauxRes ? "Regression" : "Stable"));
            r.append("\n");

            r.append("2. PERFORMANCE PAR ZONE\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-20s %8s %8s %8s%n", "Zone", "Actuel", "Prec.", "Evolution"));
            r.append("  " + "-".repeat(48) + "\n");
            weekly.performanceParZone.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    int prev = prevReport.performanceParZone.getOrDefault(e.getKey(), 0);
                    String evo = prev > 0 ? String.format("%+d", e.getValue() - prev) : "N/A";
                    r.append(String.format("  %-20s %8d %8d %8s%n", e.getKey(), e.getValue(), prev, evo));
                });
            r.append("\n");

            r.append("3. TOP CATEGORIES\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-20s %8s %8s%n", "Categorie", "Nb", "Part"));
            r.append("  " + "-".repeat(40) + "\n");
            weekly.categoriesPopulaires.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> r.append(String.format("  %-20s %8d %8s%n",
                    e.getKey(), e.getValue(),
                    total > 0 ? String.format("%.1f%%", e.getValue() * 100.0 / total) : "N/A")));
            r.append("\n");

            r.append("4. RECOMMANDATIONS\n").append(SEPARATEUR2).append("\n");
            for (String reco : recommandations) {
                r.append("  ").append(reco).append('\n');
            }
            if (recommandations.isEmpty()) {
                r.append("  Aucune alerte majeure sur la periode.\n");
            }

            finalizeReportText(r);
        });
    }
    private void generateMonthlyReport(DateRange range) {
        ReportService.MonthlyReport monthly = reportService.generateMonthlyReport(range.start, range.end);
        currentMonthlyReport = monthly;

        DateRange prevRange = previousRange(range);
        ReportService.MonthlyReport prevReport = reportService.generateMonthlyReport(prevRange.start, prevRange.end);
        int total = monthly.totalSignalements;
        int prevTotal = prevReport.totalSignalements;
        double tauxRes = monthly.tauxResolution;
        double prevTauxRes = prevReport.tauxResolution;
        String evoTotal = prevTotal > 0 ? String.format("%+d (%.0f%%)", total - prevTotal, (double) (total - prevTotal) / prevTotal * 100) : "N/A";
        String evoTaux = String.format("%+.1f pts", tauxRes - prevTauxRes);

        List<String> recommandations = new ArrayList<>();
        if (total > prevTotal * 1.15) recommandations.add("Hausse de +15% des signalements vs periode precedente. Anticiper les ressources.");
        if (tauxRes < prevTauxRes - 5) recommandations.add("Baisse du taux de resolution. Analyser les causes.");
        if (monthly.tempsTraitementMoyen > 48) recommandations.add("Temps de traitement moyen > 48h. Optimiser le processus d'affectation.");
        if (tauxRes >= 75) recommandations.add("Taux de resolution eleve : objectif atteint.");

        javafx.application.Platform.runLater(() -> {
            renderTrendChart(monthly.evolutionQuotidienne, "Periode selectionnee",
                prevReport.evolutionQuotidienne, "Periode precedente");

            ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
            stats.add(new StatisticRow("Periode analysee", range.start.format(FMT_DATE) + " -> " + range.end.format(FMT_DATE)));
            stats.add(new StatisticRow("Total signalements", total + "  (" + evoTotal + ")"));
            stats.add(new StatisticRow("Taux de resolution", String.format("%.1f%%  (%s)", tauxRes, evoTaux)));
            stats.add(new StatisticRow("Temps traitement moyen", String.format("%.1f h", monthly.tempsTraitementMoyen)));
            stats.add(new StatisticRow("Zone la plus active", monthly.zoneLaPlusActive));
            statsTable.setItems(stats);
            updateOperationalSummary(Math.max(0, total - (int) Math.round(monthly.tauxResolution * total / 100.0)), 0,
                (int) Math.round(monthly.tauxResolution * total / 100.0));
            updateRecommendations(recommandations);

            StringBuilder r = new StringBuilder();
            r.append(entete("RAPPORT DE PERIODE", "Analyse comparee avec la periode precedente de meme duree"));

            r.append("1. INDICATEURS CLES\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-38s %12s %12s %10s%n", "Indicateur", "Actuel", "Prec.", "Evolution"));
            r.append("  " + "-".repeat(74) + "\n");
            r.append(String.format("  %-38s %12d %12d %10s%n", "Total signalements", total, prevTotal, evoTotal));
            r.append(String.format("  %-38s %11.1f%% %11.1f%% %10s%n", "Taux de resolution", tauxRes, prevTauxRes, evoTaux));
            r.append(String.format("  %-38s %11.1fh %11.1fh%n", "Temps traitement moyen",
                monthly.tempsTraitementMoyen, prevReport.tempsTraitementMoyen));
            r.append("\n");

            r.append("2. ANALYSE PAR ZONE\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-20s %8s %8s %8s%n", "Zone", "Actuel", "Prec.", "Evol."));
            r.append("  " + "-".repeat(48) + "\n");
            monthly.performanceParZone.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    int prev = prevReport.performanceParZone.getOrDefault(e.getKey(), 0);
                    r.append(String.format("  %-20s %8d %8d %8s%n",
                        e.getKey(), e.getValue(), prev,
                        prev > 0 ? String.format("%+d", e.getValue() - prev) : "N/A"));
                });
            r.append("\n");

            r.append("3. EVOLUTION QUOTIDIENNE (TOP 5)\n").append(SEPARATEUR2).append("\n");
            monthly.evolutionQuotidienne.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> r.append(String.format("  %s : %s%n",
                    e.getKey(), barre(e.getValue(),
                        monthly.evolutionQuotidienne.values().stream().mapToInt(Integer::intValue).max().orElse(1), 30)
                        + "  " + e.getValue() + " sign.")));
            r.append("\n");

            r.append("4. RECOMMANDATIONS\n").append(SEPARATEUR2).append("\n");
            for (String reco : recommandations) {
                r.append("  ").append(reco).append('\n');
            }
            if (recommandations.isEmpty()) {
                r.append("  Aucune alerte majeure sur la periode.\n");
            }

            finalizeReportText(r);
        });
    }
    private void generateAgentPerformanceReport(DateRange range) {
        ReportService.WeeklyReport weeklyReport = reportService.generateWeeklyReport(range.start, range.end);

        DateRange prevRange = previousRange(range);
        ReportService.WeeklyReport prevReport = reportService.generateWeeklyReport(prevRange.start, prevRange.end);
        ReportService.MonthlyReport currentEvolution = reportService.generateMonthlyReport(range.start, range.end);
        ReportService.MonthlyReport prevEvolution = reportService.generateMonthlyReport(prevRange.start, prevRange.end);

        double tauxMoyen = weeklyReport.agentsActifs.stream().mapToDouble(a -> a.tauxReussite).average().orElse(0);
        double prevTauxMoy = prevReport.agentsActifs.stream().mapToDouble(a -> a.tauxReussite).average().orElse(0);
        int totalMissions = weeklyReport.agentsActifs.stream().mapToInt(a -> a.missionsTotal).sum();

        List<String> recommandations = new ArrayList<>();
        weeklyReport.agentsActifs.stream()
            .filter(a -> a.tauxReussite < 40 && a.missionsTotal >= 3)
            .forEach(a -> recommandations.add(a.nom + " : taux insuffisant (" + String.format("%.0f%%", a.tauxReussite) + "). Accompagnement recommande."));
        weeklyReport.agentsActifs.stream()
            .filter(a -> a.tauxReussite >= 90)
            .forEach(a -> recommandations.add(a.nom + " : performance exceptionnelle (" + String.format("%.0f%%", a.tauxReussite) + "). A valoriser."));
        if (!weeklyReport.agentsActifs.isEmpty() && weeklyReport.agentsActifs.stream().allMatch(a -> a.tauxReussite >= 60)) {
            recommandations.add("Toute l'equipe est au-dessus de 60%. Bonne dynamique collective.");
        }

        javafx.application.Platform.runLater(() -> {
            renderTrendChart(currentEvolution.evolutionQuotidienne, "Periode selectionnee",
                prevEvolution.evolutionQuotidienne, "Periode precedente");

            XYChart.Series<String, Number> seriesMissions = new XYChart.Series<>();
            XYChart.Series<String, Number> seriesTerminees = new XYChart.Series<>();
            seriesMissions.setName("Missions assignees");
            seriesTerminees.setName("Missions terminees");
            weeklyReport.agentsActifs.stream().limit(8).forEach(a -> {
                String nom = a.nom.length() > 12 ? a.nom.substring(0, 12) + "." : a.nom;
                seriesMissions.getData().add(new XYChart.Data<>(nom, a.missionsTotal));
                seriesTerminees.getData().add(new XYChart.Data<>(nom, a.missionsTerminees));
            });
            zoneBarChart.setData(FXCollections.observableArrayList(seriesMissions, seriesTerminees));

            ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
            stats.add(new StatisticRow("Periode", range.start.format(FMT_DATE) + " -> " + range.end.format(FMT_DATE)));
            stats.add(new StatisticRow("Agents actifs", String.valueOf(weeklyReport.agentsActifs.size())));
            stats.add(new StatisticRow("Total missions", String.valueOf(totalMissions)));
            stats.add(new StatisticRow("Taux moyen", String.format("%.1f%%  (%+.1f pts)", tauxMoyen, tauxMoyen - prevTauxMoy)));
            if (!weeklyReport.agentsActifs.isEmpty()) {
                ReportService.AgentStats top = weeklyReport.agentsActifs.get(0);
                stats.add(new StatisticRow("Meilleur agent", top.nom + " " + String.format("(%.0f%%)", top.tauxReussite)));
            }
            statsTable.setItems(stats);
            int terminees = weeklyReport.agentsActifs.stream().mapToInt(a -> a.missionsTerminees).sum();
            int assignees = weeklyReport.agentsActifs.stream().mapToInt(a -> a.missionsTotal).sum();
            updateOperationalSummary(Math.max(assignees - terminees, 0), 0, terminees);
            updateRecommendations(recommandations);

            StringBuilder r = new StringBuilder();
            r.append(entete("RAPPORT PERFORMANCE DES AGENTS",
                "Periode : " + range.start.format(FMT_DATE) + " au " + range.end.format(FMT_DATE)));

            r.append("1. SYNTHESE\n").append(SEPARATEUR2).append("\n");
            r.append(kpi("Nombre d'agents actifs", String.valueOf(weeklyReport.agentsActifs.size()), ""));
            r.append(kpi("Total missions assignees", String.valueOf(totalMissions), ""));
            r.append(kpi("Taux de reussite moyen", String.format("%.1f%%", tauxMoyen),
                String.format("%+.1f pts vs periode precedente", tauxMoyen - prevTauxMoy)));
            r.append("\n");

            r.append("2. CLASSEMENT DETAILLE\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-3s %-22s %8s %8s %10s  %s%n",
                "Rg", "Agent", "Assign.", "Termin.", "Taux", "Performance"));
            r.append("  " + "-".repeat(72) + "\n");
            final int[] rang = {1};
            weeklyReport.agentsActifs.forEach(a -> {
                String perf = a.tauxReussite >= 80 ? "Excellent"
                            : a.tauxReussite >= 60 ? "Bien"
                            : a.tauxReussite >= 40 ? "Moyen"
                            : "Insuffisant";
                r.append(String.format("  %-3d %-22s %8d %8d %9.1f%%  %s%n",
                    rang[0], a.nom, a.missionsTotal, a.missionsTerminees, a.tauxReussite, perf));
                rang[0]++;
            });
            r.append("\n");

            r.append("3. RECOMMANDATIONS RH\n").append(SEPARATEUR2).append("\n");
            for (String reco : recommandations) {
                r.append("  ").append(reco).append('\n');
            }
            if (recommandations.isEmpty()) {
                r.append("  Aucune recommandation RH critique sur la periode.\n");
            }

            finalizeReportText(r);
        });
    }
    private void generateGeographicAnalysis(DateRange range) {
        ReportService.MonthlyReport current = reportService.generateMonthlyReport(range.start, range.end);

        DateRange prevRange = previousRange(range);
        ReportService.MonthlyReport previous = reportService.generateMonthlyReport(prevRange.start, prevRange.end);

        int totalCurrent = current.performanceParZone.values().stream().mapToInt(Integer::intValue).sum();
        int totalPrevious = previous.performanceParZone.values().stream().mapToInt(Integer::intValue).sum();
        List<String> recommandations = new ArrayList<>();
        current.performanceParZone.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(e -> recommandations.add("Zone " + e.getKey() + " : charge la plus elevee. Renforcer la presence des agents."));
        current.performanceParZone.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .filter(e -> e.getValue() > 0)
            .ifPresent(e -> recommandations.add("Zone " + e.getKey() + " : activite faible. Verifier la couverture citoyenne."));
        recommandations.add("Optimiser les itineraires de collecte entre les zones pour reduire les deplacements.");

        javafx.application.Platform.runLater(() -> {
            renderTrendChart(current.evolutionQuotidienne, "Periode selectionnee",
                previous.evolutionQuotidienne, "Periode precedente");

            XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
            XYChart.Series<String, Number> previousSeries = new XYChart.Series<>();
            currentSeries.setName("Periode selectionnee");
            previousSeries.setName("Periode precedente");
            current.performanceParZone.forEach((z, v) -> currentSeries.getData().add(new XYChart.Data<>(z, v)));
            previous.performanceParZone.forEach((z, v) -> previousSeries.getData().add(new XYChart.Data<>(z, v)));
            zoneBarChart.setData(FXCollections.observableArrayList(currentSeries, previousSeries));

            ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
            stats.add(new StatisticRow("Periode", range.start.format(FMT_DATE) + " -> " + range.end.format(FMT_DATE)));
            stats.add(new StatisticRow("Zones couvertes", String.valueOf(current.performanceParZone.size())));
            stats.add(new StatisticRow("Zone la plus active", current.zoneLaPlusActive));
            stats.add(new StatisticRow("Total periode", String.valueOf(totalCurrent)));
            stats.add(new StatisticRow("Total periode precedente", String.valueOf(totalPrevious)));
            current.performanceParZone.forEach((z, v) -> stats.add(new StatisticRow(
                z, v + "  (" + (totalCurrent > 0 ? String.format("%.1f%%", v * 100.0 / totalCurrent) : "N/A") + ")")));
            statsTable.setItems(stats);
            updateOperationalSummary(Math.max(totalCurrent - totalPrevious, 0), 0, totalCurrent);
            updateRecommendations(recommandations);

            StringBuilder r = new StringBuilder();
            r.append(entete("ANALYSE GEOGRAPHIQUE",
                "Comparaison periode selectionnee vs periode precedente equivalente"));

            r.append("1. REPARTITION PAR ZONE\n").append(SEPARATEUR2).append("\n");
            r.append(String.format("  %-20s %10s %10s %10s%n", "Zone", "Actuel", "Prec.", "Evol."));
            r.append("  " + "-".repeat(56) + "\n");
            current.performanceParZone.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    int prev = previous.performanceParZone.getOrDefault(e.getKey(), 0);
                    String evo = prev > 0 ? String.format("%+d", e.getValue() - prev) : "N/A";
                    r.append(String.format("  %-20s %10d %10d %10s%n", e.getKey(), e.getValue(), prev, evo));
                });
            r.append("\n");

            r.append("2. VISUALISATION PAR CHARGE\n").append(SEPARATEUR2).append("\n");
            current.performanceParZone.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> r.append(String.format("  %-20s %s  (%d sign.)%n",
                    e.getKey(), barre(e.getValue(), Math.max(totalCurrent, 1), 30), e.getValue())));
            r.append("\n");

            r.append("3. RECOMMANDATIONS TERRITORIALES\n").append(SEPARATEUR2).append("\n");
            for (String reco : recommandations) {
                r.append("  ").append(reco).append('\n');
            }

            finalizeReportText(r);
        });
    }

    @FXML
    private void exportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport");
        fileChooser.setInitialFileName("rapport_smartcity_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Fichiers Excel compatibles", "*.xls"),
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try {
                exportReportFile(file);
                setExportStatus("Export reussi : " + file.getName(), true);
                showAlert("Export reussi", "Le rapport a ete exporte vers : " + file.getAbsolutePath());
            } catch (IOException e) {
                setExportStatus("Echec export : " + e.getMessage(), false);
                showAlert("Erreur d'export", "Impossible d'exporter le rapport : " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter CSV");
        fileChooser.setInitialFileName("rapport_smartcity_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            exportCsvFile(file);
            setExportStatus("CSV exporte : " + file.getName(), true);
            showAlert("Export CSV reussi", "Le rapport CSV a ete exporte vers : " + file.getAbsolutePath());
        } catch (IOException e) {
            setExportStatus("Echec export CSV : " + e.getMessage(), false);
            showAlert("Erreur d'export", "Impossible d'exporter le CSV : " + e.getMessage());
        }
    }

    private void exportReportFile(File file) throws IOException {
        String lowerName = file.getName().toLowerCase(java.util.Locale.ROOT);
        if (lowerName.endsWith(".pdf")) {
            exportPdf(file);
            return;
        }
        if (lowerName.endsWith(".xls")) {
            exportExcelCompatible(file);
            return;
        }
        if (lowerName.endsWith(".csv")) {
            exportCsvFile(file);
            return;
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write(getReportContent());
        }
    }

    private void exportPdf(File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            String reportBody = getReportContent();
            List<String> lines = reportBody == null
                ? java.util.Collections.emptyList()
                : java.util.Arrays.asList(reportBody.split("\\R", -1));

            int pageNumber = 1;
            PDPageContentStream content = createPdfPage(document, pageNumber);
            float y = 745f;

            for (String rawLine : lines) {
                String line = sanitizePdfText(rawLine).trim();
                if (line.isBlank() || isReportSeparatorLine(line) || isReportHeaderMetadata(line)) {
                    y -= 6f;
                    continue;
                }

                boolean section = line.matches("^\\d+\\.\\s+.*") || isUpperShortTitle(line);
                boolean bullet = line.startsWith("-") || rawLine.startsWith("  ");
                float fontSize = section ? 11f : 10f;
                PDType1Font font = section ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
                float lineHeight = section ? 15f : 13f;
                float indent = bullet ? 14f : 0f;

                String printable = bullet && !line.startsWith("-") ? "- " + line : line;
                int maxChars = section ? 84 : 98;
                List<String> wrapped = wrapLines(printable, maxChars);

                for (String wrappedLine : wrapped) {
                    if (y < 70f) {
                        content.close();
                        pageNumber++;
                        content = createPdfPage(document, pageNumber);
                        y = 745f;
                    }
                    content.beginText();
                    content.setFont(font, fontSize);
                    content.newLineAtOffset(45f + indent, y);
                    content.showText(sanitizePdfText(wrappedLine));
                    content.endText();
                    y -= lineHeight;
                }
            }
            content.close();
            document.save(file);
        }
    }

    private void exportExcelCompatible(File file) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("<html><head><meta charset=\"UTF-8\">");
            writer.write("<style>");
            writer.write("body{font-family:Segoe UI,Arial,sans-serif;color:#0f172a;margin:24px;}");
            writer.write(".top{border-bottom:2px solid #16a34a;padding-bottom:10px;margin-bottom:16px;}");
            writer.write(".title{font-size:24px;font-weight:700;color:#14532d;}");
            writer.write(".sub{font-size:12px;color:#475569;}");
            writer.write("table{border-collapse:collapse;width:100%;margin-top:10px;}");
            writer.write("th,td{border:1px solid #cbd5e1;padding:8px 10px;font-size:13px;}");
            writer.write("th{background:#f1f5f9;text-align:left;}");
            writer.write(".section{margin-top:22px;font-size:18px;font-weight:700;color:#1e293b;}");
            writer.write(".report p{margin:6px 0;line-height:1.45;}");
            writer.write(".report h3{margin:14px 0 8px 0;font-size:15px;color:#0f172a;}");
            writer.write(".report ul{margin:6px 0 6px 20px;}");
            writer.write("</style></head><body>\n");

            writer.write("<div class=\"top\">");
            writer.write("<div class=\"title\">SmartCity - Rapport d'activite</div>");
            writer.write("<div class=\"sub\">Genere le " + escapeHtml(java.time.LocalDateTime.now().format(FMT_HEURE)) + "</div>");
            writer.write("</div>");

            writer.write("<div class=\"section\">Indicateurs cles</div>");
            writer.write("<table><tr><th>Metrique</th><th>Valeur</th></tr>\n");
            for (StatisticRow row : statsTable.getItems()) {
                writer.write("<tr><td>" + escapeHtml(row.getMetric()) + "</td><td>" + escapeHtml(row.getValue()) + "</td></tr>\n");
            }
            writer.write("</table>");

            writer.write("<div class=\"section\">Synthese operationnelle</div>");
            writer.write("<table><tr><th>Indicateur</th><th>Valeur</th></tr>");
            writer.write("<tr><td>En attente</td><td>" + escapeHtml(safeLabelText(summaryAttente)) + "</td></tr>");
            writer.write("<tr><td>En cours</td><td>" + escapeHtml(safeLabelText(summaryEnCours)) + "</td></tr>");
            writer.write("<tr><td>Termines</td><td>" + escapeHtml(safeLabelText(summaryTermine)) + "</td></tr>");
            writer.write("<tr><td>Taux de resolution</td><td>" + escapeHtml(safeLabelText(summaryTauxLabel)) + "</td></tr>");
            writer.write("</table>");

            writer.write("<div class=\"section\">Contenu du rapport</div>");
            writer.write("<div class=\"report\">");
            writer.write(formatReportAsHtml(getReportContent()));
            writer.write("</div>");
            writer.write("</body></html>");
        }
    }

    private PDPageContentStream createPdfPage(PDDocument document, int pageNumber) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(45, 805);
        content.showText("SmartCity - Rapport d'activite");
        content.endText();

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 9);
        content.newLineAtOffset(45, 790);
        content.showText("Genere le " + java.time.LocalDateTime.now().format(FMT_HEURE));
        content.endText();

        content.moveTo(45, 782);
        content.lineTo(550, 782);
        content.stroke();

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
        content.newLineAtOffset(500, 25);
        content.showText("Page " + pageNumber);
        content.endText();

        return content;
    }

    private boolean isReportSeparatorLine(String line) {
        return line.matches("^=+$") || line.matches("^-+$");
    }

    private boolean isReportHeaderMetadata(String line) {
        String lower = line.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("smartcity")
            || lower.startsWith("pikine")
            || lower.startsWith("genere le")
            || lower.startsWith("rapport genere automatiquement");
    }

    private boolean isUpperShortTitle(String line) {
        return line.length() <= 42
            && !line.isBlank()
            && line.equals(line.toUpperCase(java.util.Locale.ROOT))
            && line.chars().anyMatch(Character::isLetter);
    }

    private String formatReportAsHtml(String report) {
        if (report == null || report.isBlank()) {
            return "<p>Aucun contenu disponible.</p>";
        }
        StringBuilder out = new StringBuilder();
        boolean inList = false;
        for (String raw : report.split("\\R", -1)) {
            String line = sanitizePdfText(raw).trim();
            if (line.isBlank() || isReportSeparatorLine(line) || isReportHeaderMetadata(line)) {
                if (inList) {
                    out.append("</ul>");
                    inList = false;
                }
                continue;
            }

            if (line.matches("^\\d+\\.\\s+.*") || isUpperShortTitle(line)) {
                if (inList) {
                    out.append("</ul>");
                    inList = false;
                }
                out.append("<h3>").append(escapeHtml(line)).append("</h3>");
                continue;
            }

            if (line.startsWith("-") || raw.startsWith("  ")) {
                if (!inList) {
                    out.append("<ul>");
                    inList = true;
                }
                String item = line.startsWith("-") ? line.substring(1).trim() : line;
                out.append("<li>").append(escapeHtml(item)).append("</li>");
                continue;
            }

            if (inList) {
                out.append("</ul>");
                inList = false;
            }
            out.append("<p>").append(escapeHtml(line)).append("</p>");
        }
        if (inList) {
            out.append("</ul>");
        }
        return out.toString();
    }

    private void exportCsvFile(File file) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(
            new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write("Section,Metrique,Valeur\n");
            for (StatisticRow row : statsTable.getItems()) {
                writeCsvRow(writer, "Indicateurs", row.getMetric(), row.getValue());
            }
            writeCsvRow(writer, "Synthese", "En attente", safeLabelText(summaryAttente));
            writeCsvRow(writer, "Synthese", "En cours", safeLabelText(summaryEnCours));
            writeCsvRow(writer, "Synthese", "Termines", safeLabelText(summaryTermine));
            writeCsvRow(writer, "Synthese", "Taux resolution", safeLabelText(summaryTauxLabel));
            writeCsvRow(writer, "Rapport", "Texte complet", getReportContent());
        }
    }

    private List<String> wrapLines(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\\R", -1)) {
            if (rawLine.length() <= maxLength) {
                lines.add(rawLine);
                continue;
            }
            String remaining = rawLine;
            while (remaining.length() > maxLength) {
                int split = remaining.lastIndexOf(' ', maxLength);
                if (split <= 0) {
                    split = maxLength;
                }
                lines.add(remaining.substring(0, split));
                remaining = remaining.substring(Math.min(split + 1, remaining.length()));
            }
            lines.add(remaining);
        }
        return lines;
    }

    private String sanitizePdfText(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value
            .replace('\t', ' ')
            .replace('\u0000', ' ')
            .replace("✅", "[OK]")
            .replace("⚠️", "[!]")
            .replace("❌", "[X]")
            .replace("•", "-")
            .replace("—", "-")
            .replace("→", "->");

        StringBuilder out = new StringBuilder(cleaned.length());
        for (char c : cleaned.toCharArray()) {
            if (c >= 32 && c <= 255) {
                out.append(c);
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void writeCsvRow(OutputStreamWriter writer, String section, String metric, String value) throws IOException {
        writer.write(escapeCsv(section));
        writer.write(',');
        writer.write(escapeCsv(metric));
        writer.write(',');
        writer.write(escapeCsv(value));
        writer.write('\n');
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }

    private String safeLabelText(Label label) {
        return label == null || label.getText() == null ? "" : label.getText();
    }

    private void setExportStatus(String message, boolean success) {
        if (exportStatusLabel == null) {
            return;
        }
        String safeMessage = message == null ? "" : message;
        exportStatusLabel.setText(safeMessage);
        if (safeMessage.isBlank()) {
            exportStatusLabel.setStyle("-fx-text-fill: #64748b;");
            return;
        }
        exportStatusLabel.setStyle(success
            ? "-fx-text-fill: #166534; -fx-font-weight: 600;"
            : "-fx-text-fill: #b91c1c; -fx-font-weight: 600;");
    }

    private void updateLastRefresh() {
        if (lastRefreshLabel == null) {
            return;
        }
        lastRefreshLabel.setText("Derniere mise a jour : "
            + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    private void updateOperationalSummary(int attente, int enCours, int termines) {
        if (summaryAttente != null) summaryAttente.setText(String.valueOf(attente));
        if (summaryEnCours != null) summaryEnCours.setText(String.valueOf(enCours));
        if (summaryTermine != null) summaryTermine.setText(String.valueOf(termines));
        int total = Math.max(attente + enCours + termines, 1);
        double taux = termines * 100.0 / total;
        if (summaryTauxLabel != null) summaryTauxLabel.setText(String.format(FR, "%.1f%%", taux));
        if (summaryProgressBar != null) summaryProgressBar.setProgress(termines / (double) total);
    }

    private void updateRecommendations(List<String> recommandations) {
        if (recoContainer == null) {
            return;
        }
        recoContainer.getChildren().clear();
        if (recommandations == null || recommandations.isEmpty()) {
            Label none = new Label("Aucune recommandation critique pour cette periode.");
            none.getStyleClass().add("text-muted");
            none.setWrapText(true);
            recoContainer.getChildren().add(none);
            return;
        }
        for (String reco : recommandations) {
            Label item = new Label("- " + reco);
            item.setWrapText(true);
            item.getStyleClass().add("text-body");
            recoContainer.getChildren().add(item);
        }
    }

    private void loadDefaultData() {
        generateReport();
    }

    private void clearCharts() {
        statusPieChart.getData().clear();
        zoneBarChart.getData().clear();
        trendLineChart.getData().clear();
        statsTable.getItems().clear();
        reportContent = "";
        if (reportTextArea != null) {
            reportTextArea.clear();
        }
        updateOperationalSummary(0, 0, 0);
        updateRecommendations(java.util.Collections.emptyList());
    }

    private String getReportContent() {
        if (reportTextArea != null && reportTextArea.getText() != null && !reportTextArea.getText().isBlank()) {
            return reportTextArea.getText();
        }
        return reportContent == null ? "" : reportContent;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class DateRange {
        final LocalDate start;
        final LocalDate end;
        final long days;

        DateRange(LocalDate start, LocalDate end, long days) {
            this.start = start;
            this.end = end;
            this.days = days;
        }
    }

    public static class StatisticRow {
        private final String metric;
        private final String value;

        public StatisticRow(String metric, String value) {
            this.metric = metric;
            this.value = value;
        }

        public String getMetric() { return metric; }
        public String getValue() { return value; }

        public javafx.beans.property.StringProperty metricProperty() {
            return new javafx.beans.property.SimpleStringProperty(metric);
        }

        public javafx.beans.property.StringProperty valueProperty() {
            return new javafx.beans.property.SimpleStringProperty(value);
        }
    }
}





