# 🔧 GUIDE DE CORRECTION RAPIDE - 4 BUGS GPS

**Pour corriger en 100 minutes** | Copie/colle prêt

---

## BUG #1: CARTE LEAFLET BLANCHE (15 minutes)

### 📍 Emplacement
**Fichier**: `src/main/java/com/smartcity/controller/AgentDashboardController.java`

### ❌ ANCIEN CODE (à supprimer)
```java
private String buildLeafletHtml(List<Signalement> missions, Coordinates agentPos) {
    return "<!DOCTYPE html><html>" +
        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
        "<script src='smart-gps-map.js'></script>" +  // ❌ NE SE CHARGE PAS
        "<div id='map' style='width: 100%; height: 100%;'></div>" +
        "...";
}
```

### ✅ NOUVEAU CODE
```java
// En haut de la classe:
private static final String SMART_GPS_MAP_JS = loadResourceOnce("js/smart-gps-map.js");

private String buildLeafletHtml(List<Signalement> missions, Coordinates agentPos) {
    return "<!DOCTYPE html><html>" +
        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
        "<script>" + SMART_GPS_MAP_JS + "</script>" +  // ✅ INJECTION DIRECTE
        "<div id='map' style='width: 100%; height: 100%;'></div>" +
        "<script>" +
            "setTimeout(() => initMap(" + agentPos.lat + ", " + agentPos.lon + "), 100);" +
        "</script>" +
        "</html>";
}

// Ajouter cette méthode utilitaire:
private static String loadResourceOnce(String path) {
    try (var is = AgentDashboardController.class.getClassLoader().getResourceAsStream(path)) {
        if (is == null) {
            logger.warn("⚠️ Resource not found: {}", path);
            return "console.log('ERROR: " + path + " not found');";
        }
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        logger.error("❌ Failed to load resource: {}", path, e);
        return "console.error('Failed to load: " + path + "');";
    }
}
```

### 📋 Checklist
- [ ] Ajouter `import java.nio.charset.StandardCharsets;`
- [ ] Ajouter variable statique `SMART_GPS_MAP_JS`
- [ ] Ajouter méthode `loadResourceOnce()`
- [ ] Remplacer `<script src='smart-gps-map.js'></script>` par injection
- [ ] Compiler et tester
- ✅ **FAIT**: Carte Leaflet visible avec marqueurs

---

## BUG #2: CLIGNOTEMENT CARTE (30 minutes)

### 📍 Emplacement
**Fichier**: `src/main/java/com/smartcity/controller/AgentDashboardController.java`

### ❌ ANCIEN CODE
```java
private void refreshTourneeMap() {
    // Appelé tous les 10 secondes
    List<Signalement> optimized = routeOptimizer.optimizeRoute(missions, agentPos);
    String html = buildLeafletHtml(optimized, agentPos);
    webEngine.loadContent(html);  // ❌ RECHARGE 100%
}
```

### ✅ NOUVEAU CODE

**STEP 1**: Ajouter fonction JS dans `smart-gps-map.js`
```javascript
// À ajouter dans src/main/resources/js/smart-gps-map.js:

function updateAgentPosition(lat, lon, missions) {
    // Nettoyer ancien marqueur agent
    if (window.agentMarker) {
        map.removeLayer(window.agentMarker);
    }
    
    // Créer nouveau marqueur agent
    window.agentMarker = L.circleMarker([lat, lon], {
        radius: 12,
        color: '#2196F3',
        fillColor: '#2196F3',
        fillOpacity: 0.9,
        weight: 2,
        title: 'Agent position'
    }).addTo(map).bindPopup('Agent actuel');
    
    // Optionnel: smooth pan (pas zoom)
    map.panTo([lat, lon], {
        animate: true,
        duration: 0.5
    });
    
    // Mettre à jour missions si nécessaire
    if (missions && Array.isArray(missions)) {
        updateMissionMarkers(missions);
    }
}

function updateMissionMarkers(missions) {
    // Nettoyer anciens marqueurs missions
    if (window.missionMarkers) {
        window.missionMarkers.forEach(m => map.removeLayer(m));
    }
    window.missionMarkers = [];
    
    // Créer nouveaux marqueurs pour chaque mission
    missions.forEach((mission, index) => {
        const marker = L.circleMarker([mission.latitude, mission.longitude], {
            radius: 8,
            color: '#FF9800',
            fillColor: '#FF9800',
            fillOpacity: 0.7,
            weight: 1
        }).addTo(map)
         .bindPopup(`Mission ${index + 1}: ${mission.description}`);
        
        window.missionMarkers.push(marker);
    });
}
```

