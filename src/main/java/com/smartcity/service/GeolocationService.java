package com.smartcity.service;

import com.smartcity.model.Signalement;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GeolocationService {

    // Source unique des coordonnées de zones
    private static final Map<String, Coordinates> ZONE_CENTERS = new HashMap<>();
    static {
        ZONE_CENTERS.put("Pikine",      new Coordinates(14.7646, -17.3920));
        ZONE_CENTERS.put("Guediawaye",  new Coordinates(14.7765, -17.4047));
        ZONE_CENTERS.put("Guédiawaye", new Coordinates(14.7765, -17.4047));
    }

    public Coordinates getCurrentPosition() {
        return ZONE_CENTERS.get("Pikine");
    }

    public Coordinates getZoneCenter(String nomZone) {
        if (nomZone == null) return ZONE_CENTERS.get("Pikine");
        return ZONE_CENTERS.getOrDefault(nomZone,
               ZONE_CENTERS.getOrDefault(nomZone.trim(), ZONE_CENTERS.get("Pikine")));
    }

    // Source unique du calcul de distance (Haversine)
    public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // Instance method pour compatibilité avec le code existant
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return distanceBetween(lat1, lon1, lat2, lon2);
    }

    public List<Signalement> optimizeCollectionRoute(List<Signalement> signalements, Coordinates startPoint) {
        if (signalements.isEmpty()) return signalements;
        List<Signalement> optimized = new java.util.ArrayList<>();
        List<Signalement> remaining = new java.util.ArrayList<>(signalements);
        Coordinates current = startPoint;
        while (!remaining.isEmpty()) {
            Signalement closest = null;
            double minDist = Double.MAX_VALUE;
            for (Signalement s : remaining) {
                double d = distanceBetween(current.lat, current.lon, s.getLatitude(), s.getLongitude());
                if (d < minDist) { minDist = d; closest = s; }
            }
            if (closest != null) {
                optimized.add(closest);
                remaining.remove(closest);
                current = new Coordinates(closest.getLatitude(), closest.getLongitude());
            }
        }
        return optimized;
    }

    public Coordinates generateRandomCoordinatesInZone(String nomZone) {
        Coordinates center = getZoneCenter(nomZone);
        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * 0.02;
        return new Coordinates(center.lat + distance * Math.cos(angle),
                               center.lon + distance * Math.sin(angle));
    }

    public String getGoogleMapsUrl(double lat, double lon) {
        return String.format("https://maps.google.com/maps?q=%.6f,%.6f&z=16", lat, lon);
    }

    public String getRouteUrl(Coordinates start, Coordinates end) {
        return String.format("https://maps.google.com/maps/dir/%.6f,%.6f/%.6f,%.6f",
                start.lat, start.lon, end.lat, end.lon);
    }

    public String getMultiPointRouteUrl(List<Signalement> signalements) {
        if (signalements.isEmpty()) return "";
        StringBuilder url = new StringBuilder("https://maps.google.com/maps/dir/");
        for (int i = 0; i < signalements.size(); i++) {
            Signalement s = signalements.get(i);
            url.append(String.format("%.6f,%.6f", s.getLatitude(), s.getLongitude()));
            if (i < signalements.size() - 1) url.append("/");
        }
        return url.toString();
    }

    public static class Coordinates {
        public final double lat;
        public final double lon;
        public Coordinates(double lat, double lon) { this.lat = lat; this.lon = lon; }
        @Override public String toString() { return String.format("(%.6f, %.6f)", lat, lon); }
    }
}
