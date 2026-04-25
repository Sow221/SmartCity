# 🔧 CHECKLIST CORRECTIONS RAPIDES - 1-2H

**Objectif:** Passer de 82/100 à 95/100 avant présentation.

---

## PRIORITÉ 1: ENCODAGE UTF-8 (15 min)

### Problème
```
[ERROR] AgentDashboardController.java:[986,19] unmappable character (0xE9)
```

### Fix
**Option A: Ajouter au pom.xml**
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
</properties>
```

**Option B: Ligne 986 - Remplacer l'accent direct par escape**
```java
// AVANT:
showAgentMessage("📍 Cliquez sur la carte pour définer votre position exacte", true);

// APRÈS:
showAgentMessage("📍 Cliquez sur la carte pour définir votre position exacte", true);
```

### Test
```bash
cd SmartCity
mvnw.cmd clean compile
# Vérifier: [INFO] BUILD SUCCESS (sans erreur E9)
```

---

## PRIORITÉ 2: NORMALISER STATUTS BD (30 min)

### Problème
```sql
Enum trouvé: 'Affecte', 'Termine'
Attendu:     'Affecté', 'Terminé'
```

### Fix - Script SQL

**AVANT:** Sauvegarde
```bash
mysqldump -u root -p db_smartcity > backup_statuts.sql
```

**Script de normalisation**
```sql
-- 1. Ajouter les nouveaux enums (MySQL):
ALTER TABLE Signalement MODIFY statut ENUM('En attente', 'Affecté', 'En cours', 'Terminé') DEFAULT 'En attente';

-- 2. Convertir les données existantes:
UPDATE Signalement SET statut = 'Affecté' WHERE statut = 'Affecte';
UPDATE Signalement SET statut = 'Terminé' WHERE statut = 'Termine';

-- 3. Vérifier:
SELECT DISTINCT statut FROM Signalement;
-- Résultat attendu: 'En attente', 'Affecté', 'En cours', 'Terminé'
```

### Vérifier en Java
Chercher + remplacer:
```java
// Chercher tous les cas de "Affecte" (sans accent)
// Dans tous les .java files
```

Command PowerShell:
```powershell
Get-ChildItem -Path src -Recurse -Filter "*.java" | Select-String "Affecte" | Select-Object Filename, LineNumber
Get-ChildItem -Path src -Recurse -Filter "*.java" | Select-String "Termine" | Select-Object Filename, LineNumber
```

---

## PRIORITÉ 3: QR CODE DUPLIQUÉ (15 min)

### Problème
Deux méthodes `generateAndDisplayMapQRCode()` dans CitizenDashboardController

### Fix
```java
// 1. Ouvrir CitizenDashboardController.java
// 2. Chercher: "private void generateAndDisplayMapQRCode()"
// 3. Trouver la 2e occurrence (~ligne 2550)
// 4. Vérifier laquelle est appelée:

// Chercher: generateAndDisplayMapQRCode()
// Garder celle qui est appelée, supprimer l'autre

// Command:
grep -n "generateAndDisplayMapQRCode()" src/main/java/com/smartcity/controller/CitizenDashboardController.java
```

### After Cleanup
Recompile pour confirmer: aucune ambigüité

---

## PRIORITÉ 4: SUPPRIMER FICHIERS BACKUP (5 min)

```bash
# Fichiers à supprimer:
del CitizenDashboardController_orig.java
del original_agent.java

# Fichier à supprimer (redondant):
del src\main\java\com\smartcity\service\GeoConfig.java

# Vérifier:
git status  # Devrait montrer les fichiers deleted
```

---

## VÉRIFICATION FINALE (30 min)

### 1. Recompile
```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
cd SmartCity
.\mvnw.cmd clean compile

# Vérifier: [INFO] BUILD SUCCESS
```

### 2. Test Manuels Rapides

**Citoyen:**
```
1. Login citoyen
2. Créer signalement (zone/catégorie/position)
3. Vérifier BD: SELECT * FROM Signalement WHERE idUser = X
4. ✅ OK si données présentes
```

**Agent:**
```
1. Login agent
2. Voir missions
3. Démarrer une mission
4. Vérifier BD: SELECT * FROM Signalement WHERE statut = 'En cours'
5. ✅ OK si mission dans "En cours"
```

**Admin:**
```
1. Login admin
2. Voir stats
3. Vérifier: Stats = vrais nombres (pas 0)
4. ✅ OK si charts remplis
```

### 3. Vérifier pas de caractères cassés
```
Login: ✅ OK (pas d'erreur UTF-8)
Dashboard: ✅ OK (accents français affichés)
Carte: ✅ OK (labels français visibles)
Notifications: ✅ OK (messages français clairs)
```

### 4. Git Commit
```bash
git add -A
git commit -m "🔧 Pre-delivery audit fixes: UTF-8, statuts, duplicate QR, cleanup"
git log --oneline | head -1
# 3a7f9e2 🔧 Pre-delivery audit fixes...
```

---

## CHECKLIST FINAL ✅

```
COMPILATION
- [ ] mvnw clean compile = SUCCESS
- [ ] Aucun [ERROR] 
- [ ] Aucun unmappable character
- [ ] 0 warnings UTF-8

DONNÉES
- [ ] BD: SELECT COUNT(*) FROM Signalement > 0
- [ ] Statuts tous normalisés: 'Affecté' (pas 'Affecte')
- [ ] Pas de doublon QR code method
- [ ] Pas de `*_orig.java` files

FONCTIONNALITÉ
- [ ] Login citoyen: ✅
- [ ] Créer signalement: ✅
- [ ] Login agent: ✅ 
- [ ] Voir missions: ✅
- [ ] Démarrer mission: ✅
- [ ] Login admin: ✅
- [ ] Voir stats: ✅

INTERFACE
- [ ] Pas d'erreur visuelle
- [ ] Textes français correct (avec accents)
- [ ] Aucun "Lorem ipsum"
- [ ] Cartes chargent
- [ ] QR code génère

PRODUCTION-READY
- [ ] Repo clean (pas de backup files)
- [ ] Commit messages clairs
- [ ] Documentation updated (ce fichier)
- [ ] Tests pasent (si existants)

STATUS: 🟢 READY FOR PRESENTATION
```

---

**Temps Total Estimé:** 1.5 - 2 heures  
**Impact:** 82/100 → 95/100  
**Effort:** Minimal, haute impact

