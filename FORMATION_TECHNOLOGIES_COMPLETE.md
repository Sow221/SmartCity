# 📚 GUIDE DE FORMATION TECHNOLOGIES SMART CITY
## Leçon complète pour toute l'équipe

---

## TABLE DES MATIÈRES

1. [Introduction](#introduction)
2. [Architecture Générale du Projet](#architecture-générale)
3. [Technologies Backend](#technologies-backend)
4. [Technologies Frontend](#technologies-frontend)
5. [Base de Données](#base-de-données)
6. [Communication Temps Réel](#communication-temps-réel)
7. [Sécurité](#sécurité)
8. [Outils et Infrastructure](#outils-et-infrastructure)
9. [Flux de Données](#flux-de-données)
10. [Résumé et Schéma Global](#résumé)

---

## 1. INTRODUCTION

### 1.1 Objectif de cette formation

Ce document a pour but de vous familiariser avec toutes les technologies utilisées dans le projet Smart City, même si vous n'avez jamais programmé. Chaque technologie sera expliquée avec :

- **Qu'est-ce que c'est ?** (explication simple)
- **À quoi ça sert ?** (utilité dans le projet)
- **Comment ça marche ?** (mécanisme simplifié)

### 1.2 Vue d'ensemble

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        APPLICATION SMART CITY                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐          │
│  │   CLIENTS    │     │   AGENTS     │     │   ADMIN      │          │
│  │  (Citoyens)  │     │  (Équipes)   │     │  (Dashboard) │          │
│  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘          │
│         │                    │                    │                   │
│         └────────────────────┼────────────────────┘                   │
│                                │                                        │
│                    ┌───────────▼───────────┐                          │
│                    │   APPLICATION JAVA    │                          │
│                    │      (Backend)        │                          │
│                    └───────────┬───────────┘                          │
│                                │                                        │
│                    ┌───────────▼───────────┐                          │
│                    │    BASE DE DONNÉES    │                          │
│                    │      MySQL 8.0        │                          │
│                    └───────────────────────┘                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. ARCHITECTURE GÉNÉRALE DU PROJET

### 2.1 Les 3 parties de l'application

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ARCHITECTURE 3-TIERS                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COUCHE PRÉSENTATION                          │   │
│  │                    (Interface Utilisateur)                      │   │
│  │                                                                 │   │
│  │   • JavaFX (fenêtres, boutons, formulaires)                     │   │
│  │   • Contrôleurs (logique d'affichage)                          │   │
│  │   • Tableaux de bord (dashboards)                              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COUCHE MÉTIER                                │   │
│  │                    (Logique Applicative)                        │   │
│  │                                                                 │   │
│  │   • Services (SignalementService, UtilisateurService...)       │   │
│  │   • Modèles (Signalement, Utilisateur, Zone...)                 │   │
│  │   • WebSocket (communication temps réel)                       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COUCHE DONNÉES                               │   │
│  │                    (Stockage et Accès)                         │   │
│  │                                                                 │   │
│  │   • MySQL (base de données relationnelle)                     │   │
│  │   • HikariCP (gestion des connexions)                         │   │
│  │   • Requêtes SQL                                              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Structure des fichiers du projet

```
SmartCity/
├── src/
│   ├── main/
│   │   ├── java/com/smartcity/
│   │   │   ├── app/           ← Point d'entrée de l'application
│   │   │   ├── config/        ← Configuration globale
│   │   │   ├── controller/    ← Contrôleurs UI (logique d'écran)
│   │   │   ├── model/         ← Modèles de données
│   │   │   ├── service/       ← Logique métier
│   │   │   ├── utils/         ← Utilitaires
│   │   │   └── websocket/     ← Communication temps réel
│   │   └── resources/
│   │       ├── sql/           ← Scripts SQL
│   │       └── images/        ← Images et ressources
│   └── test/                  ← Tests unitaires
├── pom.xml                    ← Configuration Maven
├── docker-compose.yml         ← Configuration Docker
└── README.md                  ← Documentation
```

---

## 3. TECHNOLOGIES BACKEND

### 3.1 Java 17 - Le langage de programmation

#### Qu'est-ce que c'est ?
Java est un langage de programmation créé en 1995 par Sun Microsystems (maintenant Oracle). Il permet d'écrire des programmes qui fonctionnent sur n'importe quel ordinateur.

#### À quoi ça sert dans Smart City ?
- Toute la logique métier de l'application
- Le traitement des signalements
- La gestion des utilisateurs
- Les calculs de zones et d'affectations

#### Exemple simplifié
```java
// Exemple : Création d'un signalement
public class SignalementService {
    
    public Signalement creerSignalement(String description, String localisation) {
        // Créer un nouvel objet Signalement
        Signalement signalement = new Signalement();
        signalement.setDescription(description);
        signalement.setLocalisation(localisation);
        signalement.setStatut(Statut.EN_ATTENTE);
        
        // Sauvegarder dans la base de données
        return signalementRepository.save(signalement);
    }
}
```

#### Pourquoi Java 17 ?
- **Stabilité** : Version LTS (Long Term Support) éprouvée
- **Performance** : Très rapide pour les applications de bureau
- **Écosystème** : Nombreuses bibliothèques disponibles

---

### 3.2 JavaFX 17 - L'interface graphique

#### Qu'est-ce que c'est ?
JavaFX est un framework qui permet de créer des interfaces graphiques modernes avec des fenêtres, des boutons, des formulaires, des tableaux, etc.

#### À quoi ça sert dans Smart City ?
- L'écran de connexion
- Le tableau de bord administrateur
- Le tableau de bord agent
- Le formulaire de signalement pour les citoyens
- L'affichage des cartes et zones

#### Structure d'un écran JavaFX
```
┌────────────────────────────────────────────────────────┐
│                    FENÊTRE PRINCIPALE                  │
├────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────┐ │
│  │                   MENU BAR                        │ │
│  └──────────────────────────────────────────────────┘ │
│  ┌────────────┐ ┌────────────────────────────────────┐│
│  │            │ │                                    ││
│  │  PANNEAU   │ │         CONTENU PRINCIPAL         ││
│  │  LATÉRAL   │ │                                    ││
│  │            │ │   ┌────────┐  ┌────────┐         ││
│  │  - Menu    │ │   │ Bouton │  │ Bouton │         ││
│  │  - Actions │ │   └────────┘  └────────┘         ││
│  │  - Infos   │ │                                    ││
│  │            │ │   ┌────────────────────────────┐  ││
│  │            │ │   │      TABLEAU / LISTE      │  ││
│  │            │ │   └────────────────────────────┘  ││
│  └────────────┘ └────────────────────────────────────┘│
└────────────────────────────────────────────────────────┘
```

#### Exemple de code JavaFX
```java
// Exemple : Créer un bouton
Button bouton = new Button("Créer un signalement");
bouton.setOnAction(event -> {
    // Ce qui se passe quand on clique
    System.out.println("Bouton cliqué !");
});

// Ajouter à la fenêtre
vbox.getChildren().add(bouton);
```

---

### 3.3 Les Services (Couche Métier)

#### Qu'est-ce que c'est ?
Les services sont des classes qui contiennent la logique métier de l'application. Ils font le lien entre les contrôleurs (interface) et les données.

#### Liste des services dans Smart City

| Service | Fonction |
|---------|----------|
| `SignalementService` | Gère les signalements (création, modification, suppression) |
| `UtilisateurService` | Gère les utilisateurs (inscription, connexion, profil) |
| `AffectationService` | Gère les affectations d'agents aux zones |
| `ZoneService` | Gère les zones géographiques |
| `SessionManager` | Gère la session utilisateur connecté |

#### Exemple de flux
```
Utilisateur clique sur "Se connecter"
        │
        ▼
LoginController (interface) appelle
        │
        ▼
UtilisateurService.verifierConnexion(email, motDePasse)
        │
        ▼
Requête SQL vers MySQL
        │
        ▼
MySQL vérifie les identifiants
        │
        ▼
Retourne le résultat (succès ou échec)
        │
        ▼
LoginController affiche le résultat
```

---

## 4. TECHNOLOGIES FRONTEND

### 4.1 Contrôleurs JavaFX

#### Qu'est-ce que c'est ?
Les contrôleurs sont des classes Java qui gèrent ce qui se passe dans chaque écran. Ils répondent aux actions de l'utilisateur (clics, formulaires, etc.).

#### Les contrôleurs du projet

| Contrôleur | Écran associé |
|------------|---------------|
| `LoginController` | Écran de connexion |
| `RegisterController` | Écran d'inscription |
| `AdminDashboardController` | Tableau de bord administrateur |
| `AgentDashboardController` | Tableau de bord agent |
| `CitizenDashboardController` | Tableau de bord citoyen |
| `ReportsController` | Génération des rapports |

#### Exemple de code
```java
public class LoginController {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private void handleConnexion() {
        String email = emailField.getText();
        String motDePasse = passwordField.getText();
        
        // Appeler le service
        Utilisateur utilisateur = utilisateurService.connecter(email, motDePasse);
        
        if (utilisateur != null) {
            // Connexion réussie - ouvrir le bon dashboard
            chargerDashboard(utilisateur.getRole());
        } else {
            // Afficher erreur
            afficherErreur("Email ou mot de passe incorrect");
        }
    }
}
```

---

### 4.2 Modèles de Données

#### Qu'est-ce que c'est ?
Les modèles sont des classes qui représentent les données du système. Ce sont comme des formulaires vides qu'on remplit avec des informations.

#### Les modèles du projet

```java
// filepath: src/main/java/com/smartcity/model/Utilisateur.java
public class Utilisateur {
    private Long id;
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String role;  // ADMIN, AGENT, CITOYEN
    private String telephone;
    private boolean actif;
    // ... getters et setters
}
```

```java
// filepath: src/main/java/com/smartcity/model/Signalement.java
public class Signalement {
    private Long id;
    private String description;
    private String localisation;
    private Double latitude;
    private Double longitude;
    private String photoPath;
    private Statut statut;  // EN_ATTENTE, EN_COURS, TRAITE, REJETE
    private Long idUtilisateur;
    private Long idZone;
    private Date dateCreation;
    // ... getters et setters
}
```

```java
// filepath: src/main/java/com/smartcity/model/Zone.java
public class Zone {
    private Long id;
    private String nom;
    private String description;
    private Double surface;
    private String couleur;
    // ... getters et setters
}
```

---

## 5. BASE DE DONNÉES

### 5.1 MySQL 8.0 - Le système de gestion de base de données

#### Qu'est-ce que c'est ?
MySQL est un système de gestion de base de données relationnelle (SGBDR). Il permet de stocker, organiser et récupérer des données de manière efficace.

#### Analogie simple
> Imaginez un grand classeur avec des tiroirs. Chaque tiroir contient des fiches organisées. MySQL fonctionne comme ce classeur mais en version numérique et beaucoup plus rapide.

#### À quoi ça sert dans Smart City ?
- Stocker les utilisateurs (admins, agents, citoyens)
- Stocker les signalements
- Stocker les zones géographiques
- Stocker les affectations agents/zones

---

### 5.2 Structure des tables

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TABLES DE LA BASE DE DONNÉES                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────┐      ┌─────────────────┐                          │
│  │   UTILISATEURS │      │   SIGNALEMENTS  │                          │
│  ├─────────────────┤      ├─────────────────┤                          │
│  │ id (PK)        │      │ id (PK)         │                          │
│  │ email          │◄─────│ id_utilisateur  │                          │
│  │ mot_de_passe   │      │ description     │                          │
│  │ nom            │      │ localisation    │                          │
│  │ prenom         │      │ latitude        │                          │
│  │ role           │      │ longitude       │                          │
│  │ telephone      │      │ statut          │                          │
│  │ actif          │      │ id_zone (FK)    │                          │
│  └─────────────────┘      │ date_creation  │                          │
│                            └─────────────────┘                          │
│                                    │                                    │
│                                    │ (FK)                               │
│                                    ▼                                    │
│                            ┌─────────────────┐                          │
│  ┌─────────────────┐      │     ZONES       │      ┌─────────────────┐ │
│  │  AFFECTATIONS   │      ├─────────────────┤      │    ENUMÉRATIONS │ │
│  ├─────────────────┤      │ id (PK)         │      ├─────────────────┤ │
│  │ id (PK)        │      │ nom             │      │ Statut          │ │
│  │ id_agent (FK)  │─────►│ description     │      │ Role            │ │
│  │ id_zone (FK)   │      │ surface         │      │ TypeDechet      │ │
│  │ date_debut     │      │ couleur         │      └─────────────────┘ │
│  │ date_fin       │      └─────────────────┘                           │
│  └─────────────────┘                                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 5.3 HikariCP - Gestion des connexions

#### Qu'est-ce que c'est ?
HikariCP est une bibliothèque de pool de connexions. Elle gère efficacement les connexions à la base de données.

#### Pourquoi c'est important ?
Sans pool de connexions :
```
À chaque requête : Ouvrir une connexion → Exécuter → Fermer
```
Avec HikariCP :
```
Connexion 1: Ouverte → Réutilisée pour 100 requêtes → Fermée à la fin
Connexion 2: Ouverte → Réutilisée pour 50 requêtes → Fermée à la fin
```

**Avantages :**
- Much faster (beaucoup plus rapide)
- Utilisation mémoire réduite
- Meilleure gestion des erreurs

#### Configuration dans le projet
```java
// filepath: src/main/java/com/smartcity/utils/DatabaseConnection.java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/db_smartcity");
config.setUsername("root");
config.setPassword("77884455");
config.setMaximumPoolSize(10);  // Maximum 10 connexions simultanées
config.setMinimumIdle(2);      // Minimum 2 connexions actives
config.setConnectionTimeout(30000);  // 30 secondes timeout
```

---

### 5.4 JDBC - Java Database Connectivity

#### Qu'est-ce que c'est ?
JDBC est l'API Java standard pour accéder aux bases de données. C'est le "pont" entre Java et MySQL.

#### Comment ça marche ?
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   CODE JAVA │ ───► │    JDBC     │ ───► │    MySQL    │
│             │      │   (Driver)  │      │             │
└─────────────┘      └─────────────┘      └─────────────┘
```

#### Exemple de requête SQL
```java
// Requête pour récupérer tous les signalements
String sql = "SELECT * FROM signalements WHERE statut = 'EN_ATTENTE'";

try (Connection conn = getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(sql)) {
    
    while (rs.next()) {
        String description = rs.getString("description");
        String localisation = rs.getString("localisation");
        System.out.println(description + " - " + localisation);
    }
}
```

---

## 6. COMMUNICATION TEMPS RÉEL

### 6.1 WebSocket - Communication instantanée

#### Qu'est-ce que c'est ?
WebSocket est une technologie qui permet une communication bidirectionnelle en temps réel entre le client et le serveur. Contrairement au HTTP classique (requête-réponse), WebSocket maintient une connexion ouverte.

#### Analogie
> **HTTP** : Comme appeler quelqu'un à chaque fois qu'on veut parler (raccrocher après chaque message)
> 
> **WebSocket** : Comme une ligne téléphonique ouverte en permanence

#### À quoi ça sert dans Smart City ?
- Notifications en temps réel aux agents
- Mise à jour de la position des agents sur la carte
- Alertes quand un nouveau signalement arrive
- Chat entre agents et administrateurs

---

### 6.2 Tyrus - L'implémentation WebSocket

#### Qu'est-ce que c'est ?
Tyrus est la bibliothèque Java qui implémente le protocole WebSocket dans notre projet.

#### Architecture WebSocket dans Smart City
```
┌─────────────────────────────────────────────────────────────────────────┐
│                     COMMUNICATION WEBSOCKET                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   SERVEUR (Tyrus)                                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │  WebSocketServer (port 8025)                                   │  │
│   │  - Gestion des connexions                                      │  │
│   │  - Envoi des messages                                          │  │
│   │  - Réception des messages                                      │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                ▲                                        │
│                                │ Connexion WebSocket                   │
│                                │                                        │
│   CLIENTS                     │                                        │
│   ┌──────────────┐     ┌──────┴──────┐     ┌──────────────┐          │
│   │ Application  │     │ Application │     │ Application │          │
│   │   Admin      │     │    Agent    │     │   Citoyen   │          │
│   └──────────────┘     └──────────────┘     └──────────────┘          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Exemple de code serveur
```java
// filepath: src/main/java/com/smartcity/websocket/WebSocketServer.java
@WebSocketEndpoint(path = "/ws")
public class WebSocketServer {
    
    private static Set<Session> clients = new ConcurrentHashSet<>();
    
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("Nouveau client connecté: " + session.getId());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // Traiter le message reçu
        System.out.println("Message reçu: " + message);
    }
    
    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
    }
    
    // Envoyer un message à tous les clients connectés
    public static void broadcast(String message) {
        for (Session client : clients) {
            client.getBasicRemote().sendText(message);
        }
    }
}
```

---

## 7. SÉCURITÉ

### 7.1 BCrypt - Hachage des mots de passe

#### Qu'est-ce que c'est ?
BCrypt est un algorithme de hachage de mots de passe. Il transforme un mot de passe en une chaîne de caractères illisible (le "hach").

#### Pourquoi c'est important ?
- **Jamais stocker les mots de passe en clair** : Si quelqu'un pirate la base de données, il ne peut pas voir les mots de passe
- **Salage automatique** : Chaque mot de passe est "salé" avec des données aléatoires pour éviter les attaques par table rainbow

#### Comment ça marche ?
```
Mot de passe: "MonSuperPassword123"
        │
        ▼ (BCrypt)
Hach résultat: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

#### Code utilisé
```java
// Hachage d'un mot de passe
String motDePasseClair = "MonSuperPassword123";
String motDePasseHache = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());

// Vérification d'un mot de passe
boolean estValide = BCrypt.checkpw(motDePasseSaisi, motDePasseHache);
```

---

### 7.2 Gestion des rôles

#### Les rôles dans Smart City

| Rôle | Permissions |
|------|-------------|
| **ADMIN** | Dashboard complet, gestion utilisateurs, gestion zones, rapports |
| **AGENT** | Dashboard agent, voir ses affectations, mettre à jour signalements |
| **CITOYEN** | Créer des signalements, voir ses propres signalements |

#### Vérification des droits
```java
public boolean peutAcceder(String roleRequis) {
    Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
    return utilisateur != null && utilisateur.getRole().equals(roleRequis);
}
```

---

## 8. OUTILS ET INFRASTRUCTURE

### 8.1 Maven - Gestion du projet

#### Qu'est-ce que c'est ?
Maven est un outil qui gère :
- Le téléchargement des dépendances (bibliothèques)
- La compilation du code
- Les tests automatisés
- Le packaging de l'application

#### Fichier pom.xml
Le fichier `pom.xml` (Project Object Model) contient toutes les informations du projet :

```xml
<project>
    <groupId>com.smartcity</groupId>
    <artifactId>smartcity-dechets</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <javafx.version>17.0.2</javafx.version>
    </properties>
    
    <dependencies>
        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        
        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.2.0</version>
        </dependency>
        
        <!-- HikariCP -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.0.1</version>
        </dependency>
        <!-- ... autres dépendances -->
    </dependencies>
</project>
```

#### Commandes Maven常用
```bash
# Compiler le projet
mvn clean compile

# Lancer les tests
mvn test

# Créer le package (fichier JAR)
mvn package

# Lancer l'application
mvn javafx:run
```

---

### 8.2 Docker - Conteneurisation

#### Qu'est-ce que c'est ?
Docker permet d'empaqueter l'application et toutes ses dépendances dans un "conteneur" qui fonctionne partout de la même manière.

#### Analogie
> Si vous expédiez un colis, vous le mettez dans une boîte. Docker fait la même chose pour les applications : il met l'application + Java + MySQL + configuration dans des "boîtes" (conteneurs).

#### Services Docker dans le projet
```yaml
# docker-compose.yml
services:
  db:
    image: mysql:8.0                    # Base de données MySQL
    environment:
      MYSQL_ROOT_PASSWORD: 77884455   # Mot de passe root
      MYSQL_DATABASE: db_smartcity     # Nom de la base
    ports:
      - "3306:3306"                    # Port exposé

  app:
    build: .                           # Construire l'application
    depends_on:
      - db                              # Démarrer après la DB
    environment:
      DB_URL: jdbc:mysql://db:3306/db_smartcity
```

#### Commandes Docker
```bash
# Lancer tous les services
docker-compose up

# Arrêter tous les services
docker-compose down

# Voir les logs
docker-compose logs -f
```

---

### 8.3 Gson - Traitement JSON

#### Qu'est-ce que c'est ?
Gson est une bibliothèque Google qui convertit les objets Java en JSON (et vice versa).

#### À quoi ça sert ?
- Communication avec les clients WebSocket
- Export de données
- Lecture/écriture de fichiers de configuration

#### Exemple
```java
// Objet Java vers JSON
Utilisateur utilisateur = new Utilisateur();
utilisateur.setNom("Dupont");
utilisateur.setEmail("dupont@email.com");

String json = new Gson().toJson(utilisateur);
// Résultat: {"nom":"Dupont","email":"dupont@email.com"}

// JSON vers Objet Java
String jsonRecu = "{\"nom\":\"Martin\",\"email\":\"martin@email.com\"}";
Utilisateur u = new Gson().fromJson(jsonRecu, Utilisateur.class);
```

---

### 8.4 ZXing - Génération de QR Codes

#### Qu'est-ce que c'est ?
ZXing est une bibliothèque qui génère des codes QR.

#### À quoi ça sert dans Smart City ?
- Générer des QR codes pour chaque signalement
- Permettre aux agents de scanner un QR code pour valider un passage

#### Exemple
```java
// Générer un QR code
String contenu = "SIGNALEMENT-12345";
byte[] qrCode = QrCodeUtils.genererQRCode(contenu);
// Sauvegarder l'image
QrCodeUtils.sauvegarderQRCode(qrCode, "qr_signalement_12345.png");
```

---

### 8.5 SLF4J + Logback - Journalisation

#### Qu'est-ce que c'est ?
Ces bibliothèques permettent d'enregistrer des logs (traces) de l'application.

#### Niveaux de log
| Niveau | Utilisation |
|--------|-------------|
| **ERROR** | Erreurs critiques |
| **WARN** | Avertissements |
| **INFO** | Informations générales |
| **DEBUG** | Détails pour le débogage |

#### Exemple
```java
Logger logger = LoggerFactory.getLogger(SignalementService.class);

logger.info("Création d'un nouveau signalement");
logger.debug("Détails du signalement: {}", signalement);
logger.error("Erreur de connexion à la base de données", exception);
```

---

### 8.6 PDFBox - Génération de PDF

#### Qu'est-ce que c'est ?
PDFBox permet de créer des documents PDF depuis Java.

#### À quoi ça sert dans Smart City ?
- Générer des rapports PDF
- Exporter les statistiques

#### Exemple
```java
// Créer un PDF
PDDocument document = new PDDocument();
PDPage page = new PDPage();
document.addPage(page);

PDPageContentStream content = new PDPageContentStream(document, page);
content.setFont(new PDType1Font(StandardFonts.HELVETICA), 12);
content.beginText();
content.showText("Rapport des signalements");
content.endText();
document.save("rapport.pdf");
```

---

## 9. FLUX DE DONNÉES

### 9.1 Flux complet d'un signalement

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    FLUX D'UN SIGNALEMENT                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. CITOYEN CRÉE UN SIGNALEMENT                                       │
│     ┌──────────────┐                                                   │
│     │  Application │ ──► Formulaire: Description, Photo, Localisation │
│     │   Citoyen    │                                                   │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼                                                             │
│  2. CONTRÔLEUR REÇOIT LES DONNÉES                                     │
│     ┌──────────────┐                                                   │
│     │   Citizen    │ ──► Validation des champs                         │
│     │  Dashboard   │                                                   │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼                                                             │
│  3. SERVICE TRAITE LA LOGIQUE                                          │
│     ┌─────────────────┐                                                 │
│     │ Signalement    │ ──► Créer objet Signalement                    │
│     │    Service     │ ──► Déterminer la zone                         │
│     └─────────────────┘                                                 │
│            │                                                             │
│            ▼                                                             │
│  4. REQUÊTE BASE DE DONNÉES                                            │
│     ┌──────────────┐     ┌──────────────┐                              │
│     │ JDBC/Hikari  │ ──► │    MySQL     │                              │
│     │    CP        │     │              │                              │
│     └──────────────┘     └──────────────┘                              │
│            │                                                             │
│            ▼                                                             │
│  5. NOTIFICATION EN TEMPS RÉEL                                         │
│     ┌──────────────┐     ┌──────────────┐                              │
│     │  WebSocket   │ ──► │   Admin &    │                              │
│     │   Server     │     │   Agents     │                              │
│     └──────────────┘     └──────────────┘                              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 9.2 Flux de connexion

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    FLUX DE CONNEXION                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│     ┌──────────────┐                                                   │
│     │  Écran Login │                                                   │
│     │  (JavaFX)    │                                                   │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼ Email + Mot de passe                                       │
│     ┌──────────────┐                                                   │
│     │   Login      │                                                   │
│     │ Controller   │                                                   │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼                                                             │
│     ┌──────────────┐                                                   │
│     │ Utilisateur  │ ──► Vérifier identifiants                         │
│     │  Service     │ ──► Hacher le mot de passe pour comparaison       │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼                                                             │
│     ┌──────────────┐     ┌──────────────┐                              │
│     │    MySQL     │ ◄── │   Requête    │                              │
│     │              │     │     SQL      │                              │
│     └──────────────┘     └──────────────┘                              │
│            │                                                             │
│            ▼ (Retourne l'utilisateur ou null)                           │
│     ┌──────────────┐                                                   │
│     │   Session    │ ──► Stocker l'utilisateur connecté                │
│     │   Manager    │                                                   │
│     └──────────────┘                                                   │
│            │                                                             │
│            ▼                                                             │
│     ┌──────────────┐                                                   │
│     │  Charger le  │ ──► ADMIN → AdminDashboard                        │
│     │  bon écran   │ ──► AGENT  → AgentDashboard                       │
│     │              │ ──► CITOYEN → CitizenDashboard                   │
│     └──────────────┘                                                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 10. RÉSUMÉ ET SCHÉMA GLOBAL

### 10.1 Tableau récapitulatif des technologies

| Catégorie | Technologie | Version | Utilité |
|-----------|-------------|---------|---------|
| **Langage** | Java | 17 | Logique métier |
| **UI** | JavaFX | 17.0.2 | Interface graphique |
| **Base de données** | MySQL | 8.0 | Stockage des données |
| **Connexion DB** | HikariCP | 5.0.1 | Pool de connexions |
| **JSON** | Gson | 2.10.1 | Traitement JSON |
| **WebSocket** | Tyrus | 2.1.3 | Communication temps réel |
| **Sécurité** | BCrypt | 0.4 | Hachage mots de passe |
| **QR Code** | ZXing | 3.5.2 | Génération QR codes |
| **Logs** | Logback | 1.4.11 | Journalisation |
| **PDF** | PDFBox | 2.0.27 | Génération PDF |
| **Tests** | JUnit | 5.10.0 | Tests unitaires |
| **Build** | Maven | 3.x | Gestion du projet |
| **Conteneur** | Docker | - | Déploiement |

### 10.2 Schéma global de l'architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ARCHITECTURE COMPLÈTE SMART CITY                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    CLIENTS LOURDS (JavaFX)                    │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │   │
│  │  │   Citoyen   │  │    Agent    │  │    Admin    │            │   │
│  │  │  Dashboard  │  │  Dashboard  │  │  Dashboard  │            │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    CONTRÔLEURS (Java)                          │   │
│  │  LoginController • RegisterController • AdminDashboard         │   │
│  │  AgentDashboardController • CitizenDashboardController          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    SERVICES (Logique Métier)                   │   │
│  │  SignalementService • UtilisateurService • ZoneService         │   │
│  │  AffectationService • SessionManager                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│                                    ▼                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COUCHE DONNÉES                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │   │
│  │  │  HikariCP   │  │    JDBC     │  │    MySQL    │            │   │
│  │  │ (Pool DB)   │  │  (Driver)   │  │     8.0     │            │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ═══════════════════════════════════════════════════════════════════   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    WEBSOCKET (Temps Réel)                      │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │   │
│  │  │   Tyrus     │  │  WebSocket  │  │  Agent      │            │   │
│  │  │   Server    │◄─┤   Client    │  │  Mission    │            │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ═══════════════════════════════════════════════════════════════════   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    SÉCURITÉ & OUTILS                           │   │
│  │  BCrypt (mots de passe) • Gson (JSON) • ZXing (QR)             │   │
│  │  Logback (logs) • PDFBox (PDF) • JUnit (tests)                 │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ═══════════════════════════════════════════════════════════════════   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    INFRASTRUCTURE                              │   │
│  │  Maven (build) • Docker (conteneurs) • Java 17                │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 10.3 Points clés à retenir

#### Pour un développeur nouveau :

1. **Comprendre la structure** : L'application suit le pattern MVC (Model-View-Controller)
   - Modèles = données
   - Vues = interfaces JavaFX
   - Contrôleurs = logique

2. **Commencer par les services** : Ils contiennent la logique métier principale

3. **Comprendre la base** : MySQL stocke tout, HikariCP gère les connexions

4. **WebSocket pour le temps réel** : Utilisé pour les notifications instantanées

5. **Sécurité avec BCrypt** : Jamais stocker de mots de passe en clair

---

### 10.4 Pour démarrer le développement

```bash
# 1. Cloner le projet
git clone <url-du-projet>

# 2. Installer les dépendances
mvn clean install

# 3. Configurer la base de données (voir docker-compose.yml)
docker-compose up -d

# 4. Lancer l'application
mvn javafx:run
```

---

## 📝 ANNEXE : GLOSSAIRE

| Terme | Définition |
|-------|------------|
| **API** | Application Programming Interface - Ensemble de fonctions pour communiquer avec un système |
| **Backend** | Partie serveur de l'application (logique, données) |
| **Frontend** | Partie cliente de l'application (interface) |
| **Dépendances** | Bibliothèques externes utilisées par le projet |
| **Driver** | Logiciel permettant de communiquer avec une base de données |
| **Framework** | Ensemble d'outils et de conventions pour développer plus rapidement |
| **Pool de connexions** | Technique pour réutiliser les connexions à la base de données |
| **SQL** | Structured Query Language - Langage pour manipuler les bases de données |

---

## ✅ FIN DE LA FORMATION

Ce document vous a présenté l'ensemble des technologies utilisées dans le projet Smart City.

Pour toute question ou clarification, n'hésitez pas à consulter :
- Le fichier `README.md` pour les instructions de démarrage
- Le fichier `GUIDE_TECHNIQUE_DEVELOPEUR.md` pour les détails techniques
- Le fichier `ANALYSE_TECHNIQUE_DETAILLEE.md` pour l'architecture détaillée

---

*Document créé le 23 Avril 2026 pour la formation de l'équipe Smart City*