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

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Zone> getAllZones() {
        List<Zone> zones = new ArrayList<>();
        String query = "SELECT * FROM Zone";

        try (PreparedStatement pstmt = getConn().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                zones.add(new Zone(rs.getInt("idZone"), rs.getString("nomZone")));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des zones", e);
        }
        return zones;
    }

    private static java.util.Map<Integer, Zone> zoneCache = new java.util.HashMap<>();

    public Zone getZoneById(int idZone) {
        if (zoneCache.containsKey(idZone)) {
            return zoneCache.get(idZone);
        }
        String query = "SELECT * FROM Zone WHERE idZone = ?";
        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idZone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Zone zone = new Zone(rs.getInt("idZone"), rs.getString("nomZone"));
                    zoneCache.put(idZone, zone);
                    return zone;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de la zone", e);
        }
        return null;
    }

    public boolean ajouterZone(Zone zone) {
        String query = "INSERT INTO Zone (nomZone) VALUES (?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone.getNomZone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout de la zone", e);
            return false;
        }
    }
}