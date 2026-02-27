package com.smartcity.model;

/**
 * Classe modèle pour les déchets/signalements
 */
public class Dechet {

    private int idDechet;
    private String description;
    private String categorie; // Plastique, Papier, Organique, Verre
    private String zone; // Pikine, Guédiawaye
    private String quartier;
    private String photo;
    private String statut; // En attente, En cours, Collecté
    private java.time.LocalDateTime dateSignalement;
    private int idUtilisateur;

    // Constructeurs
    public Dechet() {
    }

    public Dechet(String description, String categorie, String zone, String quartier,
            String photo, String statut, java.time.LocalDateTime dateSignalement, int idUtilisateur) {
        this.description = description;
        this.categorie = categorie;
        this.zone = zone;
        this.quartier = quartier;
        this.photo = photo;
        this.statut = statut;
        this.dateSignalement = dateSignalement;
        this.idUtilisateur = idUtilisateur;
    }

    // Getters et Setters
    public int getIdDechet() {
        return idDechet;
    }

    public void setIdDechet(int idDechet) {
        this.idDechet = idDechet;
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

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public java.time.LocalDateTime getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(java.time.LocalDateTime dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    @Override
    public String toString() {
        return "Dechet{" +
                "idDechet=" + idDechet +
                ", description='" + description + '\'' +
                ", categorie='" + categorie + '\'' +
                ", zone='" + zone + '\'' +
                ", quartier='" + quartier + '\'' +
                ", statut='" + statut + '\'' +
                ", dateSignalement=" + dateSignalement +
                '}';
    }
}
