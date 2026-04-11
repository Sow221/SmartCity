# ⚡ CHECKLIST DÉMARRAGE 5MIN - DEMAIN MATIN

**À remplir 5 minutes AVANT de lancer la démo**

---

## ✅ BEFORE LAUNCH (5MIN)

### 1. INFRA CHECK
- [ ] Ports 8081 + 8082 free? `netstat -ano | findstr :8081`
- [ ] MySQL running? `net start MySQL80` (if needed)
- [ ] Network OK? (ping google.com)

### 2. CODE READY
- [ ] Dernière version compilée? `mvn clean compile`
- [ ] GIT clean? (no uncommitted changes)
- [ ] config.properties present?

### 3. QUICK DIAGNOSTIC
- [ ] Run `diagnostic-servers.bat` → all ✅?
- [ ] GpsApiServer responds? (HTTP localhost:8081)
- [ ] WebSocket responds? (ws://localhost:8082)

---

## ✅ LAUNCH

```bash
cd c:\Users\MS\Desktop\SC\SmartCity
java -jar target/smartcity-dechets-1.0-SNAPSHOT.jar
```

**Wait for logs:**
```
✅ GPS API Server démarré sur le port 8081
✅ WebSocket server démarré
✅ [JavaFX] Stage showing
```

---

## ✅ 30SEC SMOKE TEST

### Login Agent
- [ ] Email: agent@test.com / Password: password123
- [ ] Dashboard loads? ✅
- [ ] Cartes zones clickable? ✅  
- [ ] Leaflet map visible? ✅
- [ ] Blue marker (agent pos)? ✅

### Login Citizen
- [ ] Email: citoyen@test.com / Password: password123
- [ ] Dashboard loads? ✅
- [ ] Nouveau signalement → map interactive? ✅
- [ ] **QR CODE VISIBLE?** ✅ ← **CRITICAL**
- [ ] Click map → marker appears? ✅

### Login Admin
- [ ] Email: admin@test.com / Password: password123
- [ ] Dashboard loads? ✅
- [ ] Gestion signalements map visible? ✅

---

## 🆘 IF SOMETHING FAILS

**Consult:** `PLAN_URGENCE.md`

Quick fixes:
```
Port occupied?    → taskkill /PID ... /F
App won't compile → mvn clean compile -X
Maps not loading  → check internet, check Leaflet CDN
GPS not updating  → check GpsApiServer:8081
QR missing?       → check CitizenDashboardController loadInteractiveMap()
```

---

## ✅ GO LIVE

**Everything ✅ ?**

🚀 **LAUNCH DEMO!**

---

*Estimated time: 5 minutes*  
*Confidence: 98%*  
*Bonne chance! 🍀*
