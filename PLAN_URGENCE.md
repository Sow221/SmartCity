# 🚨 PLAN D'URGENCE - SmartCity

**À utiliser SEULEMENT si problème grave 30 min avant démo**

---

## ⚡ QUICK FIXES (30 SECONDES)

### 1️⃣ App ne démarre pas
```
❌ Erreur: "Address already in use :8081"
✅ Fix: Fermer autres applis utilisant :8081

netstat -ano | findstr :8081
taskkill /PID [PID] /F

Relancer: java -jar target/smartcity-dechets-1.0-SNAPSHOT.jar
```

### 2️⃣ Carte Leaflet vide
```
❌ Problème: WebView montre page blanche
✅ Fix: 

1. Vérifier connexion internet (CDN unpkg.com)
2. Vérifier F12 console (Web Dev Tools)
   - Si error CORS: installer proxy CORS
   - Si timeout: retry après 5s

F12 → Network → chercher 'leaflet.css' (200 OK = bon)
```

### 3️⃣ Connexion impossible
```
❌ Erreur: "Identifiants invalides" même bon compte
✅ Fix:

1. Vérifier comptes existent en DB:
   mysql -u root -p77884455 db_smartcity -e "SELECT * FROM Utilisateur;"

2. Si comptes vides:
   mysql -u root -p77884455 db_smartcity < scripts/test_data.sql

3. Vérifier password BCrypt correct:
   App a logique: ValidationUtils.isPasswordValid()
```

### 4️⃣ QR code pas visible
```
❌ Problème: Citoyen dashboard → QR section absente
✅ Fix:

1. Vérifier FXML binding:
   → citizen_dashboard.fxml ligne 245
   → `<ImageView fx:id="citizenQrCodeView"...` présent?

2. Vérifier contrôleur:
   → CitizenDashboardController ligne 85
   → `@FXML private ImageView citizenQrCodeView;` ?

Si absent: chercher PRÉ-COMMIT d'hier, restore from git
```

### 5️⃣ Real-time tracking pas à jour
```
❌ Position agent n'évolue pas > 30 secondes
✅ Fix:

1. Check GpsApiServer running:
   Regarder logs: "GPS API Server démarré sur le port 8081"

2. Check polling loop:
   AgentDashboardController.refresh() appelé chaque 10s?

3. Redémarrer:
   Logout agent → Login → Dashboard → Attendez 15s
```

---

## 🔧 MEDIUM FIXES (2-3 MINUTES)

### Si compilation cassée
```bash
# Clean rebuild
mvn clean compile -e

# Si persiste:
mvn clean compile -X 2>&1 | grep ERROR

# Chercher ligne ERROR exacte, fix.
# Commit: git add . && git commit -m "Fix compilation"
```

### Si DB corrompue
```sql
-- Reset complet (WARNING: Perte données)
DROP DATABASE db_smartcity;
mysql -u root -p77884455 < src/main/resources/sql/gestion_dechets.sql

-- Recharger données test
mysql -u root -p77884455 db_smartcity < scripts/test_data.sql
```

### Si WebSocket bloque
```
❌ Erreur: "WebSocket server (port 8082 occupé ?)"
✅ Fix:

1. Redémarrer port 8082:
   netstat -ano | findstr :8082
   taskkill /PID [PID] /F

2. Éventuellement, sur port libre:
   Modifier WebSocketServer.java ligne 15:
   public static final int PORT = 8082;  → 8083

   (ATTENTION: Risqué, relancer app pour reload)
```

---

## 🟡 FALLBACK DEMO (SI TOUT BLOQUE)

**Option: Passer directement aux screenshots/vidéo**

### Pré-enregistrer démo (NOW):
```
1. Lancer app en local (fonctionne bien)
2. Faire screencast:
   - Montrer login
   - Montrer agent map + GPS tracking
   - Montrer citoyen QR + cart placement
3. Export vidéo MP4
4. Créer slides PowerPoint avec vidéo embedée
```

### Présenter via vidéo + live slides:
```
Timeline démo vidéo:
  00:00 - Login (15s)
  00:15 - Agent dashboard & map load (30s)
  00:45 - Real-time position update (20s)
  01:05 - Citizen QR code & map interaction (40s)
  01:45 - End
```

---

## 📚 RESSOURCES D'URGENCE

### Documentation disponible:
- **AUDIT_GEOLOCALISATION_COMPLET.md** — Architecture complète
- **GUIDE_TECHNIQUE_DEVELOPEUR.md** — Code détail
- **PLAN_TEST_PRESENTATION.md** — Tous les tests
- **PRE_DEMO_CHECKLIST.md** — Validation pré-démo

### Support technique:
| Question | Réponse rapide |
|----------|---|
| Ports libres? | `netstat -ano` |
| DB accessible? | `mysql -u root -p77884455 db_smartcity -e "SELECT 1;"` |
| App compile? | `mvn clean compile -q` |
| Version Java? | `java -version` (doit être 17+) |
| Quoi debugger? | `mvn compile -X 2>&1 \| grep ERROR` |

---

## 🎯 PRIORITÉS SI TEMPS LIMITÉ

**Si 30 minutes avant démo:**

### Plan A) Tout fonctionne
✅ Go for live demo!

### Plan B) Mineur issue (5-10 min fix)
1. Lancer diagnostic.bat
2. Fix le problème identifié
3. Retest 1 scenario
4. OK? → Still go live
5. Not OK? → Plan C

### Plan C) Majeur issue (>15 min à fixer)
1. Stop trying to fix (rabbit hole!)
2. Pivot to slide presentation
3. Montrer vidéo démo + Architecture diagram
4. Code walkthroughs live (pas d'app)

### Plan D) LAST RESORT
→ Annoncer "Technical difficulties" habilement
→ Focus sur présentation concept + architecture
→ Promettre démo en follow-up
→ Show code repo / documentation quality

---

## ✨ CONFIDENCE CHECK

| Élément | Ready | Actions |
|---------|-------|---------|
| Code compile | ✅ | ← Fait hier |
| DB structure | ✅ | ← Audit complet |
| Géo logic | ✅ | ← QR code fixed |
| Serveurs | ✅ | ← Diagnostic prêt |
| Tests | ✅ | ← 10 tests checklist |
| Fallback | ✅ | ← Ce plan! |

**Verdict: 🟢 TRÈS PROBABLE que tout fonctionne demain**

Pire cas? vous avez ce plan d'urgence → pas de stress.

---

**Good luck! You got this! 💪**
