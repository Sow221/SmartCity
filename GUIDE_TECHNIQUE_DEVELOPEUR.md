# 🔧 GUIDE TECHNIQUE DÉVELOPPEUR - GÉOLOCALISATION SmartCity

## ARCHITECTURE GLOBALE

```
┌─────────────────────────────────────────────────────────────┐
│                       APPLICATION UI                        │
│  ┌─────────────────┬────────────────────────────────────┐  │
│  │ AgentDashboard  │    CitizenDashboard               │  │
│  │ - Tournée       │    - Signalement                  │  │
│  │ - Cartes        │    - QR Code                      │  │
│  │ - Real-time     │    - GPS interactif               │  │
│  └────────┬────────┴────────────────────────────────────┘  │
│           ↓                                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ RealTimeGPSService + GeolocationService              │   │
│  │ - Tracking 10s Timer                                 │   │
│  │ - Distance calculations (Haversine)                  │   │
│  │ - Route optimization (TSP greedy)                    │   │
│  └────────────────────┬─────────────────────────────────┘   │
│                       ↓                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ PositionAgentService (HTTP Client)                   │   │
│  │ - GET :8081/api/position?agentId=X&token=Y          │   │
│  │ - Timeout 1s / fail-fast                             │   │
│  └────────────────────┬─────────────────────────────────┘   │
└─────────────────────────┼──────────────────────────────────┘
                          │ HTTP localhost:8081
        ┌─────────────────┼──────────────────────┐
        │                 ↓                      ↓
   ┌─────────────────────────────────┐  ┌──────────────┐
   │   GpsApiServer                  │  │ MySQL DB     │
   │ - POST /api/position (agent)    │  │ - position_  │
   │ - GET /api/position             │  │   agent      │
   │ - POST /api/citizen-position    │  │ - position_  │
   │ - GET /citizen-gps (HTML+GPS)   │  │   citoyen    │
   │ - Token validation (UUID)       │  │ - Zone (GPS) │
   │ - Haversine zone check          │  │ - Signalement │
   │ - UPSERT position_agent/citoyen │  │   (GPS)      │
   └─────────────────────────────────┘  └──────────────┘
```

---

## SERVICES DÉTAIL

### 1. GeolocationService

**Location:** `src/main/java/com/smartcity/service/GeolocationService.java`

**Public Methods:**
```java
// Distance calculation
public static double distanceBetween(double lat1, double lon1, double lat2, double lon2)
// Returns: kilometers (Haversine formula R=6371km)

// Route optimization
public List<Signalement> optimizeCollectionRoute(List<Signalement> signalements, Coordinates startPoint)
// Returns: Ordered list (greedy nearest-neighbor TSP)

// Zone center
public Coordinates getZoneCenter(String nomZone)
// Returns: Zone lat/lon from DB via ZoneService

// Current agent position
public Coordinates getCurrentPosition()
// Returns: Zone center of SessionManager.getAgentId()

// URL generation
public String getGoogleMapsUrl(double lat, double lon)
public String getMultiPointRouteUrl(List<Signalement> signalements)
// Returns: Valid Google Maps URLs
```

**Key Points:**
- Haversine = sphere calculation (good enough for small distances)
- Filter: missions with (lat=0 AND lon=0) excluded from optimization
- Greedy TSP = O(n²) performance acceptable for < 100 missions
- Coordinates inner class: immutable (lat, lon final)

---

### 2. RealTimeGPSService

**Location:** `src/main/java/com/smartcity/service/RealTimeGPSService.java`

**Lifecycle:**
```
Service instantiation
  ↓ (when Agent connects)
startRealTimeTracking()
  ├─ Timer("gps-tracker", daemon=true)
  ├─ scheduleAtFixedRate(pollPositionFromServer(), delay=0ms, period=10000ms)
  └─ [RUNS FOREVER] until stopRealTimeTracking()

stopRealTimeTracking()
  ├─ locationTracker.cancel()
  ├─ listeners.clear()
  └─ currentAgentPosition = null
```

**Listener Pattern:**

