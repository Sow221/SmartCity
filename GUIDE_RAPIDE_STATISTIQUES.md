# 🚀 GUIDE RAPIDE - STATISTIQUES SmartCity

## 📍 LOCALISATION DES PAGES

### Page 1: Statistiques (Tableau de Bord Admin)
```
App → Menu Latéral → ANALYSE → [Statistiques] ← Cliquez ici
         ↓
   ScrollPane pageStatistiques (admin_dashboard.fxml, ligne 344)
         ↓
   AdminDashboardController.handleShowStatistiques()
```

**URL Visuelle**:
```
┌─────────────────────────────────────────────────────┐
│ SmartCity / Espace Administrateur      [Mode Sombre]│
├──────────────┬──────────────────────────────────────┤
│ Navigation   │  Analyse temps réel                  │
│ [Dashboard]  │  Supervision terrain : carte live... │
│ [Signaleme.] │                                      │
│              │  ┌──────┬──────┬──────────┐           │
│ ANALYSE      │  │Charge│Retard│Taux %   │           │
│ [✓Stat.▼]    │  │  45  │  12  │  72%    │           │
│ [Rapports]   │  └──────┴──────┴──────────┘           │
│              │                                      │
│              │  Alertes terrain (max 5)             │
│              │  ┌─────────────────────────────────┐ │
│              │  │Niveau│Alerte  │Détail│Action   │ │
│              │  ├─────┼────────┼──────┼─────────┤ │
│              │  │🔴CRI│Manque..│...  │[x]      │ │
│              │  └─────────────────────────────────┘ │
│              │                                      │
│              │  Carte live des agents [WebView]    │
│              │  ┌─────────────────────────────────┐ │
│              │  │                                 │ │
│              │  │     🗺️  Pikine, Guédiawaye     │ │
│              │  │                                 │ │
│              │  └─────────────────────────────────┘ │
└──────────────┴──────────────────────────────────────┘
```

### Page 2: Rapports et Analytics
```
App → Menu Latéral → ANALYSE → [Rapports]  ← Ou depuis Admin Dashboard
         ↓
   BorderPane rootPane (reports_dashboard.fxml)
         ↓
   ReportsController.generateReport()
```

**Onglets**:
- Tab 1: Graphiques & Tableaux (Pie, Bar, Line charts)
- Tab 2: Rapport Détaillé (TextArea)

---

## 🔍 CE QUI MARCHE BIEN ✅

### 1. Graphiques Interactifs
- PieChart (répartition statuts)
- BarChart (signalements/zone)
- LineChart (évolution temporelle)
→ **Tous avec légende, labels, couleurs**

### 2. Filtrage Intelligent
```
Changez une valeur:
  Période (date start/end)  → Rapport régénéré auto
  Zone (Pikine/Guédiawaye)  → Rapport régénéré auto
  Criticité (Urgent/Crit)   → Table priorités mise à jour
  Type rapport (5 options)  → Graphiques changent
```

### 3. Exports Professionnels
```
[Exporter] → FileChooser → Choisir format:
  ├─ CSV   (téléchargement direct)
  ├─ XLSX  (Excel formaté avec couleurs)
  ├─ PDF   (mise en page professionnelle)
  └─ TXT   (rapport textuel avec barres ASCII)
```

### 4. KPIs en Temps Réel (Admin Dashboard)
```
Actualisation auto toutes les 30 secondes:
  ├─ Charge opérationnelle (tournées actives)
  ├─ Retards critiques (>72h)
  └─ Taux traitement (%)
```

### 5. Table Top 10 Priorités
```
ID | Zone | Age(h) | Statut | Agent | Priorité
───────────────────────────────────────────────
245  Pikine  >72   En attente  Non assigné  [CRITIQUE]
244  Guéd.   48    En cours    Amadou      [URGENT]
...
```

---

## 🐛 CE QUI NE MARCHE PAS ❌

### 1. **Alertes Invisibles (Admin Dashboard)**
```
Problème: Table "Alertes terrain" reste vide même s'il y a des alertes
Cause:    ObservableList activeAlerts non initialisée en FXML
Symptôme: Tableau affiche "Aucune alerte prioritaire active" même si bugs
```

### 2. **Carte Live Peut Échouer**
```
Problème: Affichage blanc ou message "Carte indisponible"
Cause:    WebView → HTML généré dynamiquement sans vérif
Symptôme: Aucune indication sur quoi a échoué (API? HTML invalide?)
```

