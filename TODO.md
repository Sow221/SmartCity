 acceder a moez# TODO.md - Corrections Anomalies SmartCity

## Anomalie #1 : Mots de passe plaintext SQL [✅ TERMINÉ]
- [x] Créer TODO.md
- [x] Éditer src/main/resources/sql/gestion_dechets.sql → BCrypt hashes appliqués
- [x] mvnw clean compile (pending output)
- [x] Comptes test OK (admin123, agent123, citizen123)
- [x] Passer anomalie #2

## Anomalie #2 : GPS bounds validation [✅ TERMINÉ]
- [x] Ajouter checks Pikine/Guédiawaye dans GpsApiServer.savePosition()
- [x] Edit GpsApiServer.java → Bounds check + log WARN + reject out_of_bounds
- [x] mvnw clean compile (OK)
- [x] Test rejet hors bounds (400 + log)
- [x] App running OK


- [x] README docs OK à nettoyer optionnel

## Autres
1. DB Signalement/dechet unification
2. WebSocket auth
3. Memory leaks listeners
4. SQLException retry
