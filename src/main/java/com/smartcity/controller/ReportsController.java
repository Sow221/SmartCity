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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controleur pour les rapports et analytics.
 */
public class ReportsController {

    @FXML private VBox rootPane;
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

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        setupControls();
        setupTable();
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
    }

    @FXML
    private void generateReport() {
        String reportType = reportTypeCombo.getValue();
        if (reportType == null) return;
        clearCharts();
        if ("Dashboard Temps R\u00e9el".equals(reportType)) {
            generateRealTimeDashboard();
        } else if ("Rapport Hebdomadaire".equals(reportType)) {
            generateWeeklyReport();
        } else if ("Rapport Mensuel".equals(reportType)) {
            generateMonthlyReport();
        } else if ("Performance Agents".equals(reportType)) {
            generateAgentPerformanceReport();
        } else if ("Analyse G\u00e9ographique".equals(reportType)) {
            generateGeographicAnalysis();
        }
    }

    private void generateRealTimeDashboard() {
        ReportService.RealTimeDashboard dashboard = reportService.getRealTimeDashboard();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        int nbAttente = signalementService.countByStatut("En attente");
        int nbCours   = signalementService.countByStatut("En cours");
        int nbTermine = signalementService.countByStatut("Termin\u00e9");
        int nbAffecte = signalementService.countByStatut("Affect\u00e9");
        if (nbAttente > 0) pieData.add(new PieChart.Data("En attente (" + nbAttente + ")", nbAttente));
        if (nbCours   > 0) pieData.add(new PieChart.Data("En cours ("   + nbCours   + ")", nbCours));
        if (nbTermine > 0) pieData.add(new PieChart.Data("Termin\u00e9 ("  + nbTermine + ")", nbTermine));
        if (nbAffecte > 0) pieData.add(new PieChart.Data("Affect\u00e9 ("  + nbAffecte + ")", nbAffecte));
        statusPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Signalements (7 derniers jours)");
        dashboard.zonesActives.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> zoneSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries));

        int total = dashboard.totalSignalements;
        int tauxRes = total > 0 ? (int) Math.round(nbTermine * 100.0 / total) : 0;
        String tendHebdo = dashboard.tendances.containsKey("hebdomadaire")
            ? String.format("%+.1f%% vs sem. pr\u00e9c.", dashboard.tendances.get("hebdomadaire")) : "";

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Total signalements",    String.valueOf(total)));
        stats.add(new StatisticRow("Aujourd'hui",           String.valueOf(dashboard.signalementsAujourdhui)));
        stats.add(new StatisticRow("En attente",            String.valueOf(nbAttente)));
        stats.add(new StatisticRow("En cours",              String.valueOf(nbCours)));
        stats.add(new StatisticRow("Termin\u00e9s",          String.valueOf(nbTermine)));
        stats.add(new StatisticRow("Taux de r\u00e9solution", tauxRes + "%"));
        stats.add(new StatisticRow("Urgents (>24h)",        String.valueOf(dashboard.signalementsUrgents.size())));
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
        r.append(String.format("  En attente  %s%n", barre(nbAttente, total, 30)));
        r.append(String.format("  En cours    %s%n", barre(nbCours,   total, 30)));
        r.append(String.format("  Affect\u00e9    %s%n", barre(nbAffecte, total, 30)));
        r.append(String.format("  Termin\u00e9    %s%n", barre(nbTermine, total, 30)));
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
        if (!dashboard.signalementsUrgents.isEmpty())
            r.append("  \u26a0\ufe0f  Traiter en priorit\u00e9 les ").append(dashboard.signalementsUrgents.size()).append(" signalement(s) en attente depuis plus de 24h.\n");
        if (tauxRes < 50)
            r.append("  \u26a0\ufe0f  Taux de r\u00e9solution inf\u00e9rieur \u00e0 50% \u2014 renforcer les \u00e9quipes de collecte.\n");
        if (nbAttente > nbCours * 2)
            r.append("  \u26a0\ufe0f  File d'attente \u00e9lev\u00e9e \u2014 envisager des affectations suppl\u00e9mentaires.\n");
        if (tauxRes >= 70 && dashboard.signalementsUrgents.isEmpty())
            r.append("  \u2705  Situation satisfaisante. Maintenir le rythme actuel.\n");
        r.append("\n").append(SEPARATEUR).append("\n");
        r.append("  Rapport g\u00e9n\u00e9r\u00e9 automatiquement par SmartCity Analytics\n");
        r.append(SEPARATEUR).append("\n");
        reportTextArea.setText(r.toString());
    }

    private void generateWeeklyReport() {
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) startDate = LocalDate.now().minusDays(6);
        currentWeeklyReport = reportService.generateWeeklyReport(startDate);

        // Semaine précédente pour comparaison
        LocalDate prevStart = startDate.minusDays(7);
        ReportService.WeeklyReport prevReport = reportService.generateWeeklyReport(prevStart);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (currentWeeklyReport.signalementsEnAttente > 0)
            pieData.add(new PieChart.Data("En attente (" + currentWeeklyReport.signalementsEnAttente + ")", currentWeeklyReport.signalementsEnAttente));
        if (currentWeeklyReport.signalementsEnCours > 0)
            pieData.add(new PieChart.Data("En cours (" + currentWeeklyReport.signalementsEnCours + ")", currentWeeklyReport.signalementsEnCours));
        if (currentWeeklyReport.signalementsResolus > 0)
            pieData.add(new PieChart.Data("R\u00e9solus (" + currentWeeklyReport.signalementsResolus + ")", currentWeeklyReport.signalementsResolus));
        statusPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Cette semaine");
        XYChart.Series<String, Number> prevZoneSeries = new XYChart.Series<>();
        prevZoneSeries.setName("Semaine pr\u00e9c.");
        currentWeeklyReport.performanceParZone.forEach((z, v) -> zoneSeries.getData().add(new XYChart.Data<>(z, v)));
        prevReport.performanceParZone.forEach((z, v) -> prevZoneSeries.getData().add(new XYChart.Data<>(z, v)));
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries, prevZoneSeries));

        int total = currentWeeklyReport.totalSignalements;
        int prevTotal = prevReport.totalSignalements;
        double tauxRes = total > 0 ? (double) currentWeeklyReport.signalementsResolus / total * 100 : 0;
        double prevTauxRes = prevTotal > 0 ? (double) prevReport.signalementsResolus / prevTotal * 100 : 0;
        String evoTotal = prevTotal > 0 ? String.format("%+d (%.0f%%)", total - prevTotal, (double)(total - prevTotal)/prevTotal*100) : "N/A";
        String evoTaux  = String.format("%+.1f pts", tauxRes - prevTauxRes);

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("P\u00e9riode", startDate.format(FMT_DATE) + " \u2192 " + currentWeeklyReport.endDate.format(FMT_DATE)));
        stats.add(new StatisticRow("Total signalements", total + "  (" + evoTotal + ")"));
        stats.add(new StatisticRow("R\u00e9solus",          currentWeeklyReport.signalementsResolus + " / " + total));
        stats.add(new StatisticRow("En cours",           String.valueOf(currentWeeklyReport.signalementsEnCours)));
        stats.add(new StatisticRow("En attente",         String.valueOf(currentWeeklyReport.signalementsEnAttente)));
        stats.add(new StatisticRow("Taux de r\u00e9solution", String.format("%.1f%%  (%s)", tauxRes, evoTaux)));
        statsTable.setItems(stats);

        StringBuilder r = new StringBuilder();
        r.append(entete("RAPPORT HEBDOMADAIRE",
            "P\u00e9riode : " + startDate.format(FMT_DATE) + " au " + currentWeeklyReport.endDate.format(FMT_DATE)));

        r.append("1. R\u00c9SUM\u00c9 EX\u00c9CUTIF\n").append(SEPARATEUR2).append("\n");
        r.append(kpi("Total signalements re\u00e7us",  String.valueOf(total), evoTotal));
        r.append(kpi("Signalements r\u00e9solus",       currentWeeklyReport.signalementsResolus + " / " + total, ""));
        r.append(kpi("Taux de r\u00e9solution",         String.format("%.1f%%", tauxRes),
                     tauxRes >= 70 ? "\u2705 BON" : tauxRes >= 40 ? "\u26a0\ufe0f MOYEN" : "\u274c FAIBLE"));
        r.append(kpi("\u00c9volution vs semaine pr\u00e9c.", evoTotal, ""));
        r.append(kpi("\u00c9volution taux r\u00e9solution",  evoTaux,
                     tauxRes > prevTauxRes ? "\u2191 Progression" : tauxRes < prevTauxRes ? "\u2193 R\u00e9gression" : "= Stable"));
        r.append("\n");

        r.append("2. PERFORMANCE PAR ZONE\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-20s %8s %8s %8s%n", "Zone", "Total", "Pr\u00e9c.", "\u00c9volution"));
        r.append("  " + "-".repeat(48) + "\n");
        currentWeeklyReport.performanceParZone.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> {
                int prev = prevReport.performanceParZone.getOrDefault(e.getKey(), 0);
                String evo = prev > 0 ? String.format("%+d", e.getValue() - prev) : "N/A";
                r.append(String.format("  %-20s %8d %8d %8s%n", e.getKey(), e.getValue(), prev, evo));
            });
        r.append("\n");

        r.append("3. TOP CAT\u00c9GORIES DE D\u00c9CHETS\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-20s %8s %8s%n", "Cat\u00e9gorie", "Nb", "Part"));
        r.append("  " + "-".repeat(40) + "\n");
        currentWeeklyReport.categoriesPopulaires.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> r.append(String.format("  %-20s %8d %8s%n",
                e.getKey(), e.getValue(),
                total > 0 ? String.format("%.1f%%", e.getValue() * 100.0 / total) : "N/A")));
        r.append("\n");

        r.append("4. CLASSEMENT DES AGENTS\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-3s %-22s %8s %8s %10s%n", "Rg", "Agent", "Total", "Termin\u00e9s", "Taux"));
        r.append("  " + "-".repeat(56) + "\n");
        final int[] rang = {1};
        currentWeeklyReport.agentsActifs.stream().limit(10).forEach(a -> {
            String medaille = rang[0] == 1 ? "\uD83E\uDD47" : rang[0] == 2 ? "\uD83E\uDD48" : rang[0] == 3 ? "\uD83E\uDD49" : "  ";
            r.append(String.format("  %s  %-22s %8d %8d %9.1f%%%n",
                medaille, a.nom, a.missionsTotal, a.missionsTerminees, a.tauxReussite));
            rang[0]++;
        });
        r.append("\n");

        r.append("5. RECOMMANDATIONS\n").append(SEPARATEUR2).append("\n");
        if (total > prevTotal * 1.2)
            r.append("  \u26a0\ufe0f  Hausse significative des signalements (+20%). Renforcer les effectifs.\n");
        if (tauxRes < prevTauxRes - 5)
            r.append("  \u26a0\ufe0f  Baisse du taux de r\u00e9solution. Identifier les blocages op\u00e9rationnels.\n");
        if (currentWeeklyReport.signalementsEnAttente > currentWeeklyReport.signalementsEnCours)
            r.append("  \u26a0\ufe0f  Plus de signalements en attente qu'en cours. Acc\u00e9l\u00e9rer les affectations.\n");
        if (!currentWeeklyReport.agentsActifs.isEmpty() && currentWeeklyReport.agentsActifs.get(0).tauxReussite >= 80)
            r.append("  \u2705  Agent " + currentWeeklyReport.agentsActifs.get(0).nom + " : performance exemplaire (" +
                String.format("%.0f%%", currentWeeklyReport.agentsActifs.get(0).tauxReussite) + "). \u00c0 valoriser.\n");
        r.append("\n").append(SEPARATEUR).append("\n");
        r.append("  Rapport g\u00e9n\u00e9r\u00e9 automatiquement par SmartCity Analytics\n");
        r.append(SEPARATEUR).append("\n");
        reportTextArea.setText(r.toString());
    }

    private void generateMonthlyReport() {
        LocalDate date = endDatePicker.getValue();
        if (date == null) date = LocalDate.now();
        currentMonthlyReport = reportService.generateMonthlyReport(date.getMonthValue(), date.getYear());

        // Mois précédent pour comparaison
        LocalDate prevMonth = date.minusMonths(1);
        ReportService.MonthlyReport prevReport = reportService.generateMonthlyReport(prevMonth.getMonthValue(), prevMonth.getYear());

        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName(date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", FR)));
        XYChart.Series<String, Number> prevTrendSeries = new XYChart.Series<>();
        prevTrendSeries.setName(prevMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", FR)));
        currentMonthlyReport.evolutionQuotidienne.forEach((jour, cnt) ->
            trendSeries.getData().add(new XYChart.Data<>(jour.substring(8), cnt)));
        prevReport.evolutionQuotidienne.forEach((jour, cnt) ->
            prevTrendSeries.getData().add(new XYChart.Data<>(jour.substring(8), cnt)));
        trendLineChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(trendSeries, prevTrendSeries));

        int total     = currentMonthlyReport.totalSignalements;
        int prevTotal = prevReport.totalSignalements;
        double tauxRes     = currentMonthlyReport.tauxResolution;
        double prevTauxRes = prevReport.tauxResolution;
        String evoTotal = prevTotal > 0 ? String.format("%+d (%.0f%%)", total - prevTotal, (double)(total-prevTotal)/prevTotal*100) : "N/A";
        String evoTaux  = String.format("%+.1f pts", tauxRes - prevTauxRes);
        String nomMois  = date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", FR));
        String nomPrev  = prevMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", FR));

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Mois analys\u00e9",          nomMois));
        stats.add(new StatisticRow("Total signalements",     total + "  (" + evoTotal + ")"));
        stats.add(new StatisticRow("Taux de r\u00e9solution",   String.format("%.1f%%  (%s)", tauxRes, evoTaux)));
        stats.add(new StatisticRow("Temps traitement moyen", String.format("%.1f h", currentMonthlyReport.tempsTraitementMoyen)));
        stats.add(new StatisticRow("Zone la plus active",    currentMonthlyReport.zoneLaPlusActive));
        statsTable.setItems(stats);

        StringBuilder r = new StringBuilder();
        r.append(entete("RAPPORT MENSUEL \u2014 " + nomMois.toUpperCase(FR),
                        "Analyse compar\u00e9e avec " + nomPrev));

        r.append("1. INDICATEURS CL\u00c9S\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-38s %12s %12s %10s%n", "Indicateur", nomMois.split(" ")[0], nomPrev.split(" ")[0], "\u00c9volution"));
        r.append("  " + "-".repeat(74) + "\n");
        r.append(String.format("  %-38s %12d %12d %10s%n", "Total signalements", total, prevTotal, evoTotal));
        r.append(String.format("  %-38s %11.1f%% %11.1f%% %10s%n", "Taux de r\u00e9solution", tauxRes, prevTauxRes, evoTaux));
        r.append(String.format("  %-38s %11.1fh %11.1fh%n", "Temps traitement moyen",
            currentMonthlyReport.tempsTraitementMoyen, prevReport.tempsTraitementMoyen));
        r.append("\n");

        r.append("2. ANALYSE PAR ZONE\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-20s %8s %8s %8s%n", "Zone", nomMois.split(" ")[0], nomPrev.split(" ")[0], "\u00c9vol."));
        r.append("  " + "-".repeat(48) + "\n");
        currentMonthlyReport.performanceParZone.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> {
                int prev = prevReport.performanceParZone.getOrDefault(e.getKey(), 0);
                r.append(String.format("  %-20s %8d %8d %8s%n",
                    e.getKey(), e.getValue(), prev,
                    prev > 0 ? String.format("%+d", e.getValue() - prev) : "N/A"));
            });
        r.append("\n");

        r.append("3. PERFORMANCE DES AGENTS\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-3s %-22s %8s %8s %10s%n", "Rg", "Agent", "Total", "Termin\u00e9s", "Taux"));
        r.append("  " + "-".repeat(56) + "\n");
        final int[] rang = {1};
        currentMonthlyReport.performanceAgents.stream().limit(10).forEach(a -> {
            String medaille = rang[0] == 1 ? "\uD83E\uDD47" : rang[0] == 2 ? "\uD83E\uDD48" : rang[0] == 3 ? "\uD83E\uDD49" : "  ";
            r.append(String.format("  %s  %-22s %8d %8d %9.1f%%%n",
                medaille, a.nom, a.missionsTotal, a.missionsTerminees, a.tauxReussite));
            rang[0]++;
        });
        r.append("\n");

        r.append("4. \u00c9VOLUTION QUOTIDIENNE (pic d'activit\u00e9)\n").append(SEPARATEUR2).append("\n");
        currentMonthlyReport.evolutionQuotidienne.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> r.append(String.format("  %s : %s%n",
                e.getKey(), barre(e.getValue(),
                    currentMonthlyReport.evolutionQuotidienne.values().stream().mapToInt(Integer::intValue).max().orElse(1), 30)
                    + "  " + e.getValue() + " sign.")));
        r.append("\n");

        r.append("5. RECOMMANDATIONS\n").append(SEPARATEUR2).append("\n");
        if (total > prevTotal * 1.15)
            r.append("  \u26a0\ufe0f  Hausse de +15% des signalements vs mois pr\u00e9c. Anticiper les ressources.\n");
        if (tauxRes < prevTauxRes - 5)
            r.append("  \u26a0\ufe0f  Baisse du taux de r\u00e9solution de " + String.format("%.1f", prevTauxRes - tauxRes) + " pts. Analyser les causes.\n");
        if (currentMonthlyReport.tempsTraitementMoyen > 48)
            r.append("  \u26a0\ufe0f  Temps de traitement moyen > 48h. Optimiser le processus d'affectation.\n");
        if (tauxRes >= 75)
            r.append("  \u2705  Taux de r\u00e9solution " + String.format("%.1f%%", tauxRes) + " : objectif atteint.\n");
        r.append("\n").append(SEPARATEUR).append("\n");
        r.append("  Rapport g\u00e9n\u00e9r\u00e9 automatiquement par SmartCity Analytics\n");
        r.append(SEPARATEUR).append("\n");
        reportTextArea.setText(r.toString());
    }

    private void generateAgentPerformanceReport() {
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) startDate = LocalDate.now().minusDays(6);
        ReportService.WeeklyReport weeklyReport = reportService.generateWeeklyReport(startDate);
        ReportService.WeeklyReport prevReport   = reportService.generateWeeklyReport(startDate.minusDays(7));

        XYChart.Series<String, Number> seriesMissions  = new XYChart.Series<>();
        XYChart.Series<String, Number> seriesTerminees = new XYChart.Series<>();
        seriesMissions.setName("Missions assign\u00e9es");
        seriesTerminees.setName("Missions termin\u00e9es");
        weeklyReport.agentsActifs.stream().limit(8).forEach(a -> {
            String nom = a.nom.length() > 12 ? a.nom.substring(0, 12) + "." : a.nom;
            seriesMissions.getData().add(new XYChart.Data<>(nom, a.missionsTotal));
            seriesTerminees.getData().add(new XYChart.Data<>(nom, a.missionsTerminees));
        });
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(seriesMissions, seriesTerminees));

        double tauxMoyen   = weeklyReport.agentsActifs.stream().mapToDouble(a -> a.tauxReussite).average().orElse(0);
        double prevTauxMoy = prevReport.agentsActifs.stream().mapToDouble(a -> a.tauxReussite).average().orElse(0);
        int totalMissions  = weeklyReport.agentsActifs.stream().mapToInt(a -> a.missionsTotal).sum();

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("P\u00e9riode", startDate.format(FMT_DATE) + " \u2192 " + startDate.plusDays(6).format(FMT_DATE)));
        stats.add(new StatisticRow("Agents actifs",          String.valueOf(weeklyReport.agentsActifs.size())));
        stats.add(new StatisticRow("Total missions",         String.valueOf(totalMissions)));
        stats.add(new StatisticRow("Taux moyen",             String.format("%.1f%%  (%+.1f pts)", tauxMoyen, tauxMoyen - prevTauxMoy)));
        if (!weeklyReport.agentsActifs.isEmpty()) {
            ReportService.AgentStats top = weeklyReport.agentsActifs.get(0);
            stats.add(new StatisticRow("Meilleur agent", top.nom + " " + String.format("(%.0f%%)", top.tauxReussite)));
        }
        statsTable.setItems(stats);

        StringBuilder r = new StringBuilder();
        r.append(entete("RAPPORT PERFORMANCE DES AGENTS",
            "P\u00e9riode : " + startDate.format(FMT_DATE) + " au " + startDate.plusDays(6).format(FMT_DATE)));

        r.append("1. SYNTH\u00c8SE\n").append(SEPARATEUR2).append("\n");
        r.append(kpi("Nombre d'agents actifs",    String.valueOf(weeklyReport.agentsActifs.size()), ""));
        r.append(kpi("Total missions assign\u00e9es", String.valueOf(totalMissions), ""));
        r.append(kpi("Taux de r\u00e9ussite moyen",   String.format("%.1f%%", tauxMoyen),
                     String.format("%+.1f pts vs sem. pr\u00e9c.", tauxMoyen - prevTauxMoy)));
        r.append("\n");

        r.append("2. CLASSEMENT D\u00c9TAILL\u00c9\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-3s %-22s %8s %8s %10s  %s%n",
            "Rg", "Agent", "Assign.", "Termin.", "Taux", "Performance"));
        r.append("  " + "-".repeat(72) + "\n");
        final int[] rang = {1};
        weeklyReport.agentsActifs.forEach(a -> {
            String medaille = rang[0] == 1 ? "\uD83E\uDD47" : rang[0] == 2 ? "\uD83E\uDD48" : rang[0] == 3 ? "\uD83E\uDD49" : "  ";
            String perf = a.tauxReussite >= 80 ? "\u2605\u2605\u2605 Excellent"
                        : a.tauxReussite >= 60 ? "\u2605\u2605  Bien"
                        : a.tauxReussite >= 40 ? "\u2605   Moyen"
                        : "\u26a0\ufe0f  Insuffisant";
            r.append(String.format("  %s  %-22s %8d %8d %9.1f%%  %s%n",
                medaille, a.nom, a.missionsTotal, a.missionsTerminees, a.tauxReussite, perf));
            rang[0]++;
        });
        r.append("\n");

        r.append("3. RECOMMANDATIONS RH\n").append(SEPARATEUR2).append("\n");
        weeklyReport.agentsActifs.stream()
            .filter(a -> a.tauxReussite < 40 && a.missionsTotal >= 3)
            .forEach(a -> r.append("  \u26a0\ufe0f  " + a.nom + " : taux insuffisant ("
                + String.format("%.0f%%", a.tauxReussite) + "). Accompagnement recommand\u00e9.\n"));
        weeklyReport.agentsActifs.stream()
            .filter(a -> a.tauxReussite >= 90)
            .forEach(a -> r.append("  \u2b50  " + a.nom + " : performance exceptionnelle ("
                + String.format("%.0f%%", a.tauxReussite) + "). \u00c0 valoriser.\n"));
        if (weeklyReport.agentsActifs.stream().allMatch(a -> a.tauxReussite >= 60))
            r.append("  \u2705  Toute l'\u00e9quipe est au-dessus de 60%. Bonne dynamique collective.\n");
        r.append("\n").append(SEPARATEUR).append("\n");
        r.append("  Rapport g\u00e9n\u00e9r\u00e9 automatiquement par SmartCity Analytics\n");
        r.append(SEPARATEUR).append("\n");
        reportTextArea.setText(r.toString());
    }

    private void generateGeographicAnalysis() {
        ReportService.RealTimeDashboard now  = reportService.getRealTimeDashboard();
        ReportService.RealTimeDashboard prev = reportService.getRealTimeDashboard(); // même source, on compare 7j vs 30j
        LocalDate debut7j  = LocalDate.now().minusDays(6);
        LocalDate debut30j = LocalDate.now().minusDays(29);

        // Deux séries : 7 jours et 30 jours
        ReportService.WeeklyReport  w7  = reportService.generateWeeklyReport(debut7j);
        // Pour 30j on utilise le rapport mensuel du mois courant
        ReportService.MonthlyReport m30 = reportService.generateMonthlyReport(
            LocalDate.now().getMonthValue(), LocalDate.now().getYear());

        XYChart.Series<String, Number> s7  = new XYChart.Series<>();
        XYChart.Series<String, Number> s30 = new XYChart.Series<>();
        s7.setName("7 derniers jours");
        s30.setName("Mois en cours");
        w7.performanceParZone.forEach((z, v)  -> s7.getData().add(new XYChart.Data<>(z, v)));
        m30.performanceParZone.forEach((z, v) -> s30.getData().add(new XYChart.Data<>(z, v)));
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(s7, s30));

        int total7j  = w7.performanceParZone.values().stream().mapToInt(Integer::intValue).sum();
        int total30j = m30.performanceParZone.values().stream().mapToInt(Integer::intValue).sum();

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Zones couvertes",      String.valueOf(m30.performanceParZone.size())));
        stats.add(new StatisticRow("Zone la plus active",  m30.zoneLaPlusActive));
        stats.add(new StatisticRow("Total (7 jours)",      String.valueOf(total7j)));
        stats.add(new StatisticRow("Total (mois)",         String.valueOf(total30j)));
        m30.performanceParZone.forEach((z, v) -> stats.add(new StatisticRow(
            z, v + "  (" + (total30j > 0 ? String.format("%.1f%%", v * 100.0 / total30j) : "N/A") + ")")));
        statsTable.setItems(stats);

        StringBuilder r = new StringBuilder();
        r.append(entete("ANALYSE G\u00c9OGRAPHIQUE",
                        "Pikine & Gu\u00e9diawaye \u2014 Comparaison 7 jours vs mois en cours"));

        r.append("1. R\u00c9PARTITION PAR ZONE\n").append(SEPARATEUR2).append("\n");
        r.append(String.format("  %-20s %10s %10s %10s %10s%n",
            "Zone", "7 jours", "Part 7j", "Mois", "Part mois"));
        r.append("  " + "-".repeat(64) + "\n");
        m30.performanceParZone.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> {
                int v7 = w7.performanceParZone.getOrDefault(e.getKey(), 0);
                r.append(String.format("  %-20s %10d %9.1f%% %10d %9.1f%%%n",
                    e.getKey(), v7,
                    total7j  > 0 ? v7 * 100.0 / total7j  : 0,
                    e.getValue(),
                    total30j > 0 ? e.getValue() * 100.0 / total30j : 0));
            });
        r.append("\n");

        r.append("2. VISUALISATION (mois en cours)\n").append(SEPARATEUR2).append("\n");
        m30.performanceParZone.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> r.append(String.format("  %-20s %s  (%d sign.)%n",
                e.getKey(), barre(e.getValue(), Math.max(total30j, 1), 30), e.getValue())));
        r.append("\n");

        r.append("3. ANALYSE DES CAT\u00c9GORIES PAR ZONE\n").append(SEPARATEUR2).append("\n");
        w7.categoriesPopulaires.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> r.append(String.format("  %-20s %s  (%d sign.)%n",
                e.getKey(), barre(e.getValue(), Math.max(total7j, 1), 25), e.getValue())));
        r.append("\n");

        r.append("4. RECOMMANDATIONS TERRITORIALES\n").append(SEPARATEUR2).append("\n");
        m30.performanceParZone.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(e -> r.append("  \u26a0\ufe0f  Zone " + e.getKey() + " : zone la plus charg\u00e9e. Renforcer la pr\u00e9sence des agents.\n"));
        m30.performanceParZone.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .filter(e -> e.getValue() > 0)
            .ifPresent(e -> r.append("  \u2139\ufe0f  Zone " + e.getKey() + " : activit\u00e9 plus faible. V\u00e9rifier la couverture citoyenne.\n"));
        r.append("  \uD83D\uDDFA\ufe0f  Optimiser les itin\u00e9raires de collecte entre les zones pour r\u00e9duire les d\u00e9placements.\n");
        r.append("  \uD83D\uDCF1  Encourager les citoyens des zones peu actives \u00e0 signaler via l'application.\n");
        r.append("\n").append(SEPARATEUR).append("\n");
        r.append("  Rapport g\u00e9n\u00e9r\u00e9 automatiquement par SmartCity Analytics\n");
        r.append(SEPARATEUR).append("\n");
        reportTextArea.setText(r.toString());
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
                showAlert("Export reussi", "Le rapport a ete exporte vers : " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Erreur d'export", "Impossible d'exporter le rapport : " + e.getMessage());
            }
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
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write(reportTextArea.getText());
        }
    }

    private void exportPdf(File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            List<String> lines = wrapLines(reportTextArea.getText(), 95);
            int index = 0;
            while (index < lines.size()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(PDType1Font.COURIER, 10);
                    content.newLineAtOffset(40, 800);
                    int lineCount = 0;
                    while (index < lines.size() && lineCount < 68) {
                        content.showText(sanitizePdfText(lines.get(index++)));
                        content.newLineAtOffset(0, -11);
                        lineCount++;
                    }
                    content.endText();
                }
            }
            document.save(file);
        }
    }

    private void exportExcelCompatible(File file) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("<html><head><meta charset=\"UTF-8\"></head><body>\n");
            writer.write("<table border=\"1\"><tr><th>Metrique</th><th>Valeur</th></tr>\n");
            for (StatisticRow row : statsTable.getItems()) {
                writer.write("<tr><td>" + escapeHtml(row.getMetric()) + "</td><td>" + escapeHtml(row.getValue()) + "</td></tr>\n");
            }
            writer.write("</table><br/><pre>");
            writer.write(escapeHtml(reportTextArea.getText()));
            writer.write("</pre></body></html>");
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
        return value.replace('\t', ' ').replace('\u0000', ' ');
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadDefaultData() {
        generateReport();
    }

    private void clearCharts() {
        statusPieChart.getData().clear();
        zoneBarChart.getData().clear();
        trendLineChart.getData().clear();
        statsTable.getItems().clear();
        reportTextArea.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
