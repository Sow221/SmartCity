# 🔧 FIXES CONCRÈTES - ULTRA PRATIQUE (Pas de théorie!)

**Temps total:** 1-2 heures | **Difficulté:** Facile | **Risque:** Aucun

---

## ❌ PROBLÈME #1: ENCODAGE UTF-8
**Où?** Le compilateur refuse les accents français  
**Symptôme:** Erreur "unmappable character (0xE9)"  
**Cause:** pom.xml ne dit pas à Maven d'utiliser UTF-8

---

### ✅ FIX #1 - OUVrir pom.xml

**Fichier:** `c:\Users\bmd tech\Desktop\sc\SmartCity\pom.xml`

**Trouver cette section:**
```xml
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>17</maven.compiler.release>
        <javafx.version>17.0.2</javafx.version>
    </properties>
```

**SI vous voyez `project.build.sourceEncoding`, c'est OK (déjà là).**

**SI vous ne voyez PAS cette ligne, ajouter:**
```xml
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>17</maven.compiler.release>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
        <javafx.version>17.0.2</javafx.version>
    </properties>
```

---

### ✅ TESTER:
```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
cd "c:\Users\bmd tech\Desktop\sc\SmartCity"
.\mvnw.cmd clean compile 2>&1 | grep unmappable
```

