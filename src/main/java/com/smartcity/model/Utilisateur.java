package com.smartcity.model;

import java.time.LocalDateTime;

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
    private String telephone;
    private LocalDateTime dateInscription;
    private boolean actif;

    // Constructeurs
    public Utilisateur() {
        this.actif = true;
    }

    public Utilisateur(String nom, String email, String motPasse, String role, String zone) {
        this.nom = nom;
        this.email = email;
        this.motPasse = motPasse;
        this.role = role;
        this.zone = zone;
        this.actif = true;
    }

    public Utilisateur(String nom, String email, String motPasse, String role, String zone, String telephone) {
        this(nom, email, motPasse, role, zone);
        this.telephone = telephone;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", zone='" + zone + '\'' +
                ", telephone='" + telephone + '\'' +
                ", actif=" + actif +
                '}';
    }
}
