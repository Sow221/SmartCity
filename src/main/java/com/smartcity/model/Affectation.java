package com.smartcity.model;

import java.time.LocalDateTime;

/**
 * Classe modèle pour les affectations d'agents aux signalements
 */
public class Affectation {

    private int idAffectation;
    private int idSignalement;
    private int idAgent;
    private LocalDateTime dateAffectation;
    private LocalDateTime dateCollecte;
    private String commentaire;

    public Affectation() {
    }

    public Affectation(int idSignalement, int idAgent) {
        this.idSignalement = idSignalement;
        this.idAgent = idAgent;
    }

    public Affectation(int idSignalement, int idAgent, LocalDateTime dateAffectation) {
        this.idSignalement = idSignalement;
        this.idAgent = idAgent;
        this.dateAffectation = dateAffectation;
    }

    public int getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(int idAffectation) {
        this.idAffectation = idAffectation;
    }

    public int getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(int idSignalement) {
        this.idSignalement = idSignalement;
    }

    public int getIdAgent() {
        return idAgent;
    }

    public void setIdAgent(int idAgent) {
        this.idAgent = idAgent;
    }

    public LocalDateTime getDateAffectation() {
        return dateAffectation;
    }

    public void setDateAffectation(LocalDateTime dateAffectation) {
        this.dateAffectation = dateAffectation;
    }

    public LocalDateTime getDateCollecte() {
        return dateCollecte;
    }

    public void setDateCollecte(LocalDateTime dateCollecte) {
        this.dateCollecte = dateCollecte;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    @Override
    public String toString() {
        return "Affectation{" +
                "idAffectation=" + idAffectation +
                ", idSignalement=" + idSignalement +
                ", idAgent=" + idAgent +
                ", dateAffectation=" + dateAffectation +
                ", dateCollecte=" + dateCollecte +
                ", commentaire='" + commentaire + '\'' +
                '}';
    }
}
