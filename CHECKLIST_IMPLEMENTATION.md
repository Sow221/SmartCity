# ✅ CHECKLIST D'IMPLÉMENTATION - 9 Problèmes

**À faire:** Cocher les cases une par une lors de l'implémentation  
**Status:** 📋 PRÊT À COMMENCER

---

## 🔴 PHASE 1: DÉMO CRITIQUE (P0)

### ✏️ **Tâche 1.1: Réparer Carte Blanche (Problème #1)**
- [ ] Lire `smart-gps-map.js` au startup de `AgentDashboardController`
- [ ] Stocker le contenu en variable statique `SMART_GPS_MAP_JS`
- [ ] Injecter directement dans `buildLeafletHtml()` via `<script>${code}</script>`
- [ ] Tester dans WebView: vérifier que la carte s'affiche
- [ ] Vérifier les marqueurs de missions s'affichent
- **Fichiers à modifier:** 
  - `src/main/java/com/smartcity/controller/AgentDashboardController.java`

### ✏️ **Tâche 1.2: Supprimer Clignotement (Problème #2)**
- [ ] Créer fonction JS `updateAgentPosition(lat, lon, missions)` dans `smart-gps-map.js`
- [ ] Remplacer `refreshMapWithOptimizedRoute()` par `webEngine.executeScript(...)`
- [ ] Tester: timer GPS qui ne recrée pas la carte
- [ ] Vérifier: interactions utilisateur (zoom, pan) persistent
- **Fichiers à modifier:**
  - `src/main/java/com/smartcity/controller/AgentDashboardController.java`
  - `src/main/resources/js/smart-gps-map.js`

### ✏️ **Tâche 1.3: Corriger missionPoint() (Problème #6)**
- [ ] Vérifier la structure de `Signalement`: getLatitude(), getLongitude()
- [ ] Modifier logic: utiliser coords réelles si non-null/non-zéro
- [ ] Fallback sur grille zone uniquement si coords manquantes
- [ ] Vérifier en base: SELECT COUNT(*) WHERE latitude=0 OR longitude=0
- [ ] Si beaucoup de 0,0: lancer script de correction SQL
- **Fichiers à modifier:**
  - `src/main/java/com/smartcity/controller/AgentDashboardController.java` (ligne 1131)
  - `scripts/fix_missions_coordinates.sql` (créer si nécessaire)

---

## 🟠 PHASE 2: STABILITÉ (P1)

### ✏️ **Tâche 2.1: Fixer ALTER TABLE (Problème #9)**
- [ ] Implémenter `hasZoneGpsColumns(Connection conn)` 
- [ ] Vérifier colonnes AVANT ALTER TABLE
- [ ] Remplacer catch silencieux par logger.error
- [ ] Tester: aucun avertissement lors du startup
- **Fichiers à modifier:**
  - `src/main/java/com/smartcity/service/GpsApiServer.java` (ligne 85)

### ✏️ **Tâche 2.2: Réparer GPS HTTP (Problème #3)**
- [ ] **Option A (Démo):** Changer point de terminaison en `http://localhost:8081/gps`
- [ ] **Option B (Production):** Générer certificat auto-signé + configurer HTTPS
- [ ] Tester GPS sur navigateur du téléphone
- **Fichiers à modifier:**
  - `src/main/java/com/smartcity/service/GpsApiServer.java`
  - Config ou `.properties` si applicable

---

## 🟡 PHASE 3: NETTOYAGE (P2-P3)

### ✏️ **Tâche 3.1: Injecter citizen-geolocation.js (Problème #4)**
- [ ] Appliquer même pattern que smart-gps-map.js
- [ ] Lire le fichier au startup
- [ ] Injecter dans HTML généré par CitizenDashboardController
- [ ] Supprimer duplication HTML inline si applicable
- **Fichiers à modifier:**
  - `src/main/java/com/smartcity/controller/CitizenDashboardController.java`
  - `src/main/resources/js/citizen-geolocation.js`

