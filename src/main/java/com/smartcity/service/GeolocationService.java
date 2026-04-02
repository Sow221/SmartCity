package com.smartcity.service;

import com.smartcity.model.Signalement;
import com.smartcity.model.Zone;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service pour la géolocalisation et la cartographie
 */
public class GeolocationService {

    /**
     * Obtenir la position courante de l'utilisateur (simulation)
     */
    public Coordinates getCurrentPosition() {
        // À remplacer par une vraie géolocalisation si besoin
        // Ici, retourne une position par défaut (Pikine)
        return new Coordinates(14.7549, -17.3925);
    }

    // Coordonnées des zones principales de Dakar
    private static final Map<String, Coordinates> ZONE_CENTERS = new HashMap<>();

    static {
        // Coordonnées réelles de Pikine et Guédiawaye
        ZONE_CENTERS.put("Pikine", new Coordinates(14.7549, -17.3925));
        ZONE_CENTERS.put("Guédiawaye", new Coordinates(14.7692, -17.4281));
    }

    /**
     * Obtenir les coordonnées du centre d'une zone
     */
    public Coordinates getZoneCenter(String nomZone) {
        return ZONE_CENTERS.getOrDefault(nomZone, new Coordinates(14.7549, -17.3925));
    }

    /**
     * Calculer la distance entre deux points (en km)
     */
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

    /**
     * Optimiser un itinéraire de collecte (algorithme du plus proche voisin
     * simplifié)
     */
    public List<Signalement> optimizeCollectionRoute(List<Signalement> signalements, Coordinates startPoint) {
        if (signalements.isEmpty()) {
            return signalements;
        }

        List<Signalement> optimizedRoute = new java.util.ArrayList<>();
        List<Signalement> remaining = new java.util.ArrayList<>(signalements);

        Coordinates currentPos = startPoint;

        while (!remaining.isEmpty()) {
            Signalement closest = null;
            double minDistance = Double.MAX_VALUE;

            for (Signalement s : remaining) {
                double distance = calculateDistance(currentPos.lat, currentPos.lon,
                        s.getLatitude(), s.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = s;
                }
            }

            if (closest != null) {
                optimizedRoute.add(closest);
                remaining.remove(closest);
                currentPos = new Coordinates(closest.getLatitude(), closest.getLongitude());
            }
        }

        return optimizedRoute;
    }

    /**
     * Générer des coordonnées aléatoires dans une zone donnée
     */
    public Coordinates generateRandomCoordinatesInZone(String nomZone) {
        Coordinates center = getZoneCenter(nomZone);

        // Rayon d'environ 2-3 km autour du centre
        double radius = 0.02; // ~2.2 km
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;

        double lat = center.lat + (distance * Math.cos(angle));
        double lon = center.lon + (distance * Math.sin(angle));

        return new Coordinates(lat, lon);
    }

    /**
     * Obtenir l'URL Google Maps pour un point
     */
    public String getGoogleMapsUrl(double lat, double lon) {
        return String.format("https://maps.google.com/maps?q=%.6f,%.6f&z=16", lat, lon);
    }

    /**
     * Obtenir l'URL d'itinéraire Google Maps
     */
    public String getRouteUrl(Coordinates start, Coordinates end) {
        return String.format("https://maps.google.com/maps/dir/%.6f,%.6f/%.6f,%.6f",
                start.lat, start.lon, end.lat, end.lon);
    }

    /**
     * Obtenir l'URL d'itinéraire multi-points
     */
    public String getMultiPointRouteUrl(List<Signalement> signalements) {
        if (signalements.isEmpty()) {
            return "";
        }

        StringBuilder url = new StringBuilder("https://maps.google.com/maps/dir/");
        for (int i = 0; i < signalements.size(); i++) {
            Signalement s = signalements.get(i);
            url.append(String.format("%.6f,%.6f", s.getLatitude(), s.getLongitude()));
            if (i < signalements.size() - 1) {
                url.append("/");
            }
        }

        return url.toString();
    }

    /**
     * Classe interne pour les coordonnées
     */
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
}