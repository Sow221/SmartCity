package com.smartcity.service;

import com.smartcity.model.Signalement;
import java.util.List;

public class GeolocationService {

    // Calcul de distance Haversine (vol d'oiseau, metres -> km)
    public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return distanceBetween(lat1, lon1, lat2, lon2);
    }

    /**
     * Retourne le centre GPS d'une zone depuis la DB via ZoneService.
     * Plus de coordonnees hardcodees dans le code.
     */
    public Coordinates getZoneCenter(String nomZone) {
        return new ZoneService().getCenter(nomZone);
    }

    public Coordinates getCurrentPosition() {
        // Position par defaut : centre de Pikine (utilise si GPS agent inactif)
        return new ZoneService().getCenter("Pikine");
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
