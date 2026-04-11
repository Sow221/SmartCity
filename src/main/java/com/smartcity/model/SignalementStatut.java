package com.smartcity.model;

import java.text.Normalizer;
import java.util.Locale;

public enum SignalementStatut {
    EN_ATTENTE("En attente", "En attente"),
    AFFECTE("Affecte", "Affect\u00e9"),
    EN_COURS("En cours", "En cours"),
    TERMINE("Termine", "Termin\u00e9");

    /** Cat\u00e9gories de d\u00e9chets centralis\u00e9es. */
    public static final java.util.List<String> CATEGORIES = java.util.List.of(
        "Plastique", "Papier", "Verre", "M\u00e9tal", "Organique", "Autre"
    );

    /** Retourne la couleur CSS associ\u00e9e \u00e0 un statut (pour les TableCell). */
    public static String couleur(String statut) {
        SignalementStatut s = fromAny(statut);
        if (s == null) return "#F57C00";
        return switch (s) {
            case EN_COURS  -> "#1565C0";
            case TERMINE   -> "#2E7D32";
            case AFFECTE   -> "#7B1FA2";
            default        -> "#F57C00";
        };
    }

    private final String dbValue;
    private final String label;

    SignalementStatut(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public String dbValue() {
        return dbValue;
    }

    public String label() {
        return label;
    }

    public static SignalementStatut fromAny(String value) {
        if (value == null) {
            return null;
        }

        String n = normalize(value);
        if (n.equals(normalize(EN_ATTENTE.dbValue)) || n.equals(normalize(EN_ATTENTE.label))) {
            return EN_ATTENTE;
        }
        if (n.startsWith("affect")) {
            return AFFECTE;
        }
        if (n.equals(normalize(EN_COURS.dbValue)) || n.equals(normalize(EN_COURS.label))) {
            return EN_COURS;
        }
        if (n.startsWith("termine") || n.startsWith("resolu") || n.startsWith("collect")) {
            return TERMINE;
        }
        return null;
    }

    public static String toDbValue(String anyValue, String fallback) {
        SignalementStatut statut = fromAny(anyValue);
        return statut != null ? statut.dbValue : fallback;
    }

    public static String toDbValueOrSelf(String anyValue) {
        return toDbValue(anyValue, anyValue);
    }

    public static String toLabel(String anyValue, String fallback) {
        SignalementStatut statut = fromAny(anyValue);
        return statut != null ? statut.label : fallback;
    }

    public static String toLabelOrSelf(String anyValue) {
        return toLabel(anyValue, anyValue);
    }

    public boolean matches(String value) {
        return this == fromAny(value);
    }

    public static boolean isCompleted(String value) {
        return TERMINE.matches(value);
    }

    private static String normalize(String value) {
        String out = Normalizer.normalize(value, Normalizer.Form.NFD);
        out = out.replaceAll("\\p{M}+", "");
        out = out.toLowerCase(Locale.ROOT).trim();
        out = out.replaceAll("\\s+", " ");
        return out;
    }
}
