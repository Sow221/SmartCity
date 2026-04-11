# 🧪 PLAN DE TEST PRÉSENTATIONS - GÉOLOCALISATION

**Date:** 11 avril 2026  
**Durée estimée:** 2-3 heures  
**Tester avec:** 2-3 appareils (PC + téléphones mobile)

---

## 📋 PRE-TEST CHECKLIST

### Environment Setup (30 min)

- [ ] **PC Développement:**
  - [ ] `mvn clean compile` → 0 erreurs ✅
  - [ ] Port 8081 libre: `netstat -ano | findstr :8081` (vide) ✅
  - [ ] MySQL running: `mysqladmin -u root -p ping` ✅
  - [ ] DB créée: `mysql -u root < scripts/gestion_dechets.sql` ✅

- [ ] **Migrations SQL:**
  - [ ] Position tables créées (auto par GpsApiServer)
  - [ ] Zone table remplie: Pikine + Guédiawaye ✅
  - [ ] Check: `SELECT COUNT(*) FROM Zone WHERE latitude != 0;` → 2 ✅

- [ ] **Ressources:**
  - [ ] Leaflet CDN accessible: `curl https://tile.openstreetmap.org/0/0/0.png` ✅
  - [ ] Son notification présent: `/src/main/resources/sounds/beep.mp3` ✅
  - [ ] FXML bindings correct: `fx:id="mapWebView"`, `citizenQrCodeView` ✅

- [ ] **Réseau:**
  - [ ] WiFi local opérationnel
  - [ ] PC IP 192.168.x.x (not 127.0.0.1)
  - [ ] Téléphones sur même WiFi que PC
  - [ ] Ping PC from phone: réponse < 100ms ✅

---

## 🧪 TEST 1: Compilation + Démarrage

**Durée:** 10 min  
**Objectif:** Vérifier build + app lance sans erreur

### Steps

1. Terminal: Compiler projet
   ```bash
   cd C:\Users\MS\Desktop\SC\SmartCity
   mvn clean compile -q
   ```
   **Expected:** ✅ Pas d'erreurs
   ```
   [BUILD SUCCESS]
   
   elapsed: 45 seconds
   ```

2. IntelliJ / VS Code: Lancer MainApp.java
   ```
   Right-click MainApp.java → Run 'MainApp.main()'
   ```
   **Expected:** ✅ App démarre
   ```
   ✅ Serveur GPS API démarré sur le port 8081
   ✅ Accessible sur http://localhost:8081
   ```

3. Vérifier logs console
   ```
   [INFO] GpsApiServer : GPS API Server démarré sur le port 8081
   [INFO] AgentDashboardController : 🗺️ GPS RealTime initialisé
   ```

### Acceptance Criteria
- [ ] `mvn compile` retourne 0 erreurs
- [ ] GpsApiServer port 8081 dépmarrage visible en logs
- [ ] Login screen visible (no crash)
- [ ] AgentDashboardController initializes GPS service

---

## 🧪 TEST 2: Real-Time Tracking - Position Agent

**Durée:** 15 min  
**Objectif:** Vérifier position agent update en temps réel

### Setup
1. App: Create test agent account (si pas présent)
   - Email: `agent@test.fr`
   - Password: `Test123`
   - Role: Agent
   - Zone: Pikine

2. DB check agent ID:
   ```sql
   SELECT idUser FROM Utilisateur WHERE email='agent@test.fr';
   -- Note the ID, e.g., idUser = 5
   ```

### Steps

1. **App Login:** Connect as agent@test.fr
2. **Dashboard:** Verify "Position..." label visible in top bar
3. **Open QR Page:** In browser, navigate to:
   ```
   http://192.168.1.X:8081/gps?agentId=5&token=<TOKEN>
   ```
   Where:
   - X = last octet of PC IP
   - TOKEN = from logs or copy from GpsApiServer output

4. **Send Position GPS:**
   - Clic button "📱 Envoyer ma position GPS"
   - Wait for response: "✅ Position envoyée (GPS)"

5. **App Refresh:** Wait 10 seconds or click "Actualiser"

