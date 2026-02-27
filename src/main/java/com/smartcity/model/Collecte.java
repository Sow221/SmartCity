package com.smartcity.model;

/**
 * Classe modèle pour les collectes
 */
public class Collecte {

    private int idCollecte;
    private int idDechet;
    private int idAgent;
    private String dateCollecte;
    private String statut; // En attente, En cours, Collecté
    private String commentaire;

    // Constructeurs
    public Collecte() {
    }

    public Collecte(int idDechet, int idAgent, String dateCollecte, String statut, String commentaire) {
        this.idDechet = idDechet;
        this.idAgent = idAgent;
        this.dateCollecte = dateCollecte;
        this.statut = statut;
        this.commentaire = commentaire;
    }

    // Getters et Setters
    public int getIdCollecte() {
        return idCollecte;
    }

    public void setIdCollecte(int idCollecte) {
        this.idCollecte = idCollecte;
    }

    public int getIdDechet() {
        return idDechet;
    }

    public void setIdDechet(int idDechet) {
        this.idDechet = idDechet;
    }

    public int getIdAgent() {
        return idAgent;
    }

    public void setIdAgent(int idAgent) {
        this.idAgent = idAgent;
    }

    public String getDateCollecte() {
        return dateCollecte;
    }

    public void setDateCollecte(String dateCollecte) {
        this.dateCollecte = dateCollecte;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    @Override
    public String toString() {
        return "Collecte{" +
                "idCollecte=" + idCollecte +
                ", idDechet=" + idDechet +
                ", idAgent=" + idAgent +
                ", dateCollecte='" + dateCollecte + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