**STEP 2**: Modifier `refreshTourneeMap()` dans AgentDashboardController
```java
private void refreshTourneeMap() {
    // Récupérer données
    List<Signalement> optimized = routeOptimizer.optimizeRoute(missions, agentPos);
    
    // Construire JSON pour les missions
    String missionsJson = buildMissionsJson(optimized);  // À créer ci-dessous
    
    // ✅ APPELER FONCTION JS AU LIEU DE RECHARGER HTML
    String script = String.format(
        "updateAgentPosition(%.6f, %.6f, %s);",
        agentPos.lat, agentPos.lon, missionsJson
    );
    
    webEngine.executeScript(script);
}

// Nouvelle méthode helper:
private String buildMissionsJson(List<Signalement> missions) {
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < missions.size(); i++) {
        Signalement s = missions.get(i);
        json.append("{")
            .append("\"id\":").append(s.getIdSignalement()).append(",")
            .append("\"description\":\"").append(escapeJson(s.getDescription())).append("\",")
            .append("\"latitude\":").append(s.getLatitude()).append(",")
            .append("\"longitude\":").append(s.getLongitude())
            .append("}");
        if (i < missions.size() - 1) json.append(",");
    }
    json.append("]");
    return json.toString();
}

private String escapeJson(String str) {
    return str == null ? "" : str.replace("\"", "\\\"").replace("\n", "\\n");
}
```

### 📋 Checklist
- [ ] Ajouter fonction `updateAgentPosition()` dans smart-gps-map.js
- [ ] Ajouter fonction `updateMissionMarkers()` dans smart-gps-map.js
- [ ] Créer méthode `buildMissionsJson()` dans AgentDashboardController
- [ ] Créer méthode `escapeJson()` dans AgentDashboardController
- [ ] Modifier `refreshTourneeMap()` pour utiliser `executeScript()` au lieu de `loadContent()`
- [ ] Compiler et tester (pas de recharge HTML à chaque 10s)
- ✅ **FAIT**: Carte fluide sans clignotement

---

## BUG #3: GPS HTTPS (45 minutes)

### 📍 Emplacement
**Fichier**: `src/main/java/com/smartcity/service/GpsApiServer.java`

### ❌ ANCIEN CODE
```java
public void startServer() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8081), 0);
    // ... reste du code ...
    server.start();
}
```

### ✅ NOUVEAU CODE

**STEP 1**: Ajouter imports
```java
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.io.FileInputStream;
```

**STEP 2**: Créer ou utiliser certificat existant
```bash
# (À exécuter une fois dans le répertoire du projet)
keytool -genkey -alias smartcity-gps -keyalg RSA -keysize 2048 \
  -keystore gps-keystore.jks -validity 365 -storepass "smartcity123" \
  -dname "CN=SmartCity GPS, O=SmartCity, C=SN"
```

**STEP 3**: Modifier la méthode `startServer()`
```java
public void startServer() throws Exception {
    // ✅ Utiliser HTTPS au lieu de HTTP
    HttpsServer server = HttpsServer.create(new InetSocketAddress("0.0.0.0", 8081), 0);
    
    try {
        // Charger le keystore
        String keystorePath = "gps-keystore.jks";
        String keystorePassword = "smartcity123";
        
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        
        // Initialiser KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keystorePassword.toCharArray());
        
        // Créer SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());
        
        // Configurer le serveur HTTPS
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        
        // Créer les contexts (endpoints)
        server.createContext("/gps", new GpsHandler());
        server.createContext("/gps/position", new PositionHandler());
        
        server.setExecutor(null);
        server.start();
        
        logger.info("✅ HTTPS GPS Server started on https://0.0.0.0:8081");
        logger.info("   Certificate: {} (validity: 365 days)", keystorePath);
        
    } catch (FileNotFoundException e) {
        logger.error("❌ Keystore not found: {}. Generate it with:", "gps-keystore.jks");
        logger.error("   keytool -genkey -alias smartcity-gps -keyalg RSA -keysize 2048 \\");
        logger.error("   -keystore gps-keystore.jks -validity 365 -storepass smartcity123");
        throw e;
    } catch (Exception e) {
        logger.error("❌ Failed to start HTTPS GPS Server", e);
        throw e;
    }
}
```

