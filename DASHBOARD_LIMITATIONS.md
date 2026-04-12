# Dashboard Agent - Limitations Connues

## État Production ✅
Le dashboard agent est **100% fonctionnel** pour les cas d'usage critiques.

## Limitations Identifiées et Statut

### ✅ CORRIGÉES
- [x] **handleItineraireOptimal WebView reload** - FIXED: utilise maintenant `refreshMapWithOptimizedRouteUpdate()` sans rechargement
- [x] **Double import IOException** - FIXED: supprimé le doublon
- [x] **Memory leak JSBridgeAgent** - FIXED: instance unique + cleanup propre dans `dispose()`
- [x] **QR code en bas de carte** - FONCTIONNEL: généré dynamiquement avec ZXing

### ⚠️ LIMITATIONS (Fonctionnalité réduite mais pas critique)
- [ ] **Filtre date Dashboard** - DatePicker non fonctionnel (chargerDonnees() ne filtre pas par date)
  - Impact: Les clients utilisent chargerDonnees() qui retourne toutes les missions de l'agent
  - Workaround: Historique avec filtre date fonctionne correctement
  - Effort pour fixer: Modifier AffectationService.getSignalementsByAgent() pour accepter une date

- [ ] **ComboBox filtre statut (Mes Missions)** - Déclaré en FXML mais pas de handler
  - Impact: ComboBox visible mais sans fonctionnalité
  - Workaround: Les données affichées représentent l'état réel (pas de statuts cachés)
  - Effort pour fixer: ~50 lignes de code pour initialiser et filtrer

### 🔍 CODE MORT (Nettoyage possible)
- `SMART_GPS_MAP_JS` - charge un fichier jamais utilisé (carte utilise HTML inline)
- `buildLeafletHtml()` - méthode vide, marquée DEPRECATED
- `indexOfMission()` - utilisée uniquement en failover, inutile pour vraies coordonnées
- `fmt()`, `statusColorHex()`, `showAgentDashboardPage()`, `updateDistanceAndTime()` - méthodes inutilisées

### ✅ FONCTIONNEL ET TESTÉ
- Dashboard: 4 cards stats (Total/En attente/En cours/Terminées)
- Mes Missions: Table 8 colonnes + Actions (Démarrer/Terminé)  
- Tournée: Leaflet WebView + Itinéraire optimisé
- Historique: Filtre par date (fonctionnel contrairement au Dashboard)
- Profil: Modification nom/email, mot de passe
- GPS: QR code dynamique en bas de carte, URL GPS copiable
- Position agent: Suivi en temps réel via RealTimeGPSService

## Recommandations

1. **Production OK**: Déployer tel quel, les limitations ne bloquent pas les workflows critiques
2. **Amélioration future**: Implémenter filtre date dashboard (faible complexité)
3. **Cleanup possible**: Supprimer le code mort (références issues de refactorisation ancienne)

## Notes pour Devs

- QR code génération: `generateAndDisplayMapQRCode()` et `generateAndDisplayMapQRCode()` (deux versions, une pour profil skippée, une pour carte)
- WebSocket GPS: Actif (port 3002), prêt pour push temps réel
- Token persistence: Implémenté dans gps_token table
