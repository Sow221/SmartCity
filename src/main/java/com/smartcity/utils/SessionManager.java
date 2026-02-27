package com.smartcity.utils;

import com.smartcity.model.Utilisateur;

/**
 * Gestionnaire de session utilisateur
 */
public class SessionManager {

    private static Utilisateur utilisateurConnecte;
    private static SessionManager instance;

    private SessionManager() {
    }

    /**
     * Obtient l'instance unique du SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Définit l'utilisateur connecté
     */
    public static void setUtilisateurConnecte(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    /**
     * Obtient l'utilisateur connecté
     */
    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public static boolean isConnected() {
        return utilisateurConnecte != null;
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public static boolean isAdmin() {
        return utilisateurConnecte != null && "Administrateur".equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifie si l'utilisateur est un agent
     */
    public static boolean isAgent() {
        return utilisateurConnecte != null && "Agent".equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifie si l'utilisateur est un citoyen
     */
    public static boolean isCitoyen() {
        return utilisateurConnecte != null && "Citoyen".equals(utilisateurConnecte.getRole());
    }

    /**
     * Déconnexion de l'utilisateur
     */
    public static void logout() {
        utilisateurConnecte = null;
    }
}
