package com.smartcity.model;

import java.time.LocalDateTime;

public class Signalement {
    private int idSignalement;
    private int idZone;
    private int idUser;
    private String description;
    private String categorie;
    private LocalDateTime dateSignalement;
    private String statut;
    private String photoDepot;
    private Double latitude;
    private Double longitude;

    // Champs pour l'affichage (optionnels, peuvent être récupérés via des services)
    private String utilisateurNom;
    private String zoneNom;

    public Signalement() {}

    // Getters et Setters
    public int getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(int idSignalement) {
        this.idSignalement = idSignalement;
    }

    public int getIdZone() {
        return idZone;
    }

    public void setIdZone(int idZone) {
        this.idZone = idZone;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPhotoDepot() {
        return photoDepot;
    }

    public void setPhotoDepot(String photoDepot) {
        this.photoDepot = photoDepot;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Méthodes pour l'affichage (optionnelles)
    public String getUtilisateurNom() {
        return utilisateurNom;
    }

    public void setUtilisateurNom(String utilisateurNom) {
        this.utilisateurNom = utilisateurNom;
    }

    public String getZoneNom() {
        return zoneNom;
    }

    public void setZoneNom(String zoneNom) {
        this.zoneNom = zoneNom;
    }

    @Override
    public String toString() {
        return "Signalement #" + idSignalement + " - " + description;
    }
}