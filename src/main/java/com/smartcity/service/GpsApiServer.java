package com.smartcity.service;

import com.smartcity.utils.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serveur HTTP embarqué (JDK pur) pour recevoir la position GPS des agents
 * depuis leur téléphone via navigateur.
 *
 * Endpoints :
 *   POST /api/position  → body: agentId=X&lat=Y&lon=Z
 *   GET  /gps           → page HTML servie au téléphone de l'agent
 */
public class GpsApiServer {

    private static final Logger logger = LoggerFactory.getLogger(GpsApiServer.class);
    public static final int PORT = 8081;

    private HttpServer server;

    public void start(int agentId) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/position", exchange -> handlePosition(exchange));
        server.createContext("/gps", exchange -> handleGpsPage(exchange, agentId));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        logger.info("GPS API Server démarré sur le port {}", PORT);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.info("GPS API Server arrêté");
        }
    }

    /** Retourne l'IP locale du PC pour construire l'URL à afficher/QR coder */
    public static String getLocalIp() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                .filter(addr -> !addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                .map(addr -> addr.getHostAddress())
                .findFirst()
                .orElse("localhost");
        } catch (Exception e) {
            return "localhost";
        }
    }

    public static String getGpsPageUrl(int agentId) {
        return "http://" + getLocalIp() + ":" + PORT + "/gps?agentId=" + agentId;
    }

    // ── Handlers ────────────────────────────────────────────────────────────

    private void handlePosition(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // GET /api/position?agentId=X — lecture position
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("agentId=")) {
                try {
                    int agentId = Integer.parseInt(query.replace("agentId=", "").split("&")[0]);
                    double[] pos = readPosition(agentId);
                    if (pos != null) {
                        sendResponse(exchange, 200,
                            String.format("{\"lat\":%.6f,\"lon\":%.6f}", pos[0], pos[1]));
                    } else {
                        sendResponse(exchange, 404, "{\"status\":\"not_found\"}");
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"status\":\"invalid\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"status\":\"missing_agentId\"}");
            }
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        // body: agentId=1&lat=14.76460&lon=-17.39200
        try {
            String[] params = body.split("&");
            int agentId = 0;
            double lat = 0, lon = 0;
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "agentId" -> agentId = Integer.parseInt(kv[1]);
                        case "lat"     -> lat = Double.parseDouble(kv[1]);
                        case "lon"     -> lon = Double.parseDouble(kv[1]);
                    }
                }
            }

            if (agentId > 0 && lat != 0 && lon != 0) {
                savePosition(agentId, lat, lon);
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            } else {
                sendResponse(exchange, 400, "{\"status\":\"invalid\"}");
            }
        } catch (Exception e) {
            logger.error("Erreur parsing position GPS", e);
            sendResponse(exchange, 500, "{\"status\":\"error\"}");
        }
    }

    private void handleGpsPage(HttpExchange exchange, int agentId) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        String html = buildGpsHtml(agentId);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private double[] readPosition(int agentId) {
        String sql = "SELECT latitude, longitude FROM position_agent WHERE idAgent = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new double[]{rs.getDouble(1), rs.getDouble(2)};
            }
        } catch (SQLException e) {
            logger.error("Erreur lecture position agent {}", agentId, e);
        }
        return null;
    }

    private void savePosition(int agentId, double lat, double lon) {
        String sql = "INSERT INTO position_agent (idAgent, latitude, longitude, updatedAt) "
                   + "VALUES (?, ?, ?, NOW()) "
                   + "ON DUPLICATE KEY UPDATE latitude=?, longitude=?, updatedAt=NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            ps.setDouble(2, lat);
            ps.setDouble(3, lon);
            ps.setDouble(4, lat);
            ps.setDouble(5, lon);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur sauvegarde position agent {}", agentId, e);
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Page HTML servie au téléphone de l'agent.
     * watchPosition() envoie la position toutes les 5s automatiquement.
     */
    private String buildGpsHtml(int agentId) {
        String apiUrl = "http://" + getLocalIp() + ":" + PORT + "/api/position";
        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<title>SmartCity GPS</title>"
            + "<style>"
            + "body{font-family:Arial,sans-serif;background:#1565C0;color:white;"
            + "display:flex;flex-direction:column;align-items:center;justify-content:center;"
            + "height:100vh;margin:0;text-align:center;}"
            + "h2{font-size:22px;margin-bottom:10px;}"
            + "#status{font-size:15px;margin:10px;padding:12px 20px;"
            + "background:rgba(255,255,255,0.15);border-radius:12px;min-width:260px;}"
            + "#coords{font-size:13px;color:#B3E5FC;margin-top:8px;}"
            + ".dot{width:14px;height:14px;background:#4CAF50;border-radius:50%;"
            + "display:inline-block;margin-right:8px;animation:pulse 1.5s infinite;}"
            + "@keyframes pulse{0%,100%{opacity:1;}50%{opacity:0.3;}}"
            + "</style></head><body>"
            + "<h2>&#128205; SmartCity GPS</h2>"
            + "<div id='status'>Initialisation...</div>"
            + "<div id='coords'></div>"
            + "<script>"
            + "var agentId=" + agentId + ";"
            + "var apiUrl='" + apiUrl + "';"
            + "var watchId=null;"
            + "function sendPosition(lat,lon){"
            + "  fetch(apiUrl,{method:'POST',"
            + "    headers:{'Content-Type':'application/x-www-form-urlencoded'},"
            + "    body:'agentId='+agentId+'&lat='+lat+'&lon='+lon})"
            + "  .then(()=>{"
            + "    document.getElementById('status').innerHTML="
            + "      '<span class=\"dot\"></span>Position envoy\u00e9e';"
            + "    document.getElementById('coords').innerHTML="
            + "      lat.toFixed(5)+', '+lon.toFixed(5);"
            + "  }).catch(()=>{"
            + "    document.getElementById('status').innerHTML='&#9888; Erreur r\u00e9seau';"
            + "  });"
            + "}"
            + "if(navigator.geolocation){"
            + "  watchId=navigator.geolocation.watchPosition("
            + "    function(pos){sendPosition(pos.coords.latitude,pos.coords.longitude);},"
            + "    function(err){document.getElementById('status').innerHTML='&#9888; GPS: '+err.message;},"
            + "    {enableHighAccuracy:true,maximumAge:5000,timeout:10000}"
            + "  );"
            + "  document.getElementById('status').innerHTML='Activation GPS...';"
            + "}else{"
            + "  document.getElementById('status').innerHTML='GPS non support\u00e9';"
            + "}"
            + "</script></body></html>";
    }
}
