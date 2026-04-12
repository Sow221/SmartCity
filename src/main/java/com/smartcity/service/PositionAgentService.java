package com.smartcity.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Lit la position GPS d'un agent depuis GpsApiServer via HTTP.
 * Utilise localhost car le serveur tourne sur la même machine.
 */
public class PositionAgentService {

    private static final Logger logger = LoggerFactory.getLogger(PositionAgentService.class);

    private static final String BASE_URL = "http://localhost:" + GpsApiServer.PORT;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .build();

    public static class Position {
        public final double lat;
        public final double lon;
        public Position(double lat, double lon) { this.lat = lat; this.lon = lon; }
    }

    public Position getPosition(int agentId) {
        try {
            String token = GpsApiServer.getOrCreateToken(agentId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/position?agentId=" + agentId + "&token=" + token))
                    .timeout(Duration.ofSeconds(1))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("lat") && json.has("lon")) {
                    return new Position(json.get("lat").getAsDouble(), json.get("lon").getAsDouble());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.debug("Serveur GPS injoignable pour agent {}: {}", agentId, e.getMessage());
        }
        return null;
    }

    /** IP locale du serveur (pour construire les URLs QR code). */
    public static String getLocalIp() {
        return GpsApiServer.getLocalIp();
    }

    /** URL de la page GPS mobile pour un agent (avec token de sécurité). */
    public static String getGpsPageUrl(int agentId, String serverIp) {
        return GpsApiServer.getGpsPageUrl(agentId, serverIp);
    }
}
