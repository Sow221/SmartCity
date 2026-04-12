# 🚀 GUIDE RAPIDE POST-CORRECTIONS

## Avant de Déployer

```bash
# 1. Compiler le projet
mvn clean compile

# 2. Vérifier s'il n'y a pas d'erreurs
mvn clean test -Dtest=** # Ou vos tests spécifiques
```

## Démarrer l'Application

```bash
# 1. Lancer l'application Java
mvn javafx:run

# 2. En parallèle, gps-server doit fonctionner
# Windows:
start-gps-server.bat

# Linux/Mac:
./start-gps-server.bat  # ou bash start-gps-server.sh si créé
```

## Tester GPS sur Mobile

### Via HTTP (RECOMMANDÉ pour tests)
```
http://192.168.1.X:3001/gps?agentId=1&token=XXXXXXXX

Remplacer:
- 192.168.1.X par l'IP locale du serveur
- agentId par l'ID agent réel
- token par le token généré
```

### Via HTTPS (Production)
```
https://192.168.1.X:3002/gps?agentId=1&token=XXXXXXXX

Note: Accepter le certificat auto-signé sur mobile
```

## Points Clés à Tester

### ✅ Coordonnées Dynamiques
- Créer une mission EN AVEC coordonnées GPS réelles
- Vérifier qu'elle apparaît à son emplacement exact (pas en grille)
- Créer une mission SANS coordonnées  
- Vérifier qu'elle apparaît autour du centre de zone

### ✅ Temps Réel WebSocket
- Ouvrir console navigateur (F12)
- Vérifier: `✅ WebSocket connecté`
- Créer nouvelle mission
- Observer mise à jour INSTANTANÉE sur la carte (pas 30s d'attente)

### ✅ Position Agent
- Sur mobile: cliquer "Envoyer ma position GPS"
- Observer marqueur agent qui se met à jour
- Rayon de zone doit s'afficher

### ✅ Smart GPS Map
- Vérifier tous les marqueurs sont colorés par statut
- Vérifier la mission la plus proche brille
- Vérifier les boutons "Sélectionner" et "Naviguer" fonctionnent

## Résolution des Problèmes

### ❌ "Connexion HTTPS bloquée"
```
→ Solution: Utiliser HTTP à la place
→ http://192.168.1.X:3001/...
```

### ❌ "Position GPS n'apparaît pas"
```
→ Vérifier: Coordonnées != 0,0 dans table Signalement
→ Vérifier: Mission associée à une Zone existante
→ Vérifier: Lancer le gps-server avant l'app Java
```

### ❌ "WebSocket pas connecté"
```
→ Vérifier console (F12) pour messages d'erreur
→ Vérifier port 3002 disponible
→ Vérifier network connectivity
```

### ❌ "Compilation échoue avec imports"
```
→ Vérifier org.slf4j:slf4j-api dans pom.xml
→ Vérifier org.slf4j:slf4j-simple dans pom.xml
→ Lancer: mvn clean dependency:resolve
```

## Configuration Recommandée

### Development
```
GPS: HTTP (3001)
WebSocket: WS (3002)
Network: WiFi local (192.168.x.x)
```

### Production
```
GPS: HTTPS (3002)
WebSocket: WSS (3002)
Network: VPN ou certificat valide
```

## Fichiers Traces à Vérifier

```
logs/                          # Logs de l'application
target/classes/config.properties  # Configuration runtime
src/main/resources/js/smart-gps-map.js  # Carte JavaScript
```

## Commandes Utiles

```bash
# Nettoyer les builds
mvn clean

# Compiler uniquement
mvn compile

# Compiler + tests
mvn clean test

# Package JAR
mvn clean package

# Voir tousles logs
tail -f logs/*.log

# Tester port disponible
netstat -int | grep 3001
```

---

## ✅ Checklist de Déploiement

- [ ] Toutes les corrections compilent sans erreur
- [ ] GpsApiServer démarre sur port 3001 (HTTP) + 3002 (HTTPS)
- [ ] WebSocketServer démarre sur port 3002
- [ ] Position agent se met à jour
- [ ] Missions s'affichent aux bonnes coordonnées
- [ ] WebSocket connecté (console affiche ✅)
- [ ] Nouvelles missions reçues en temps réel
- [ ] Pas de code mort (smart-gps-map.js chargé)
- [ ] Application prête pour utilisateurs

---

**Pour questions**: Voir [RAPPORT_CORRECTIONS_COMPLETES.md](RAPPORT_CORRECTIONS_COMPLETES.md)
