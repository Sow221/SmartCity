package com.smartcity.config;

import com.smartcity.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration centralisee pour tous les services geolocalisation.
 * Ports resolus une seule fois au demarrage (lazy, thread-safe).
 */
public class GeoConfig {

    private static final Logger logger = LoggerFactory.getLogger(GeoConfig.class);

    public static final int DEFAULT_GPS_PORT       = 3001;
    public static final int DEFAULT_WEBSOCKET_PORT = 8888;

    private static volatile int    actualGpsPort       = -1;
    private static volatile int    actualWebSocketPort = -1;
    private static volatile String localIp             = null;

    public static int getGpsPort() {
        if (actualGpsPort == -1) init();
        return actualGpsPort;
    }

    public static int getWebSocketPort() {
        if (actualWebSocketPort == -1) init();
        return actualWebSocketPort;
    }

    public static String getLocalIp() {
        if (localIp == null) init();
        return localIp;
    }

    public static String getGpsBaseUrl() {
        return "http://" + getLocalIp() + ":" + getGpsPort();
    }

    public static String getWebSocketBaseUrl() {
        return "ws://" + getLocalIp() + ":" + getWebSocketPort();
    }

    public static String getWebSocketUrl(String endpoint) {
        return getWebSocketBaseUrl() + endpoint;
    }

    public static String getWebSocketUrl() {
        return getWebSocketUrl("/ws/agent-missions");
    }

    public static String getGpsPageUrl(int agentId, String token) {
        return getGpsBaseUrl() + "/gps?agentId=" + agentId + "&token=" + token;
    }

    public static String getCitizenGpsPageUrl(int citizenId, String token) {
        return getGpsBaseUrl() + "/citizen-gps?citizenId=" + citizenId + "&token=" + token;
    }

    private static synchronized void init() {
        if (actualGpsPort != -1) return;
        // ✅ UTILISER LA SOURCE CENTRALISÉE (NetworkUtils)
        localIp             = NetworkUtils.detectLocalIp();
        actualGpsPort       = NetworkUtils.findFreePort(DEFAULT_GPS_PORT);
        actualWebSocketPort = NetworkUtils.findFreePort(DEFAULT_WEBSOCKET_PORT);
        logger.info("GeoConfig OK - GPS port: {}, WS port: {}, IP: {}",
                actualGpsPort, actualWebSocketPort, localIp);
    }
}
