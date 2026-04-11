# ✅ CHECKLIST PRÉ-DÉMONSTRATION - SmartCity
**Date:** 11 avril 2026  
**Heure:** À remplir avant 09:00 demain  

---

## 🔴 CRITIQUES (À vérifier AVANT de lancer l'app)

### 1. Ports disponibles
```bash
# Commande: netstat -ano | findstr "8081 8082"
# Résultat attendu: AUCUNE ligne (ports libres)
```
- [ ] Port 8081 libre (GPS API Server)
- [ ] Port 8082 libre (WebSocket Server)

### 2. Base de données
```bash
# Commande: mysql -u root -p -e "USE db_smartcity; SELECT * FROM Zone;"
# Résultat attendu: Pikine + Guédiawaye avec coordonnées
```
- [ ] MySQL démarré (`net start MySQL80` ou équivalent)
- [ ] DB `db_smartcity` créée
- [ ] Zones Pikine (14.7646, -17.3920) et Guédiawaye présentes
- [ ] Tables Signalement, Utilisateur, Affectation existent

### 3. Compilation
```bash
# Commande executée
mvn clean compile -q
# Résultat attendu: BUILD SUCCESS
```
- [ ] ✅ **Compilation réussie** (vérifier ci-dessus)

---

## 🟡 IMPORTANTS (À tester AVANT la démo)

### 4. Démarrage GpsApiServer (port 8081)
**Logs attendus:**
```
[INFO] GPS API Server démarré sur le port 8081
```
- [ ] Serveur démarre sans erreur
- [ ] Ne bloque pas au lancement
- [ ] Endpoints accessibles: `/api/position`, `/api/citizen-position`

### 5. Démarrage WebSocketServer (port 8082)
**Logs attendus:**
```
[INFO] WebSocket server démarré sur ws://localhost:8082/ws/agent-missions
```
- [ ] Serveur démarre sans erreur
- [ ] Port 8082 validé
- [ ] Tyrus JAR dans classpath

### 6. Géolocalisation - Flows critiques

#### **Test Agent:**
1. Login avec compte agent (exemple: `agent@test.com / password123`)
2. Check dashboard chargé
3. Click "Cartes des zones" → **Doit voir Leaflet map + marqueur bleu**
4. **Position affichée:** "Position agent: 14.7646, -17.3920" (ou sa zone)

#### **Test Citizen:**
1. Login avec compte citoyen
2. Click "Nouveau signalement"
3. **Doit voir:**
   - [ ] Carte Leaflet interactive
   - [ ] QR code 140x140px en bas (NEW)
   - [ ] Lien GPS cliquable
   - [ ] Bouton "Copier"
4. Click carte → Marqueur placé
5. Position affichée: "Position sélectionnée: XX.XXXX, -XX.XXXX"

### 7. Real-time Tracking (bonus test)
```
Agent login en terminal 1
Admin login en terminal 2
→ Admin voit position agent mise à jour toutes les 10s
```
- [ ] Position s'actualise après ~10s
- [ ] Pas de crash UI
- [ ] Marqueur se déplace smoothly

---

## 🟢 COSMÉTIQUES (Avant de présenter - nice to have)

- [ ] Pages responsive (test sur petit écran: F12 → responsive mode)
- [ ] Thème sombre fonctionne (toggle button header)
- [ ] Connexion/déconnexion smooth
- [ ] Pas de console errors (F12 sur browser embedded)

---

## 📊 À REMPLIR 30 MIN AVANT DÉMO

| Test | Statut | Notes |
|------|--------|-------|
| Port 8081 libre | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| Port 8082 libre | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| MySQL running | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| Compilation | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| GpsApiServer start | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| WebSocketServer start | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| Agent login → map | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| Citizen login → QR | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| Clic carte fonctionne | [ ] OK | \_\_\_\_\_\_\_\_\_\_|
| **GLOBAL** | ✅ GO/❌ NO-GO | \_\_\_\_\_\_\_\_\_\_|

---

## 🚨 PLAN D'ACTION SI PROBLÈME

| Problème | Solution |
|----------|----------|
| **Port 8081 occupé** | `netstat -ano \| findstr :8081` → `taskkill /PID XXXX /F` |
| **Port 8082 occupé** | `netstat -ano \| findstr :8082` → `taskkill /PID XXXX /F` |
| **MySQL down** | `net start MySQL80` ou `services.msc` |
| **Compilation fail** | `mvn clean compile -X` (debug output) + check JDK version |
| **GpsApiServer port error** | Check logs dans console Java |
| **Leaflet map vide** | Check network (CDN unpkg accessible?) + F12 console |
| **QR code invisible** | Vérifier FXML binding `citizenQrCodeView` existe |

---

## 📝 SIGNATURE

```
Date: _______________
Statut: _______________  (PAS DE BLOCKERS / MINOR / CRITICAL)
Validateur: _______________
Verdict: ✅ GO FOR DEMO / ❌ DEFER / ⚠️  CONTINUE WITH CAUTION
```

---

**Gardez ce checklist à côté de vous demain 09:00 ✅**
