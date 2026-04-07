package com.smartcity.model;

/**
 * Classe modele pour la Zone avec coordonnees GPS du centre.
 */
public class Zone {

    private int idZone;
    private String nomZone;
    private double latitude;
    private double longitude;

    public Zone() {}

    public Zone(String nomZone) {
        this.nomZone = nomZone;
    }

    public Zone(int idZone, String nomZone) {
        this.idZone = idZone;
        this.nomZone = nomZone;
    }

    public Zone(int idZone, String nomZone, double latitude, double longitude) {
        this.idZone = idZone;
        this.nomZone = nomZone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getIdZone() { return idZone; }
    public void setIdZone(int idZone) { this.idZone = idZone; }

    public String getNomZone() { return nomZone; }
    public void setNomZone(String nomZone) { this.nomZone = nomZone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    /** Retourne true si la zone a des coordonnees GPS renseignees. */
    public boolean hasCoordinates() {
        return latitude != 0.0 || longitude != 0.0;
    }

    @Override
    public String toString() { return nomZone; }
}
