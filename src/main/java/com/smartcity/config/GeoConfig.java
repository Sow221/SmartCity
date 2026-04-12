package com.smartcity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Configuration centralisee pour tous les services geolocalisation.
 * Ports resolus une seule fois au demarrage (lazy, thread-safe).
 */
public class GeoConfig {

    private static final Logger logger = LoggerFactory.getLogger(GeoConfig.class);

    public static final int DEFAULT_GPS_PORT       = 3001;
    public static final int DEFAULT_WEBSOCKET_PORT = 3002;

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
        localIp             = detectLocalIp();
        actualGpsPort       = findFreePort(DEFAULT_GPS_PORT);
        actualWebSocketPort = findFreePort(DEFAULT_WEBSOCKET_PORT);
        logger.info("GeoConfig OK - GPS port: {}, WS port: {}, IP: {}",
                actualGpsPort, actualWebSocketPort, localIp);
    }

    private static String detectLocalIp() {
        try {
            java.util.List<String> candidates = java.util.Collections
                    .list(java.net.NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .filter(ni -> {
                        try { return ni.isUp() && !ni.isLoopback() && !ni.isVirtual(); }
                        catch (Exception e) { return false; }
                    })
                    .flatMap(ni -> java.util.Collections.list(ni.getInetAddresses()).stream())
                    .filter(addr -> !addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                    .map(java.net.InetAddress::getHostAddress)
                    .collect(java.util.stream.Collectors.toList());
            return candidates.stream()
                    .filter(ip -> ip.startsWith("192.168.") || ip.startsWith("10."))
                    .findFirst()
                    .orElse(candidates.isEmpty() ? "localhost" : candidates.get(0));
        } catch (Exception e) {
            return "localhost";
        }
    }

    public static int findFreePort(int preferred) {
        for (int p = preferred; p < preferred + 20; p++) {
            try (ServerSocket s = new ServerSocket(p)) {
                s.setReuseAddress(true);
                return p;
            } catch (IOException ignored) {}
        }
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException e) {
            logger.error("Impossible de trouver un port libre", e);
            return preferred;
        }
    }
}