### 3. **Génération Rapport Lente**
```
Problème: "Chargement..." pendant 5-10 secondes
Cause:    Dashboard Temps Réel = 5+ requêtes SQL + calculs
Symptôme: Utilisateur clique plusieurs fois, requêtes s'empilent
```

### 4. **Pas de Pagination**
```
Problème: Table "Top 10 Priorités" affiche MAX 10 lignes, pas plus
Cause:    .limit(10) dur-codé, pas de "Afficher plus"
Symptôme: Signalements #11-20 invisibles
```

### 5. **Feedback Export Absent**
```
Problème: Clic [Exporter PDF] → rien visible si erreur
Cause:    Exception loggée mais pas de Toast/Alert utilisateur
Symptôme: "Ça marche? Je sais pas..."
```

### 6. **Timer Auto-Refresh Jamais Arrêté**
```
Problème: Page Statistiques → Naviguez ailleurs → Timer continue
Cause:    stopLiveStatistiquesAutoRefresh() pas appelée en exit
Symptôme: Fuite mémoire, CPU utilisé pour rien
```

### 7. **Pas de "Dernière Actualisation"**
```
Problème: Données anciennes ou fraîches? Pas d'indication
Cause:    Label lastRefreshLabel jamais rempli
Symptôme: Utilisateur peut baser décisions sur données obsolètes
```

---

## 💡 CE QUI DEVRAIT Y AVOIR 🟡

### 1. Dashboard Admin - À Élargir
```
Actuellement:        À Ajouter:
├─ 3 KPIs            ├─ En Attente (détails + âge max)
├─ 5 Alertes         ├─ Tendances (graphique 7j)
└─ 1 Carte live      ├─ Top agents (perfs)
                     ├─ Santé système (OK/WARNING/CRITICAL)
                     ├─ Timestamp "Actualisation"
                     └─ Bouton "Forcer refresh" (ne pas attendre 30s)
```

### 2. Rapports - Nouvelles Analyses
```
Types existants (5):              Types manquants:
├─ Temps Réel                     ├─ Comparatif Zones (Pikine vs Guéd)
├─ Hebdo                          ├─ Efficacité Agents
├─ Mensuel                        ├─ Prévisions (ETA fin)
├─ Performance Agents             ├─ Incidents Récurrents
└─ Analyse Géographique           └─ Distribution Catégories (heatmap)
```

### 3. Améliorer Alertes
```
Manquant:
├─ Couleurs par sévérité (CRITICAL=🔴, WARNING=🟠, INFO=🔵)
├─ Icons visuelles (⚠️, 🔴, etc.)
├─ Bouton "Acknowledge" direct
└─ Historique (alertes masquées depuis 24h)
```

### 4. Exports Avancés
```
Existants:                Manquants:
├─ CSV                   ├─ PNG (screenshot graphique)
├─ XLSX                  ├─ Email automatisé (scheduler)
├─ PDF                   ├─ Lien public (partage rapport)
└─ TXT                   └─ API JSON (pour BI tools)
```

### 5. Métriques Métier
```
Manquant:
├─ SLA Score (% traité <48h)
├─ Zone critique (laquelle traîne?)
├─ Catégorie critique (quel type?)
├─ ETA fin backlog
├─ Charge par agent (moyenne)
└─ Coût par signalement
```

### 6. Géolocalisation Avancée
```
Manquant dans carte live:
├─ Polygones zones géographiques
├─ Clusters signalements (points rouges)
├─ Filtre par zone (cliquer zone = zoom)
├─ Trail historique agent
└─ Distance agent-signalement
```

---

## 📋 TABLEAU DE VUE GLOBALE

```
┌─────────────────────────────┬───────┬──────────┬──────────────┐
│ Fonctionnalité              │ Existe│ Fonctionne│ Évaluation   │
├─────────────────────────────┼───────┼──────────┼──────────────┤
│ Graphiques (Pie/Bar/Line)   │ ✅   │ ✅ Bon   │ ⭐⭐⭐⭐⭐  │
│ Tableaux (priorités, métriq)│ ✅   │ ✅ OK    │ ⭐⭐⭐⭐    │
│ Filtrage (période/zone/crit)│ ✅   │ ✅ Bon   │ ⭐⭐⭐⭐⭐  │
│ Exports (CSV/XLSX/PDF/TXT) │ ✅   │ ✅ OK    │ ⭐⭐⭐⭐    │
│ KPIs temps réel            │ ✅   │ ⚠️ Parfois vides│ ⭐⭐⭐    │
│ Alertes                     │ ✅   │ ❌ Invisibles│ ⭐⭐      │
│ Carte live agents           │ ✅   │ ⚠️ Peut échouer│ ⭐⭐⭐    │
│ Pagination                  │ ❌   │ N/A      │ ⭐          │
│ Tri colonnes tableau        │ ❌   │ N/A      │ ⭐          │
│ API JSON                    │ ❌   │ N/A      │ ⭐          │
│ Documentation utilisateur   │ ❌   │ N/A      │ ⭐          │
└─────────────────────────────┴───────┴──────────┴──────────────┘

Couverture: ~75% des besoins ✅
```

