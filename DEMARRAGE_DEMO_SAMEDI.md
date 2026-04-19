# ⚡ DÉMARRAGE RAPIDE - COMMANDES DÉMO

**Fichier**: À exécuter samedi matin pour démo 11h

---

## 📋 PRÉREQUIS

✅ MySQL 8.0 installé  
✅ Java 17 LTS installé  
✅ Workspace: `C:\Users\MS\Desktop\SC\SmartCity`  
✅ Port 3306 libre (MySQL)  
✅ Port 8080 libre (UI JavaFX)  
✅ Port 8081 libre (GPS HTTPS)  
✅ Port 8082 libre (WebSocket)  

---

## 🚀 ÉTAPE 1: Démarrer MySQL (2 min)

### Sous Windows

```bash
# Option A: Service Windows (si installé)
net start MySQL80
# Statut: MySQL80 Service is starting... le service MySQL80 a démarré avec succès.

# Option B: Directement
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysqld.exe --console

# Vérifier connexion
mysql -h localhost -u root -p77884455
# Prompt: mysql>
```

### Vérifier base de données
```sql
mysql> use db_smartcity;
Database changed

mysql> show tables;
# Doit afficher 15+ tables (Utilisateur, Signalement, Zone, etc.)

mysql> select count(*) from Utilisateur;
# Doit afficher: 3 (agent, admin, citizen)

mysql> exit
```

---

## 🚀 ÉTAPE 2: Démarrer App SmartCity (5 min)

### Terminal 1: Compiler + Lancer

```bash
cd C:\Users\MS\Desktop\SC\SmartCity

# Compile et lance
mvnw.cmd clean javafx:run

# Attendre ~35 secondes pour compilation

# Statut attendu:
# [INFO] Building SmartCity Dechets 1.0-SNAPSHOT
# [INFO] BUILD SUCCESS
# [JavaFX Application] Starting SmartCity...
```

### Vérifier démarrage

**Console output attendu**:
```
✅ smart-gps-map.js chargé en mémoire (4500 bytes)
✅ GPS Server HTTPS démarré sur https://0.0.0.0:8081
✅ WebSocket Server démarré sur port 8082
✅ JavaFX UI ready at localhost:8080
```

**Fenêtre UI attendue**:
- Titre: "SmartCity - Gestion Déchets"
- Logo SmartCity visible
- Champs Login: Email, Password
- Boutons: Login, Register, Forgot Password

---

## 🚀 ÉTAPE 3: Login Agent (1 min)

```
Email:    agent@smartcity.sn
Password: password123
Bouton:   LOGIN
```

**Écran attendu après login**:
- Dashboard Agent s'affiche
- Carte Leaflet visible (OpenStreetMap)
- Marqueurs missions (couleurs)
- Tableau "Mes missions" avec données
- Zoom niveau 13 sur zone MEDINA

---

## 🚀 ÉTAPE 4: Tester Carte Interactive (3 min)

### Test 1: Visibilité Carte
```
Actions:
1. Attendre chargement 2s
2. Vérifier carte blanche? NON ✅ (sinon problem)
3. Vérifier marqueurs visibles? OUI ✅
   - Marqueurs rouges (NEW)
   - Marqueurs orange (IN_PROGRESS)
   - Marqueurs verts (RESOLVED)
4. Vérifier zoom fonctionne? OUI ✅
   - Scroll mouse: zoom in/out
   - Double-clic: zoom 2x
```

### Test 2: Pas de Clignotement
```
Actions:
1. Initialiser zoom: niveau 13
2. Pannier carte: drag avec souris
3. Attendre 10 secondes sans action
4. Vérifier zoom inchangé? OUI ✅ (pas clignotement)
5. Vérifier pan inchangé? OUI ✅
6. Repeat étape 3-5 plusieurs fois
```

### Test 3: GPS Geolocation
```
Actions:
1. Chercher bouton "Ma Position" (bleu)
2. Cliquer "Ma Position"
3. Navigateur demande permission geolocation
   - Accepter: Allow
4. Marqueur bleu apparaît (position agent)
5. Console F12 (Ctrl+Shift+I):
   - Network tab: https://localhost:8081 (voir requête)
   - Console tab: pas d'erreur 403/405
6. Vérifier position correcte (près de Dakar)
```

### Test 4: Clic sur Mission
```
Actions:
1. Cliquer marqueur mission (n'importe quel)
2. Popup affiche:
   - Zone
   - ID Mission
   - Type/Catégorie
   - Statut
   - Adresse
3. Fermer popup: clic ailleurs
```

---

## 🚀 ÉTAPE 5: Changer Statut Mission (2 min)

### Via Tableau
```
1. Tableau "Mes missions" visible
2. Sélectionner mission (clic ligne)
3. Bouton "Marquer en cours" 
   (ou statut actuel)
4. Popup confirmation
5. Cliquer OK
6. Attendre: Statut change DANS TABLEAU
7. Attendre 5s: Marqueur change COULEUR SUR CARTE
```

