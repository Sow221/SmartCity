package com.smartcity.model;

public class Zone {
    private int idZone;
    private String nomZone;

    // Constructeurs
    public Zone() {}
    public Zone(int idZone, String nomZone) {}
    public Zone(String nomZone) {
        this.nomZone = nomZone;
    }

    // Getters et Setters
    public int getIdZone() {
        return idZone;
    }

    public void setIdZone(int idZone) {
        this.idZone = idZone;
    }

    public String getNomZone() {
        return nomZone;
    }

    public void setNomZone(String nomZone) {
        this.nomZone = nomZone;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "idZone=" + idZone +
                ", nomZone='" + nomZone + '\'' +
                '}';
    }
}