---

## 🎬 COMMENT UTILISER (Guide Rapide)

### 1️⃣ Accéder à la Page Statistiques
```
1. Cliquez sur [Statistiques] menu latéral
2. Attendez chargement initial (< 2 sec)
3. Vous voyez 3 KPIs en haut
4. Table alertes (max 5)
5. Carte live agents (actualise auto 30s)
```

### 2️⃣ Générer un Rapport
```
1. Cliquez sur [Rapports] menu latéral (ou lien depuis stats)
2. Choisissez:
   - Période (dates)
   - Zone (Pikine/Guéd)
   - Criticité (Urgent>24h, Critique>72h)
   - Type rapport (5 options)
3. Cliquez [Générer]
4. Attendez chargement
5. Regardez graphiques + Top 10 priorités
```

### 3️⃣ Exporter un Rapport
```
1. Générez rapport (voir étape 2)
2. Cliquez [Exporter]
3. Choisissez format (CSV/XLSX/PDF/TXT)
4. Choisissez destination sauvegarde
5. Cliquez [Enregistrer]
6. Téléchargement commence
```

### 4️⃣ Voir Détails Complets
```
1. Générez rapport
2. Allez onglet [Rapport détaillé]
3. Lisez texte formaté complet
4. Cliquez [Copier] ou [Exporter TXT]
```

---

## 📞 PROBLÈMES & SOLUTIONS RAPIDES

| Problème | Solution |
|----------|----------|
| **Rapport très lent** | C'est normal si beaucoup données. Attendez 10s max. |
| **Carte blanche** | Essayez [Actualiser]. Sinon, contact admin réseau. |
| **Table alertes vide** | C'est bug connu. Actualisez page (F5). |
| **Export échoue silencieusement** | Vérifiez droits d'écriture dossier. Relancez. |
| **Seulement 10 priorités affichées** | C'est limite, pas plus visible. Demandez pagination. |
| **Zone n'est pas appliquée** | Sélectionnez zone dans ComboBox, puis [Générer]. |

---

## 🔧 POUR LE DÉVELOPPEUR

### Fichiers Clés
```
ReportsController.java
├─ generateReport() → generateRealTimeDashboard()
├─ updateTopPriorities() → table Top 10
└─ exportReport() → CSV/XLSX/PDF/TXT

AdminDashboardController.java
├─ handleShowStatistiques() → affiche page
├─ refreshAnalysisCompactMetrics() → met à jour KPIs
├─ refreshLiveTracking() → met à jour carte + KPIs
└─ startLiveStatistiquesAutoRefresh() → timer 30s

Services:
├─ ReportService.java → générère rapports
├─ AnalyticsRulesService.java → calcule KPIs/alertes
└─ SignalementService.java → données

FXML:
├─ admin_dashboard.fxml (ligne 344) → pageStatistiques
└─ reports_dashboard.fxml → page rapports
```

### Bugs à Fixer (Code)
```java
// 1. AdminDashboardController.refreshAnalysisCompactMetrics()
// AVANT: if (tableAlerts != null)  activeAlerts.setAll(rows);
// APRÈS: if (tableAlerts != null && activeAlerts != null)  activeAlerts.setAll(rows);

// 2. ReportsController.generateRealTimeDashboard()
// AVANT: final AnalyticsRulesService.AnalysisSnapshot snapshot = ...
// APRÈS: if (snapshot == null) { snapshot = new AnalysisSnapshot(); }

// 3. AdminDashboardController.stopLiveStatistiquesAutoRefresh()
// À APPELER DANS: showPage() quand pageStatistiques != pageToShow

// 4. ReportsController.exportReport()
// AVANT: } catch (IOException e) { logger.error(...); }
// APRÈS: } catch (IOException e) { showAlert("Erreur", "Export échoué: " + e.getMessage()); }
```

---

**Fin du guide rapide**
