package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.model.SignalementStatut;
import com.smartcity.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central business engine for KPI thresholds and terrain anomalies.
 * This service is the single source of truth for dashboard KPI and alert rules.
 */
public class AnalyticsRulesService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsRulesService.class);

    public static final int THRESHOLD_GREEN_HOURS = 24;
    public static final int THRESHOLD_ORANGE_HOURS = 72;
    public static final int THRESHOLD_CRITICAL_HOURS = 72;
    public static final int THRESHOLD_NON_ASSIGNED_HOURS = 12;
    public static final int THRESHOLD_BLOCKED_HOURS = 24;
    public static final int THRESHOLD_GPS_MISSING_MINUTES = 5;
    public static final int THRESHOLD_AGENT_INACTIVE_MINUTES = 60;

    private final SignalementService signalementService = new SignalementService();
    private final AffectationService affectationService = new AffectationService();
    private final SuiviService suiviService = new SuiviService();

    public AnalysisSnapshot computeAnalysisSnapshot() {
        LocalDateTime now = LocalDateTime.now();
        List<Signalement> all = signalementService.getAllSignalements();
        Map<Integer, Boolean> assignedBySignalement = loadAssignedSignalementMap();
        Map<Integer, Long> lastStatusAgeHours = loadLastStatusAgeHours();
        List<AffectationService.AgentLiveStatus> agents = affectationService.getAgentLiveStatuses();

        int total = 0;
        int enAttente = 0;
        int enCours = 0;
        int traitees = 0;
        int retardsCritiques = 0;
        int nonAssignees12h = 0;
        int missionsBloquees24h = 0;

        List<AlertItem> alerts = new ArrayList<>();

        for (Signalement s : all) {
            SignalementStatut st = SignalementStatut.fromAny(s.getStatut());
            if (st == null) {
                continue;
            }
            total++;
            if (st == SignalementStatut.EN_ATTENTE) enAttente++;
            if (st == SignalementStatut.EN_COURS) enCours++;
            if (st == SignalementStatut.TERMINE) traitees++;

            if (st != SignalementStatut.TERMINE && s.getDateSignalement() != null) {
                long ageHours = Math.max(0, ChronoUnit.HOURS.between(s.getDateSignalement(), now));
                if (ageHours > THRESHOLD_CRITICAL_HOURS) {
                    retardsCritiques++;
                }

                boolean assigned = assignedBySignalement.getOrDefault(s.getIdSignalement(), false);
                if (!assigned && ageHours > THRESHOLD_NON_ASSIGNED_HOURS) {
                    nonAssignees12h++;
                    alerts.add(new AlertItem(
                        "MISSION_NON_ASSIGNEE_" + s.getIdSignalement(),
                        AlertSeverity.MAJEUR,
                        "Mission non assignee > 12h",
                        "Mission #" + s.getIdSignalement() + " (" + safeText(s.getZoneNom()) + ")"
                    ));
                }
            }

            if (st != SignalementStatut.TERMINE) {
                long ageSinceStatusChange = lastStatusAgeHours.getOrDefault(s.getIdSignalement(), 0L);
                if (ageSinceStatusChange > THRESHOLD_BLOCKED_HOURS) {
                    missionsBloquees24h++;
                    alerts.add(new AlertItem(
                        "MISSION_BLOQUEE_" + s.getIdSignalement(),
                        AlertSeverity.MAJEUR,
                        "Mission bloquee > 24h",
                        "Mission #" + s.getIdSignalement() + " sans changement de statut"
                    ));
                }
            }
        }

        int active = enAttente + enCours;
        double tauxTraitementGlobal = total > 0 ? (traitees * 100.0 / total) : 0.0;

        int gpsMissing = 0;
        int agentsInactifs = 0;
        for (AffectationService.AgentLiveStatus a : agents) {
            boolean enService = (a.missionsActives != null && a.missionsActives > 0)
                || (a.missionsEnCours != null && a.missionsEnCours > 0);
            if (!enService) {
                continue;
            }
            if (a.dernierePosition == null || Duration.between(a.dernierePosition, now).toMinutes() > THRESHOLD_GPS_MISSING_MINUTES) {
                gpsMissing++;
                alerts.add(new AlertItem(
                    "GPS_PERDU_" + a.idAgent,
                    AlertSeverity.CRITIQUE,
                    "GPS non recu > 5 min",
                    safeText(a.nomAgent) + " (" + safeText(a.zoneNom) + ")"
                ));
            }
            if (a.dernierePosition == null || Duration.between(a.dernierePosition, now).toMinutes() > THRESHOLD_AGENT_INACTIVE_MINUTES) {
                agentsInactifs++;
                alerts.add(new AlertItem(
                    "AGENT_INACTIF_" + a.idAgent,
                    AlertSeverity.MAJEUR,
                    "Agent inactif > 1h en service",
                    safeText(a.nomAgent) + " (" + safeText(a.zoneNom) + ")"
                ));
            }
        }

        if (retardsCritiques > 0) {
            alerts.add(new AlertItem(
                "RETARDS_CRITIQUES_GLOBAL",
                AlertSeverity.CRITIQUE,
                "Retards critiques globaux",
                retardsCritiques + " mission(s) depassent 72h"
            ));
        }
        if (active >= 20) {
            alerts.add(new AlertItem(
                "ZONE_SATUREE_GLOBAL",
                AlertSeverity.MAJEUR,
                "Saturation operationnelle",
                "Charge active elevee: " + active + " mission(s)"
            ));
        }

        alerts.sort(Comparator
            .comparing((AlertItem a) -> a.severity.rank)
            .thenComparing(a -> a.title));
        if (alerts.size() > 5) {
            alerts = new ArrayList<>(alerts.subList(0, 5));
        }

        AnalysisSnapshot snapshot = new AnalysisSnapshot();
        snapshot.generatedAt = now;
        snapshot.missionsTotal = total;
        snapshot.missionsEnAttente = enAttente;
        snapshot.missionsEnCours = enCours;
        snapshot.missionsTraitees = traitees;
        snapshot.chargeOperationnelleActive = active;
        snapshot.retardsCritiquesGlobaux = retardsCritiques;
        snapshot.tauxTraitementGlobal = tauxTraitementGlobal;
        snapshot.missionsNonAssignees12h = nonAssignees12h;
        snapshot.missionsBloquees24h = missionsBloquees24h;
        snapshot.gpsMissing5min = gpsMissing;
        snapshot.agentsInactifs1h = agentsInactifs;
        snapshot.alerts = alerts;
        return snapshot;
    }

    private Map<Integer, Boolean> loadAssignedSignalementMap() {
        Map<Integer, Boolean> out = new HashMap<>();
        String query = "SELECT idSignalement FROM Affectation";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.put(rs.getInt("idSignalement"), true);
            }
        } catch (Exception e) {
            logger.warn("Unable to load assignment map: {}", e.getMessage());
        }
        return out;
    }

    private Map<Integer, Long> loadLastStatusAgeHours() {
        // Ensure historique schema exists before querying.
        suiviService.getKpiParZone();

        Map<Integer, Long> out = new HashMap<>();
        String query = """
            SELECT s.idSignalement,
                   TIMESTAMPDIFF(HOUR, COALESCE(MAX(h.dateChangement), s.dateSignalement), NOW()) AS ageHours
            FROM Signalement s
            LEFT JOIN HistoriqueStatut h ON h.idSignalement = s.idSignalement
            GROUP BY s.idSignalement, s.dateSignalement
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.put(rs.getInt("idSignalement"), rs.getLong("ageHours"));
            }
        } catch (Exception e) {
            logger.warn("Unable to load status history ages: {}", e.getMessage());
        }
        return out;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public enum AlertSeverity {
        CRITIQUE(0),
        MAJEUR(1),
        MINEUR(2);

        final int rank;

        AlertSeverity(int rank) {
            this.rank = rank;
        }
    }

    public static class AlertItem {
        public final String key;
        public final AlertSeverity severity;
        public final String title;
        public final String detail;
        public final LocalDateTime createdAt;

        public AlertItem(String key, AlertSeverity severity, String title, String detail) {
            this.key = key;
            this.severity = severity;
            this.title = title;
            this.detail = detail;
            this.createdAt = LocalDateTime.now();
        }
    }

    public static class AnalysisSnapshot {
        public LocalDateTime generatedAt;
        public int missionsTotal;
        public int missionsEnAttente;
        public int missionsEnCours;
        public int missionsTraitees;
        public int chargeOperationnelleActive;
        public int retardsCritiquesGlobaux;
        public double tauxTraitementGlobal;
        public int missionsNonAssignees12h;
        public int missionsBloquees24h;
        public int gpsMissing5min;
        public int agentsInactifs1h;
        public List<AlertItem> alerts = new ArrayList<>();

        public String refreshLabel() {
            return generatedAt == null ? "" : generatedAt.toString();
        }
    }
}
