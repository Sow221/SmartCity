package com.smartcity.model;

import java.time.LocalDateTime;

public class Affectation {
    private int idAffectation;
    private int idSignalement;
    private int idAgent;
    private LocalDateTime dateAffectation;

    // Champs supplémentaires pour l'affichage
    private String signalementDescription;
    private String agentNom;

    // Note: Les champs dateCollecte et commentaire n'existent pas dans votre table
    // Ils sont commentés car ils ne font pas partie de votre schéma

    public Affectation() {}

    // Getters et Setters
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

    // Méthodes pour les champs qui n'existent pas dans votre table
    // Elles sont conservées pour la compatibilité mais ne seront pas utilisées
    public LocalDateTime getDateCollecte() {
        return null; // Cette colonne n'existe pas
    }

    public void setDateCollecte(LocalDateTime dateCollecte) {
        // Ignoré car la colonne n'existe pas
    }

    public String getCommentaire() {
        return null; // Cette colonne n'existe pas
    }

    public void setCommentaire(String commentaire) {
        // Ignoré car la colonne n'existe pas
    }

    // Champs d'affichage
    public String getSignalementDescription() {
        return signalementDescription;
    }

    public void setSignalementDescription(String signalementDescription) {
        this.signalementDescription = signalementDescription;
    }

    public String getAgentNom() {
        return agentNom;
    }

    public void setAgentNom(String agentNom) {
        this.agentNom = agentNom;
    }

    @Override
    public String toString() {
        return "Affectation #" + idAffectation + " - Signalement #" + idSignalement;
    }
}