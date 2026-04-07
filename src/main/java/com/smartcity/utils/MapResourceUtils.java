package com.smartcity.utils;

import java.net.URL;

/**
 * Fournit les URLs locales des ressources Leaflet embarquées.
 * Fallback CDN si la ressource locale est introuvable.
 */
public class MapResourceUtils {

    private static final String LEAFLET_JS_CDN  = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
    private static final String LEAFLET_CSS_CDN = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
    private static final String HEAT_JS_CDN     = "https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js";

    public static String leafletJs() {
        return localOrCdn("/js/leaflet.js", LEAFLET_JS_CDN);
    }

    public static String leafletCss() {
        return localOrCdn("/js/leaflet.css", LEAFLET_CSS_CDN);
    }

    public static String leafletHeatJs() {
        return localOrCdn("/js/leaflet-heat.js", HEAT_JS_CDN);
    }

    private static String localOrCdn(String resourcePath, String cdnUrl) {
        URL url = MapResourceUtils.class.getResource(resourcePath);
        if (url != null) return url.toExternalForm();
        return cdnUrl;
    }

    /**
     * Génère le bloc HTML head avec Leaflet (CSS + JS) depuis les ressources locales.
     */
    public static String leafletHead() {
        return "<link rel='stylesheet' href='" + leafletCss() + "'/>"
             + "<script src='" + leafletJs() + "'></script>";
    }

    /**
     * Génère le bloc HTML head avec Leaflet + Heat.
     */
    public static String leafletHeadWithHeat() {
        return "<link rel='stylesheet' href='" + leafletCss() + "'/>"
             + "<script src='" + leafletJs() + "'></script>"
             + "<script src='" + leafletHeatJs() + "'></script>";
    }
}
