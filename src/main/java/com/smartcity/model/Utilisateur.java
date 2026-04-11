package com.smartcity.model;

import java.time.LocalDateTime;

/**
 * Classe modèle pour l'utilisateur
 */
public class Utilisateur {

    private int idUser;
    private String prenom;
    private String nom;
    private String email;
    private String motDePasse;
    private String role; // Citoyen, Agent, Administrateur
    private int age;
    private String localite;
    private String photoProfil;
    private int idZone;
    private boolean actif;
    private LocalDateTime dateInscription;

    // Constructeurs
    public Utilisateur() {
        this.actif = true;
    }

    public Utilisateur(String prenom, String nom, String email, String motDePasse, String role, int idZone) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.idZone = idZone;
        this.actif = true;
    }

    public Utilisateur(String prenom, String nom, String email, String motDePasse, String role, int idZone, int age, String localite, String photoProfil) {
        this(prenom, nom, email, motDePasse, role, idZone);
        this.age = age;
        this.localite = localite;
        this.photoProfil = photoProfil;
    }

    // Getters et Setters
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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

    public int getIdZone() {
        return idZone;
    }

    public void setIdZone(int idZone) {
        this.idZone = idZone;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    @Override
    public String toString() {
        return (prenom != null ? prenom + " " : "") + nom + " (" + role + ")";
    }
}
