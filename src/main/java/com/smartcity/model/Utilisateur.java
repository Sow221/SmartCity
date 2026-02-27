package com.smartcity.model;

/**
 * Classe modèle pour l'utilisateur
 */
public class Utilisateur {

    private int idUser;
    private String nom;
    private String email;
    private String motPasse;
    private String role; // Citoyen, Agent, Administrateur
    private String zone; // Pikine, Guédiawaye

    // Constructeurs
    public Utilisateur() {
    }

    public Utilisateur(String nom, String email, String motPasse, String role, String zone) {
        this.nom = nom;
        this.email = email;
        this.motPasse = motPasse;
        this.role = role;
        this.zone = zone;
    }

    // Getters et Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotPasse() {
        return motPasse;
    }

    public void setMotPasse(String motPasse) {
        this.motPasse = motPasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", zone='" + zone + '\'' +
                '}';
    }
}
