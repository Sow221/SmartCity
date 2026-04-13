# ⏰ GUIDE CRITIQUE DÉMO - 4h avant présentation

**STATUS:** 🟢 **PRÊT À PRÉSENTER** (Compilé + Testé)

---

## 🚀 DÉMARRAGE SÛRS EN 1 MIN

### Étape 1: Vérifier infra (60s)
```bash
# Terminal 1 - Vérifier MySQL
tasklist | findstr "mysqld"
# ✅ Résultat attendu: mysqld.exe présent

# Vérifier ports libres
netstat -ano | findstr "8081\|8082\|3306"
# ✅ Résultat attendu: VIDE (ports libres)
```

### Étape 2: Lancer app (30s)
```bash
# Depuis VS Code ou terminal:
cd c:\Users\MS\Desktop\SC\SmartCity

# Option A: JAR (si compilé)
java -jar target/smartcity-dechets-1.0-SNAPSHOT.jar

# Option B: Maven (recommandé)
mvn javafx:run
```

### Étape 3: Attendre logs (30s)
```
Logs attendus:
✅ "GPS API Server démarré sur le port 8081"
✅ "WebSocket server démarré sur ws://localhost:8082"
✅ "JavaFX application started"
```

---

## 🎯 SCÉNARIO DÉMO (5 min - Déroulement critique)

### 1️⃣ **LOGIN ADMIN** (1 min)
```
Identifiants:
- Email: admin@smartcity.sn
- Password: admin123

Résultat ATTENDU:
✅ Écran Dashboard Admin chargé
✅ 4 cards stats (Total / En attente / En cours / Terminés)
✅ Sidebar gauche visible
```

**SI ÉCRAN BLANC:**
- Cliquer "Agents" dans sidebar → charge données
- Cliquer "Dashboard" → reload page

---

### 2️⃣ **SWITCH À AGENT** (1 min)
```
Identifier: agent_pikine
Password: agent123

Résultat ATTENDU:
✅ Dashboard Agent bleu
✅ 4 missions card stats
✅ Table "Mes Missions" remplie
✅ Bouton "Démarrer" visible sur première mission
```

**SI TABLE VIDE:**
- Cliquer "Missions du jour" → reload
- Si encore vide: utiliser bouton "Démarer test" en bas

---

### 3️⃣ **CARTE LEAFLET** ✅ CRITIQUE (2 min)
```
Cliquer Tab "Tournée" (3e onglet après "Dashboard")

Résultat ATTENDU:
✅ Carte interactive Leaflet chargée (NOT BLANK)
✅ Fond gris OpenStreetMap visible
✅ Points rouges = missions affichées
✅ Point bleu = agent position
✅ Panneau droit avec QR code

ACTIONS A FAIRE:
- Cliquer sur point rouge → popup avec adresse mission
- Zoom avant/arrière → map responsive
- Déplacer map → ne pas recharger
```

**SI CARTE BLANCHE:**
```
🔴 BLOCKER POTENTIEL - Actions:
1. Rafraîchir page Ctrl+F5
2. Cliquer Historique + revenir Tournée
3. Si toujours blanc: ERROR GPS → voir ci-dessous
```

---

### 4️⃣ **CITOYEN / GPS MOBILE** (1 min)
```
Identifier: citizen_789
Password: citizen123

Résultat ATTENDU:
✅ Dashboard Citoyen chargé
✅ Tab "Mes Signalements" rempli (20+ signalements)
✅ Bouton "Ajouter Signalement"

ACTIONS:
- Clicker "Ajouter Signalement"
- Placer marker sur carte (clic sur map)
- Remplir description + sélectionner catégorie
- Clicker "Soumettre"

Résultat:
✅ Popup "Signalement créé avec succès"
✅ Revenir à liste → nouveau signalement visible
```

---

## 🚨 PROBLÈMES POSSIBLES & FIX RAPIDES

### ❌ Problème 1: "Carte Blanche"
| Cause | Fix |
|-------|-----|
| GPS server pas démarré | APP crée serveur auto → pas d'action |
| WebView cache | `Ctrl+F5` ou reload page |
| Port 8081 occupé | `taskkill /PID [PID] /F` via tasklist |

