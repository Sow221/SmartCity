# 🚀 GUIDE DÉMARRAGE RAPIDE - SmartCity Présentation

**Dernière mise à jour:** 11 avril 2026  
**Status:** ✅ Prêt pour démo

---

## ⏱️ 5 MINUTES AVANT PRÉSENTATION

### Checklist rapide:
```
□ 1. Port 8081 libre       → netstat -ano | findstr :8081
□ 2. Port 8082 libre       → netstat -ano | findstr :8082
□ 3. MySQL démarré         → net start MySQL80 (si nécessaire)
□ 4. Lancer script diag    → diagnostic-servers.bat
□ 5. Si OK → Lancer l'app
```

### Commande lancement (PowerShell):
```powershell
cd c:\Users\MS\Desktop\SC\SmartCity
java -jar target/smartcity-dechets-1.0-SNAPSHOT.jar
```

---

## 🎯 SCÉNARIOS DÉMO CONCIS

### Scénario 1: Agent GPS Tracking (7 min)
```
1. Login: agent@test.com / password123
2. Dashboard → "Cartes des zones"
3. VOIR: Carte Leaflet + marqueur bleu position agent
4. VOIR: Position 14.7646, -17.3920 (Pikine)
5. Attendre 10s → Position se met à jour (ou click "Actualiser")
```

### Scénario 2: Citoyen QR Code (7 min)
```
1. Login: citoyen@test.com / password123
2. Sidebar → "➕ Nouveau signalement"
3. VOIR: 
   - Carte interactive + clic possible
   - QR CODE 140x140 en bas (nouveau ✨)
   - Lien GPS + bouton "Copier"
4. Click sur la carte → Marqueur placé
5. Position affichée: lat/lon mise à jour
```

### Scénario 3: Admin Gestion (5 min)
```
1. Login: admin@test.com / password123
2. Gestion utilisateurs/agents/zones
3. Admin dashboard → voir tous les signalements
4. Carte par zone (Gestion signalements)
```

---

## 🔴 PROBLÈMES COURANTS & FIXES

| Problème | Solution | Commande |
|----------|----------|----------|
| **Port 8081 occupé** | Libérer le port | `taskkill /PID XXX /F` (PID = netstat output) |
| **Port 8082 occupé** | Lancer sur port libre | Changez port dans WebSocketServer.java (RISQUÉ) |
| **MySQL ne répond pas** | Redémarrer MySQL | `net stop MySQL80` → `net start MySQL80` |
| **Carte vide (Leaflet)** | Check CDN (unpkg) | Vérifier connexion internet + F12 console |
| **QR code invisible** | Vérifier FXML binding | Check `citizenQrCodeView` dans citizen_dashboard.fxml |
| **Connexion fail** | Check credentials | admin/agent/citoyen@test.com → password123 |
| **AppFail at startup** | Check logs | Regarder console Maven ou `/logs/smartcity.log` |

---

## 💻 COMPTES DE TEST

| Rôle | Email | Password | Zone |
|------|-------|----------|------|
| Admin | admin@test.com | password123 | (N/A) |
| Agent | agent@test.com | password123 | Pikine |
| Agent 2 | agent2@test.com | password123 | Guédiawaye |
| Citoyen | citoyen@test.com | password123 | Pikine |

**Créer comptes test rapido:**
```sql
mysql -u root -p77884455 db_smartcity <<EOF
TRUNCATE TABLE Utilisateur;
INSERT INTO Utilisateur (prenom, nom, email, mdp, role, idZone, actif) VALUES
  ('Admin', 'Test', 'admin@test.com', '\$2a\$10\$...[bcrypt]...', 'Administrateur', 1, TRUE),
  ('Agent', 'Test', 'agent@test.com', '\$2a\$10\$...[bcrypt]...', 'Agent', 1, TRUE),
  ('Citoyen', 'Test', 'citoyen@test.com', '\$2a\$10\$...[bcrypt]...', 'Citoyen', 1, TRUE);
EOF
```

---

## 📊 POINTS CLÉS À PRÉSENTER

### **Géolocalisation ✨ [Core Feature]**
- ✅ Cartes Leaflet (OpenStreetMap)
- ✅ Real-time GPS tracking agents (10s polling)
- ✅ QR code citoyens pour fallback geo
- ✅ Routes optimisées (TSP greedy)
- ✅ Animations & UI smooth

### **Sécurité**
- ✅ BCrypt passwords
- ✅ SQL injection prevention (PreparedStatements)
- ✅ Session timeout
- ✅ Role-based access control

### **Architecture**
- ✅ 3 dashboards (Admin/Agent/Citizen)
- ✅ Services découplés (GeolocationService, RealTimeGPSService, etc.)
- ✅ Real-time WebSocket + HTTP API
- ✅ Configuration externalisée

---

## 🎬 DÉMARRAGE COMPLET (Pas à pas)

### Étape 1: Préparer l'environnement
```bash
# Ouvrir PowerShell
cd c:\Users\MS\Desktop\SC\SmartCity

# Lancer diagnostic
.\diagnostic-servers.bat
# Résultat attendu: 🟢 TOUS LES DIAGNOSTICS OK
```

### Étape 2: Compiler (si besoin)
```bash
mvn clean package -DskipTests -q
# Résumé: construira JAR en target/
```

### Étape 3: Démarrer l'app
```bash
java -jar target/smartcity-dechets-1.0-SNAPSHOT.jar
# Logs: 
#   GPS API Server démarré sur le port 8081
#   WebSocket server démarré sur ws://localhost:8082/ws/...
#   JavaFX window ouvre
```

### Étape 4: Tester les flows
- Agent login → Carte → QR code (si citoyen)
- Citoyen login → Nouveau signalement → QR visible

### Étape 5: End - Graceful shutdown
```bash
Click Logout ou close window
# Nettoyage threads + sockets automatique
```

---

## 📱 TEST MOBILE (Optionnel - Nice to have)

Si vous testez le **QR code sur vraie appli mobile**:

1. Scan le QR genéré avec iPhone/Android QR scanner
2. Browser ouvre: `http://192.168.XX.XX:8081/gps/...` (IP local)
3. Page demande permission géolocalisation
4. Envoie coordonnées GPS → carte se met à jour en temps réel

**Prérequis:** Téléphone + desktop **sur même WiFi**

---

## 🎓 TIPS POUR LA DÉMO

| Tip | Détail |
|-----|--------|
| **Timing** | Allocate 20-25 min pour 3 scénarios complètement |
| **Fallback** | Screenshots pré-prises en cas de problem tech |
| **Data** | Avoir données de test prêtes (comptes + zones) |
| **Network** | Tester WiFi stabilité avant (GPS tracking = polling) |
| **Permissions** | Windows Defender peut bloquer ports → check firewall |
| **Logs** | Garder console ouverte pour montrer logs en live |

---

## ✅ POST-DÉMO CHECKLIST

Après présentation:
- [ ] Note ce qui a bien marché
- [ ] Note les problèmes encountered
- [ ] Garder screenshots + vidéo
- [ ] Documente fixes appliquées

---

**Vous êtes prêt(e) ! Good luck tomorrow! 🚀**