```java
public interface LocationUpdateListener {
    void onLocationUpdate(Coordinates newPosition);
    void onMissionsDistanceUpdate(List<MissionWithDistance> missionsWithDistances);
}

// In AgentDashboardController:
gpsService.addLocationUpdateListener(new LocationUpdateListener() {
    @Override
    public void onLocationUpdate(Coordinates pos) {
        updatePositionLabel(pos);  // Update UI label
    }
    
    @Override
    public void onMissionsDistanceUpdate(List<MissionWithDistance> distances) {
        updateLiveDistances(distances);  // Refresh mission distances
        checkNearbyMissions(distances);  // Alert if < 1km
    }
});
```

**MissionWithDistance Details:**
```java
public class MissionWithDistance {
    public final Signalement mission;
    public final double distanceKm;
    public final int estimatedMinutes;      // ceil(km * 4)
    public final boolean isNearby;          // < 1km
    public final boolean isClosest;         // Marked as closest in batch
    public final String directionText;      // Formatted: "À pied (0.5km)"
}
```

**Platform.runLater() usage:**
- All listeners called from Timer thread (daemon)
- MUST wrap UI updates in Platform.runLater()
- Failure = IllegalStateException (not FX thread)

---

### 3. PositionAgentService

**Location:** `src/main/java/com/smartcity/service/PositionAgentService.java`

**Responsibilities:**
```java
public Position getPosition(int agentId)
// The worker method:
// 1. Get token from GpsApiServer.getOrCreateToken()
// 2. HttpClient GET http://localhost:8081/api/position?agentId=X&token=Y
// 3. Parse JSON: {"lat": 14.7650, "lon": -17.3920}
// 4. OR null if timeout/error

// HttpClient config:
private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(1))
    .build();

// Request config:
HttpRequest.newBuilder()
    .uri(URI.create(SERVER_URL + "/api/position?..."))
    .timeout(Duration.ofSeconds(1))
    .GET()
    .build()
```

**Error Handling:**
```java
if (response.statusCode() == 200) {
    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
    return new Position(json.get("lat").getAsDouble(), json.get("lon").getAsDouble());
}
// else
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    logger.warn("Interruption lors de la requête GPS pour agent {}", agentId);
} catch (Exception e) {
    logger.debug("Serveur GPS injoignable pour agent {}: {}", agentId, e.getMessage());
}
return null;  // Graceful fail
```

**Token Security:**
```java
String token = GpsApiServer.getOrCreateToken(agentId);
// Tokens by agentId stored as: Map<Integer, String> agentTokens
// UUID format: no hyphens (32 hex chars)
// Valid for session lifetime
```

---

### 4. GpsApiServer

**Location:** `src/main/java/com/smartcity/service/GpsApiServer.java`

**Server Setup:**
```java
server = HttpServer.create(new InetSocketAddress(PORT), 0);
server.createContext("/api/position", exchange -> handlePosition(exchange));
server.createContext("/api/citizen-position", exchange -> handleCitizenPosition(exchange));
server.createContext("/gps", exchange -> handleGpsPage(exchange, id, "agent"));
server.createContext("/citizen-gps", exchange -> handleGpsPage(exchange, id, "citizen"));

server.setExecutor(Executors.newFixedThreadPool(4));  // 4 threads max
server.start();
```

**Port:** `8081` (hardcoded - consider config.properties)

**Endpoints:**

| Path | Method | Input | Output | Notes |
|------|--------|-------|--------|-------|
| `/api/position` | GET | `?agentId=X&token=Y` | `{"lat":,.,"lon":.}` or 403/404 | Read position |
| `/api/position` | POST | Body: `agentId=X&lat=..&lon=..&token=Y` | `{"status":"ok"}` or 400/403 | Write position |
| `/api/citizen-position` | GET | `?citizenId=X&token=Y` | `{"lat":,.,"lon":.}` | Read citizen pos |
| `/api/citizen-position` | POST | Body: `citizenId=X&lat=..&lon=..&token=Y` | `{"status":"ok"}` | Write citizen pos |
| `/gps` | GET | `?agentId=X&token=Y` | HTML page + JS | Mobile agent form |
| `/citizen-gps` | GET | `?citizenId=X&token=Y` | HTML page + JS | Mobile citizen form |

**Auto-Schema Migration:**

```java
private void ensureGpsSchema() {
    // Called at server startup
    // 1. CREATE TABLE IF NOT EXISTS position_agent
    // 2. CREATE TABLE IF NOT EXISTS position_citoyen
    // 3. ALTER TABLE Zone ADD COLUMN latitude/longitude
    // 4. UPDATE Zone SET latitude=.., longitude=.. (Pikine + Guédiawaye)
}

// Result: Zero manual SQL migrations needed ✅
```

