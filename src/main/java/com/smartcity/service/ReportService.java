package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service pour les rapports et analyses.
 */
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public WeeklyReport generateWeeklyReport(LocalDate startDate) {
        if (startDate == null) throw new IllegalArgumentException("startDate ne peut pas etre null");
        return generateWeeklyReport(startDate, startDate.plusDays(6), null);
    }

    public WeeklyReport generateWeeklyReport(LocalDate startDate, LocalDate endDate) {
        return generateWeeklyReport(startDate, endDate, null);
    }

    public WeeklyReport generateWeeklyReport(LocalDate startDate, LocalDate endDate, String zoneScope) {
        DateRange range = normalizeRange(startDate, endDate);

        WeeklyReport report = new WeeklyReport();
        report.startDate = range.start;
        report.endDate = range.end;
        report.zoneScope = zoneScope;

        report.totalSignalements = countSignalementsByDateRange(range.start, range.end, zoneScope);
        report.signalementsResolus = countSignalementsByDateRangeAndStatus(range.start, range.end, SignalementStatut.TERMINE.dbValue(), zoneScope);
        report.signalementsEnCours = countSignalementsByDateRangeAndStatus(range.start, range.end, SignalementStatut.EN_COURS.dbValue(), zoneScope);
        report.signalementsEnAttente = countSignalementsByDateRangeAndStatus(range.start, range.end, SignalementStatut.EN_ATTENTE.dbValue(), zoneScope);

        report.performanceParZone = getPerformanceByZone(range.start, range.end, zoneScope);
        report.categoriesPopulaires = getTopCategories(range.start, range.end, zoneScope);
        report.agentsActifs = getTopActiveAgents(range.start, range.end, zoneScope);

        return report;
    }

    public MonthlyReport generateMonthlyReport(int month, int year) {
        if (month < 1 || month > 12) throw new IllegalArgumentException("Mois invalide: " + month);
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Annee invalide: " + year);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return generateMonthlyReport(startDate, endDate, null);
    }

    public MonthlyReport generateMonthlyReport(LocalDate startDate, LocalDate endDate) {
        return generateMonthlyReport(startDate, endDate, null);
    }

    public MonthlyReport generateMonthlyReport(LocalDate startDate, LocalDate endDate, String zoneScope) {
        DateRange range = normalizeRange(startDate, endDate);

        MonthlyReport report = new MonthlyReport();
        report.month = range.start.getMonthValue();
        report.year = range.start.getYear();
        report.startDate = range.start;
        report.endDate = range.end;
        report.zoneScope = zoneScope;

        report.totalSignalements = countSignalementsByDateRange(range.start, range.end, zoneScope);
        report.tauxResolution = calculateResolutionRate(range.start, range.end, zoneScope);
        report.tempsTraitementMoyen = calculateAverageProcessingTime(range.start, range.end, zoneScope);

        report.evolutionQuotidienne = getDailyEvolution(range.start, range.end, zoneScope);
        report.performanceAgents = getAgentPerformanceDetailed(range.start, range.end, zoneScope);

        Map<String, Integer> perfParZone = getPerformanceByZone(range.start, range.end, zoneScope);
        report.performanceParZone = perfParZone;
        report.zoneLaPlusActive = perfParZone.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucune");

        return report;
    }

    public RealTimeDashboard getRealTimeDashboard() {
        return getRealTimeDashboard(null);
    }

    public RealTimeDashboard getRealTimeDashboard(String zoneScope) {
        RealTimeDashboard dashboard = new RealTimeDashboard();
        dashboard.timestamp = LocalDateTime.now();
        dashboard.zoneScope = zoneScope;

        dashboard.totalSignalements = countSignalementsByDateRange(LocalDate.of(2000, 1, 1), LocalDate.now(), zoneScope);
        dashboard.signalementsAujourdhui = countSignalementsByDateRange(LocalDate.now(), LocalDate.now(), zoneScope);
        dashboard.utilisateursActifs = countActiveUsers();

        dashboard.signalementsUrgents = getUrgentSignalements(zoneScope);
        dashboard.zonesActives = getMostActiveZones(7, zoneScope);
        dashboard.tendances = calculateTrends(zoneScope);

        return dashboard;
    }

    public boolean hasAtLeastOneYearHistory() {
        String query = "SELECT MIN(DATE(dateSignalement)) FROM Signalement";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                Date minDate = rs.getDate(1);
                return minDate != null && !minDate.toLocalDate().isAfter(LocalDate.now().minusYears(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur verification historisation", e);
        }
        return false;
    }

    private DateRange normalizeRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de debut et de fin sont obligatoires");
        }
        if (endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }
        return new DateRange(startDate, endDate);
    }

    private int countSignalementsByDateRange(LocalDate start, LocalDate end, String zoneScope) {
        String query = "SELECT COUNT(*) FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
            return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end), zoneScope);
        }
        return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end));
    }

    private int countSignalementsByDateRangeAndStatus(LocalDate start, LocalDate end, String status, String zoneScope) {
        String query = "SELECT COUNT(*) FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? AND s.statut = ? ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
            return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end), status, zoneScope);
        }
        return executeCountQuery(query, Date.valueOf(start), Date.valueOf(end), status);
    }

    private double calculateResolutionRate(LocalDate start, LocalDate end, String zoneScope) {
        int total = countSignalementsByDateRange(start, end, zoneScope);
        int resolved = countSignalementsByDateRangeAndStatus(start, end, SignalementStatut.TERMINE.dbValue(), zoneScope);
        return total > 0 ? (double) resolved / total * 100 : 0.0;
    }

    private double calculateAverageProcessingTime(LocalDate start, LocalDate end, String zoneScope) {
        String query = "SELECT AVG(TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte)) as avgTime "
            + "FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "JOIN Affectation a ON s.idSignalement = a.idSignalement "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? "
            + "AND s.statut = ? AND a.dateCollecte IS NOT NULL ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int i = 1;
            pstmt.setDate(i++, Date.valueOf(start));
            pstmt.setDate(i++, Date.valueOf(end));
            pstmt.setString(i++, SignalementStatut.TERMINE.dbValue());
            if (hasZone(zoneScope)) {
                pstmt.setString(i++, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("avgTime");
            }
        } catch (SQLException e) {
            logger.error("Erreur calcul temps moyen", e);
        }
        return 0.0;
    }

    private Map<String, Integer> getPerformanceByZone(LocalDate start, LocalDate end, String zoneScope) {
        Map<String, Integer> performance = new HashMap<>();
        String query = "SELECT z.nomZone, COUNT(*) as count "
            + "FROM Signalement s JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        query += "GROUP BY z.nomZone ORDER BY count DESC";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int i = 1;
            pstmt.setDate(i++, Date.valueOf(start));
            pstmt.setDate(i++, Date.valueOf(end));
            if (hasZone(zoneScope)) {
                pstmt.setString(i++, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) performance.put(rs.getString("nomZone"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur performance par zone", e);
        }
        return performance;
    }

    private Map<String, Integer> getTopCategories(LocalDate start, LocalDate end, String zoneScope) {
        Map<String, Integer> categories = new HashMap<>();
        String query = "SELECT s.categorie, COUNT(*) as count FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        query += "GROUP BY s.categorie ORDER BY count DESC LIMIT 5";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int i = 1;
            pstmt.setDate(i++, Date.valueOf(start));
            pstmt.setDate(i++, Date.valueOf(end));
            if (hasZone(zoneScope)) {
                pstmt.setString(i++, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) categories.put(rs.getString("categorie"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Erreur top categories", e);
        }
        return categories;
    }

    private List<AgentStats> getTopActiveAgents(LocalDate start, LocalDate end, String zoneScope) {
        List<AgentStats> agents = new ArrayList<>();
        String query = "SELECT u.nom, u.email, z.nomZone AS zoneNom, "
            + "COUNT(a.idAffectation) as missions, "
            + "SUM(CASE WHEN s.statut = ? THEN 1 ELSE 0 END) as terminees, "
            + "SUM(CASE WHEN a.dateAffectation IS NOT NULL "
            + "THEN GREATEST(TIMESTAMPDIFF(HOUR, a.dateAffectation, COALESCE(a.dateCollecte, NOW())), 0) ELSE 0 END) AS heuresActives "
            + "FROM Utilisateur u "
            + "LEFT JOIN Affectation a ON u.idUser = a.idAgent "
            + "LEFT JOIN Signalement s ON a.idSignalement = s.idSignalement "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE u.role = 'Agent' AND u.actif = 1 "
            + "AND (a.idAffectation IS NULL OR DATE(s.dateSignalement) BETWEEN ? AND ?) ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        query += "GROUP BY u.idUser, u.nom, u.email, z.nomZone "
            + "ORDER BY missions DESC, terminees DESC LIMIT 10";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int i = 1;
            pstmt.setString(i++, SignalementStatut.TERMINE.dbValue());
            pstmt.setDate(i++, Date.valueOf(start));
            pstmt.setDate(i++, Date.valueOf(end));
            if (hasZone(zoneScope)) {
                pstmt.setString(i++, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AgentStats stats = new AgentStats();
                    stats.nom = rs.getString("nom");
                    stats.email = rs.getString("email");
                    stats.zoneNom = rs.getString("zoneNom");
                    stats.missionsTotal = rs.getInt("missions");
                    stats.missionsTerminees = rs.getInt("terminees");
                    stats.tempsActifHeures = rs.getDouble("heuresActives");
                    stats.tauxReussite = stats.missionsTotal > 0
                        ? (double) stats.missionsTerminees / stats.missionsTotal * 100
                        : 0.0;
                    stats.efficaciteZone = stats.tempsActifHeures > 0
                        ? stats.missionsTerminees / stats.tempsActifHeures
                        : 0.0;
                    agents.add(stats);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur agents actifs", e);
        }
        return agents;
    }

    private int countActiveUsers() {
        String query = "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1";
        return executeCountQuery(query);
    }

    private List<Signalement> getUrgentSignalements(String zoneScope) {
        String query = "SELECT s.*, z.nomZone, u.nom as utilisateurNom "
            + "FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "LEFT JOIN Utilisateur u ON s.idUser = u.idUser "
            + "WHERE s.statut = 'En attente' "
            + "AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR) ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        query += "ORDER BY s.dateSignalement ASC LIMIT 10";

        List<Signalement> urgents = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (hasZone(zoneScope)) {
                pstmt.setString(1, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) urgents.add(mapResultSetToSignalement(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur signalements urgents", e);
        }
        return urgents;
    }

    private Map<String, Integer> getMostActiveZones(int days, String zoneScope) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return getPerformanceByZone(startDate, LocalDate.now(), zoneScope);
    }

    private Map<String, Double> calculateTrends(String zoneScope) {
        Map<String, Double> trends = new HashMap<>();

        LocalDate thisWeekStart = LocalDate.now().minusDays(6);
        LocalDate lastWeekStart = LocalDate.now().minusDays(13);
        LocalDate lastWeekEnd = LocalDate.now().minusDays(7);

        int thisWeek = countSignalementsByDateRange(thisWeekStart, LocalDate.now(), zoneScope);
        int lastWeek = countSignalementsByDateRange(lastWeekStart, lastWeekEnd, zoneScope);

        double weeklyTrend = lastWeek > 0 ? ((double) (thisWeek - lastWeek) / lastWeek) * 100 : 0.0;
        trends.put("hebdomadaire", weeklyTrend);
        return trends;
    }

    private Map<String, Integer> getDailyEvolution(LocalDate start, LocalDate end, String zoneScope) {
        Map<String, Integer> evolution = new LinkedHashMap<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            evolution.put(cur.toString(), 0);
            cur = cur.plusDays(1);
        }

        String query = "SELECT DATE(s.dateSignalement) as jour, COUNT(*) as cnt FROM Signalement s "
            + "LEFT JOIN Zone z ON s.idZone = z.idZone "
            + "WHERE DATE(s.dateSignalement) BETWEEN ? AND ? ";
        if (hasZone(zoneScope)) {
            query += "AND z.nomZone = ? ";
        }
        query += "GROUP BY DATE(s.dateSignalement)";

        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int i = 1;
            pstmt.setDate(i++, Date.valueOf(start));
            pstmt.setDate(i++, Date.valueOf(end));
            if (hasZone(zoneScope)) {
                pstmt.setString(i++, zoneScope);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evolution.put(rs.getString("jour"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur getDailyEvolution", e);
        }
        return evolution;
    }

    private List<AgentStats> getAgentPerformanceDetailed(LocalDate start, LocalDate end, String zoneScope) {
        if (start == null || end == null || start.isAfter(end)) return new ArrayList<>();
        return getTopActiveAgents(start, end, zoneScope);
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
            logger.error("Erreur requete de comptage", e);
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
        } catch (SQLException ignored) {
        }

        Timestamp timestamp = rs.getTimestamp("dateSignalement");
        if (timestamp != null) {
            s.setDateSignalement(timestamp.toLocalDateTime());
        }

        return s;
    }

    private boolean hasZone(String zoneScope) {
        return zoneScope != null && !zoneScope.isBlank();
    }

    private static class DateRange {
        final LocalDate start;
        final LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }

    public static class WeeklyReport {
        public LocalDate startDate;
        public LocalDate endDate;
        public String zoneScope;
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
        public String zoneScope;
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
        public String zoneScope;
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
        public String zoneNom;
        public int missionsTotal;
        public int missionsTerminees;
        public double tauxReussite;
        public double tempsActifHeures;
        public double efficaciteZone;
    }
}
