# TODO.md - Setup Database & Launch SmartCity

## 🚀 Étapes de configuration rapide (5 min)

### 1. MySQL Setup
```
# Installer MySQL si pas présent
# Windows: Download MySQL Community Server

# Créer DB
mysql -u root -p
CREATE DATABASE db_smartcity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### 2. Config DB
```
# Éditer config.properties (créé ci-dessus)
db.password=VOTRE_MOT_DE_PASSE_ROOT
```

### 3. Initialiser DB
```
mysql -u root -p db_smartcity < scripts/reset_database.sql
mysql -u root -p db_smartcity < scripts/test_data.sql
```

- [x] P16: Add Rôle ComboBox register

### Phase 6: Global (Élevé)
- [ ] P13: Add ProgressIndicator loading states tables
- [ ] P9: Citizen header/sidebar teal #0f766e
- [ ] P7: Onboarding message dashboard vide
- [ ] P11: Emoji → Font icons (optional)

### Validation
```
mvn clean compile
mvnw javafx:run
Test all dashboards + auth
```

**Confirmez pour démarrer Phase 1?**
