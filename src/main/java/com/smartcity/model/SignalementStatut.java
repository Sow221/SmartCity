package com.smartcity.model;

import java.text.Normalizer;
import java.util.Locale;

public enum SignalementStatut {
    EN_ATTENTE("En attente", "En attente"),
    AFFECTE("Affecte", "Affecté"),
    EN_COURS("En cours", "En cours"),
    TERMINE("Termine", "Terminé");

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
        if (value == null)
            return null;
        String n = normalize(value);
        if (n.equals(normalize(EN_ATTENTE.dbValue)) || n.equals(normalize(EN_ATTENTE.label)))
            return EN_ATTENTE;
        if (n.startsWith("affect"))
            return AFFECTE;
        if (n.equals(normalize(EN_COURS.dbValue)) || n.equals(normalize(EN_COURS.label)))
            return EN_COURS;
        if (n.startsWith("termine") || n.startsWith("resolu") || n.startsWith("collect"))
            return TERMINE;
        return null;
    }

    public static String toDbValue(String anyValue, String fallback) {
        SignalementStatut s = fromAny(anyValue);
        return s != null ? s.dbValue : fallback;
    }

    public static String toLabel(String anyValue, String fallback) {
        SignalementStatut s = fromAny(anyValue);
        return s != null ? s.label : fallback;
    }

    private static String normalize(String s) {
        String out = Normalizer.normalize(s, Normalizer.Form.NFD);
        out = out.replaceAll("\\p{M}+", "");
        out = out.toLowerCase(Locale.ROOT).trim();
        out = out.replaceAll("\\s+", " ");
        return out;
    }
}

