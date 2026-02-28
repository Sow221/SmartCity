package com.smartcity.model;

import java.time.LocalDateTime;

/**
 * Classe modèle pour les signalements de déchets
 */
public class Signalement {

    private int idSignalement;
    private String description;
    private String categorie;
    private String zone;
    private LocalDateTime dateSignalement;
    private String statut;
    private String photo;
    private int idUser;

    public Signalement() {
    }

    public Signalement(String description, String categorie, String zone,
            LocalDateTime dateSignalement, String statut, String photo, int idUser) {
        this.description = description;
        this.categorie = categorie;
        this.zone = zone;
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

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
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

    @Override
    public String toString() {
        return "Signalement{" +
                "idSignalement=" + idSignalement +
                ", description='" + description + '\'' +
                ", categorie='" + categorie + '\'' +
                ", zone='" + zone + '\'' +
                ", dateSignalement=" + dateSignalement +
                ", statut='" + statut + '\'' +
                ", idUser=" + idUser +
                '}';
    }
}
