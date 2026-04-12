package com.smartcity.service;

import com.smartcity.model.Zone;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des zones.
 */
public class ZoneService {
    private static final Logger logger = LoggerFactory.getLogger(ZoneService.class);
    private static volatile boolean zoneGpsSchemaChecked = false;

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Zone> getAllZones() {
        ensureZoneGpsSchema();
        List<Zone> zones = new ArrayList<>();
        // Essayer avec colonnes GPS, fallback sans si elles n'existent pas
        String query = "SELECT idZone, nomZone, latitude, longitude FROM Zone";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) zones.add(mapZone(rs));
        } catch (SQLException e) {
            // Colonnes GPS absentes : fallback sans coordonnees
            logger.warn("Colonnes GPS absentes dans Zone, fallback sans coordonnees: {}", e.getMessage());
            String fallback = "SELECT idZone, nomZone FROM Zone";
            try (Connection conn = getConn();
                 PreparedStatement pstmt = conn.prepareStatement(fallback);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    zones.add(new Zone(rs.getInt("idZone"), rs.getString("nomZone")));
                }
            } catch (SQLException e2) {
                logger.error("Erreur lors de la recuperation des zones", e2);
            }
        }
        return zones;
    }

    // Cache instance-level thread-safe
    private final java.util.concurrent.ConcurrentHashMap<Integer, Zone> zoneCache = new java.util.concurrent.ConcurrentHashMap<>();

    public Zone getZoneById(int idZone) {
        ensureZoneGpsSchema();
        if (zoneCache.containsKey(idZone)) return zoneCache.get(idZone);
        String query = "SELECT idZone, nomZone, latitude, longitude FROM Zone WHERE idZone = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idZone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Zone zone = mapZone(rs);
                    zoneCache.put(idZone, zone);
                    return zone;
                }
            }
        } catch (SQLException e) {
            // Fallback sans colonnes GPS
            String fallback = "SELECT idZone, nomZone FROM Zone WHERE idZone = ?";
            try (Connection conn = getConn();
                 PreparedStatement pstmt = conn.prepareStatement(fallback)) {
                pstmt.setInt(1, idZone);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Zone zone = new Zone(rs.getInt("idZone"), rs.getString("nomZone"));
                        zoneCache.put(idZone, zone);
                        return zone;
                    }
                }
            } catch (SQLException e2) {
                logger.error("Erreur lors de la recuperation de la zone", e2);
            }
        }
        return null;
    }

    public Zone getZoneByName(String nomZone) {
        ensureZoneGpsSchema();
        if (nomZone == null) return null;
        // Chercher dans le cache d'abord
        for (Zone z : zoneCache.values()) {
            if (z.getNomZone().equalsIgnoreCase(nomZone)) return z;
        }
        String query = "SELECT idZone, nomZone, latitude, longitude FROM Zone WHERE nomZone = ?";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nomZone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Zone zone = mapZone(rs);
                    zoneCache.put(zone.getIdZone(), zone);
                    return zone;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur getZoneByName: {}", e.getMessage());
        }
        return null;
    }

    public boolean ajouterZone(Zone zone) {
        ensureZoneGpsSchema();
        String query = "INSERT INTO Zone (nomZone, latitude, longitude) VALUES (?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, zone.getNomZone());
            pstmt.setDouble(2, zone.getLatitude());
            pstmt.setDouble(3, zone.getLongitude());
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) zoneCache.clear(); // invalider le cache
            return ok;
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout de la zone", e);
            return false;
        }
    }

    /**
     * Retourne les coordonnees GPS du centre d'une zone depuis la DB.
     * Fallback sur Pikine si la zone n'a pas de coordonnees.
     */
    public GeolocationService.Coordinates getCenter(String nomZone) {
        Zone zone = getZoneByName(nomZone);
        if (zone != null && zone.hasCoordinates()) {
            return new GeolocationService.Coordinates(zone.getLatitude(), zone.getLongitude());
        }
        // Fallback : Pikine
        return new GeolocationService.Coordinates(14.7646, -17.3920);
    }

    public GeolocationService.Coordinates getCenterById(int idZone) {
        Zone zone = getZoneById(idZone);
        if (zone != null && zone.hasCoordinates()) {
            return new GeolocationService.Coordinates(zone.getLatitude(), zone.getLongitude());
        }
        return new GeolocationService.Coordinates(14.7646, -17.3920);
    }

    private Zone mapZone(ResultSet rs) throws SQLException {
        Zone zone = new Zone();
        zone.setIdZone(rs.getInt("idZone"));
        zone.setNomZone(rs.getString("nomZone"));
        try {
            zone.setLatitude(rs.getDouble("latitude"));
            zone.setLongitude(rs.getDouble("longitude"));
        } catch (SQLException e) {
            // Colonnes optionnelles si ancienne DB sans migration
            logger.debug("Colonnes latitude/longitude absentes pour la zone: {}", e.getMessage());
        }
        return zone;
    }

    private void ensureZoneGpsSchema() {
        if (zoneGpsSchemaChecked) {
            return;
        }
        synchronized (ZoneService.class) {
            if (zoneGpsSchemaChecked) {
                return;
            }
            try (Connection conn = getConn()) {
                if (!hasZoneGpsColumns(conn)) {
                    try (PreparedStatement ps1 = conn.prepareStatement(
                            "ALTER TABLE Zone ADD COLUMN latitude DECIMAL(10,8) DEFAULT 0.0");
                         PreparedStatement ps2 = conn.prepareStatement(
                            "ALTER TABLE Zone ADD COLUMN longitude DECIMAL(11,8) DEFAULT 0.0");
                         PreparedStatement ps3 = conn.prepareStatement(
                            "UPDATE Zone SET latitude = 14.7646, longitude = -17.3920 WHERE nomZone = 'Pikine'");
                         PreparedStatement ps4 = conn.prepareStatement(
                            "UPDATE Zone SET latitude = 14.7765, longitude = -17.4047 WHERE nomZone IN ('Guédiawaye', 'Guediawaye')")) {
                        ps1.executeUpdate();
                        ps2.executeUpdate();
                        ps3.executeUpdate();
                        ps4.executeUpdate();
                        logger.info("Colonnes GPS de Zone ajoutées automatiquement.");
                    }
                }
            } catch (SQLException e) {
                logger.warn("Vérification auto du schéma Zone GPS impossible: {}", e.getMessage());
            } finally {
                zoneGpsSchemaChecked = true;
            }
        }
    }

    private boolean hasZoneGpsColumns(Connection conn) {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Zone' "
            + "AND COLUMN_NAME IN ('latitude', 'longitude')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() && rs.getInt(1) == 2;
        } catch (SQLException e) {
            return false;
        }
    }
}
