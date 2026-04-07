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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        pieData.add(new PieChart.Data("En attente", signalementService.countByStatut("En attente")));
        pieData.add(new PieChart.Data("En cours", signalementService.countByStatut("En cours")));
        pieData.add(new PieChart.Data("Termin\u00e9", signalementService.countByStatut("Termin\u00e9")));
        statusPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Signalements par Zone");
        for (Map.Entry<String, Integer> entry : dashboard.zonesActives.entrySet()) {
            zoneSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries));

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Total Signalements", String.valueOf(dashboard.totalSignalements)));
        stats.add(new StatisticRow("Aujourd'hui", String.valueOf(dashboard.signalementsAujourdhui)));
        stats.add(new StatisticRow("Utilisateurs Actifs", String.valueOf(dashboard.utilisateursActifs)));
        stats.add(new StatisticRow("Signalements Urgents", String.valueOf(dashboard.signalementsUrgents.size())));
        for (Map.Entry<String, Double> trend : dashboard.tendances.entrySet()) {
            String symbol = trend.getValue() >= 0 ? "+" : "-";
            stats.add(new StatisticRow("Tendance " + trend.getKey(),
                String.format("%s%.1f%%", symbol, Math.abs(trend.getValue()))));
        }
        statsTable.setItems(stats);

        StringBuilder report = new StringBuilder();
        report.append("=== DASHBOARD TEMPS REEL ===\n");
        report.append("Genere le : ").append(dashboard.timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        report.append("STATISTIQUES GLOBALES\n");
        report.append("- Total des signalements : ").append(dashboard.totalSignalements).append("\n");
        report.append("- Signalements aujourd'hui : ").append(dashboard.signalementsAujourdhui).append("\n");
        report.append("- Utilisateurs actifs : ").append(dashboard.utilisateursActifs).append("\n\n");
        if (!dashboard.signalementsUrgents.isEmpty()) {
            report.append("SIGNALEMENTS URGENTS (").append(dashboard.signalementsUrgents.size()).append(")\n");
            dashboard.signalementsUrgents.forEach(s ->
                report.append("- #").append(s.getIdSignalement())
                      .append(" - ").append(s.getCategorie())
                      .append(" (").append(s.getZoneNom()).append(")\n"));
            report.append("\n");
        }
        report.append("ZONES LES PLUS ACTIVES\n");
        dashboard.zonesActives.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> report.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" signalements\n"));
        reportTextArea.setText(report.toString());
    }

    private void generateWeeklyReport() {
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        currentWeeklyReport = reportService.generateWeeklyReport(startDate);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        pieData.add(new PieChart.Data("En attente", currentWeeklyReport.signalementsEnAttente));
        pieData.add(new PieChart.Data("En cours", currentWeeklyReport.signalementsEnCours));
        pieData.add(new PieChart.Data("R\u00e9solus", currentWeeklyReport.signalementsResolus));
        statusPieChart.setData(pieData);

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Performance par Zone");
        for (Map.Entry<String, Integer> entry : currentWeeklyReport.performanceParZone.entrySet()) {
            zoneSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries));

        double tauxResolution = currentWeeklyReport.totalSignalements > 0
            ? (double) currentWeeklyReport.signalementsResolus / currentWeeklyReport.totalSignalements * 100 : 0;

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("P\u00e9riode",
            startDate.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " +
            currentWeeklyReport.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        stats.add(new StatisticRow("Total Signalements", String.valueOf(currentWeeklyReport.totalSignalements)));
        stats.add(new StatisticRow("R\u00e9solus", String.valueOf(currentWeeklyReport.signalementsResolus)));
        stats.add(new StatisticRow("En Cours", String.valueOf(currentWeeklyReport.signalementsEnCours)));
        stats.add(new StatisticRow("En Attente", String.valueOf(currentWeeklyReport.signalementsEnAttente)));
        stats.add(new StatisticRow("Taux de R\u00e9solution", String.format("%.1f%%", tauxResolution)));
        statsTable.setItems(stats);

        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT HEBDOMADAIRE ===\n");
        report.append("Periode : ")
              .append(currentWeeklyReport.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
              .append(" - ")
              .append(currentWeeklyReport.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
              .append("\n\n");
        report.append("RESUME EXECUTIF\n");
        report.append("- Total signalements traites : ").append(currentWeeklyReport.totalSignalements).append("\n");
        report.append("- Signalements resolus : ").append(currentWeeklyReport.signalementsResolus).append("\n");
        report.append("- Taux de resolution : ").append(String.format("%.1f%%", tauxResolution)).append("\n\n");
        report.append("PERFORMANCE PAR ZONE\n");
        currentWeeklyReport.performanceParZone.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> report.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" signalements\n"));
        report.append("\nCATEGORIES LES PLUS SIGNALEE\n");
        currentWeeklyReport.categoriesPopulaires.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> report.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(" signalements\n"));
        report.append("\nTOP AGENTS ACTIFS\n");
        currentWeeklyReport.agentsActifs.stream()
            .limit(5)
            .forEach(agent -> report.append("- ").append(agent.nom)
                .append(" : ").append(agent.missionsTerminees).append("/").append(agent.missionsTotal)
                .append(" missions (").append(String.format("%.1f%%", agent.tauxReussite)).append(")\n"));
        reportTextArea.setText(report.toString());
    }

    private void generateMonthlyReport() {
        LocalDate date = endDatePicker.getValue();
        if (date == null) date = LocalDate.now();
        currentMonthlyReport = reportService.generateMonthlyReport(date.getMonthValue(), date.getYear());

        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Signalements par jour");
        for (Map.Entry<String, Integer> entry : currentMonthlyReport.evolutionQuotidienne.entrySet()) {
            String dayOfMonth = entry.getKey().substring(8);
            trendSeries.getData().add(new XYChart.Data<>(dayOfMonth, entry.getValue()));
        }
        trendLineChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(trendSeries));

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Mois", date.getMonth().name() + " " + date.getYear()));
        stats.add(new StatisticRow("Total Signalements", String.valueOf(currentMonthlyReport.totalSignalements)));
        stats.add(new StatisticRow("Taux de R\u00e9solution", String.format("%.1f%%", currentMonthlyReport.tauxResolution)));
        stats.add(new StatisticRow("Temps Traitement Moyen", String.format("%.1f heures", currentMonthlyReport.tempsTraitementMoyen)));
        statsTable.setItems(stats);

        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT MENSUEL ===\n");
        report.append("Mois : ").append(date.getMonth().name()).append(" ").append(date.getYear()).append("\n\n");
        report.append("INDICATEURS CLES\n");
        report.append("- Signalements traites : ").append(currentMonthlyReport.totalSignalements).append("\n");
        report.append("- Taux de resolution : ").append(String.format("%.1f%%", currentMonthlyReport.tauxResolution)).append("\n");
        report.append("- Temps de traitement moyen : ").append(String.format("%.1f heures", currentMonthlyReport.tempsTraitementMoyen)).append("\n\n");
        report.append("ANALYSE GEOGRAPHIQUE\n");
        report.append("- Zone la plus active : ").append(currentMonthlyReport.zoneLaPlusActive).append("\n");
        if (currentMonthlyReport.performanceParZone != null) {
            currentMonthlyReport.performanceParZone.forEach((zone, count) ->
                report.append("  - ").append(zone).append(" : ").append(count).append(" signalements\n"));
        }
        report.append("\nPERFORMANCE DES AGENTS\n");
        currentMonthlyReport.performanceAgents.stream()
            .limit(10)
            .forEach(agent -> report.append("- ").append(agent.nom)
                .append(" : ").append(agent.missionsTerminees).append(" missions terminees")
                .append(" (").append(String.format("%.1f%%", agent.tauxReussite)).append(" de reussite)\n"));
        reportTextArea.setText(report.toString());
    }

    private void generateAgentPerformanceReport() {
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        ReportService.WeeklyReport weeklyReport = reportService.generateWeeklyReport(startDate);

        XYChart.Series<String, Number> agentSeries = new XYChart.Series<>();
        agentSeries.setName("Missions Terminees");
        weeklyReport.agentsActifs.stream()
            .limit(10)
            .forEach(agent -> agentSeries.getData().add(new XYChart.Data<>(
                agent.nom.length() > 10 ? agent.nom.substring(0, 10) + "..." : agent.nom,
                agent.missionsTerminees)));
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(agentSeries));

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Nombre d'agents actifs", String.valueOf(weeklyReport.agentsActifs.size())));
        if (!weeklyReport.agentsActifs.isEmpty()) {
            double tauxMoyen = weeklyReport.agentsActifs.stream().mapToDouble(a -> a.tauxReussite).average().orElse(0);
            int totalMissions = weeklyReport.agentsActifs.stream().mapToInt(a -> a.missionsTotal).sum();
            stats.add(new StatisticRow("Taux de reussite moyen", String.format("%.1f%%", tauxMoyen)));
            stats.add(new StatisticRow("Total missions assignees", String.valueOf(totalMissions)));
            ReportService.AgentStats meilleur = weeklyReport.agentsActifs.stream()
                .max((a, b) -> Double.compare(a.tauxReussite, b.tauxReussite))
                .orElse(null);
            if (meilleur != null) {
                stats.add(new StatisticRow("Meilleur agent", meilleur.nom + " (" + String.format("%.1f%%", meilleur.tauxReussite) + ")"));
            }
        }
        statsTable.setItems(stats);

        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT PERFORMANCE AGENTS ===\n");
        report.append("Periode : ").append(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
              .append(" - ").append(startDate.plusDays(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        report.append("CLASSEMENT DES AGENTS\n");
        final LocalDate finalStartDate = startDate;
        weeklyReport.agentsActifs.stream()
            .sorted((a, b) -> Double.compare(b.tauxReussite, a.tauxReussite))
            .forEach(agent -> {
                report.append(agent.nom).append("\n");
                report.append("  - Missions : ").append(agent.missionsTerminees).append("/").append(agent.missionsTotal).append("\n");
                report.append("  - Taux de reussite : ").append(String.format("%.1f%%", agent.tauxReussite)).append("\n");
                report.append("  - Email : ").append(agent.email).append("\n\n");
            });
        reportTextArea.setText(report.toString());
    }

    private void generateGeographicAnalysis() {
        ReportService.RealTimeDashboard dashboard = reportService.getRealTimeDashboard();

        XYChart.Series<String, Number> zoneSeries = new XYChart.Series<>();
        zoneSeries.setName("Activite par Zone");
        dashboard.zonesActives.forEach((zone, count) ->
            zoneSeries.getData().add(new XYChart.Data<>(zone, count)));
        zoneBarChart.setData(FXCollections.<XYChart.Series<String, Number>>observableArrayList(zoneSeries));

        ObservableList<StatisticRow> stats = FXCollections.observableArrayList();
        stats.add(new StatisticRow("Nombre de zones", String.valueOf(dashboard.zonesActives.size())));
        String zonePlusActive = dashboard.zonesActives.entrySet().stream()
            .max(Map.Entry.<String, Integer>comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucune");
        stats.add(new StatisticRow("Zone la plus active", zonePlusActive));
        int totalSignalements = dashboard.zonesActives.values().stream().mapToInt(Integer::intValue).sum();
        stats.add(new StatisticRow("Total signalements", String.valueOf(totalSignalements)));
        if (totalSignalements > 0) {
            dashboard.zonesActives.forEach((zone, count) -> {
                double pourcentage = (double) count / totalSignalements * 100;
                stats.add(new StatisticRow(zone, String.format("%d (%.1f%%)", count, pourcentage)));
            });
        }
        statsTable.setItems(stats);

        StringBuilder report = new StringBuilder();
        report.append("=== ANALYSE GEOGRAPHIQUE ===\n");
        report.append("Genere le : ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        report.append("REPARTITION PAR ZONE\n");
        dashboard.zonesActives.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                double pourcentage = totalSignalements > 0 ? (double) entry.getValue() / totalSignalements * 100 : 0;
                report.append("- ").append(entry.getKey())
                      .append(" : ").append(entry.getValue()).append(" signalements")
                      .append(" (").append(String.format("%.1f%%", pourcentage)).append(")\n");
            });
        report.append("\nRECOMMANDATIONS\n");
        if (!dashboard.zonesActives.isEmpty()) {
            String zoneMax = dashboard.zonesActives.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
            report.append("- Renforcer la presence des agents dans la zone ").append(zoneMax).append("\n");
            report.append("- Sensibiliser davantage les citoyens dans les zones moins actives\n");
            report.append("- Optimiser les itineraires de collecte entre les zones\n");
        }
        reportTextArea.setText(report.toString());
    }

    @FXML
    private void exportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport");
        fileChooser.setInitialFileName("rapport_smartcity_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(reportTextArea.getText());
                showAlert("Export reussi", "Le rapport a ete exporte vers : " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Erreur d'export", "Impossible d'exporter le rapport : " + e.getMessage());
            }
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