**Token Security:**
```java
public static String getOrCreateToken(int agentId) {
    return agentTokens.computeIfAbsent(agentId, id -> 
        UUID.randomUUID().toString().replace("-", "")
    );
}

// Validation in handlers:
String expected = agentTokens.get(agentId);
if (expected == null || !expected.equals(token)) {
    sendResponse(exchange, 403, "{\"status\":\"forbidden\"}");
    return;
}
```

**Position Validation:**

```java
// Must pass ALL:
if (agentId > 0 
    && lat >= -90 && lat <= 90 
    && lon >= -180 && lon <= 180 
    && (lat != 0 || lon != 0)  // Not both zero
) {
    boolean inAnyZone = isPositionInAnyZone(lat, lon);  // 10km radius
    if (!inAnyZone) {
        sendResponse(exchange, 400, "{\"status\":\"out_of_bounds\"}");
        return;
    }
    savePosition(agentId, lat, lon);
}
```

**Zone Check - Haversine:**

```java
private boolean isPositionInAnyZone(double lat, double lon) {
    String sql = "SELECT latitude, longitude FROM Zone 
                  WHERE latitude IS NOT NULL AND longitude != 0";
    
    for each zone in resultSet:
        double distKm = haversineKm(lat, lon, zoneLat, zoneLon);
        if (distKm <= 10.0) return true;  // Within zone radius
    
    if (noZonesFound) return true;  // Fail-open for initialization
    return false;
}

private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371;  // Earth radius in km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2)
             + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon/2)*Math.sin(dLon/2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}
```

**HTML Page Generation:**

Agent HTML = blue (`#1565C0`), Citizen HTML = green (`#2E7D32`)

Key features:
- Status indicator with pulse animation
- GPS button with fallback manual input
- JavaScript: navigator.geolocation.getCurrentPosition()
- POST to `/api/position` with token auth
- Zone validation server-side ("out_of_bounds" response)

---

## DATABASE SCHEMA

### Tables

**Zone:**
```sql
CREATE TABLE Zone (
    idZone INT AUTO_INCREMENT PRIMARY KEY,
    nomZone VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(10,8) DEFAULT 0.0,
    longitude DECIMAL(11,8) DEFAULT 0.0
);
```

