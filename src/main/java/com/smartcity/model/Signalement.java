package com.smartcity.model;

import java.time.LocalDateTime;

/**
 * Classe modele pour les signalements de dechets.
 */
public class Signalement {

    private int idSignalement;
    private String description;
    private String categorie;
    private int idZone;
    private double latitude;
    private double longitude;
    private LocalDateTime dateSignalement;
    private String statut;
    private String photo;
    private int idUser;
    private String zoneNom;
    private String utilisateurNom;

    public Signalement() {
    }

    public Signalement(String description, String categorie, int idZone, double latitude, double longitude,
            LocalDateTime dateSignalement, String statut, String photo, int idUser) {
        this.description = description;
        this.categorie = categorie;
        this.idZone = idZone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateSignalement = dateSignalement;
        this.statut = statut;
        this.photo = photo;
        this.idUser = idUser;
    }

    public int getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(int idSignalement) {
        this.idSignalement = idSignalement;
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

    public int getIdZone() {
        return idZone;
    }

    public void setIdZone(int idZone) {
        this.idZone = idZone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getZoneNom() {
        return zoneNom;
    }

    public void setZoneNom(String zoneNom) {
        this.zoneNom = zoneNom;
    }

    public String getUtilisateurNom() {
        return utilisateurNom;
    }

    public void setUtilisateurNom(String utilisateurNom) {
        this.utilisateurNom = utilisateurNom;
    }

    @Override
    public String toString() {
        return "Signalement{" +
                "idSignalement=" + idSignalement +
                ", description='" + description + '\'' +
                ", categorie='" + categorie + '\'' +
                ", statut='" + statut + '\'' +
                ", zoneNom='" + zoneNom + '\'' +
                ", utilisateurNom='" + utilisateurNom + '\'' +
                '}';
    }
}