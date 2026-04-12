package com.smartcity.service;

/**
 * Délégation vers com.smartcity.config.GeoConfig.
 * Ce fichier existe uniquement pour la compatibilité des imports existants.
 * Toute la logique réelle est dans com.smartcity.config.GeoConfig.
 */
public class GeoConfig {

    private GeoConfig() {}

    public static int getGpsPort()       { return com.smartcity.config.GeoConfig.getGpsPort(); }
    public static int getWsPort()        { return com.smartcity.config.GeoConfig.getWebSocketPort(); }
    public static String getLocalIp()    { return com.smartcity.config.GeoConfig.getLocalIp(); }
    public static String getGpsBaseUrl() { return com.smartcity.config.GeoConfig.getGpsBaseUrl(); }

    public static int findFreePort(int preferred) {
        return com.smartcity.config.GeoConfig.findFreePort(preferred);
    }
}
