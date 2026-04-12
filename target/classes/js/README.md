# JavaScript Resources

## Status

### ✅ ACTIVE FILES (En utilisation)

- **smart-gps-map.js**: 
  - Utilisé par: `AgentDashboardController.java`
  - Fonction: Gestion interactive de la carte Leaflet dans la page "Tournée du jour"
  - Chargement: Préchargé lors de l'initialisation du contrôleur
  - Statut: NÉCESSAIRE & CRITIQUE

- **leaflet.js**: 
  - Utilisé par: Chargement CDN dans HTML dynamique
  - Fonction: Bibiliothèque Leaflet pour cartes OpenStreetMap
  - Statut: NÉCESSAIRE & EXTERNE (via CDN)

- **leaflet.css**:
  - Utilisé par: Chargement CDN dans HTML dynamique
  - Fonction: Styles pour les cartes Leaflet
  - Statut: NÉCESSAIRE & EXTERNE (via CDN)

- **leaflet-heat.js**:
  - Utilisé par: `MapResourceUtils.java`
  - Fonction: Heat maps pour visualiser les zones de concentration de signalements
  - Chargement: CDN ou local fallback
  - Statut: NÉCESSAIRE & ACTIF

### ❌ DEAD CODE (À nettoyer)

- **citizen-geolocation.js**:
  - Status: INUTILISÉ - Aucune référence dans le code
  - Raison: Code legacy conservé pour l'historique
  - Action: SUPPRIMABLE - Peut être supprimé en toute sécurité
  - Date de marquage: 2026-04-11
  - Motif de suppression: Remplacé par `smart-gps-map.js` (meilleure architecture)

## Architecture Note

La migration vers une architecture HTML dynamique (buildDynamicMapHtml) rend inutile le chargement classique de fichiers JS externes. 
Tous les JS est maintenant intégré directement dans le HTML généré pour éviter les problèmes de CORS et de chargement.

## Maintenance

- Ne modifiez que `smart-gps-map.js` si nécessaire
- Le reste est soit du CDN, soit du code legacy
- Si vous devez ajouter de la logique JS, préférez l'intégration directe dans `buildDynamicMapHtml()`
