package com.smartcity.service;

import com.smartcity.model.Zone;
import com.smartcity.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ZoneService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    public List<Zone> getAllZones() {
        List<Zone> zones = new ArrayList<>();
        String query = "SELECT * FROM Zone ORDER BY nomZone";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                zones.add(mapResultSetToZone(rs));
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
                return mapResultSetToZone(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la zone: " + e.getMessage());
        }
        return null;
    }

    public Zone getZoneByNom(String nomZone) {
        String query = "SELECT * FROM Zone WHERE nomZone = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, nomZone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToZone(rs);
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

    public boolean updateZone(Zone zone) {
        String query = "UPDATE Zone SET nomZone = ? WHERE idZone = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setString(1, zone.getNomZone());
            pstmt.setInt(2, zone.getIdZone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la zone: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteZone(int idZone) {
        String query = "DELETE FROM Zone WHERE idZone = ?";

        try (PreparedStatement pstmt = getConn().prepareStatement(query)) {
            pstmt.setInt(1, idZone);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la zone: " + e.getMessage());
            return false;
        }
    }

    private Zone mapResultSetToZone(ResultSet rs) throws SQLException {
        Zone zone = new Zone();
        zone.setIdZone(rs.getInt("idZone"));
        zone.setNomZone(rs.getString("nomZone"));
        return zone;
    }
}