6. **Verify Update:**
   - [ ] Position label shows: "📍 14.7640, -17.3915 (Live)" (or similar)
   - [ ] Label updated within 10s of GPS submission
   - [ ] No crash/error in app

### Logs to Check
```
Agent App Console:
[INFO] RealTimeGPSService : 📍 Got position: 14.7640, -17.3915

GpsApiServer Console:
[INFO] GpsApiServer : ✅ Position saved: agent=5, lat=14.7640, lon=-17.3915
```

### Acceptance Criteria
- [ ] Position update visible in agent app within 10s
- [ ] No console errors
- [ ] Repeated sends (10s cycle) keep updating position

---

## 🧪 TEST 3: QR Code Citizen GPS

**Durée:** 20 min  
**Objectif:** Vérifier QR code fonctionne et position remonte

### Setup
1. Create test citizen account
   - Email: `citizen@test.fr`
   - Password: `Test123`
   - Role: Citoyen

2. Create test signal via Agent app
   - Or use existing from DB

### Steps

1. **App Citizen Dashboard:** Login as citizen@test.fr

2. **New Signal Page:** Clic "➕ Nouveau signalement"

3. **Verify QR Code:**
   - [ ] QR code visible in "GPS Mobile" section
   - [ ] QR code not blank/corrupted
   - [ ] Size ~140x140 pixels
   - [ ] URL link displayed below: `http://192.168.1.X:8081/citizen-gps?...`

4. **Scan QR Code:**
   - Phone 2 (different device): Open camera
   - Scan QR from PC screen
   - Browser auto-opens URL

5. **Expected:** HTML Page on Phone
   ```
   Title: SmartCity GPS (green background for citizen)
   Button: "📱 Envoyer ma position GPS"
   Status: "Activation GPS..."
   ```

6. **Send Position:**
   - Clic "📱 Envoyer ma position GPS"
   - If GPS blocked:
     - [ ] Fallback "✏️ Saisir manuellement" appears
     - [ ] Enter: lat=14.7650, lon=-17.3920
     - [ ] Clic "Confirmer"
   - [ ] Status updates: "✅ Position envoyée (GPS)"
   - [ ] Coordinates display: "14.76500, -17.39200"

7. **Check DB:**
   ```sql
   SELECT lat, lon FROM position_citoyen WHERE idCitoyen = <CITIZEN_ID>;
   -- Should show: 14.765, -17.3920
   ```

### Acceptance Criteria
- [ ] QR code scanned successfully
- [ ] HTML page loaded without SSL errors
- [ ] Position sent from phone to GpsApiServer
- [ ] DB row created/updated in position_citoyen
- [ ] No "out_of_bounds" error (position within Dakar region)

---

## 🧪 TEST 4: Map Interactive Citizen

**Durée:** 15 min  
**Objectif:** Vérifier citizen peut placer position sur clic carte

### Steps

1. **App Citizen:** "Nouveau signalement" page

2. **Verify Map Loaded:**
   - [ ] Leaflet map visible in center
   - [ ] Map centered on Pikine (14.76, -17.39)
   - [ ] OpenStreetMap tiles loading
   - [ ] Zoom buttons visible (+ / -)

3. **Click Map to Place Marker:**
   - Clic on map somewhere (e.g., near Pikine)
   - [ ] Marker appears at clicked location
   - [ ] Status label updates: "✅ Position sélectionnée: 14.76500, -17.39200"

4. **Change Zone & Recentrer:**
   - Dropdown "Zone": Select "Guédiawaye"
   - [ ] Map recenters to Guédiawaye (14.77, -17.40)
   - Previous marker disappears
   - [ ] Status resets: "Cliquez sur la carte..."

5. **Save Signalement:**
   - Fill form (description, category, etc.)
   - Click position marker again
   - Clic "Enregistrer"
   - [ ] Signalement saved with GPS coordinates

### Acceptance Criteria
- [ ] Map loads without console errors
- [ ] Click → marker + status update
- [ ] Zone change → map recenters
- [ ] Position persisted on save

---

## 🧪 TEST 5: Route Optimization (TSP)

**Durée:** 15 min  
**Objectif:** Vérifier optimisation route TSP

