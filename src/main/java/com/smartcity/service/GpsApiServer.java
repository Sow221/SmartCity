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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serveur HTTP embarqué (JDK pur) pour recevoir la position GPS des agents ET citoyens
 * depuis leur téléphone via navigateur.
 *
 * Endpoints :
 *   POST /api/position  → body: agentId=X&lat=Y&lon=Z (agents)
 *   POST /api/citizen-position → body: citizenId=X&lat=Y&lon=Z (citoyens)
 *   GET  /api/position?agentId=X → lecture position agent
 *   GET  /api/citizen-position?citizenId=X → lecture position citoyen
 *   GET  /gps?agentId=X → page HTML agent
 *   GET  /citizen-gps?citizenId=X → page HTML citoyen
 */
public class GpsApiServer {

    private static final Logger logger = LoggerFactory.getLogger(GpsApiServer.class);
    public static final int PORT = 8081;

    // Token par agent : agentId -> token UUID
    private static final ConcurrentHashMap<Integer, String> agentTokens = new ConcurrentHashMap<>();
    // Token par citoyen : citizenId -> token UUID
    private static final ConcurrentHashMap<Integer, String> citizenTokens = new ConcurrentHashMap<>();

    /** Vérifie qu'un token correspond à un agent connu (utilisé par le WebSocket). */
    public static boolean isValidAgentToken(String token) {
        if (token == null || token.isBlank()) return false;
        return agentTokens.values().stream().anyMatch(t -> t.equals(token));
    }

    /** Génère (ou récupère) un token pour un agent. */
    public static String getOrCreateToken(int agentId) {
        return agentTokens.computeIfAbsent(agentId, id -> UUID.randomUUID().toString().replace("-", ""));
    }

    /** Génère (ou récupère) un token pour un citoyen. */
    public static String getOrCreateCitizenToken(int citizenId) {
        return citizenTokens.computeIfAbsent(citizenId, id -> UUID.randomUUID().toString().replace("-", ""));
    }

    private HttpServer server;

    public void start(int defaultAgentId) throws IOException {
        ensureGpsSchema();
        com.smartcity.service.AffectationService.resetPositionTableFlag();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/position", exchange -> handlePosition(exchange));
        server.createContext("/api/citizen-position", exchange -> handleCitizenPosition(exchange));
        server.createContext("/gps", exchange -> {
            int id = parseParam(exchange.getRequestURI().getQuery(), "agentId");
            handleGpsPage(exchange, id < 0 ? defaultAgentId : id, "agent");
        });
        server.createContext("/citizen-gps", exchange -> {
            int id = parseParam(exchange.getRequestURI().getQuery(), "citizenId");
            handleGpsPage(exchange, id, "citizen");
        });
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        logger.info("GPS API Server démarré sur le port {} (Agents + Citoyens)", PORT);
    }

