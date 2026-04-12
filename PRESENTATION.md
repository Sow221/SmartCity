# SmartCity — Présentation du projet

---

## Qu'est-ce que SmartCity ?

SmartCity est une application desktop développée en Java/JavaFX pour gérer et suivre les signalements de déchets ménagers dans les communes de **Pikine** et **Guédiawaye** au Sénégal.

L'objectif est de connecter trois acteurs : les citoyens qui signalent les problèmes, les agents qui interviennent sur le terrain, et les administrateurs qui pilotent l'ensemble.

---

## Le problème résolu

Dans ces communes, la gestion des déchets souffre d'un manque de coordination : les signalements sont informels, les agents n'ont pas de visibilité sur les zones prioritaires, et les responsables n'ont aucun tableau de bord pour suivre l'activité. SmartCity centralise tout ça dans une seule application.

---

## Les trois profils utilisateurs

**Administrateur**
Pilote l'ensemble du système. Il gère les comptes utilisateurs et agents, consulte tous les signalements avec des filtres, et accède aux statistiques globales par zone, statut et catégorie.

**Agent**
Reçoit ses missions de collecte au quotidien. Il peut démarrer et terminer une mission, consulter la carte interactive de sa zone (Leaflet/OpenStreetMap), et accéder à son historique filtré par date.

**Citoyen**
Crée des signalements de déchets avec localisation GPS. Il suit l'état de ses signalements (En attente → Affecté → En cours → Terminé) et gère son profil personnel.

---

## Fonctionnalités clés

- Authentification sécurisée avec mots de passe hashés (BCrypt) et contrôle d'accès par rôle
- Carte interactive des zones d'intervention via Leaflet et OpenStreetMap (intégrée dans une WebView JavaFX)
- Suivi en temps réel des statuts : En attente, Affecté, En cours, Terminé
- Tableau de bord administrateur avec graphiques et statistiques
- Notifications UI après chaque action (succès / erreur)
- Confirmation obligatoire avant toute action sensible (suppression, fin de mission)

---

## Stack technique

### Langage & Runtime
- **Java 17** — langage principal de toute l'application (logique métier, services, contrôleurs, utilitaires)

### Interface utilisateur
- **JavaFX 17.0.2**
  - `javafx-controls` — tous les composants visuels : Button, TableView, ComboBox, Label, TextField, PasswordField, BarChart, PieChart, etc.
  - `javafx-fxml` — chaque écran est décrit dans un fichier `.fxml` (login, register, dashboards admin/agent/citoyen) et lié à son contrôleur Java
  - `javafx-web` — WebView utilisée dans le dashboard agent pour afficher la carte Leaflet interactive directement dans la fenêtre JavaFX
  - `javafx-swing` — utilisé pour l'interopérabilité lors de la génération d'images (QR codes via ZXing)
- **CSS JavaFX** — fichier `design-system.css` unique appliqué à toute l'application : palette de couleurs, dark mode, animations hover, styles des tableaux, cartes, formulaires, toasts

### Cartographie
- **Leaflet.js** — carte interactive chargée dans la WebView de l'espace agent : affichage des zones, marqueurs de signalements, itinéraire
- **OpenStreetMap** — fond de carte utilisé par Leaflet (tuiles gratuites)
- **leaflet-heat.js** — plugin heatmap pour visualiser les zones à forte densité de signalements sur la carte agent
- **smart-gps-map.js** — script maison qui orchestre la carte, les marqueurs et la communication avec le backend Java via `WebEngine.executeScript()`

### Base de données
- **MySQL 8** — stockage de toutes les données : utilisateurs, signalements, affectations, zones, positions GPS, tokens
- **mysql-connector-j 8.2.0** — driver JDBC pour toutes les requêtes SQL (`PreparedStatement`, `ResultSet`)
- **HikariCP 5.0.1** — pool de connexions utilisé dans `DatabaseConnection.java` pour éviter d'ouvrir une nouvelle connexion à chaque requête

### Sécurité
- **jBCrypt 0.4** — utilisé dans `UtilisateurService` pour hasher les mots de passe à l'inscription et les vérifier à la connexion
- **TLS / SSL auto-signé** — le serveur GPS embarqué (`GpsApiServer`) génère un certificat auto-signé à la volée (RSA 2048, SHA256) pour exposer une URL HTTPS sur le réseau local, nécessaire pour que le navigateur mobile autorise l'accès au GPS
- **Tokens UUID** — chaque agent et citoyen reçoit un token unique (UUID) stocké en base (`gps_token`) pour sécuriser les appels à l'API GPS

