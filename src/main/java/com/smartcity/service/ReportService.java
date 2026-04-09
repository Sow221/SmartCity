package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.model.Utilisateur;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Service pour les rapports et analyses
 */
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private final SignalementService signalementService = new SignalementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ZoneService zoneService = new ZoneService();

    /**
     * Rapport hebdomadaire
     */
    public WeeklyReport generateWeeklyReport(LocalDate startDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate ne peut pas être null");
        LocalDate endDate = startDate.plusDays(6);
        
        WeeklyReport report = new WeeklyReport();
        report.startDate = startDate;
        report.endDate = endDate;
        
        // Statistiques générales
        report.totalSignalements = countSignalementsByDateRange(startDate, endDate);
        report.signalementsResolus = countSignalementsByDateRangeAndStatus(startDate, endDate, SignalementStatut.TERMINE.dbValue());
        report.signalementsEnCours = countSignalementsByDateRangeAndStatus(startDate, endDate, SignalementStatut.EN_COURS.dbValue());
        report.signalementsEnAttente = countSignalementsByDateRangeAndStatus(startDate, endDate, SignalementStatut.EN_ATTENTE.dbValue());
        
        // Performances par zone
        report.performanceParZone = getPerformanceByZone(startDate, endDate);
        
        // Catégories les plus signalées
        report.categoriesPopulaires = getTopCategories(startDate, endDate);
        
        // Agents les plus actifs
        report.agentsActifs = getTopActiveAgents(startDate, endDate);
        
        return report;
    }

    /**
     * Rapport mensuel détaillé
     */
    public MonthlyReport generateMonthlyReport(int month, int year) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("Mois invalide: " + month);
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Année invalide: " + year);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        MonthlyReport report = new MonthlyReport();
        report.month = month;
        report.year = year;
        report.startDate = startDate;
        report.endDate = endDate;
        
        // Statistiques de base
        report.totalSignalements = countSignalementsByDateRange(startDate, endDate);
        report.tauxResolution = calculateResolutionRate(startDate, endDate);
        report.tempsTraitementMoyen = calculateAverageProcessingTime(startDate, endDate);
        
        // Évolution quotidienne
        report.evolutionQuotidienne = getDailyEvolution(startDate, endDate);
        
        // Performance des agents
        report.performanceAgents = getAgentPerformanceDetailed(startDate, endDate);
        
        // Analyse géographique
        Map<String, Integer> perfParZone = getPerformanceByZone(startDate, endDate);
        report.performanceParZone = perfParZone;
        report.zoneLaPlusActive = perfParZone.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucune");
        
        return report;
    }

    /**
     * Dashboard en temps réel
     */
    public RealTimeDashboard getRealTimeDashboard() {
        RealTimeDashboard dashboard = new RealTimeDashboard();
        dashboard.timestamp = LocalDateTime.now();
        
        // Statistiques actuelles
        dashboard.totalSignalements = signalementService.countAll();
        dashboard.signalementsAujourdhui = countSignalementsToday();
        dashboard.utilisateursActifs = countActiveUsers();
        
        // Signalements urgents
        dashboard.signalementsUrgents = getUrgentSignalements();
        
        // Zones les plus actives
        dashboard.zonesActives = getMostActiveZones(7); // 7 derniers jours
        
        // Tendances
        dashboard.tendances = calculateTrends();
        
        return dashboard;
    }

    // Méthodes utilitaires
    
    private int countSignalementsByDateRange(LocalDate start, LocalDate end) {
        String query = "SELECT COUNT(*) FROM Signalement WHERE DATE(dateSignalement) BETWEEN ? AND ?";
        return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end));
    }

    private int countSignalementsByDateRangeAndStatus(LocalDate start, LocalDate end, String status) {
        String query = "SELECT COUNT(*) FROM Signalement WHERE DATE(dateSignalement) BETWEEN ? AND ? AND statut = ?";
        return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end), status);
    }

    private double calculateResolutionRate(LocalDate start, LocalDate end) {
        int total = countSignalementsByDateRange(start, end);
        int resolved = countSignalementsByDateRangeAndStatus(start, end, SignalementStatut.TERMINE.dbValue());
        return total > 0 ? (double) resolved / total * 100 : 0.0;
    }

    private double calculateAverageProcessingTime(LocalDate start, LocalDate end) {
        String query = "SELECT AVG(TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte)) as avgTime "
            + "FROM Signalement s "
            + "JOIN Affectation a ON s.idSignalement = a.idSignalement "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? "
            + "AND s.statut IN ('Termine', 'Termin\u00e9') AND a.dateCollecte IS NOT NULL";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("avgTime");
            }
        } catch (SQLException e) {
            logger.error("Erreur calcul temps moyen", e);
        }
        return 0.0;
    }

    private Map<String, Integer> getPerformanceByZone(LocalDate start, LocalDate end) {
        Map<String, Integer> performance = new HashMap<>();
        String query = "SELECT z.nomZone, COUNT(*) as count "
            + "FROM Signalement s JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? "
            + "GROUP BY z.nomZone ORDER BY count DESC";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) performance.put(rs.getString("nomZone"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur performance par zone", e);
        }
        return performance;
    }

    private Map<String, Integer> getTopCategories(LocalDate start, LocalDate end) {
        Map<String, Integer> categories = new HashMap<>();
        String query = "SELECT categorie, COUNT(*) as count FROM Signalement "
            + "WHERE DATE(dateSignalement) BETWEEN ? AND ? "
            + "GROUP BY categorie ORDER BY count DESC LIMIT 5";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) categories.put(rs.getString("categorie"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur top catégories", e);
        }
        return categories;
    }

    private List<AgentStats> getTopActiveAgents(LocalDate start, LocalDate end) {
        List<AgentStats> agents = new ArrayList<>();
        String query = "SELECT u.nom, u.email, COUNT(a.idAffectation) as missions, "
            + "SUM(CASE WHEN s.statut IN ('Termine', 'Termin\u00e9', 'Collecte') THEN 1 ELSE 0 END) as terminees "
            + "FROM Utilisateur u "
            + "LEFT JOIN Affectation a ON u.idUser = a.idAgent "
            + "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement "
            + "WHERE u.role = 'Agent' AND DATE(a.dateAffectation) BETWEEN ? AND ? "
            + "GROUP BY u.idUser, u.nom, u.email "
            + "ORDER BY missions DESC, terminees DESC LIMIT 10";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AgentStats stats = new AgentStats();
                    stats.nom = rs.getString("nom");
                    stats.email = rs.getString("email");
                    stats.missionsTotal = rs.getInt("missions");
                    stats.missionsTerminees = rs.getInt("terminees");
                    stats.tauxReussite = stats.missionsTotal > 0 ?
                        (double) stats.missionsTerminees / stats.missionsTotal * 100 : 0.0;
                    agents.add(stats);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur agents actifs", e);
        }
        return agents;
    }

    private int countSignalementsToday() {
        return countSignalementsByDateRange(LocalDate.now(), LocalDate.now());
    }

    private int countActiveUsers() {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1";
        return executeCountQuery(query);
    }

    private List<Signalement> getUrgentSignalements() {
        String query = "SELECT s.*, z.nomZone, u.nom as utilisateurNom "
            + "FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
            + "WHERE s.statut = 'En attente' "
            + "AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR) "
            + "ORDER BY s.dateSignalement ASC LIMIT 10";
        List<Signalement> urgents = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) urgents.add(mapResultSetToSignalement(rs));
        } catch (SQLException e) {
            logger.error("Erreur signalements urgents", e);
        }
        return urgents;
    }

    private Map<String, Integer> getMostActiveZones(int days) {
        Map<String, Integer> zones = new HashMap<>();
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        return getPerformanceByZone(startDate, LocalDate.now());
    }

    private Map<String, Double> calculateTrends() {
        Map<String, Double> trends = new HashMap<>();
        
        // Tendance hebdomadaire (cette semaine vs semaine dernière)
        LocalDate thisWeekStart = LocalDate.now().minusDays(6);
        LocalDate lastWeekStart = LocalDate.now().minusDays(13);
        LocalDate lastWeekEnd = LocalDate.now().minusDays(7);
        
        int thisWeek = countSignalementsByDateRange(thisWeekStart, LocalDate.now());
        int lastWeek = countSignalementsByDateRange(lastWeekStart, lastWeekEnd);
        
        double weeklyTrend = lastWeek > 0 ? 
            ((double) (thisWeek - lastWeek) / lastWeek) * 100 : 0.0;
        
        trends.put("hebdomadaire", weeklyTrend);
        
        return trends;
    }

    private Map<String, Integer> getDailyEvolution(LocalDate start, LocalDate end) {
        Map<String, Integer> evolution = new LinkedHashMap<>();
        // Initialiser tous les jours à 0
        LocalDate cur = start;
        while (!cur.isAfter(end)) { evolution.put(cur.toString(), 0); cur = cur.plusDays(1); }
        // Une seule requête SQL GROUP BY
        String query = "SELECT DATE(dateSignalement) as jour, COUNT(*) as cnt FROM Signalement "
            + "WHERE DATE(dateSignalement) BETWEEN ? AND ? GROUP BY DATE(dateSignalement)";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) evolution.put(rs.getString("jour"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.error("Erreur getDailyEvolution", e);
        }
        return evolution;
    }

    private List<AgentStats> getAgentPerformanceDetailed(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) return new ArrayList<>();
        return getTopActiveAgents(start, end);
    }

    private Map<String, Object> getGeographicAnalysis(LocalDate start, LocalDate end) {
        return new HashMap<>(); // remplacé par zoneLaPlusActive + performanceParZone dans MonthlyReport
    }

    private int executeCountQuery(String query, Object... params) {
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur requête de comptage", e);
            return 0;
        }
    }

    private Signalement mapResultSetToSignalement(ResultSet rs) throws SQLException {
        Signalement s = new Signalement();
        s.setIdSignalement(rs.getInt("idSignalement"));
        s.setDescription(rs.getString("description"));
        s.setCategorie(rs.getString("categorie"));
        s.setIdZone(rs.getInt("idZone"));
        s.setStatut(SignalementStatut.toLabelOrSelf(rs.getString("statut")));
        s.setIdUser(rs.getInt("idUser"));
        
        try {
            s.setZoneNom(rs.getString("nomZone"));
            s.setUtilisateurNom(rs.getString("utilisateurNom"));
        } catch (SQLException e) {
            // Colonnes optionnelles
        }
        
        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null) {
            s.setDateSignalement(timestamp.toLocalDateTime());
        }
        
        return s;
    }

    // Classes de données pour les rapports
    
    public static class WeeklyReport {
        public LocalDate startDate;
        public LocalDate endDate;
        public int totalSignalements;
        public int signalementsResolus;
        public int signalementsEnCours;
        public int signalementsEnAttente;
        public Map<String, Integer> performanceParZone;
        public Map<String, Integer> categoriesPopulaires;
        public List<AgentStats> agentsActifs;
    }
    
    public static class MonthlyReport {
        public int month;
        public int year;
        public LocalDate startDate;
        public LocalDate endDate;
        public int totalSignalements;
        public double tauxResolution;
        public double tempsTraitementMoyen;
        public Map<String, Integer> evolutionQuotidienne;
        public List<AgentStats> performanceAgents;
        public String zoneLaPlusActive;
        public Map<String, Integer> performanceParZone;
    }
    
    public static class RealTimeDashboard {
        public LocalDateTime timestamp;
        public int totalSignalements;
        public int signalementsAujourdhui;
        public int utilisateursActifs;
        public List<Signalement> signalementsUrgents;
        public Map<String, Integer> zonesActives;
        public Map<String, Double> tendances;
    }
    
    public static class AgentStats {
        public String nom;
        public String email;
        public int missionsTotal;
        public int missionsTerminees;
        public double tauxReussite;
    }
}