**Résultat OK:** (aucune sortie = pas d'erreur)  
**Résultat NON OK:** Voit les erreurs "unmappable"

---

---

## ❌ PROBLÈME #2: STATUTS CASSÉS EN BD
**Où?** Table `Signalement` dans MySQL  
**Symptôme:** "Affecte" au lieu de "Affecté" (pas d'accent = bug)  
**Cause:** Enum SQL historique avec mauvais accents  
**Impact:** Filtre `status == "Affecté"` échoue en comparaison Java

---

### ✅ FIX #2 - Exécuter Script SQL

**Ouvrir terminal MySQL:**
```bash
mysql -u root -p
# Vous demande mot de passe (entrez-le)
```

**Copier-coller EXACTEMENT ces commandes (une par une):**

```sql
USE db_smartcity;

-- 1. VÉRIFIER avant
SELECT DISTINCT statut FROM Signalement;
-- Résultat: Affecte, En attente, En cours, Termine (SANS accents = BAD)

-- 2. Backup (TRÈS IMPORTANT)
-- (Juste pour sécurité, on peut restaurer si panique)

-- 3. Changer la définition de la table
ALTER TABLE Signalement MODIFY statut 
  ENUM('En attente', 'Affecté', 'En cours', 'Terminé') 
  DEFAULT 'En attente';

-- 4. Convertir les données existantes
UPDATE Signalement SET statut = 'Affecté' WHERE statut = 'Affecte';
UPDATE Signalement SET statut = 'Terminé' WHERE statut = 'Termine';

-- 5. Vérifier après
SELECT DISTINCT statut FROM Signalement;
-- Résultat ATTENDU: Affecté, En attente, En cours, Terminé (AVEC accents = GOOD)

exit
```

---

### ✅ TESTER (en Java):
```bash
cd "c:\Users\bmd tech\Desktop\sc\SmartCity"
.\mvnw.cmd clean compile
# BUILD SUCCESS = bon!
```

**Puis en app:**
- Login agent
- Voir une mission
- Vérifier qu'elle s'affiche correctement (pas "Affecte" écrit partout)

---

---

## ❌ PROBLÈME #3: QR CODE DUPLIQUÉ
**Où?** `CitizenDashboardController.java`  
**Symptôme:** Deux méthodes portent le même nom  
**Cause:** Copier-coller oublié pendant dev  
**Impact:** Confusion = lequel on appelle?

---

### ✅ FIX #3 - Trouver & Supprimer

**Ouvrir:** `src/main/java/com/smartcity/controller/CitizenDashboardController.java`

**Rechercher:** `generateAndDisplayMapQRCode`

**Trouver tous les occurrences:**
```bash
grep -n "generateAndDisplayMapQRCode" "c:\Users\bmd tech\Desktop\sc\SmartCity\src\main\java\com\smartcity\controller\CitizenDashboardController.java"
```

**Résultat ressemble à:**
```
Ligne 850: private void generateAndDisplayMapQRCode() {
Ligne 900: ... code ...
Ligne 950: }
Ligne 2000: generateAndDisplayMapQRCode();  ← APPEL
Ligne 2500: private void generateAndDisplayMapQRCode() {  ← DOUBLON!
Ligne 2550: ... code ...
Ligne 2600: }
```

**À faire:**
- **Garder** celle qui est appelée (ligne 850)
- **SUPPRIMER** la deuxième définition (lignes 2500-2600)

**Pour supprimer:** Sélectionner les lignes 2500-2600 et appuyer `Delete`

---

### ✅ TESTER:
```bash
cd "c:\Users\bmd tech\Desktop\sc\SmartCity"
.\mvnw.cmd clean compile
# Pas d'erreur "method already defined" = bon!
```

---

---

## ❌ PROBLÈME #4: FICHIERS BACKUP INUTILES
**Où?** Racine du projet  
**Symptôme:** Fichiers non utilisés qui confondent les devs  
**Cause:** Sauvegarde oubliée du ancien code

---

### ✅ FIX #4 - SUPPRIMER

**Trois fichiers à SUPPRIMER COMPLÈTEMENT:**

```bash
del "c:\Users\bmd tech\Desktop\sc\SmartCity\CitizenDashboardController_orig.java"
del "c:\Users\bmd tech\Desktop\sc\SmartCity\original_agent.java"
del "c:\Users\bmd tech\Desktop\sc\SmartCity\src\main\java\com\smartcity\service\GeoConfig.java"
```

**Vérifier suppression:**
```bash
cd "c:\Users\bmd tech\Desktop\sc\SmartCity"
git status
# Devrait montrer: deleted:   CitizenDashboardController_orig.java
# Devrait montrer: deleted:   original_agent.java
# Devrait montrer: deleted:   src/main/java/.../GeoConfig.java
```

---

---

## ✅ APRÈS LES 4 FIXES

### Recompile COMPLET:
```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
cd "c:\Users\bmd tech\Desktop\sc\SmartCity"
.\mvnw.cmd clean compile
```

**Résultat ATTENDU:**
```
[INFO] Compiling 40 source files with javac
[INFO] ────────────────────────────────────────────
[INFO] BUILD SUCCESS
[INFO] ────────────────────────────────────────────
```

**SI ERREURS:**
```
- unmappable character → Fix #1 pas bon
- method already defined → Fix #3 pas bon
- class not found → Fix #4 pas bon
```

---

---

## 🧪 TESTS RAPIDES (30 min après fixes)

### Test #1: Login Citoyen
```
1. Lancer app
2. Click "S'inscrire" → Créer compte
   Email: test@example.com
   Nom: Test
   Role: Citoyen
   Mot de passe: Pass123!
3. Login avec ces identifiants
4. Résultat OK? → ✅
```

### Test #2: Créer Signalement
```
1. Login citoyen
2. Click "Ajouter signalement"
3. Remplir:
   - Description: "Test rubbish"
   - Catégorie: "Plastique"
   - Zone: "Pikine"
4. Click "Ajouter"
5. Vérifier en BD:
   mysql> SELECT * FROM Signalement WHERE description = 'Test rubbish';
   Résultat OK? → ✅
```

### Test #3: Login Agent
```
1. Logout
2. Login agent1/pass
3. Voir missions du jour
4. Vérifier les statuts affichent "Affecté" (avec accent!)
5. Résultat OK? → ✅
```

### Test #4: Login Admin
```
1. Logout
2. Login admin/admin
3. Voir dashboard stats
4. Vérifier pas de crash
5. Résultat OK? → ✅
```

---

---

## 📋 CHECKLIST FINALE

```
ENCODAGE UTF-8
☐ Ouvrir pom.xml
☐ Ajouter/vérifier project.build.sourceEncoding=UTF-8
☐ mvnw clean compile
☐ ✅ BUILD SUCCESS (pas unmappable character)

STATUTS BD
☐ mysql> USE db_smartcity;
☐ Exécuter les 5 commandes SQL
☐ SELECT DISTINCT statut FROM Signalement;
☐ ✅ Résultat: 'En attente', 'Affecté', 'En cours', 'Terminé' (AVEC accents)

QR CODE DUPLIQUÉ
☐ Ouvrir CitizenDashboardController.java
☐ Trouver les 2 generateAndDisplayMapQRCode()
☐ Garder 1, supprimer 1
☐ mvnw clean compile
☐ ✅ BUILD SUCCESS (pas "method already defined")

FICHIERS BACKUP
☐ Supprimer CitizenDashboardController_orig.java
☐ Supprimer original_agent.java
☐ Supprimer src/.../GeoConfig.java
☐ git status → vérifier deleted files

TESTS RAPIDES
☐ Test #1: Login citoyen OK
☐ Test #2: Créer signalement OK
☐ Test #3: Agent voit "Affecté" (accent) OK
☐ Test #4: Admin pas de crash OK
☐ ✅ Tout marche!

FINAL
☐ git add -A
☐ git commit -m "🔧 Critical fixes: UTF-8, statuts, QR code, cleanup"
☐ git log --oneline | head -1
☐ ✅ PRÊT POUR PRÉSENTATION
```

---

## ⏱️ TEMPS PAR PROBLÈME

| # | Problème | Temps | Difficulté |
|---|----------|-------|-----------|
| 1 | Encodage UTF-8 | 10 min | ⭐ Trivial |
| 2 | Statuts BD | 15 min | ⭐ Trivial |
| 3 | QR code | 10 min | ⭐ Trivial |
| 4 | Backups | 5 min | ⭐ Trivial |
| Tests | Vérification | 30 min | ⭐⭐ Facile |
| **TOTAL** | | **~1h** | |

---

## 🚨 SI PANIQUE

**Q: J'ai fait une bêtise en SQL?**  
R: Facile à corriger! Les UPDATE ne sont que "Affecte" → "Affecté". Faire l'inverse:
```sql
UPDATE Signalement SET statut = 'Affecte' WHERE statut = 'Affecté';
```

**Q: J'ai supprimé le mauvais fichier?**  
R: Git l'a! Restore:
```bash
git restore CitizenDashboardController.java
```

**Q: Ça compile pas encore?**  
R: Copier la ligne d'erreur, on debug ensemble.

---

**Commencez par FIX #1 maintenant!**  
**Ensuite FIX #2 (SQL)**  
**Puis FIX #3 & #4**  
**Recompile**  
**Tests rapides**  
**Fini!**