### Serveur HTTP embarqué
- **com.sun.net.httpserver (JDK)** — serveur HTTP/HTTPS léger intégré au JDK, utilisé dans `GpsApiServer` pour exposer les endpoints `/api/position`, `/api/citizen-position`, `/gps`, `/citizen-gps` sans dépendance externe
- Sert aussi la **page HTML mobile** générée dynamiquement (buildGpsHtml) que l'agent ou le citoyen ouvre sur son téléphone pour envoyer sa position GPS

### Communication temps réel
- **Jakarta WebSocket API 2.1.0** — API standard pour définir les endpoints WebSocket (`@ServerEndpoint`)
- **Tyrus 2.1.3** — implémentation du serveur WebSocket embarqué, démarré dans `WebSocketServer.java` sur le port configuré via `GeoConfig`
- **Grizzly** — conteneur réseau sous-jacent de Tyrus, gère les connexions entrantes
- Utilisé pour le **push temps réel** des positions agents vers le dashboard (`AgentMissionEndpoint`)
- **`java.util.Timer`** — utilisé dans `RealTimeGPSService` pour interroger la base toutes les 10 secondes et notifier les listeners JavaFX via `Platform.runLater()`

### Sérialisation / Échanges de données
- **Gson 2.10.1** — sérialisation/désérialisation JSON dans `JsonUtils.java`, utilisé pour les échanges entre la WebView JavaScript et le code Java, et pour les réponses de l'API GPS

### Génération de documents
- **Apache PDFBox 2.0.27** — génération de rapports PDF exportables depuis le dashboard administrateur (`ReportService`)
- **ZXing 3.5.2** — génération de QR codes dans `QrCodeUtils.java` : l'URL GPS de l'agent/citoyen est encodée en QR code affiché dans l'interface pour être scanné depuis un mobile

### Logging
- **SLF4J 2.0.9** — façade de logging utilisée dans tous les services et contrôleurs (`LoggerFactory.getLogger`)
- **Logback 1.4.11** — implémentation configurée dans `logback.xml` : logs en console + rotation de fichiers dans le dossier `logs/`

### Build & Gestion de dépendances
- **Maven** — gestion de toutes les dépendances, compilation, packaging
- **Maven Wrapper (`mvnw`)** — permet de lancer le build sans installer Maven globalement
- **javafx-maven-plugin 0.0.8** — lance l'application JavaFX avec `mvn javafx:run` en configurant automatiquement le module-path
- **maven-compiler-plugin 3.11.0** — compilation Java 17 avec encodage UTF-8
- **maven-surefire-plugin 3.2.2** — exécution des tests JUnit

### Tests
- **JUnit Jupiter 5.10.0** — framework de tests unitaires (scope test)
- **Mockito 5.5.0** — mocking des dépendances pour tester les services sans base de données réelle

### Conteneurisation & CI
- **Docker / Docker Compose** — `Dockerfile` et `docker-compose.yml` présents pour conteneuriser l'application et la base MySQL
- **GitHub Actions** — pipeline CI défini dans `.github/workflows/ci.yml` pour compiler et vérifier le build automatiquement

---

## Architecture

L'application suit une architecture en couches classique :

```
FXML (vue)  →  Controller (logique UI)  →  Service (logique métier)  →  DB (MySQL)
```

Chaque profil a son propre dashboard FXML avec header, sidebar et zone de contenu dynamique. Les pages sont chargées à la demande dans la zone centrale sans rechargement de la fenêtre.

---

## Base de données

Quatre tables principales :

- **Utilisateur** — tous les comptes (admin, agent, citoyen)
- **Signalement** — les déclarations de déchets avec coordonnées GPS, catégorie, statut
- **Affectation** — le lien entre un signalement et l'agent chargé de le traiter
- **dechet** — table complémentaire utilisée par certaines vues

Les statuts sont normalisés : `En attente` → `Affecté` → `En cours` → `Terminé`

---

## Ce qui a été livré

- Login + inscription avec validation et gestion des erreurs
- Dashboard Administrateur complet (utilisateurs, agents, signalements, statistiques)
- Dashboard Agent complet (missions du jour, carte, historique, profil)
- Dashboard Citoyen complet (signalements, ajout avec GPS, profil)
- Design system CSS unifié sur toute l'application
- Carte GPS interactive avec itinéraire pour les agents
- Logs applicatifs (Logback)

---

## Périmètre géographique

Pikine et Guédiawaye — deux communes de la banlieue de Dakar, Sénégal, avec une forte densité de population et des enjeux importants en matière de gestion des déchets urbains.

---

*Projet académique — 2026*
