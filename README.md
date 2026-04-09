# SmartCity - Guide Equipe

Application JavaFX de gestion et suivi des dechets menagers pour Pikine et Guediawaye (Senegal).

Ce README sert de document de suivi technique pour toute l'equipe.

## 1. Objectif du projet

SmartCity gere les signalements de dechets via 3 profils:
- `Administrateur`: pilotage global, gestion utilisateurs/agents/signalements, statistiques.
- `Agent`: missions de collecte, mise a jour des statuts, carte des zones, historique.
- `Citoyen`: creation de signalements, suivi personnel, gestion du profil.

## 2. Technologies et frameworks utilises

- `Java 17`
- `JavaFX 17`:
  - `javafx-controls` (UI)
  - `javafx-fxml` (vues FXML + controleurs)
  - `javafx-web` (WebView pour carte interactive agent)
- `Maven` (build/dependances)
- `MySQL 8` + `mysql-connector-j` (persistance)
- `Leaflet + OpenStreetMap` (carte dans l'espace agent via WebView)

Note securite actuelle:
- Les mots de passe sont hashes avec `BCrypt`.
- Le fichier racine `config.properties` contient des secrets locaux et ne doit jamais etre committe.

## 3. Architecture code

Principaux dossiers:
- `src/main/java/com/smartcity/app` -> demarrage application
- `src/main/java/com/smartcity/controller` -> controleurs JavaFX
- `src/main/java/com/smartcity/service` -> acces DB / logique metier
- `src/main/java/com/smartcity/model` -> modeles
- `src/main/java/com/smartcity/utils` -> session + connexion DB
- `src/main/resources/fxml` -> vues FXML
- `src/main/resources/css` -> styles CSS
- `src/main/resources/sql` -> script SQL initial

## 4. Dashboards actuellement implementes

### Administrateur
- Header + sidebar + contenu dynamique
- Pages:
  - Dashboard global (cards + graphiques)
  - Gestion utilisateurs (ajout/modif/suppression)
  - Gestion agents (ajout/modif/suppression)
  - Gestion signalements (filtres)
  - Statistiques

### Agent
- Header bleu + sidebar agent + contenu dynamique
- Pages:
  - Missions du jour (cards + urgences)
  - Mes missions (actions demarrer/terminer)
  - Carte des zones (Leaflet via WebView + itineraire)
  - Historique filtre par date
  - Mon profil

### Citoyen
- Header + sidebar + contenu dynamique
- Pages:
  - Vue generale personnelle
  - Ajouter signalement
  - Mes signalements
  - Mon profil

## 5. Regles metier appliquees

- Controle d'acces par role (`Administrateur`, `Agent`, `Citoyen`)
- Agent: voit uniquement les missions de sa zone
- Citoyen: voit uniquement ses signalements
- Confirmation avant actions sensibles (suppression / terminer mission)
- Notifications UI apres action (succes/erreur)

## 6. Base de donnees

Script principal:
- `src/main/resources/sql/gestion_dechets.sql`

Tables principales:
- `Utilisateur`
- `Signalement`
- `Affectation`
- `dechet` (table alternative utilisee par certaines vues/services)

Important:
- Des incoherences historiques existent entre certaines tables/services (`Signalement` vs `dechet`).
- Avant sprint de stabilisation, valider le modele cible unique avec l'equipe.
- Les statuts `Signalement` doivent etre normalises en base sur `En attente`, `Affecte`, `En cours`, `Termine`.

## 7. Configuration locale

### Prerequis
- JDK 17
- MySQL 8+
- Maven (ou wrapper `mvnw`)

### DB
1. Creer/importer la base via `gestion_dechets.sql`
2. Verifier la connexion dans:
   - `src/main/java/com/smartcity/utils/DatabaseConnection.java`
   - URL / user / password MySQL

### Lancer le projet
- Compilation:
```bash
./mvnw -DskipTests compile
```
- Execution JavaFX:
```bash
./mvnw javafx:run
```

## 8. Comptes de test (a maintenir par l'equipe)

A ajuster selon les donnees reelles de votre base locale. Minimum recommande:
- 1 administrateur
- 2 agents (zones differentes)
- 2 citoyens

## 9. Suivi equipe (maitrise projet)

Checklist onboarding (nouveau membre):
1. Comprendre l'architecture `FXML <-> Controller <-> Service <-> DB`.
2. Savoir lancer l'application et compiler sans erreur.
3. Maitriser les 3 parcours fonctionnels (Admin / Agent / Citoyen).
4. Comprendre la logique des statuts mission/signalement.
5. Savoir ajouter une colonne TableView + mapping model/service.
6. Savoir ajouter une page FXML et la brancher dans un dashboard.

Checklist qualite avant merge:
1. Build Maven OK.
2. Aucun changement de role non voulu.
3. Regression verifiee sur login + dashboards.
4. Messages utilisateur visibles sur actions critiques.
5. Cohesion SQL/Service verifiee.

## 10. Prochaines priorites recommandees

1. Unifier totalement la couche signalement (`Signalement` vs `dechet`).
2. Externaliser la configuration DB (fichier env/properties).
3. Ajouter des tests de non-regression sur services critiques.
4. Normaliser les encodages UTF-8 sur tous les fichiers (accents).

---
Derniere mise a jour technique: `2026-02-28`