**Position Agent (Real-time):**
```sql
CREATE TABLE position_agent (
    idAgent INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idAgent) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Position Citizen (Real-time):**
```sql
CREATE TABLE position_citoyen (
    idCitoyen INT PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Signalement (with GPS):**
```sql
CREATE TABLE Signalement (
    idSignalement INT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    categorie ENUM('Plastique', 'Papier', 'Verre', 'Métal', 'Organique', 'Autre'),
    idZone INT,
    latitude DECIMAL(10,8),          -- ← GPS position
    longitude DECIMAL(11,8),         -- ← GPS position
    dateSignalement DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('En attente', 'Affecte', 'En cours', 'Termine'),
    photo VARCHAR(255),
    idUser INT,
    FOREIGN KEY (idZone) REFERENCES Zone(idZone),
    FOREIGN KEY (idUser) REFERENCES Utilisateur(idUser)
);
```

### Data Denormalization

Note: **zoneNom** and **utilisateurNom** stored in Signalement model for convenience:

```java
private String zoneNom;           // Denormalized from Zone join
private String utilisateurNom;    // Denormalized from Utilisateur join
```

This avoids frequent joins in GridView rendering.

---

## LEAFLET.JS INTEGRATION

### Agent Dashboard Map

**File:** `src/main/resources/js/smart-gps-map.js`

```javascript
class SmartCityGPSMap {
    constructor() {
        this.map = null;           // L.map instance
        this.agentMarker = null;    // L.marker agent
        this.missionMarkers = [];   // L.marker[] missions
        this.selectedMission = null;
        this.missions = [];
        this.agentPosition = null;
    }
    
    init() {
        this.map = L.map('gps-map').setView([14.7646, -17.3920], 13);
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap'
        }).addTo(this.map);
    }
    
    updateAgentPosition(lat, lon) {
        // Create/update agent marker (blue pulse)
    }
    
    updateMissions(missions, distances) {
        // Create/update mission markers (red/orange/green)
        // Apply glow effect to closest
    }
}
```

**CSS Animations:**
```css
.agent-marker {
    background: #2196F3;
    animation: pulse 2s infinite;
}

.mission-marker-closest {
    animation: glow 1s ease-in-out infinite alternate;
}

@keyframes pulse {
    0% { box-shadow: 0 0 0 0 rgba(33, 150, 243, 0.7); }
    70% { box-shadow: 0 0 0 10px rgba(33, 150, 243, 0); }
    100% { box-shadow: 0 0 0 0 rgba(33, 150, 243, 0); }
}
```

**Java ↔ JavaScript Bridge:**

```java
// In AgentDashboardController.refreshTourneeMap():
mapWebView.getEngine().getLoadWorker().stateProperty()
    .addListener((obs, oldState, newState) -> {
        if (newState == Worker.State.SUCCEEDED) {
            netscape.javascript.JSObject window = 
                (netscape.javascript.JSObject) mapWebView.getEngine()
                    .executeScript("window");
            
            // Call JavaScript method
            window.call("updateMissions", missions, distances);
            window.call("updateAgentPosition", lat, lon);
        }
    });
```

---

### Citizen Dashboard Map

**File:** `src/main/resources/js/citizen-geolocation.js`

```javascript
class CitizenGeolocation {
    getCurrentPosition()    // Promise<{lat, lng, accuracy}>
    centerMapOnUser(map)    // Auto-center on browser geolocation
    searchAddress(query)    // Nominatim OSM search
    reverseGeocode(lat, lng) // Coords → address
}

// Install Java-JS bridge:
let javaController = window.java_controller;

// On map click:
map.on('click', function(e) {
    javaController.onMapClick(e.latlng.lat, e.latlng.lng);
});
```

**Java callback:**
```java
public void onMapClick(double lat, double lon) {
    selectedLatitude = lat;
    selectedLongitude = lon;
    Platform.runLater(() -> {
        gpsPositionStatusLabel.setText(
            String.format("✅ Position: %.5f, %.5f", lat, lon)
        );
    });
}
```

---

## WORKFLOW EXAMPLES

### Example 1: Agent Real-Time Tracking

```java
// STEP 1: Agent logs in
AgentDashboardController.initialize()
  // Loads missions
  affectationService.getSignalementsByAgent(agentId)
  
  // STEP 2: Start GPS tracking
  → initRealTimeGPS()
    → gpsService.startRealTimeTracking()
      → Timer(10s) begin

// STEP 2: Agent navigates to "Carte zones"
AgentDashboardController.handleShowCarteZones()
  → refreshTourneeMap()
    → String html = buildMapHtml()
    → mapWebView.getEngine().loadContent(html)
    → When ready: window.updateMissions(missions, distances)

// STEP 3: Every 10 seconds (background Timer):
RealTimeGPSService.pollPositionFromServer()
  → PositionAgentService.getPosition(agentId)
    → HTTP GET :8081/api/position?agentId=123&token=abc
    → GpsApiServer.handlePosition()
      → SELECT lat, lon FROM position_agent WHERE idAgent=123
      → return {lat: 14.765, lon: -17.392}
  → setCurrentPosition(14.765, -17.392)
    → notifyListeners(position)
      → AgentDashboardController.onLocationUpdate(pos)
        → lblPositionActuelle.setText("📍 14.765, -17.392 (Live)")
      → onMissionsDistanceUpdate(distances)
        → mapDistanceLabel.setText("0.5 km")
        → mapTempsLabel.setText("2 min")
        → if (pageCarteZones.isVisible)
          → refreshTourneeMap()
            → mapWebView.updateMissions(...)

// STEP 4: Agent clicks "Itinéraire optimal"
AgentDashboardController.handleItineraireOptimal()
  → geolocationService.optimizeCollectionRoute(missions, startPoint)
    → Greedy TSP: sort missions by nearest
  → Show Alert with ordered missions
  → Generate Google Maps URL
```

### Example 2: Citizen QR Scan GPS

```json
// STEP 1: Citizen in app sees QR code
CitizenDashboardController.loadInteractiveMap()
  → generateQrCode(url) where
     url = "http://192.168.1.100:8081/citizen-gps?citizenId=42&token=xyz"
  → Display in ImageView citizenQrCodeView

// STEP 2: Citizen scans QR with phone
// Browser opens URL → GpsApiServer.handleGpsPage()
// Returns HTML with geolocation.getCurrentPosition()

// STEP 3: Citizen clicks "Envoyer ma position GPS"
// JavaScript tryGps() → navigator.geolocation.getCurrentPosition()
//   OK: (pos) → lat=14.7650, lon=-17.3920
//   ERROR: showManual() fallback

// STEP 4: sendPosition(14.7650, -17.3920, 'GPS')
// HTTP POST http://192.168.1.100:8081/api/citizen-position
// Body: citizenId=42&lat=14.7650&lon=-17.3920&token=xyz

// STEP 5: GpsApiServer.handleCitizenPosition(POST)
// 1. Validate token
// 2. Validate coords (-90 to 90 lat, -180 to 180 lon)
// 3. Check Haversine distance ≤ 10km from Zone
// 4. If OK: INSERT INTO position_citoyen (idCitoyen, lat, lon, updatedAt)
// 5. Return HTTP 200 {"status":"ok"}

// STEP 6: Phone shows "✅ Position envoyée (GPS)"

// STEP 7: Agent app (real-time loop) receives position next cycle:
// - RealTimeGPSService polling finds citoyen position
// - Updates mission distance
// - IF mission belongs to agent's tour:
//   Display on map with updated position
```

---

## DEBUGGING TIPS

### Enable SQL Query Logging

```properties
# src/main/resources/config.properties
# If using MySQLLogs
logging.level.com.mysql.cj.log=debug
```

### RealTimeGPS Debug

```java
// In RealTimeGPSService
private void pollPositionFromServer() {
    com.smartcity.utils.SessionManager.getAgentId().ifPresent(agentId -> {
        logger.debug("📍 Polling position for agent {}", agentId);  // ADD
        PositionAgentService.Position pos = positionAgentService.getPosition(agentId);
        if (pos != null) {
            logger.debug("📍 Got position: {}, {}", pos.lat, pos.lon);  // ADD
            setCurrentPosition(new Coordinates(pos.lat, pos.lon));
        } else {
            logger.warn("📍 Position NULL for agent {}", agentId);  // ADD
        }
    });
}
```

### WebView Map Errors

```java
mapWebView.getEngine().getLoadWorker()
    .exceptionProperty()
    .addListener((obs, oldVal, error) -> {
        if (error != null) {
            logger.error("WebView error: {}", error);  // Log to see what failed
            // Could be: Leaflet CDN unreachable, JS syntax error, etc.
        }
    });
```

### GpsApiServer Log Verbosity

```java
// In GpsApiServer.handlePosition()
logger.info("✅ Position saved: agent={}, lat={}, lon={}", 
            agentId, lat, lon);  // Useful to trace POST requests
```

---

## OPTIMIZATION IDEAS (Future)

1. **Batch Position Updates:**
   - Instead of polling every 10s, agent sends every N movements
   - Reduces DB writes + network traffic

2. **Client-Side Caching:**
   - Cache mission list locally
   - Server delta = only changed missions
   - Faster UI refresh

3. **WebSocket Instead of HTTP:**
   - Real HTTP polling → WebSocket push
   - Server → Client (agent's position changes)
   - Lower latency + fewer connections

4. **Clustering Markers:**
   - If > 100 missions on map, group nearby markers
   - Click cluster → zoom + uncluster
   - Better performance + UX

5. **Tile Caching:**
   - Leaflet tiles cached locally
   - Works offline for known areas

6. **TSP Improvements:**
   - 2-opt local search (better than greedy)
   - Or use distance matrix API (OSRM, Graphhopper)

---

## DEPLOYMENT CHECKLIST

Before going to production:

- [ ] Change `GpsApiServer.PORT` to config property
- [ ] Add connection pool to DB (HikariCP)
- [ ] Enable SSL/HTTPS for positions (sensitive data)
- [ ] Implement rate limiting on endpoints
- [ ] Add request logging (Apache Commons Logging)
- [ ] Monitor memory (GC logs, Timer threads)
- [ ] Backup position_agent/position_citoyen regularly
- [ ] Test with 100+ agents + 1000+ missions
- [ ] Load test on single machine (stress GpsApiServer threads)

---

**End of Technical Guide**

