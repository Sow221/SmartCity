package com.smartcity.service;

import com.smartcity.config.GeoConfig;
import com.smartcity.utils.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Serveur HTTP embarqué pour la géolocalisation GPS temps réel.
 * Agents et citoyens envoient leur position depuis un navigateur mobile.
 * Port : résolu dynamiquement via GeoConfig (3001 par défaut).
 */
public class GpsApiServer {


    /**
     * ℹ️ NOTES SUR LES PROTOCOLES:
     * 
     * HTTP (PORT 3001):
     *   - ✅ Fonctionne parfaitement sur WiFi local (192.168.x.x, 10.x.x.x)
     *   - ✅ Pas d'avertissement certificat
     *   - ✅ Idéal pour développement et tests
     *   - ✅ Utilisé par défaut si HTTPS bloqué
     * 
     * HTTPS (PORT 3002):
     *   - ✅ Sécurisé pour production
     *   - ⚠️ Certificat auto-signé → avertissements navigateur mobile
     *   - 💡 Solution: utiliser HTTP sur WiFi local, HTTPS en production
     * 
     * RECOMMANDATION: Pour développement/tests, préférer HTTP avec WiFi local
     */


    private static final Logger logger = LoggerFactory.getLogger(GpsApiServer.class);

    /** Port résolu une seule fois au démarrage — utilisé par PositionAgentService et MainApp. */
    public static final int PORT = GeoConfig.getGpsPort();
    public static final int HTTPS_PORT = PORT + 1;

    private static final ConcurrentHashMap<Integer, String> agentTokens   = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, String> citizenTokens = new ConcurrentHashMap<>();

    private HttpServer server;
    private HttpsServer httpsServer;

    // ── Tokens ──────────────────────────────────────────────────────────────

    public static String getOrCreateToken(int agentId) {
        String token = agentTokens.computeIfAbsent(agentId,
                id -> UUID.randomUUID().toString().replace("-", ""));
        saveTokenToDatabase(agentId, "agent", token);
        return token;
    }

    public static String getOrCreateCitizenToken(int citizenId) {
        String token = citizenTokens.computeIfAbsent(citizenId,
                id -> UUID.randomUUID().toString().replace("-", ""));
        saveTokenToDatabase(citizenId, "citizen", token);
        return token;
    }

    public static boolean isValidAgentToken(String token) {
        return token != null && agentTokens.values().stream().anyMatch(t -> t.equals(token));
    }

    // ── Démarrage ────────────────────────────────────────────────────────────

    public void start(int defaultAgentId) throws IOException {
        ensureGpsSchema();
        AffectationService.resetPositionTableFlag();

        int resolvedPort = GeoConfig.getGpsPort();
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", resolvedPort), 0);
        httpsServer = HttpsServer.create(new InetSocketAddress("0.0.0.0", HTTPS_PORT), 0);

        createContexts(server);
        createContexts(httpsServer);

        httpsServer.setHttpsConfigurator(new HttpsConfigurator(createSslContext()) {
            @Override
            public void configure(HttpsParameters params) {
                SSLContext c = getSSLContext();
                SSLParameters sslParams = c.getDefaultSSLParameters();
                sslParams.setNeedClientAuth(false);
                params.setSSLParameters(sslParams);
            }
        });

        var executor = Executors.newFixedThreadPool(8);
        server.setExecutor(executor);
        httpsServer.setExecutor(executor);

        server.start();
        httpsServer.start();
        logger.info("✅ GPS API Server démarré sur http://{}:{} (recommandé en test) et https://{}:{}", getLocalIp(), PORT, getLocalIp(), HTTPS_PORT);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
        if (httpsServer != null) {
            httpsServer.stop(0);
        }
        logger.info("GPS API Server arrêté");
    }

