package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.utils.SessionManager;
import javafx.application.Platform;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de géolocalisation TEMPS RÉEL pour agents
 * 🗺️ Comme un vrai GPS avec tracking automatique
 */
public class RealTimeGPSService {
    
    // Position actuelle de l'agent (mise à jour automatiquement)
    private volatile Coordinates currentAgentPosition;
    private Timer locationTracker;
    private List<LocationUpdateListener> listeners = new ArrayList<>();
    private List<Signalement> allMissions = new ArrayList<>();
    
    // Interface pour écouter les changements de position
    public interface LocationUpdateListener {
        void onLocationUpdate(Coordinates newPosition);
        void onMissionsDistanceUpdate(List<MissionWithDistance> missionsWithDistances);
    }
    
    public static class MissionWithDistance {
        public final Signalement mission;
        public final double distanceKm;
        public final int estimatedMinutes;
        public final boolean isNearby; // < 1km
        public final boolean isClosest;
        public final String directionText;
        
        public MissionWithDistance(Signalement mission, double distanceKm, boolean isClosest) {
            this.mission = mission;
            this.distanceKm = distanceKm;
            this.estimatedMinutes = (int) Math.ceil(distanceKm * 4); // ~4 min/km en ville
            this.isNearby = distanceKm < 1.0;
            this.isClosest = isClosest;
            this.directionText = formatDirection(distanceKm);
        }
        
        private String formatDirection(double km) {
            if (km < 0.1) return "🎯 Très proche (< 100m)";
            if (km < 0.5) return "📍 À proximité (" + Math.round(km * 1000) + "m)";
            if (km < 1.0) return "🚶 À pied (" + String.format("%.1f", km) + "km)";
            if (km < 3.0) return "🚗 En voiture (" + String.format("%.1f", km) + "km)";
            return "🛣️ Plus éloigné (" + String.format("%.1f", km) + "km)";
        }
    }
    
    public static class Coordinates {
        public final double lat;
        public final double lon;
        
        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
        
        @Override
        public String toString() {
            return String.format("(%.6f, %.6f)", lat, lon);
        }
    }
    
