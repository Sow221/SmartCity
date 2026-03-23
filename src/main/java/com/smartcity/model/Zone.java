package com.smartcity.model;

/**
 * Classe modèle pour la Zone
 */
public class Zone {

    private int idZone;
    private String nomZone;

    public Zone() {
    }

    public Zone(String nomZone) {
        this.nomZone = nomZone;
    }

    public Zone(int idZone, String nomZone) {
        this.idZone = idZone;
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
        return nomZone;
    }
}