package com.smartcity.model;

import java.time.LocalDateTime;

public class Utilisateur {

    private int idUser;
    private String nom;
    private String prenom;
    private String email;
    private int age;
    private String motDePasse;
    private String localite;
    private String photoProfil;
    private String role; // ADMIN, AGENT, CITOYEN

    // Champs supplémentaires utiles côté Java
    private LocalDateTime dateInscription;
    private boolean actif;

    // Constructeurs
    public Utilisateur() {
        this.actif = true;
        this.dateInscription = LocalDateTime.now();
    }

    public Utilisateur(String nom, String prenom, String email,
                       String motDePasse, String localite, String photoProfil) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.age = age;
        this.motDePasse = motDePasse;
        this.localite = localite;
        this.photoProfil = photoProfil;
        this.role = role;
        this.actif = true;
        this.dateInscription = LocalDateTime.now();
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", localite='" + localite + '\'' +
                ", photoProfil='" + photoProfil + '\'' +
                ", role='" + role + '\'' +
                ", actif=" + actif +
                ", dateInscription=" + dateInscription +
                '}';
    }
}
