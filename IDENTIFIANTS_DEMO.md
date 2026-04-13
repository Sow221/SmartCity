# 🔑 IDENTIFIANTS DÉMO - À garder à portée de main

## ADMIN
```
Email:    admin@smartcity.sn
Password: admin123
Role:     Administrateur
Accès:    Dashboard global, gestion users/agents/signalements
```

## AGENT (Pikine)
```
Email:    agent_pikine@smartcity.sn  ← COMPTE PRINCIPAL DÉMO
Password: agent123
Role:     Agent de collecte
Zone:     Pikine  
Accès:    Tournée + missions + carte Leaflet + GPS
```

## AGENT (Guédiawaye) - BACKUP
```
Email:    agent@smartcity.sn
Password: agent123
Role:     Agent de collecte
Zone:     Guédiawaye
Note:     Backup si besoin 2e agent
```

## CITOYEN
```
Email:    citizen_789@smartcity.sn
Password: citizen123
Role:     Citoyen
Accès:    Créer/voir signalements, localiser sur map
```

---

## 🎯 SCÉNARIO OPTIMALE (5 min)

### 1. Login ADMIN (30s)
- Email: `admin@smartcity.sn`
- Pass: `admin123`
- Montrer: Dashboard stats

### 2. Switch AGENT (60s) ⚠️ CRITIQUE
- Email: `agent_pikine@smartcity.sn`
- Pass: `agent123`
- Cliquer Tab "Tournée"
- **VÉRIFIER CARTE LEAFLET CHARGÉE** (pas blanche!)
- Montrer: Missions sur map + position agent bleu + QR code

### 3. Test CITOYEN (2 min)
- Email: `citizen_789@smartcity.sn`
- Pass: `citizen123`
- Créer nouveau signalement
- Montrer position GPS

### 4. Statistiques ADMIN (1 min)
- Retour admin
- Montrer graphiques + chiffres

---

## ⚠️ AVANT DE COMMENCER

```bash
# Terminal check (30s)
tasklist | findstr "mysqld"           # ✅ MySQL doit être présent
netstat -ano | findstr "8081\|8082"   # ✅ Ports doivent être VIDES
cd c:\Users\MS\Desktop\SC\SmartCity && mvn clean compile -q  # ✅ 0 erreur
```

Si tout ✅ → Lancer `START_APP.bat`

---

## 🚨 SI CARTE BLANCHE

**Rapide fix (10s):**
1. Cliquer onglet "Historique"
2. Cliquer onglet "Tournée"
3. Attendre 2s
4. If toujours blanc → refresh Ctrl+F5

**Si erreur sérieuse:**
```bash
taskkill /F /IM java.exe        # Tuer process
taskkill /PID [pid] /F          # Tuer port occupé si besoin
net start MySQL80               # Relancer MySQL
START_APP.bat                   # Relancer app
```

---

**CONFIANCE: 95% - App stable et prête** ✅