### Setup
1. Create 5 test signalements via Agent or SQL
   - Spread across Pikine + Guédiawaye
   - Each with lat/lon
   ```sql
   INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, statut, idUser)
   VALUES 
     ('Plastique 1', 'Plastique', 1, 14.7650, -17.3920, 'En attente', 1),
     ('Papier 1', 'Papier', 1, 14.7655, -17.3925, 'En attente', 1),
     ('Verre 1', 'Verre', 2, 14.7760, -17.4040, 'En attente', 1),
     ...
   ```

2. Agent app: Refresh dashboard
   - Should see 5 missions

### Steps

1. **Agent Dashboard:** Go to "Mes missions"
   - [ ] See all 5 missions in table

2. **Clic "Itinéraire optimal":**
   - From quick actions bar button

3. **Expected:** Information dialog appears
   ```
   Title: Itinéraire Optimisé (5 missions)
   
   1. Mission #X - Plastique
      📍 Distance: 0.32 km
      🗺️ Zone: Pikine
   
   2. Mission #Y - Papier
      📍 Distance: 0.18 km
      🗺️ Zone: Pikine
   
   [...]
   
   Distance totale estimée: 3.35 km
   ⏱️ Temps estimé: 13 minutes
   🔗 Lien Google Maps: https://maps.google.com/maps/dir/14.76/-17.39/14.77/-17.40/...
   ```

4. **Verify:**
   - [ ] Missions sorted by distance (nearest first)
   - [ ] Total distance reasonable for nearest-neighbor
   - [ ] Google Maps link valid (paste in browser)
   - [ ] No mission with (0, 0) coordinates included

5. **Google Maps Link:**
   - Copy URL and paste in browser
   - [ ] Page loads
   - [ ] Route displayed with waypoints

### Acceptance Criteria
- [ ] TSP algorithm returns sorted missions
- [ ] Google Maps URL valid and clickable
- [ ] Dialog displays without crash
- [ ] Total distance < random order (if applicable)

---

## 🧪 TEST 6: Real-Time Alarm (Proximity)

**Durée:** 10 min  
**Objectif:** Vérifier alerte proximité mission

### Setup
1. Agent app: Create mission very close to agent (same zone center)
   ```sql
   -- Assume agent assigned to Pikine (14.7646, -17.3920)
   -- Create mission at exact same coords
   INSERT INTO Signalement (description, categorie, idZone, latitude, longitude, ...)
   VALUES ('Très proche', 'Plastique', 1, 14.7646, -17.3920, 'En attente', 1);
   ```

2. Refresh Agent dashboard
   - [ ] Mission appears in list

### Steps

1. **Send Position:**
   - QR page: Envoyer position 14.7646, -17.3920 (exact same)

2. **Wait 10 seconds:**
   - RealTime GPS polling cycles

3. **Expected:**
   - [ ] RED ALERT MESSAGE: "🚨 MISSION À PROXIMITÉ! Vérifiez la carte"
   - [ ] BEEP SOUND plays (if not muted)
   - [ ] Alert auto-disappears after 3 seconds

4. **Map Updated:**
   - Mission should show distance ≈ 0.00 km

### Acceptance Criteria
- [ ] Alert message displays for nearby mission
- [ ] Sound plays on nearby alert
- [ ] Alert only for missions < 1km away
- [ ] No false positives

---

## 🧪 TEST 7: Fallback - Manual Coordinates

**Durée:** 10 min  
**Objectif:** Vérifier saisie manuelle si GPS échoue

### Steps

1. **QR Page (Phone 2):**
   - Clic "📱 Envoyer ma position GPS"
   - Wait 5-8 seconds
   - Status should show: "GPS refusé" or timeout

2. **Manual Input Appears:**
   - [ ] Fallback section visible: "Entrez vos coordonnées"
   - [ ] Input fields for latitude + longitude

3. **Enter Coordinates:**
   - Latitude: `14.765`
   - Longitude: `-17.392`
   - Clic "✅ Confirmer"

4. **Verify:**
   - [ ] Status: "✅ Position envoyée (Manuel)"
   - [ ] DB row created in position_citoyen
   - [ ] No validation error

### Acceptance Criteria
- [ ] Fallback works when GPS unavailable
- [ ] Manual coordinates validated (-90 to 90, -180 to 180)
- [ ] Submitted to DB successfully

