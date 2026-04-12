package com.smartcity.service;

import com.smartcity.model.Signalement;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.stream.Collectors;

public class RealTimeGPSService {

    private volatile Coordinates currentAgentPosition;
    private Timer locationTracker;
    private final List<LocationUpdateListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private List<Signalement> allMissions = new ArrayList<>();

    public interface LocationUpdateListener {
        void onLocationUpdate(Coordinates newPosition);

        void onMissionsDistanceUpdate(List<MissionWithDistance> missionsWithDistances);
    }

    public static class MissionWithDistance {
        public final Signalement mission;
        public final double distanceKm;
        public final int estimatedMinutes;
        public final boolean isNearby;
        public final boolean isClosest;
        public final String directionText;

        public MissionWithDistance(Signalement mission, double distanceKm, boolean isClosest) {
            this.mission = mission;
            this.distanceKm = distanceKm;
            this.estimatedMinutes = (int) Math.ceil(distanceKm * 4);
            this.isNearby = distanceKm < 1.0;
            this.isClosest = isClosest;
            this.directionText = formatDirection(distanceKm);
        }

        private String formatDirection(double km) {
            if (km < 0.1)
                return "Très proche (< 100m)";
            if (km < 0.5)
                return "À proximité (" + Math.round(km * 1000) + "m)";
            if (km < 1.0)
                return "À pied (" + String.format("%.1f", km) + "km)";
            if (km < 3.0)
                return "En voiture (" + String.format("%.1f", km) + "km)";
            return "Plus éloigné (" + String.format("%.1f", km) + "km)";
        }
    }

    public static class Coordinates {
        public final double lat;
        public final double lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }


    public void startRealTimeTracking() {
        if (locationTracker != null)
            locationTracker.cancel();
        locationTracker = new Timer("gps-tracker", true);
        locationTracker.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                pollPositionFromServer();
            }
        }, 0, 10_000);
    }

    private void pollPositionFromServer() {
        com.smartcity.utils.SessionManager.getAgentId().ifPresent(agentId -> {
            double[] pos = readPositionFromDb(agentId);
            if (pos != null) {
                setCurrentPosition(new Coordinates(pos[0], pos[1]));
            }
        });
    }

    private double[] readPositionFromDb(int agentId) {
        try (java.sql.Connection conn = com.smartcity.utils.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT latitude, longitude FROM position_agent WHERE idAgent=?")) {
            ps.setInt(1, agentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new double[]{rs.getDouble(1), rs.getDouble(2)} : null;
            }
        } catch (java.sql.SQLException e) {
            return null;
        }
    }

    public void stopRealTimeTracking() {
        if (locationTracker != null) {
            locationTracker.cancel();
            locationTracker = null;
        }
        listeners.clear();
        currentAgentPosition = null;
    }

    public void setCurrentPosition(Coordinates position) {
        this.currentAgentPosition = position;
        notifyListeners(position);
        updateMissionDistances();
    }

    public Coordinates getCurrentPosition() {
        return currentAgentPosition;
    }

    public void updateMissions(List<Signalement> missions) {
        this.allMissions = new ArrayList<>(missions);
        updateMissionDistances();
    }

    public List<MissionWithDistance> calculateMissionsDistances() {
        if (currentAgentPosition == null || allMissions.isEmpty())
            return Collections.emptyList();

        List<MissionWithDistance> result = new ArrayList<>();
        double closestDistance = Double.MAX_VALUE;

        for (Signalement mission : allMissions) {
            // Ignorer les missions sans coordonnées GPS valides
            if (mission.getLatitude() == 0.0 && mission.getLongitude() == 0.0) continue;
            double distance = GeolocationService.distanceBetween(currentAgentPosition.lat, currentAgentPosition.lon,
                    mission.getLatitude(), mission.getLongitude());
            if (distance < closestDistance)
                closestDistance = distance;
            result.add(new MissionWithDistance(mission, distance, false));
        }

        if (result.isEmpty()) return Collections.emptyList();
        final double closest = closestDistance;
        return result.stream()
                .map(mwd -> new MissionWithDistance(mwd.mission, mwd.distanceKm, Math.abs(mwd.distanceKm - closest) < 0.001))
                .sorted(Comparator.comparingDouble(mwd -> mwd.distanceKm))
                .collect(Collectors.toList());
    }

    public Optional<Signalement> findClosestMission() {
        return calculateMissionsDistances().stream().filter(mwd -> mwd.isClosest).map(mwd -> mwd.mission).findFirst();
    }

    public String getNavigationUrl(Signalement destination) {
        if (currentAgentPosition == null)
            return String.format("https://www.google.com/maps/search/%.6f,%.6f", destination.getLatitude(),
                    destination.getLongitude());
        return String.format("https://www.google.com/maps/dir/%.6f,%.6f/%.6f,%.6f", currentAgentPosition.lat,
                currentAgentPosition.lon, destination.getLatitude(), destination.getLongitude());
    }

    public void addLocationUpdateListener(LocationUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeLocationUpdateListener(LocationUpdateListener listener) {
        listeners.remove(listener);
    }

    public GeolocationService.Coordinates getZoneCenter(String zone) {
        return new GeolocationService().getZoneCenter(zone);
    }

    public void setManualPosition(double lat, double lon) {
        setCurrentPosition(new Coordinates(lat, lon));
    }

    private void notifyListeners(Coordinates position) {
        Platform.runLater(() -> listeners.forEach(l -> l.onLocationUpdate(position)));
    }

    private void updateMissionDistances() {
        List<MissionWithDistance> distances = calculateMissionsDistances();
        Platform.runLater(() -> listeners.forEach(l -> l.onMissionsDistanceUpdate(distances)));
    }
}

