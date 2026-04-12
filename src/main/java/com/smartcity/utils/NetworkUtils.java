package com.smartcity.utils;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ Utilitaires réseau centralisés - UNIQUE SOURCE DE VÉRITÉ
 * Évite la duplication entre GeoConfig et GpsApiServer
 */
public class NetworkUtils {

    /**
     * Détecte l'adresse IP locale (WiFi ou LAN)
     * Préfère 192.168.* ou 10.*, sinon utilise la première adresse non-loopback
     * @return IP locale ou "localhost" si non trouvée
     */
    public static String detectLocalIp() {
        try {
            List<String> candidates = Collections
                    .list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .filter(ni -> {
                        try { 
                            return ni.isUp() && !ni.isLoopback() && !ni.isVirtual(); 
                        } catch (Exception e) { 
                            return false; 
                        }
                    })
                    .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                    .filter(addr -> !addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                    .map(java.net.InetAddress::getHostAddress)
                    .collect(Collectors.toList());
            
            // Préférer les adresses privées (plus fiables)
            return candidates.stream()
                    .filter(ip -> ip.startsWith("192.168.") || ip.startsWith("10."))
                    .findFirst()
                    .orElse(candidates.isEmpty() ? "localhost" : candidates.get(0));
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * Teste si un port est disponible
     */
    public static boolean isPortAvailable(int port) {
        try (java.net.ServerSocket ss = new java.net.ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    /**
     * Trouve un port libre en partant d'un port préféré
     */
    public static int findFreePort(int preferred) {
        for (int p = preferred; p < preferred + 20; p++) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        return preferred; // Fallback - laisse l'OS choisir
    }
}
