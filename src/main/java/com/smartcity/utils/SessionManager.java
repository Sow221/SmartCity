package com.smartcity.utils;

import com.smartcity.model.Utilisateur;

/**
 * Gestionnaire de session utilisateur
 */
public class SessionManager {

    private static Utilisateur utilisateurConnecte;
    private static boolean isLoggedIn = false;

    /**
     * Définit l'utilisateur connecté
     */
    public static void setUtilisateurConnecte(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
        isLoggedIn = true;
    }

    /**
     * Retourne l'utilisateur connecté
     */
    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public static boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public static boolean isAdmin() {
        return utilisateurConnecte != null && "Administrateur".equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifie si l'utilisateur est agent
     */
    public static boolean isAgent() {
        return utilisateurConnecte != null && "Agent".equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifie si l'utilisateur est citoyen
     */
    public static boolean isCitoyen() {
        return utilisateurConnecte != null && "Citoyen".equals(utilisateurConnecte.getRole());
    }

    /**
     * Déconnexion
     */
    public static void logout() {
        utilisateurConnecte = null;
        isLoggedIn = false;
    }
}