    private void createContexts(HttpServer server) {
        server.createContext("/api/position",        this::handlePosition);
        server.createContext("/api/citizen-position", this::handleCitizenPosition);
        server.createContext("/gps", exchange -> {
            int id = parseParam(exchange.getRequestURI().getQuery(), "agentId");
            if (id <= 0) { exchange.sendResponseHeaders(400, -1); return; }
            handleGpsPage(exchange, id, "agent");
        });
        server.createContext("/citizen-gps", exchange -> {
            int id = parseParam(exchange.getRequestURI().getQuery(), "citizenId");
            if (id <= 0) { exchange.sendResponseHeaders(400, -1); return; }
            handleGpsPage(exchange, id, "citizen");
        });
    }

    private SSLContext createSslContext() {
        try {
            KeyStore keyStore = createSelfSignedKeyStore();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            char[] password = "changeit".toCharArray();
            kmf.init(keyStore, password);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le SSLContext pour le serveur HTTPS", e);
        }
    }

    @SuppressWarnings("restriction")
    private KeyStore createSelfSignedKeyStore() throws Exception {
        char[] password = "changeit".toCharArray();
        // Generate RSA key pair
        java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new java.security.SecureRandom());
        java.security.KeyPair keyPair = keyGen.generateKeyPair();

        // Use Reflection to call sun.security.tools.keytool.CertAndKeyGen at runtime only
        // This avoids any compile-time dependency on internal JDK APIs
        Class<?> certAndKeyGenClass = Class.forName("sun.security.tools.keytool.CertAndKeyGen");
        Object certGen = certAndKeyGenClass
                .getConstructor(String.class, String.class)
                .newInstance("RSA", "SHA256withRSA");
        certAndKeyGenClass.getMethod("generate", int.class).invoke(certGen, 2048);

        java.security.PrivateKey privateKey = (java.security.PrivateKey)
                certAndKeyGenClass.getMethod("getPrivateKey").invoke(certGen);

        Class<?> x500NameClass = Class.forName("sun.security.x509.X500Name");
        Object owner = x500NameClass.getConstructor(String.class)
                .newInstance("CN=SmartCity GPS API, OU=SmartCity, O=SmartCity, L=Local, ST=None, C=FR");