---

## 🧪 TEST 8: Zone Validation (Out of Bounds)

**Durée:** 10 min  
**Objectif:** Vérifier rejection position hors-zone

### Steps

1. **QR Page or Manual Input:**

2. **Enter Invalid Coordinates:**
   - Latitude: `5.0` (Guinea/Mali - hors Sénégal)
   - Longitude: `-5.0`
   - Clic "Confirmer"

3. **Expected:**
   - [ ] Status: "⚠️ Hors zone autorisée"
   - [ ] NO db row created
   - [ ] HTML status shows error (not "ok")

4. **Check Server Logs:**
   ```
   [WARN] GpsApiServer : 🚫 Position GPS hors zone autorisée pour citoyen X: (5, -5)
   ```

### Acceptance Criteria
- [ ] Out-of-bounds positions rejected
- [ ] DB not updated
- [ ] User informed of rejection
- [ ] Zone radius ~10km check functional

---

## 🧪 TEST 9: Network Resilience

**Durée:** 10 min  
**Objectif:** Vérifier app ne crash pas en perte réseau

### Steps

1. **Agent App Online:**
   - Dashboard visible
   - Position label showing

2. **WiFi Disconnect:**
   - Disconnect network (unplug cable OR disable WiFi)

3. **Expected Behavior:**
   - [ ] App does NOT crash
   - [ ] Position label unchanged (cached)
   - [ ] Map might show as blank
   - [ ] Fallback message displayed

4. **Reconnect WiFi:**
   - Reconnect to network

5. **Expected:**
   - [ ] Automatic recovery
   - [ ] New position acquired within 10s
   - [ ] No manual refresh required

### Acceptance Criteria
- [ ] App graceful degradation on network loss
- [ ] No crash/exception
- [ ] Auto-recovery on reconnect
- [ ] Fallback UI shown

---

## 🧪 TEST 10: Map Display - Agent Tournée

**Durée:** 15 min  
**Objectif:** Vérifier map affiche missions dynamiquement

### Setup
1. Agent with 5-10 missions assigned

### Steps

1. **Dashboard:** "🗺️ Carte zones"
   - [ ] Map loads + centered on Pikine
   - [ ] OpenStreetMap tiles visible

2. **Marker Verification:**
   - [ ] 🔵 Blue marker = agent position (with pulse)
   - [ ] 🔴 Red markers = pending missions
   - [ ] 🟠 Orange markers = in-progress missions
   - [ ] 🟢 Green markers = completed missions

3. **Update Position:**
   - Send new position via QR page

4. **Map Refresh:**
   - Within 10s, agent marker moves to new position
   - [ ] Agent marker repositioned on map

5. **Click Mission:**
   - Clic a red marker
   - [ ] Popup appears: "Signalement #X"
   - [ ] Right panel updates with mission details:
     - ID
     - Zone
     - Address
     - Status
     - Distance
     - Time estimate
     - Comment

6. **Mission State Change:**
   - Agent app: "🚀 Démarrer" → mission becomes "En cours"
   - Marker color changes red → orange
   - [ ] Real-time on map

### Acceptance Criteria
- [ ] Marker colors reflect mission states
- [ ] Agent position updates live
- [ ] Click marker → details panel
- [ ] No console errors
- [ ] Smooth animations (pulse, glow effects)

---

## ✅ FINAL VALIDATION

### Checklist Final (Before Presentation)

- [ ] All 10 tests passed
- [ ] No console errors (java/javascript)
- [ ] Position updates within 10 seconds consistently
- [ ] QR code scanned and position received
- [ ] Map renders smoothly (Leaflet/OSM)
- [ ] Route optimization returns correct order
- [ ] No crashes on network disconnect
- [ ] Zone validation rejects out-of-bounds
- [ ] Manual fallback works as expected
- [ ] Alert plays for nearby missions
- [ ] DB synchronized with UI (no stale data)

### Sign-Off

- **Tested By:** [Name]
- **Date:** [Date]
- **Environment:** [PC IP, Phones, WiFi SSID]
- **Result:** ✅ **CLEARED FOR DEMO**

---

