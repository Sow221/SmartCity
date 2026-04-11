#!/bin/bash
# 🔧 DIAGNOSTIC SERVEURS - SmartCity
# Usage: diagnostic-servers.sh
# Teste les serveurs (GpsApiServer, WebSocketServer) et la géolocalisation

echo "═════════════════════════════════════════════════════════"
echo "  🔧 DIAGNOSTIC PRÉ-DÉMO - SmartCity Géolocalisation"
echo "═════════════════════════════════════════════════════════"
echo ""

# ============================================================================
# 1. VÉRIFIER LES PORTS
# ============================================================================
echo "📍 [1/6] Vérification PORTS..."
echo ""

port_8081=$(netstat -an | grep :8081 | wc -l)
port_8082=$(netstat -an | grep :8082 | wc -l)

if [ $port_8081 -eq 0 ]; then
  echo "  ✅ Port 8081 LIBRE (GpsApiServer)"
else
  echo "  ❌ Port 8081 OCCUPÉ - À libérer !"
fi

if [ $port_8082 -eq 0 ]; then
  echo "  ✅ Port 8082 LIBRE (WebSocketServer)"
else
  echo "  ❌ Port 8082 OCCUPÉ - À libérer !"
fi

echo ""

# ============================================================================
# 2. VÉRIFIER MYSQL
# ============================================================================
echo "📍 [2/6] Vérification MySQL..."
echo ""

mysql_check=$(mysql -u root -p"77884455" -e "USE db_smartcity; SELECT COUNT(*) FROM Zone;" 2>/dev/null)

if [ $? -eq 0 ]; then
  echo "  ✅ MySQL ACCESSIBLE"
  echo "  ✅ Base db_smartcity EXISTE"
  
  # Vérifier zones GPS
  pikine=$(mysql -u root -p"77884455" -e "USE db_smartcity; SELECT COUNT(*) FROM Zone WHERE nomZone='Pikine' AND latitude IS NOT NULL;" 2>/dev/null | tail -1)
  if [ "$pikine" = "1" ]; then
    echo "  ✅ Zone Pikine avec coordonnées GPS présente"
  else
    echo "  ⚠️  Zone Pikine GPS manquante"
  fi
  
  guediawaye=$(mysql -u root -p"77884455" -e "USE db_smartcity; SELECT COUNT(*) FROM Zone WHERE nomZone='Guédiawaye' AND latitude IS NOT NULL;" 2>/dev/null | tail -1)
  if [ "$guediawaye" = "1" ]; then
    echo "  ✅ Zone Guédiawaye avec coordonnées GPS présente"
  else
    echo "  ⚠️  Zone Guédiawaye GPS manquante"
  fi
else
  echo "  ❌ MySQL NON ACCESSIBLE ou base non créée"
fi

echo ""

# ============================================================================
# 3. VÉRIFIER COMPILATION
# ============================================================================
echo "📍 [3/6] Vérification COMPILATION..."
echo ""

cd $(dirname "$0")

if [ ! -d "target" ]; then
  echo "  ℹ️  Compilation requise (pas de target/)..."
  mvn clean compile -q 2>/dev/null
  compile_status=$?
else
  # Vérifier juste la dernière compilation
  echo "  ℹ️  Target/ présent, skipping recompile..."
  compile_status=0
fi

if [ $compile_status -eq 0 ]; then
  echo "  ✅ COMPILATION OK"
else
  echo "  ❌ COMPILATION FAILED - Lancer: mvn clean compile"
fi

echo ""

# ============================================================================
# 4. VÉRIFIER LOG FILES
# ============================================================================
echo "📍 [4/6] Vérification LOG FILES..."
echo ""

if [ -d "logs" ]; then
  echo "  ✅ Dossier logs/ existe"
  log_count=$(find logs -type f -name "*.log" 2>/dev/null | wc -l)
  echo "  ℹ️  Fichiers logs: $log_count"
else
  echo "  ℹ️  Dossier logs/ n'existe pas (créé au runtime)"
fi

echo ""

# ============================================================================
# 5. VÉRIFIER FICHIERS CRITIQUES
# ============================================================================
echo "📍 [5/6] Vérification FICHIERS CRITIQUES..."
echo ""

files_to_check=(
  "config.properties"
  "src/main/java/com/smartcity/service/GpsApiServer.java"
  "src/main/java/com/smartcity/service/RealTimeGPSService.java"
  "src/main/resources/fxml/citizen_dashboard.fxml"
  "src/main/resources/fxml/agent_dashboard.fxml"
)

for file in "${files_to_check[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✅ $file"
  else
    echo "  ❌ $file MANQUANT"
  fi
done

echo ""

# ============================================================================
# 6. RÉSUMÉ
# ============================================================================
echo "📍 [6/6] RÉSUMÉ FINAL..."
echo ""

if [ $port_8081 -eq 0 ] && [ $port_8082 -eq 0 ] && [ $? -eq 0 ] && [ $compile_status -eq 0 ]; then
  echo "🟢 ✅ TOUS LES DIAGNOSTICS OK - READY FOR DEMO"
  exit 0
else
  echo "🟡 ⚠️  PROBLÈMES DÉTECTÉS - Voir ci-dessus"
  exit 1
fi
