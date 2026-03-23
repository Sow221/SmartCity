package com.smartcity.service;

import com.smartcity.model.Zone;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion des zones.
 */
public class ZoneService {

    private Connection getConn() {
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
            System.err.println("Erreur lors de la récupération des zones: " + e.getMessage());
        }
        return zones;
    }

    public Zone getZoneById(int idZone) {
        String query = "SELECT * FROM Zone WHERE idZone = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idZone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Zone(rs.getInt("idZone"), rs.getString("nomZone"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la zone: " + e.getMessage());
        }
        return null;
    }

    public boolean ajouterZone(Zone zone) {
        String query = "INSERT INTO Zone (nomZone) VALUES (?)";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone.getNomZone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la zone: " + e.getMessage());
            return false;
        }
    }
}