        java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate)
                certAndKeyGenClass.getMethod("getSelfCertificate", x500NameClass, long.class)
                        .invoke(certGen, owner, 365L * 24 * 3600);

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setKeyEntry("gps", privateKey, password, new java.security.cert.Certificate[]{cert});
        return ks;
    }
    // ── URLs publiques ───────────────────────────────────────────────────────

    public static String getLocalIp() {
        // ✅ UTILISER LA SOURCE CENTRALISÉE (NetworkUtils) - UNIQUE SOURCE DE VÉRITÉ
        return com.smartcity.utils.NetworkUtils.detectLocalIp();
    }

    public static String getGpsPageUrl(int agentId) {
        return getGpsPageUrl(agentId, getLocalIp());
    }

    public static String getGpsPageUrl(int agentId, String ip) {
        String token = getOrCreateToken(agentId);
        return "https://" + ip + ":" + HTTPS_PORT + "/gps?agentId=" + agentId + "&token=" + token;
    }

    public static String getCitizenGpsPageUrl(int citizenId) {
        return getCitizenGpsPageUrl(citizenId, getLocalIp());
    }

    public static String getCitizenGpsPageUrl(int citizenId, String ip) {
        String token = getOrCreateCitizenToken(citizenId);
        return "https://" + ip + ":" + HTTPS_PORT + "/citizen-gps?citizenId=" + citizenId + "&token=" + token;
    }

    // ── Schéma DB ────────────────────────────────────────────────────────────

    private void ensureGpsSchema() {
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS position_agent ("
                    + "idAgent INT PRIMARY KEY, latitude DECIMAL(10,8) NOT NULL, "
                    + "longitude DECIMAL(11,8) NOT NULL, "
                    + "updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS position_citoyen ("
                    + "idCitoyen INT PRIMARY KEY, latitude DECIMAL(10,8) NOT NULL, "
                    + "longitude DECIMAL(11,8) NOT NULL, "
                    + "updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS gps_token ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, userId INT NOT NULL, "
                    + "userType ENUM('agent','citizen') NOT NULL, "
                    + "token VARCHAR(255) UNIQUE NOT NULL, "
                    + "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (userId) REFERENCES Utilisateur(idUser) ON DELETE CASCADE, "
                    + "UNIQUE KEY uq_user_type (userId, userType))");

            if (!hasColumn(conn, "Zone", "latitude")) {
                stmt.execute("ALTER TABLE Zone ADD COLUMN latitude DECIMAL(10,8) DEFAULT 0.0");
                stmt.execute("ALTER TABLE Zone ADD COLUMN longitude DECIMAL(11,8) DEFAULT 0.0");
                stmt.execute("UPDATE Zone SET latitude=14.7646, longitude=-17.3920 WHERE nomZone='Pikine'");
                stmt.execute("UPDATE Zone SET latitude=14.7765, longitude=-17.4047 "
                        + "WHERE nomZone IN ('Guédiawaye','Guediawaye')");
                logger.info("✅ Colonnes GPS Zone initialisées");
            }

            loadTokensFromDatabase(conn);

        } catch (SQLException e) {
            logger.warn("⚠️ Initialisation schéma GPS incomplète: {}", e.getMessage());
        }
    }

    private boolean hasColumn(Connection conn, String table, String col) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, table);
            ps.setString(2, col);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void loadTokensFromDatabase(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT userId, userType, token FROM gps_token "
                + "WHERE createdAt > DATE_SUB(NOW(), INTERVAL 30 DAY)");
             java.sql.ResultSet rs = ps.executeQuery()) {
            int count = 0;
            while (rs.next()) {
                int uid = rs.getInt("userId");
                String type = rs.getString("userType");
                String token = rs.getString("token");
                if ("agent".equals(type))   agentTokens.put(uid, token);
                else                        citizenTokens.put(uid, token);
                count++;
            }
            if (count > 0) logger.info("✅ {} tokens GPS rechargés depuis la base", count);
        } catch (SQLException e) {
            logger.warn("⚠️ Erreur chargement tokens GPS: {}", e.getMessage());
        }
    }

    public static void saveTokenToDatabase(int userId, String type, String token) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO gps_token (userId, userType, token) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE token=?, createdAt=NOW()")) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, token);
            ps.setString(4, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warn("⚠️ Erreur sauvegarde token GPS: {}", e.getMessage());
        }
    }

    // ── Handlers HTTP ────────────────────────────────────────────────────────

    private void handlePosition(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            int agentId = parseParam(exchange.getRequestURI().getQuery(), "agentId");
            String token = parseParamStr(exchange.getRequestURI().getQuery(), "token");
            if (agentId > 0 && agentTokens.getOrDefault(agentId, "").equals(token)) {
                double[] pos = readPosition(agentId);
                sendJson(exchange, 200, pos != null
                        ? String.format("{\"lat\":%.6f,\"lon\":%.6f}", pos[0], pos[1])
                        : "{\"status\":\"not_found\"}");
            } else {
                sendJson(exchange, 403, "{\"status\":\"forbidden\"}");
            }
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int agentId    = parseBodyInt(body, "agentId");
            double lat     = parseBodyDouble(body, "lat");
            double lon     = parseBodyDouble(body, "lon");
            String token   = parseBodyStr(body, "token");

            if (agentId > 0 && agentTokens.getOrDefault(agentId, "").equals(token)
                    && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                savePosition(agentId, lat, lon);
                sendJson(exchange, 200, "{\"status\":\"ok\"}");
            } else {
                sendJson(exchange, 400, "{\"status\":\"invalid\"}");
            }
        } catch (Exception e) {
            logger.error("❌ handlePosition: {}", e.getMessage());
            sendJson(exchange, 500, "{\"status\":\"error\"}");
        }
    }

    private void handleCitizenPosition(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            int citizenId = parseParam(exchange.getRequestURI().getQuery(), "citizenId");
            String token  = parseParamStr(exchange.getRequestURI().getQuery(), "token");
            if (citizenId > 0 && citizenTokens.getOrDefault(citizenId, "").equals(token)) {
                double[] pos = readCitizenPosition(citizenId);
                sendJson(exchange, 200, pos != null
                        ? String.format("{\"lat\":%.6f,\"lon\":%.6f}", pos[0], pos[1])
                        : "{\"status\":\"not_found\"}");
            } else {
                sendJson(exchange, 403, "{\"status\":\"forbidden\"}");
            }
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            int citizenId = parseBodyInt(body, "citizenId");
            double lat    = parseBodyDouble(body, "lat");
            double lon    = parseBodyDouble(body, "lon");
            String token  = parseBodyStr(body, "token");

            if (citizenId > 0 && citizenTokens.getOrDefault(citizenId, "").equals(token)
                    && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                saveCitizenPosition(citizenId, lat, lon);
                sendJson(exchange, 200, "{\"status\":\"ok\"}");
            } else {
                sendJson(exchange, 400, "{\"status\":\"invalid\"}");
            }
        } catch (Exception e) {
            logger.error("❌ handleCitizenPosition: {}", e.getMessage());
            sendJson(exchange, 500, "{\"status\":\"error\"}");
        }
    }

    private void handleGpsPage(HttpExchange exchange, int userId, String userType) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = buildGpsHtml(userId, userType).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    // ── DB positions ─────────────────────────────────────────────────────────

    private double[] readPosition(int agentId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT latitude, longitude FROM position_agent WHERE idAgent=?")) {
            ps.setInt(1, agentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new double[]{rs.getDouble(1), rs.getDouble(2)} : null;
            }
        } catch (SQLException e) { return null; }
    }

    private void savePosition(int agentId, double lat, double lon) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO position_agent (idAgent,latitude,longitude) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE latitude=?,longitude=?,updatedAt=NOW()")) {
            ps.setInt(1, agentId); ps.setDouble(2, lat); ps.setDouble(3, lon);
            ps.setDouble(4, lat);  ps.setDouble(5, lon);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("❌ savePosition agent {}: {}", agentId, e.getMessage());
        }
    }

    private double[] readCitizenPosition(int citizenId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT latitude, longitude FROM position_citoyen WHERE idCitoyen=?")) {
            ps.setInt(1, citizenId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new double[]{rs.getDouble(1), rs.getDouble(2)} : null;
            }
        } catch (SQLException e) { return null; }
    }

    private void saveCitizenPosition(int citizenId, double lat, double lon) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO position_citoyen (idCitoyen,latitude,longitude) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE latitude=?,longitude=?,updatedAt=NOW()")) {
            ps.setInt(1, citizenId); ps.setDouble(2, lat); ps.setDouble(3, lon);
            ps.setDouble(4, lat);    ps.setDouble(5, lon);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("❌ saveCitizenPosition citoyen {}: {}", citizenId, e.getMessage());
        }
    }

    // ── Helpers HTTP ─────────────────────────────────────────────────────────

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    private void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private int parseParam(String query, String key) {
        if (query == null) return -1;
        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                try { return Integer.parseInt(kv[1]); } catch (Exception e) { return -1; }
            }
        }
        return -1;
    }

    private String parseParamStr(String query, String key) {
        if (query == null) return null;
        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return kv[1];
        }
        return null;
    }

    private int parseBodyInt(String body, String key) {
        try { return Integer.parseInt(parseBodyStr(body, key)); } catch (Exception e) { return -1; }
    }

    private double parseBodyDouble(String body, String key) {
        try { return Double.parseDouble(parseBodyStr(body, key)); } catch (Exception e) { return 0; }
    }

    private String parseBodyStr(String body, String key) {
        for (String p : body.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return kv[1];
        }
        return "";
    }

    // ── Page HTML mobile ─────────────────────────────────────────────────────

    private String buildGpsHtml(int userId, String userType) {
        String token    = "agent".equals(userType) ? getOrCreateToken(userId) : getOrCreateCitizenToken(userId);
        String apiUrl   = "location.protocol+'//'+location.host+'/api/"
                        + ("agent".equals(userType) ? "position" : "citizen-position");
        String userParam = "agent".equals(userType) ? "agentId" : "citizenId";
        String title    = "agent".equals(userType) ? "Agent GPS" : "Citoyen GPS";
        String bgColor  = "agent".equals(userType) ? "#1565C0" : "#2E7D32";

        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
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
            + "  <p style='margin-bottom:8px;font-size:13px;'>Coordonn\u00e9es :</p>"
            + "  <input id='mlat' type='number' step='0.00001' placeholder='Latitude (ex: 14.76460)'/>"
            + "  <input id='mlon' type='number' step='0.00001' placeholder='Longitude (ex: -17.39200)'/>"
            + "  <button onclick='sendManual()'>\u2705 Confirmer</button>"
            + "</div>"
            + "<p id='warn'>\u26A0\uFE0F Le GPS n\u00e9cessite HTTPS sur mobile. Utilisez la saisie manuelle ou connectez-vous via WiFi local.</p>"
            + "<script>"
            + "var userId=" + userId + ",token='" + token + "',apiUrl=" + apiUrl + ",userParam='" + userParam + "';"
            + "function send(lat,lon,src){"
            + "  fetch(apiUrl,{method:'POST',"
            + "    headers:{'Content-Type':'application/x-www-form-urlencoded'},"
            + "    body:userParam+'='+userId+'&lat='+lat+'&lon='+lon+'&token='+token})"
            + "  .then(r=>r.json()).then(d=>{"
            + "    if(d.status==='ok'){"
            + "      document.getElementById('status').innerHTML='<span class=\"dot\"></span>Envoy\u00e9e ('+src+')';"
            + "      document.getElementById('coords').innerHTML=parseFloat(lat).toFixed(5)+', '+parseFloat(lon).toFixed(5);"
            + "    } else {"
            + "      document.getElementById('status').innerHTML='\u26A0\uFE0F '+d.status;"
            + "    }"
            + "  }).catch(()=>{"
            + "    document.getElementById('status').innerHTML='\u26A0\uFE0F Erreur r\u00e9seau. V\u00e9rifiez le WiFi.';"
            + "  });"
            + "}"
            + "function tryGps(){"
            + "  if(location.protocol!=='https:'){showManual();"
            + "    document.getElementById('status').innerHTML='\u26A0\uFE0F GPS n\u00e9cessite HTTPS \u2014 saisie manuelle';"
            + "    return;}"
            + "  document.getElementById('status').innerHTML='Demande GPS...';"
            + "  if(!navigator.geolocation){showManual();return;}"
            + "  navigator.geolocation.getCurrentPosition("
            + "    function(p){send(p.coords.latitude,p.coords.longitude,'GPS');},"
            + "    function(err){"
            + "      document.getElementById('status').innerHTML='\u26A0\uFE0F GPS refus\u00e9 \u2014 saisie manuelle';"
            + "      showManual();"
            + "    },{enableHighAccuracy:true,timeout:8000,maximumAge:0});"
            + "}"
            + "function showManual(){document.getElementById('manual').style.display='block';}"
            + "function sendManual(){"
            + "  var lat=parseFloat(document.getElementById('mlat').value);"
            + "  var lon=parseFloat(document.getElementById('mlon').value);"
            + "  if(isNaN(lat)||isNaN(lon)||lat<-90||lat>90||lon<-180||lon>180){alert('Invalide');return;}"
            + "  send(lat,lon,'Manuel');"
            + "}"
            + "if(location.protocol==='https:'){tryGps();}else{showManual();"
            + "  document.getElementById('status').innerHTML='\u26A0\uFE0F Connexion HTTP \u2014 saisie manuelle requise';}"
            + "</script></body></html>";
    }
}