    /**
     * Auto-répare le schéma minimal GPS si la base est incomplète.
     * Cela évite les erreurs en boucle sur position_agent/position_citoyen.
     */
    private void ensureGpsSchema() {
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS position_agent (
                    idAgent INT PRIMARY KEY,
                    latitude DECIMAL(10,8) NOT NULL,
                    longitude DECIMAL(11,8) NOT NULL,
                    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
                )
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS position_citoyen (
                    idCitoyen INT PRIMARY KEY,
                    latitude DECIMAL(10,8) NOT NULL,
                    longitude DECIMAL(11,8) NOT NULL,
                    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
                )
                """);

            if (!hasZoneGpsColumns(conn)) {
                stmt.execute("ALTER TABLE Zone ADD COLUMN latitude DECIMAL(10,8) DEFAULT 0.0");
                stmt.execute("ALTER TABLE Zone ADD COLUMN longitude DECIMAL(11,8) DEFAULT 0.0");
                stmt.execute("UPDATE Zone SET latitude = 14.7646, longitude = -17.3920 WHERE nomZone = 'Pikine'");
                stmt.execute("UPDATE Zone SET latitude = 14.7765, longitude = -17.4047 WHERE nomZone IN ('Guédiawaye', 'Guediawaye')");
                logger.info("Colonnes GPS ajoutées à Zone (migration automatique).");
            }
        } catch (SQLException e) {
            logger.warn("Initialisation automatique du schéma GPS incomplète: {}", e.getMessage());
        }
    }

    private boolean hasZoneGpsColumns(Connection conn) {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Zone' "
            + "AND COLUMN_NAME IN ('latitude', 'longitude')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 2;
        } catch (SQLException e) {
            return false;
        }
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
            // Priorité : IP LAN (192.168.x.x ou 10.x.x.x) avant les autres
            java.util.List<String> candidates = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .filter(ni -> {
                    try { return ni.isUp() && !ni.isLoopback() && !ni.isVirtual(); }
                    catch (Exception e) { return false; }
                })
                .flatMap(ni -> Collections.list(ni.getInetAddresses()).stream())
                .filter(addr -> !addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                .map(addr -> addr.getHostAddress())
                .collect(java.util.stream.Collectors.toList());
            // Préférer 192.168.x.x ou 10.x.x.x
            return candidates.stream()
                .filter(ip -> ip.startsWith("192.168.") || ip.startsWith("10."))
                .findFirst()
                .orElse(candidates.isEmpty() ? "localhost" : candidates.get(0));
        } catch (Exception e) {
            return "localhost";
        }
    }

    public static String getGpsPageUrl(int agentId, String ip) {
        String token = getOrCreateToken(agentId);
        return "http://" + ip + ":" + PORT + "/gps?agentId=" + agentId + "&token=" + token;
    }

    public static String getGpsPageUrl(int agentId) {
        return getGpsPageUrl(agentId, getLocalIp());
    }

    public static String getCitizenGpsPageUrl(int citizenId, String ip) {
        String token = getOrCreateCitizenToken(citizenId);
        return "http://" + ip + ":" + PORT + "/citizen-gps?citizenId=" + citizenId + "&token=" + token;
    }

    public static String getCitizenGpsPageUrl(int citizenId) {
        return getCitizenGpsPageUrl(citizenId, getLocalIp());
    }

    // ── Handlers ────────────────────────────────────────────────────────────

    private void handleCitizenPosition(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // GET /api/citizen-position?citizenId=X — lecture position (token requis)
        if ("GET".equals(exchange.getRequestMethod())) {
            int citizenId = parseParam(exchange.getRequestURI().getQuery(), "citizenId");
            String token = parseParamStr(exchange.getRequestURI().getQuery(), "token");
            String expected = citizenTokens.get(citizenId);
            if (citizenId <= 0 || expected == null || !expected.equals(token)) {
                sendResponse(exchange, 403, "{\"status\":\"forbidden\"}");
                return;
            }
            double[] pos = readCitizenPosition(citizenId);
            if (pos != null) {
                sendResponse(exchange, 200,
                    String.format("{\"lat\":%.6f,\"lon\":%.6f}", pos[0], pos[1]));
            } else {
                sendResponse(exchange, 404, "{\"status\":\"not_found\"}");
            }
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            String[] params = body.split("&");
            int citizenId = 0;
            double lat = 0, lon = 0;
            String token = null;
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "citizenId" -> citizenId = Integer.parseInt(kv[1]);
                        case "lat"       -> lat = Double.parseDouble(kv[1]);
                        case "lon"       -> lon = Double.parseDouble(kv[1]);
                        case "token"     -> token = kv[1];
                    }
                }
            }

            // Valider le token
            String expected = citizenTokens.get(citizenId);
            if (expected == null || !expected.equals(token)) {
                logger.warn("🚫 Token GPS invalide pour citizenId={}", citizenId);
                sendResponse(exchange, 403, "{\"status\":\"forbidden\"}");
                return;
            }

            if (citizenId > 0 && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180
                    && (lat != 0 || lon != 0)) {
                // Vérifier que la position est dans le rayon d'une zone connue (10 km)
                boolean inAnyZone = isPositionInAnyZone(lat, lon);
                if (!inAnyZone) {
                    logger.warn("🚫 Position GPS hors zone autorisée pour citoyen {}: ({}, {})", citizenId, lat, lon);
                    sendResponse(exchange, 400, "{\"status\":\"out_of_bounds\"}");
                    return;
                }
                
                saveCitizenPosition(citizenId, lat, lon);
                sendResponse(exchange, 200, "{\"status\":\"ok\"}");
            } else {
                sendResponse(exchange, 400, "{\"status\":\"invalid\"}");
            }
        } catch (Exception e) {
            logger.error("Erreur parsing position GPS citoyen", e);
            sendResponse(exchange, 500, "{\"status\":\"error\"}");
        }
    }

    private void handlePosition(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // GET /api/position?agentId=X — lecture position (token requis)
        if ("GET".equals(exchange.getRequestMethod())) {
            int agentId = parseParam(exchange.getRequestURI().getQuery(), "agentId");
            String token = parseParamStr(exchange.getRequestURI().getQuery(), "token");
            String expected = agentTokens.get(agentId);
            if (agentId <= 0 || expected == null || !expected.equals(token)) {
                sendResponse(exchange, 403, "{\"status\":\"forbidden\"}");
                return;
            }
                    double[] pos = readPosition(agentId);
                    if (pos != null) {
                        sendResponse(exchange, 200,
                            String.format("{\"lat\":%.6f,\"lon\":%.6f}", pos[0], pos[1]));
            } else {
                    sendResponse(exchange, 404, "{\"status\":\"not_found\"}");
                }
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            String[] params = body.split("&");
            int agentId = 0;
            double lat = 0, lon = 0;
            String token = null;
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    switch (kv[0]) {
                        case "agentId" -> agentId = Integer.parseInt(kv[1]);
                        case "lat"     -> lat = Double.parseDouble(kv[1]);
                        case "lon"     -> lon = Double.parseDouble(kv[1]);
                        case "token"   -> token = kv[1];
                    }
                }
            }

            // Valider le token
            String expected = agentTokens.get(agentId);
            if (expected == null || !expected.equals(token)) {
                logger.warn("🚫 Token GPS invalide pour agentId={}", agentId);
                sendResponse(exchange, 403, "{\"status\":\"forbidden\"}");
                return;
            }

            if (agentId > 0 && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180
                    && (lat != 0 || lon != 0)) {
                // Vérifier que la position est dans le rayon d'une zone connue (5 km)
                boolean inAnyZone = isPositionInAnyZone(lat, lon);
                if (!inAnyZone) {
                    logger.warn("🚫 Position GPS hors zone autorisée pour agent {}: ({}, {})", agentId, lat, lon);
                    sendResponse(exchange, 400, "{\"status\":\"out_of_bounds\"}");
                    return;
                }
                
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

    private void handleGpsPage(HttpExchange exchange, int userId, String userType) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        String html = buildGpsHtml(userId, userType);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private double[] readPosition(int agentId) {
        String sql = "SELECT latitude, longitude FROM position_agent WHERE idAgent = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new double[]{rs.getDouble(1), rs.getDouble(2)};
            }
        } catch (java.sql.SQLSyntaxErrorException e) {
            logger.warn("Table position_agent absente — exécuter scripts/migrations/2026-04-create_gps_tables.sql");
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
        } catch (java.sql.SQLSyntaxErrorException e) {
            logger.warn("Table position_agent absente — exécuter scripts/migrations/2026-04-create_gps_tables.sql");
        } catch (SQLException e) {
            logger.error("Erreur sauvegarde position agent {}", agentId, e);
        }
    }

    private double[] readCitizenPosition(int citizenId) {
        String sql = "SELECT latitude, longitude FROM position_citoyen WHERE idCitoyen = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, citizenId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new double[]{rs.getDouble(1), rs.getDouble(2)};
            }
        } catch (java.sql.SQLSyntaxErrorException e) {
            logger.warn("Table position_citoyen absente — exécuter scripts/migrations/2026-04-create_gps_tables.sql");
        } catch (SQLException e) {
            logger.error("Erreur lecture position citoyen {}", citizenId, e);
        }
        return null;
    }

    private void saveCitizenPosition(int citizenId, double lat, double lon) {
        String sql = "INSERT INTO position_citoyen (idCitoyen, latitude, longitude, updatedAt) "
                   + "VALUES (?, ?, ?, NOW()) "
                   + "ON DUPLICATE KEY UPDATE latitude=?, longitude=?, updatedAt=NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, citizenId);
            ps.setDouble(2, lat);
            ps.setDouble(3, lon);
            ps.setDouble(4, lat);
            ps.setDouble(5, lon);
            ps.executeUpdate();
        } catch (java.sql.SQLSyntaxErrorException e) {
            logger.warn("Table position_citoyen absente — exécuter scripts/migrations/2026-04-create_gps_tables.sql");
        } catch (SQLException e) {
            logger.error("Erreur sauvegarde position citoyen {}", citizenId, e);
        }
    }

    private boolean isPositionInAnyZone(double lat, double lon) {
        // Vérifier d'abord que les colonnes GPS existent dans Zone
        String sql = "SELECT latitude, longitude FROM Zone WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND latitude != 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            boolean hasZones = false;
            while (rs.next()) {
                hasZones = true;
                double zoneLat = rs.getDouble(1);
                double zoneLon = rs.getDouble(2);
                double distKm = haversineKm(lat, lon, zoneLat, zoneLon);
                if (distKm <= 10.0) return true;
            }
            // Si aucune zone n'a de coordonnées GPS, accepter par défaut
            if (!hasZones) {
                logger.warn("Aucune zone avec coordonnées GPS — position acceptée par défaut");
                return true;
            }
        } catch (SQLException e) {
            logger.warn("Erreur vérification zone GPS, position acceptée par défaut: {}", e.getMessage());
            return true; // fail-open
        }
        return false;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private static int parseParam(String query, String key) {
        if (query == null) return -1;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                try { return Integer.parseInt(kv[1]); } catch (NumberFormatException e) { return -1; }
            }
        }
        return -1;
    }

    private static String parseParamStr(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return kv[1];
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("======================================");
            System.out.println("  SERVEUR GPS API SMARTCITY");
            System.out.println("======================================");
            System.out.println("");
            
            GpsApiServer server = new GpsApiServer();
            server.start(0);
            
            System.out.println("✅ Serveur demarre sur le port 8081");
            System.out.println("✅ Accessible sur http://localhost:8081");
            System.out.println("");
            System.out.println("Appuyez sur CTRL+C pour arreter");
            System.out.println("======================================");
            
            // Garder le serveur en vie
            synchronized (GpsApiServer.class) {
                GpsApiServer.class.wait();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur demarrage serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Page HTML servie au téléphone de l'agent ou du citoyen.
     * watchPosition() envoie la position automatiquement.
     */
    private String buildGpsHtml(int userId, String userType) {
        String token = userType.equals("agent") ? getOrCreateToken(userId) : getOrCreateCitizenToken(userId);
        String apiUrl = "http://" + getLocalIp() + ":" + PORT + "/api/" + (userType.equals("agent") ? "position" : "citizen-position");
        String userParam = userType.equals("agent") ? "agentId" : "citizenId";
        String title = userType.equals("agent") ? "Agent GPS" : "Citoyen GPS";
        String bgColor = userType.equals("agent") ? "#1565C0" : "#2E7D32";
        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<title>SmartCity GPS</title>"
            + "<style>"
            + "*{box-sizing:border-box;margin:0;padding:0;}"
            + "body{font-family:Arial,sans-serif;background:" + bgColor + ";color:white;"
            + "display:flex;flex-direction:column;align-items:center;justify-content:center;"
            + "min-height:100vh;padding:20px;text-align:center;}"
            + "h2{font-size:20px;margin-bottom:16px;}"
            + "#status{font-size:15px;margin:10px 0;padding:14px 20px;"
            + "background:rgba(255,255,255,0.15);border-radius:12px;width:100%;max-width:320px;}"
            + "#coords{font-size:13px;color:rgba(255,255,255,0.8);margin:6px 0;}"
            + ".dot{width:12px;height:12px;background:#4CAF50;border-radius:50%;"
            + "display:inline-block;margin-right:6px;animation:pulse 1.5s infinite;}"
            + "@keyframes pulse{0%,100%{opacity:1;}50%{opacity:0.3;}}"
            + ".btn{background:rgba(255,255,255,0.25);color:white;border:2px solid rgba(255,255,255,0.6);"
            + "padding:12px 24px;border-radius:10px;font-size:15px;cursor:pointer;"
            + "margin:8px 0;width:100%;max-width:320px;}"
            + ".btn:active{background:rgba(255,255,255,0.4);}"
            + "#manual{display:none;margin-top:12px;width:100%;max-width:320px;}"
            + "#manual input{width:100%;padding:10px;border-radius:8px;border:none;"
            + "font-size:14px;margin:4px 0;color:#333;}"
            + "#manual button{background:#FF9800;color:white;border:none;padding:12px;"
            + "border-radius:8px;font-size:15px;cursor:pointer;width:100%;margin-top:8px;}"
            + "#warn{font-size:12px;color:rgba(255,255,255,0.7);margin-top:10px;max-width:320px;}"
            + "</style></head><body>"
            + "<h2>\uD83D\uDCCD SmartCity " + title + "</h2>"
            + "<div id='status'>Activation GPS...</div>"
            + "<div id='coords'></div>"
            + "<button class='btn' onclick='tryGps()'>\uD83D\uDCF1 Envoyer ma position GPS</button>"
            + "<button class='btn' onclick='showManual()'>\u270F\uFE0F Saisir manuellement</button>"
            + "<div id='manual'>"
            + "  <p style='margin-bottom:8px;font-size:13px;'>Entrez vos coordonn\u00e9es :</p>"
            + "  <input id='mlat' type='number' step='0.00001' placeholder='Latitude (ex: 14.76460)'/>"
            + "  <input id='mlon' type='number' step='0.00001' placeholder='Longitude (ex: -17.39200)'/>"
            + "  <button onclick='sendManual()'>\u2705 Confirmer</button>"
            + "</div>"
            + "<p id='warn'>Si le GPS est bloqu\u00e9, utilisez la saisie manuelle ou cliquez directement sur la carte dans l'application.</p>"
            + "<script>"
            + "var userId=" + userId + ";"
            + "var token='" + token + "';"
            + "var apiUrl='" + apiUrl + "';"
            + "var userParam='" + userParam + "';"
            + "var sent=false;"
            + "function sendPosition(lat,lon,src){"
            + "  if(sent)return;"
            + "  fetch(apiUrl,{method:'POST',"
            + "    headers:{'Content-Type':'application/x-www-form-urlencoded'},"
            + "    body:userParam+'='+userId+'&lat='+lat+'&lon='+lon+'&token='+token})"
            + "  .then(r=>r.json()).then(d=>{"
            + "    if(d.status==='ok'){"
            + "      sent=true;"
            + "      document.getElementById('status').innerHTML='<span class=\'dot\'></span>Position envoy\u00e9e ('+src+')';"
            + "      document.getElementById('coords').innerHTML=parseFloat(lat).toFixed(5)+', '+parseFloat(lon).toFixed(5);"
            + "    } else {"
            + "      document.getElementById('status').innerHTML='\u26A0\uFE0F Hors zone autoris\u00e9e';"
            + "    }"
            + "  }).catch(()=>{"
            + "    document.getElementById('status').innerHTML='\u26A0\uFE0F Erreur r\u00e9seau. V\u00e9rifiez le WiFi.';"
            + "  });"
            + "}"
            + "function tryGps(){"
            + "  document.getElementById('status').innerHTML='Demande GPS en cours...';"
            + "  if(!navigator.geolocation){"
            + "    document.getElementById('status').innerHTML='GPS non support\u00e9 — utilisez la saisie manuelle';"
            + "    showManual(); return;"
            + "  }"
            + "  navigator.geolocation.getCurrentPosition("
            + "    function(p){sendPosition(p.coords.latitude,p.coords.longitude,'GPS');},"
            + "    function(err){"
            + "      var msg=err.code===1?'GPS refus\u00e9 (HTTPS requis sur ce navigateur)':err.message;"
            + "      document.getElementById('status').innerHTML='\u26A0\uFE0F '+msg;"
            + "      showManual();"
            + "    },"
            + "    {enableHighAccuracy:true,timeout:8000,maximumAge:0}"
            + "  );"
            + "}"
            + "function showManual(){"
            + "  document.getElementById('manual').style.display='block';"
            + "}"
            + "function sendManual(){"
            + "  var lat=parseFloat(document.getElementById('mlat').value);"
            + "  var lon=parseFloat(document.getElementById('mlon').value);"
            + "  if(isNaN(lat)||isNaN(lon)||lat<-90||lat>90||lon<-180||lon>180){"
            + "    alert('Coordonn\u00e9es invalides'); return;"
            + "  }"
            + "  sendPosition(lat,lon,'Manuel');"
            + "}"
            + "// Tentative automatique au chargement"
            + "tryGps();"
            + "</script></body></html>";
    }
}