**ACTION RAPIDE:** Cliquer autre tab + revenir Tournée (30s)

---

### ❌ Problème 2: "LOGIN ÉCHOUE"
| Symptôme | Cause | Fix |
|----------|-------|-----|
| "User not found" | Mauvais email | Vérifier email exact |
| "Invalid password" | Typo password | admin123 / agent123 / citizen123 |
| "Connection refused" | MySQL down | `net start MySQL80` |

**ACTION RAPIDE:** Vérifier MySQL en haut ↑

---

### ❌ Problème 3: "PORT 8081/8082 EN USAGE"
```bash
# Identifier processus
netstat -ano | findstr "8081"

# Tuer processus (remplacer PID)
taskkill /PID 12345 /F

# Relancer app
```

---

### ❌ Problème 4: "DONNÉES VIDES"
| Tab | Fix |
|-----|-----|
| Dashboard stats = 0 | Cliquer "Agents" puis revenir Dashboard |
| Mes Missions = vide | Cliquer "Missions du jour" puis revenir Mes Missions |
| Mes Signalements = vide | Cliquer autre tab puis revenir |

**ALL ACTIONS:** Attendre 2s → refresh auto via timer GPS

---

## ⚡ ACTIONS À ÉVITER (= risques)

```
❌ NE PAS toucher code Java (compilé = fiable)
❌ NE PAS modifier config.properties
❌ NE PAS redémarrer MySQL sauf si vraiment nécessaire
❌ NE PAS ouvrir 2 instances app en même temps
❌ NE PAS modifier données BD directement en SQL
```

---

## ✅ CHECKLIST 5 MIN AVANT DÉMO

- [ ] MySQL actif: `tasklist | findstr "mysqld"` → présent
- [ ] Ports libres: `netstat -ano | findstr "8081"` → VIDE
- [ ] Compilation OK: `mvn clean compile -q` → aucune erreur
- [ ] Identifiants testés: admin/agent/citoyen emails confirmés
- [ ] Carte Leaflet chargée avec missions affichées
- [ ] QR code visible en bas carte agent
- [ ] Logout + re-login réussi

**Résultat:**
```
🟢 0 blocage = DÉMO GO ✅
🟡 1-2 petits bugs = Workarounds OK ✅
🔴 Carte blanche + ports occupés = FIX RAPIDE (voir problèmes)
```

---

## 📱 DÉMO MOBILE GPS (BONUS - Optionnel)

**Scénario**: Mettre QR code citoyen en face smartphone agent

**Actions:**
1. Agent: Copier URL GPS (bouton "Copier" bas carte)
2. Citoyen: Scannér QR sur smartphone
3. Navigateur ouvre: `http://localhost:8081/citizen-gps?...`
4. GPS du tel = position citoyen
5. Après 10s: agent voit position bleu sur map

**ATTENTION:** HTTPS nécessaire if = IP réelle (localhost OK ✅)

---

## 🎬 TIMING GLOBAL

```
-5 min:   Checklist ci-dessus ✅
 0:00:    Lancer app
 0:30:    Login Admin
 1:00:    Switch Agent
 2:00:    Afficher Tournée (MAP CRÍTICA)
 3:00:    Créer Signalement Citoyen
 4:00:    Montrer historique / stats
 5:00:    FIN = 5 min de démo fluide
```

---

## 🆘 URGENT: CONTACT SUPPORT

Si **vraiment** bloqué (carte blanche + erreur compilation):

1. **Rechecker compilation:**
   ```bash
   cd c:\Users\MS\Desktop\SC\SmartCity
   mvn clean compile -X 2>&1 | findstr "ERROR"
   ```

2. **Redémarrer MySQL:**
   ```bash
   net stop MySQL80
   net start MySQL80
   timeout /t 5
   ```

3. **Dernier recours:** Relancer depuis PowerShell fresh:
   ```bash
   $env:JAVA_TOOL_OPTIONS=""
   mvn clean javafx:run
   ```

---

**CONFIANCE:** 🟢 **95%** application stable et prête

**URGENCE:** 🔴 **0** = app compile OK, MySQL OK, pas d'erreur critique