### ✏️ **Tâche 3.2: Persister Tokens GPS (Problème #8)**
- [ ] Créer classe `GpsTokenManager` (ou enhanced `GpsApiServer`)
- [ ] Implémenter `saveToken(uuid, userId, expiry)`
- [ ] Implémenter `validateToken(uuid)`
- [ ] Charger tokens depuis fichier/BD au startup
- [ ] Tester: token persiste après redémarrage
- **Fichiers à modifier/créer:**
  - `src/main/java/com/smartcity/service/GpsApiServer.java`
  - `src/main/java/com/smartcity/service/GpsTokenManager.java` (nouveau)
  - `gps_tokens.json` (pour démo - sera créé par le code)

### ✏️ **Tâche 3.3: Résoudre WebSocket (Problème #5)**
- [ ] **Option A:** Supprimer `AgentMissionEndpoint` (si pas prévu)
- [ ] **Option B:** Laisser inactif + documenter "pour Phase 2"
- [ ] Choisir l'une des deux après discussion avec l'équipe
- **Fichiers concernés:**
  - `src/main/java/com/smartcity/websocket/AgentMissionEndpoint.java`

### ✏️ **Tâche 3.4: Nettoyer dossier GPS (Problème #7)**
- [ ] Supprimer dossier `gps-server/` s'il est réellement vide
- [ ] Ajouter `gps-server/` à `.gitignore` si on le garde
- [ ] Documenter concept dans `README.md` si prévu pour futur
- **Fichiers concernés:**
  - `gps-server/` (dossier)
  - `.gitignore`
  - `README.md`

---

## 🧪 VALIDATION COMPLÈTE

### Tests Avant Livraison
- [ ] **Carte:** S'affiche correctement (pas blanche)
- [ ] **Missions:** Aux bonnes coordonnées (pas grille fictive)
- [ ] **Interactions:** Zoom/pan fonctionne sans reload
- [ ] **Timer GPS:** Carte ne clignote pas (10s update)
- [ ] **GPS Mobile:** Position obtenue (HTTPS ou localhost)
- [ ] **Logs:** Pas d'erreurs ALTER TABLE
- [ ] **Redémarrage:** Tokens GPS persistent (démo)
- [ ] **Code:** Pas de fichiers inutilisés

### Régression Testing
- [ ] Agent Dashboard toujours fonctionnel
- [ ] Citizen Dashboard toujours fonctionnel (avec citizen-geolocation)
- [ ] Reports Dashboard ok
- [ ] Login/Auth ok
- [ ] Aucun crash détecté

---

## 📊 SUIVI TEMPS ESTIMÉ

| Phase | Tâches | Temps Estimé | Notes |
|-------|--------|--------------|-------|
| P0 #1 | 1.1 Carte blanche | 1-2h | Injection JS simple |
| P0 #2 | 1.2 Clignotement | 1-2h | Lié à #1, testing crucial |
| P0 #3 | 1.3 Coords missions | 30min-1h | Logic simple, but vérifier data |
| **P0 Total** | | **3-5h** | **Jour 1-2** |
| P1 #9 | 2.1 ALTER TABLE | 30min | Quick fix |
| P1 #3 | 2.2 GPS HTTP | 1-2h | Choix architecture |
| **P1 Total** | | **1.5-2.5h** | **Jour 2** |
| P2 #4 | 3.1 citizen-geo | 1h | Pattern COPY from #1 |
| P2 #8 | 3.2 Tokens persist | 1-2h | Fichier JSON simple |
| P3 #5 | 3.3 WebSocket | 15min | Décision + suppr/doc |
| P3 #7 | 3.4 Nettoyer | 15min | Ménage |
| **P2-P3 Total** | | **2.5-3.5h** | **Jour 3** |
| **TOTAL** | | **7-11h** | **3-4 jours** |

---

## 🚀 ORDRE SUGGERÉ D'IMPLÉMENTATION

1. **Matin Jour 1:** Tâches 1.1 + 1.2 (carte & interactions)
2. **Après-midi Jour 1:** Tâche 1.3 (missions coordonnées) + testing
3. **Matin Jour 2:** Tâche 2.1 (ALTER TABLE) + 2.2 (GPS)
4. **Après-midi Jour 2:** Testing complet
5. **Jour 3:** Tâches P2-P3 (nettoyage) + régression testing
6. **Jour 3 Soir:** Déploiement démo

---

**Créé:** 11/04/2026  
**Mis à jour:** En attente d'implémentation  
**Responsable:** À désigner  