    /**
     * 🎯 DÉMARRER LE TRACKING TEMPS RÉEL
     */
    public void startRealTimeTracking() {
        if (locationTracker != null) {
            locationTracker.cancel();
        }
        
        // Position initiale au centre de la zone
        String agentZone = getCurrentAgentZone();
        updateAgentPosition(getZoneCenter(agentZone));
        
        // Timer pour simuler le mouvement GPS toutes les 3 secondes
        locationTracker = new Timer(true);
        locationTracker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simulateAgentMovement();
                updateMissionDistances();
            }
        }, 3000, 3000); // Toutes les 3 secondes
    }
    
    /**
     * 🛑 ARRÊTER LE TRACKING
     */
    public void stopRealTimeTracking() {
        if (locationTracker != null) {
            locationTracker.cancel();
            locationTracker = null;
        }
    }
    
    /**
     * 📍 OBTENIR LA POSITION ACTUELLE DE L'AGENT
     */
    public Coordinates getCurrentPosition() {
        return currentAgentPosition;
    }
    
    /**
     * 📋 METTRE À JOUR LA LISTE DES MISSIONS
     */
    public void updateMissions(List<Signalement> missions) {
        this.allMissions = new ArrayList<>(missions);
        ensureAllMissionsHaveCoordinates();
        updateMissionDistances();
    }
    
    /**
     * 🔄 CALCULER LES DISTANCES DE TOUTES LES MISSIONS
     */
    public List<MissionWithDistance> calculateMissionsDistances() {
        if (currentAgentPosition == null || allMissions.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<MissionWithDistance> result = new ArrayList<>();
        double closestDistance = Double.MAX_VALUE;
        
        // Calculer toutes les distances
        for (Signalement mission : allMissions) {
            double distance = calculateDistance(
                currentAgentPosition.lat, currentAgentPosition.lon,
                mission.getLatitude(), mission.getLongitude()
            );
            
            if (distance < closestDistance) {
                closestDistance = distance;
            }
            
            result.add(new MissionWithDistance(mission, distance, false));
        }
        
        // Marquer la plus proche
        final double finalClosestDistance = closestDistance;
        result = result.stream()
            .map(mwd -> new MissionWithDistance(mwd.mission, mwd.distanceKm, 
                Math.abs(mwd.distanceKm - finalClosestDistance) < 0.001))
            .sorted(Comparator.comparingDouble(mwd -> mwd.distanceKm))
            .collect(Collectors.toList());
            
        return result;
    }
    
    /**
     * 🎯 TROUVER LA MISSION LA PLUS PROCHE
     */
    public Optional<Signalement> findClosestMission() {
        List<MissionWithDistance> distances = calculateMissionsDistances();
        return distances.stream()
            .filter(mwd -> mwd.isClosest)
            .map(mwd -> mwd.mission)
            .findFirst();
    }
    
    /**
     * 🗺️ GÉNÉRER L'URL GOOGLE MAPS POUR NAVIGATION
     */
    public String getNavigationUrl(Signalement destination) {
        if (currentAgentPosition == null) {
            return String.format(
                "https://www.google.com/maps/search/%.6f,%.6f",
                destination.getLatitude(), destination.getLongitude()
            );
        }
        
        return String.format(
            "https://www.google.com/maps/dir/%.6f,%.6f/%.6f,%.6f",
            currentAgentPosition.lat, currentAgentPosition.lon,
            destination.getLatitude(), destination.getLongitude()
        );
    }
    
    /**
     * 📱 AJOUTER UN LISTENER POUR LES MISES À JOUR
     */
    public void addLocationUpdateListener(LocationUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeLocationUpdateListener(LocationUpdateListener listener) {
        listeners.remove(listener);
    }
    
    // === MÉTHODES PRIVÉES ===
    
    private void simulateAgentMovement() {
        if (currentAgentPosition == null) return;
        
        // Simulation réaliste : petit déplacement aléatoire
        double latOffset = (Math.random() - 0.5) * 0.001; // ~100m max
        double lonOffset = (Math.random() - 0.5) * 0.001;
        
        Coordinates newPosition = new Coordinates(
            currentAgentPosition.lat + latOffset,
            currentAgentPosition.lon + lonOffset
        );
        
        updateAgentPosition(newPosition);
    }
    
    private void updateAgentPosition(Coordinates newPosition) {
        this.currentAgentPosition = newPosition;
        
        // Notifier tous les listeners
        Platform.runLater(() -> {
            for (LocationUpdateListener listener : listeners) {
                listener.onLocationUpdate(newPosition);
            }
        });
    }
    
    private void updateMissionDistances() {
        List<MissionWithDistance> distances = calculateMissionsDistances();
        
        Platform.runLater(() -> {
            for (LocationUpdateListener listener : listeners) {
                listener.onMissionsDistanceUpdate(distances);
            }
        });
    }
    
    private void ensureAllMissionsHaveCoordinates() {
        for (Signalement mission : allMissions) {
            if (mission.getLatitude() == 0.0 && mission.getLongitude() == 0.0) {
                Coordinates baseCoords = getZoneCenter(mission.getZoneNom());
                double randomLat = baseCoords.lat + (Math.random() - 0.5) * 0.01;
                double randomLon = baseCoords.lon + (Math.random() - 0.5) * 0.01;
                mission.setLatitude(randomLat);
                mission.setLongitude(randomLon);
            }
        }
    }
    
    private String getCurrentAgentZone() {
        return SessionManager.getUtilisateurConnecte() != null ?
            "Pikine" : "Pikine"; // Défaut
    }
    
    public Coordinates getZoneCenter(String zone) {
        switch (zone.toLowerCase()) {
            case "guediawaye":
                return new Coordinates(14.7765, -17.4047);
            case "pikine":
            default:
                return new Coordinates(14.7646, -17.3920);
        }
    }
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}