### 📋 Checklist
- [ ] Générer certificat avec keytool (une seule fois)
- [ ] Ajouter imports HTTPS/SSL
- [ ] Remplacer `HttpServer` par `HttpsServer`
- [ ] Charger le keystore et initialiser SSLContext
- [ ] Configurer le serveur avec `setHttpsConfigurator()`
- [ ] Compiler et tester
- ✅ **FAIT**: GPS fonctionne sur Android 12+ et iOS

---

## BUG #4: MISSIONS FAUSSES COORDS (10 minutes)

### 📍 Emplacement
**Fichier**: `src/main/java/com/smartcity/controller/AgentDashboardController.java`

**Méthode**: `missionPoint()`

### ❌ ANCIEN CODE
```java
private Point missionPoint(Signalement mission, int index) {
    Point base = zoneCenter(mission.getZoneNom());
    double latOffset = (index % 5) * 0.004 + 0.001;
    double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
    return new Point(base.lat + latOffset, base.lon + lonOffset);  // ❌ Ignore vraies coords!
}
```

### ✅ NOUVEAU CODE
```java
private Point missionPoint(Signalement mission, int index) {
    // ✅ CHERCHER D'ABORD LES VRAIES COORDONNÉES
    if (mission.getLatitude() != null && mission.getLongitude() != null &&
        mission.getLatitude() != 0.0 && mission.getLongitude() != 0.0) {
        // Utiliser les vraies coordonnées du signalement
        return new Point(mission.getLatitude(), mission.getLongitude());
    }
    
    // Fallback: si coords manquantes, placer autour du centre de zone
    Point base = zoneCenter(mission.getZoneNom());
    double latOffset = (index % 5) * 0.004 + 0.001;    // ~400m
    double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
    return new Point(base.lat + latOffset, base.lon + lonOffset);
}
```

### 📋 Checklist
- [ ] Ajouter condition: if (latitude != null && longitude != null && != 0.0)
- [ ] Retourner position GPS réelle si disponible
- [ ] Garder fallback pour données incomplètes
- [ ] Compiler et tester
- ✅ **FAIT**: Missions affichées à la bonne position

---

## 🧪 TESTS APRÈS CORRECTIONS

### Test 1: Carte visible
```java
// Après FIX #1
1. Lancer app
2. Login agent
3. Dashboard agent s'affiche
4. ✅ Vérifier: Carte visible avec marqueurs
```

### Test 2: Pas clignotement
```java
// Après FIX #2
1. Lancer app
2. Attendre 10 secondes
3. ✅ Vérifier: Zoom/pan restent identiques (pas recharge)
```

### Test 3: HTTPS fonctionne
```javascript
// Après FIX #3
Ouvrir console browser:
navigator.geolocation.getCurrentPosition(pos => {
    console.log("✅ Géolocalisation OK:", pos);
});
// ✅ Doit afficher position (pas erreur 403)
```

### Test 4: Positions correctes
```java
// Après FIX #4
1. Vérifier DB: SELECT latitude, longitude FROM Signalement;
2. ✅ Vérifier carte: Missions affichées aux vraies coordonnées
```

---

## ⏱️ TEMPS PAR FIX

```
FIX #1 (smart-gps-map.js):  15 min
FIX #2 (clignotement):      30 min
FIX #3 (HTTPS):             45 min
FIX #4 (coords missions):   10 min
────────────────────────────────
TOTAL:                       100 min
```

---

## 🎯 RÉSULTAT FINAL

Après appliquer tous les fixes:
- ✅ Carte Leaflet interactive et visible
- ✅ Marqueurs agents et missions
- ✅ Pas de clignotement (UX fluide)
- ✅ GPS fonctionne Android 12+/iOS
- ✅ Missions à bonnes positions
- ✅ Route optimisée correcte

**→ DÉMO 100% OPÉRATIONNELLE** 🎉

---

**Fin du guide de correction rapide**