### Vérification DB
```bash
# Ouvrir nouveau terminal:
mysql -h localhost -u root -p77884455

mysql> use db_smartcity;
mysql> SELECT id_signalement, statut, latitude, longitude 
       FROM Signalement WHERE statut='IN_PROGRESS' LIMIT 1;
# Doit afficher la mission changée
```

---

## 🚀 ÉTAPE 6: Admin Dashboard (2 min)

### Logout Agent
```
1. Cliquer bouton Logout (coin haut droit)
2. Ou: Menu → Disconnect
3. Retour à login page
```

### Login Admin
```
Email:    admin@smartcity.sn
Password: password123
Bouton:   LOGIN
```

### Explorer Dashboard Admin
```
Actions:
1. Voir graphiques missions par zone
2. Voir statistiques par statut
3. Voir tableau utilisateurs
4. Voir gestion zones
5. Tester filtres dates
6. Exporter rapport (si disponible)
```

---

## 🔧 TROUBLESHOOTING RAPIDE

### Problème: Carte blanche
```
Diagnostic:
1. Ouvrir F12 console
2. Chercher erreur smart-gps-map.js
3. Vérifier: Network → js/ folder

Solution rapide:
- Recharger page (F5)
- Redémarrer app
- Vérifier mvnw compile réussi
```

### Problème: Clignotement constant
```
Diagnostic:
1. Console F12
2. Vérifier executeScript() s'exécute
3. Pas d'erreur JavaScript

Solution rapide:
- Cliquer "Arrêter" refresh (si bouton existe)
- Accepter slow update
- Pas critical pour démo
```

### Problème: GPS pas d'autorisation
```
Diagnostic:
1. Console F12
2. Chercher: "Geolocation permission denied"
3. Vérifier https:// (pas http://)

Solution rapide:
Android:
- Settings → Apps → SmartCity → Permissions → Location → Allow
- Redémarrer app

iOS:
- Settings → SmartCity → Location → "While Using"

Desktop:
- Accepter demande browser (notification popup)
```

### Problème: App ne démarre pas
```
Diagnostic:
1. Vérifier MySQL tournant: mysql -h localhost -u root -p77884455
2. Vérifier ports libres: netstat -ano | findstr ":3306"
3. Vérifier Java 17: java -version

Solution rapide:
- Arrêter MySQL: net stop MySQL80
- Tuer processus Java: taskkill /F /IM java.exe
- Redémarrer terminal
- `mvnw.cmd clean javafx:run`
```

### Problème: Erreur compilation
```
Diagnostic:
1. Lire message erreur complet
2. Chercher [ERROR] dans output

Solution rapide:
- Nettoyer: mvnw clean
- Compiler: mvnw compile
- Si encore erreur: relancer terminal
```

---

## 📊 CHECKLIST DÉMARRAGE (À COPIER)

### Avant démo (30 min avant)
- [ ] MySQL 8.0 démarré (net start MySQL80)
- [ ] Terminal 1 prêt: cd C:\Users\MS\Desktop\SC\SmartCity
- [ ] Commande compile mémorisée: mvnw.cmd clean javafx:run
- [ ] Identifiants copiés/mémorisés (email, password)
- [ ] Ports testés: 3306, 8080, 8081, 8082

### Au démarrage app
- [ ] Console: BUILD SUCCESS
- [ ] Console: smart-gps-map.js chargé
- [ ] Console: GPS Server HTTPS started
- [ ] Fenêtre UI: Login page visible
- [ ] Pas d'erreur rouge dans console

### Après login agent
- [ ] Carte visible (pas blanche)
- [ ] Marqueurs affichés
- [ ] Tableau missions rempli
- [ ] Zoom fonctionne
- [ ] Pas de clignotement

### Avant démo live
- [ ] Une mission marquée "IN_PROGRESS"
- [ ] Carte zoomée sur zone MEDINA
- [ ] Console F12 fermée (pas confondre visiteur)
- [ ] Volume son OFF (pas de notification surprise)
- [ ] Backup screenshot (plan B si crash)

---

## ⏱️ TIMING PRÉVU

```
Setup MySQL:           2 min
Build & Launch App:    5 min
Login:                 1 min
Présenter Carte:       3 min
Tester GPS:            2 min
Changer Statut:        2 min
Admin Dashboard:       2 min
────────────────────────────
TOTAL:                17 min
Buffer (questions):    8 min
────────────────────────────
DÉMO TOTALE:          25 min ✅
```

---

## 📞 CONTACTS D'URGENCE

Si problème pendant démo:

1. **Carte noire**: Recharger (F5)
2. **App crash**: Redémarrer terminal
3. **DB erreur**: Arrêter/redémarrer MySQL
4. **Port occupé**: Tuer java.exe et relancer

**Fallback**: Montrer vidéo pré-enregistrée démo (à préparer)

---

## 🎬 FINALE

Après démo:

```bash
# Terminal 1: Arrêter app
Ctrl+C

# Terminal 2: Arrêter MySQL
net stop MySQL80

# Sauvegarder logs (optionnel)
copy target\logs\smartcity.log logs\demo-19avril.log
```

---

**Prêt? Bonne chance samedi! 🚀**

