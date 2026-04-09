package com.smartcity.controller;

/**
 * Ligne d'affichage du suivi live des agents pour le dashboard admin.
 */
public class AgentLiveRow {
    private final String nom;
    private final String zone;
    private final int missionsActives;
    private final int missionsEnCours;
    private final String position;
    private final String fraicheur;

    public AgentLiveRow(
        String nom,
        String zone,
        int missionsActives,
        int missionsEnCours,
        String position,
        String fraicheur
    ) {
        this.nom = nom;
        this.zone = zone;
        this.missionsActives = missionsActives;
        this.missionsEnCours = missionsEnCours;
        this.position = position;
        this.fraicheur = fraicheur;
    }

    public String nom() {
        return nom;
    }

    public String zone() {
        return zone;
    }

    public int missionsActives() {
        return missionsActives;
    }

    public int missionsEnCours() {
        return missionsEnCours;
    }

    public String position() {
        return position;
    }

    public String fraicheur() {
        return fraicheur;
    }
}
