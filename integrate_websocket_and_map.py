#!/usr/bin/env python3
"""
Corrections finales pour AgentDashboardController:
1. Charger smart-gps-map.js depuis le classpath
2. Ajouter client WebSocket
3. Intégrer tout dans la page générée
"""

import re

file_path = r"src/main/java/com/smartcity/controller/AgentDashboardController.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# ===== ÉTAPE 1 : Ajouter une méthode statique pour charger smart-gps-map.js =====
load_js_method = '''
    /** ✅ CHARGER LE JAVASCRIPT DE LA CARTE DEPUIS LES RESSOURCES */
    private static String getSmartGpsMapJs() {
        try {
            InputStream stream = AgentDashboardController.class
                    .getResourceAsStream("/js/smart-gps-map.js");
            if (stream == null) {
                logger.warn("⚠️ smart-gps-map.js not found in resources");
                return ""; // Fallback silencieux
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("⚠️ Error loading smart-gps-map.js: {}", e.getMessage());
            return "";
        }
    }

    /** ✅ CRÉER LE CLIENT WEBSOCKET POUR MISES À JOUR TEMPS RÉEL */
    private static String getWebSocketClientJs() {
        return """
            // 🔌 WebSocket Client - Mises à jour temps réel des missions et positions
            let ws = null;
            let wsConnectAttempts = 0;
            
            function connectWebSocket() {
                try {
                    let protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
                    let wsUrl = protocol + '//' + location.hostname + ':3002/ws/agent-missions';
                    ws = new WebSocket(wsUrl);
                    
                    ws.onopen = function() {
                        console.log('✅ WebSocket connecté pour mises à jour temps réel');
                        wsConnectAttempts = 0; // Reset counter on success
                    };
                    
                    ws.onmessage = function(event) {
                        try {
                            let data = JSON.parse(event.data);
                            
                            // Position d'agent en temps réel
                            if (data.type === 'position' && data.lat && data.lon) {
                                console.log('📍 Position agent reçue:', data.lat, data.lon);
                                if (typeof updateAgentPosition === 'function') {
                                    updateAgentPosition(data.lat, data.lon);
                                }
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                            
                            // Nouvelle mission
                            if (data.type === 'mission') {
                                console.log('🎯 Nouvelle mission reçue');
                                if (window.parent.refreshAgentMissions) {
                                    window.parent.refreshAgentMissions();
                                }
                            }
                        } catch(parseErr) {
                            console.debug('WebSocket message parse error (non-JSON):', event.data);
                        }
                    };
                    
                    ws.onerror = function(err) {
                        console.error('❌ WebSocket error:', err);
                    };
                    
                    ws.onclose = function() {
                        console.warn('⚠️ WebSocket fermé');
                        // Reconnexion après délai
                        wsConnectAttempts++;
                        let delay = Math.min(300000, 5000 * Math.pow(1.5, wsConnectAttempts));
                        console.log('🔄 Reconnexion dans', Math.round(delay/1000), 's');
                        setTimeout(connectWebSocket, delay);
                    };
                } catch(err) {
                    console.error('WebSocket setup error:', err);
                    setTimeout(connectWebSocket, 5000);
                }
            }
            
            // Connecter au chargement de la page
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', connectWebSocket);
            } else {
                connectWebSocket();
            }
            """;
    }
'''

# Find a good place to insert these methods - before buildLeafletHtml
build_leaflet_pos = content.find("private String buildLeafletHtml")
if build_leaflet_pos > 0:
    # Find the previous method's closing brace
    insert_pos = content.rfind("}", 0, build_leaflet_pos)
    if insert_pos > 0:
        content = content[:insert_pos+1] + "\n" + load_js_method + "\n" + content[insert_pos+1:]
        print("✅ Helper methods added (loadSmartGpsMapJs, getWebSocketClientJs)")

# ===== ÉTAPE 2 : Injecter le code dans buildLeafletHtml =====
# Find the return statement at the end of buildLeafletHtml and add WebSocket code

# Pattern: find "</body></html>" near the end of buildLeafletHtml method
pattern = r'("</body></html>";)(\s*})'

replacement = r'''"
                + getSmartGpsMapJs()  // ✅ Charger smart-gps-map.js
                + "<script>"
                + getWebSocketClientJs()  // ✅ Ajouter client WebSocket
                + "</script>"
                + "</body></html>";
\2'''

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("✅ WebSocket client injected into buildLeafletHtml()")
else:
    print("⚠️ Could not find injection point in buildLeafletHtml")

# ===== ÉTAPE 3 : Ajouter imports manquants =====
if "import java.nio.charset.StandardCharsets;" not in content:
    # Add after other imports
    import_pos = content.find("import com.smartcity")
    if import_pos > 0:
        import_pos = content.rfind("\n", 0, import_pos) + 1
        content = content[:import_pos] + "import java.nio.charset.StandardCharsets;\n" + content[import_pos:]
        print("✅ StandardCharsets import added")

if "import java.io.InputStream;" not in content:
    import_pos = content.find("import java.nio.charset.StandardCharsets;")
    if import_pos > 0:
        import_pos = content.find("\n", import_pos) + 1
        content = content[:import_pos] + "import java.io.InputStream;\n" + content[import_pos:]
        print("✅ InputStream import added")

# Write file
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"✅ AgentDashboardController fully updated!")
print("   - smart-gps-map.js will be loaded dynamically")
print("   - WebSocket client initialized for real-time updates")
