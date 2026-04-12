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

| Couche | Technologie |
|---|---|
| Langage | Java 17 |
| Interface | JavaFX 17 (FXML + CSS) |
| Carte | Leaflet + OpenStreetMap via WebView |
| Base de données | MySQL 8 |
| Build | Maven |
| Sécurité | BCrypt (hachage mots de passe) |

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
