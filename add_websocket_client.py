#!/usr/bin/env python3
"""Add WebSocket client code to buildLeafletHtml in AgentDashboardController"""

import re

file_path = r"src/main/java/com/smartcity/controller/AgentDashboardController.java"

# Read file
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# The WebSocket client JavaScript code to inject
websocket_client_js = '''
            + "<script>"
            + "// 🔌 WebSocket Client pour mise à jour temps réel"
            + "let ws = null;"
            + "function connectWebSocket() {"
            + "  let protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';"
            + "  let port = location.host.includes(':') ? ':' + location.host.split(':')[1] : ':3002';"
            + "  ws = new WebSocket(protocol + '//' + location.hostname + port + '/ws/agent-missions');"
            + "  "
            + "  ws.onopen = function() { console.log('✅ WebSocket connecté'); };"
            + "  "
            + "  ws.onmessage = function(event) {"
            + "    try {"
            + "      let data = JSON.parse(event.data);"
            + "      // Mettre à jour la position de l'agent si position reçue"
            + "      if (data.type === 'position' && data.agentId) {"
            + "        console.log('📍 Position reçue:', data.lat, data.lon);"
            + "        if (typeof updateAgentPosition === 'function') {"
            + "          updateAgentPosition(data.lat, data.lon);"
            + "        }"
            + "      }"
            + "      // Mettre à jour les missions si données reçues"
            + "      if (data.type === 'mission' && window.parent.refreshAgentMissions) {"
            + "        console.log('🎯 Nouvelle mission reçue');"
            + "        window.parent.refreshAgentMissions();"
            + "      }"
            + "    } catch(e) { console.error('WebSocket parse error:', e); }"
            + "  };"
            + "  "
            + "  ws.onerror = function(err) { console.error('❌ WebSocket error:', err); };"
            + "  "
            + "  ws.onclose = function() {"
            + "    console.warn('⚠️ WebSocket fermé, reconnexion dans 5s');"
            + "    setTimeout(connectWebSocket, 5000);"
            + "  };"
            + "}"
            + "// Connecter au démarrage"
            + "connectWebSocket();"
            + "</script>"
'''

# Find where the Leaflet script ends (before closing the HTML string)
# Look for the main JS setup and inject WebSocket code right after Leaflet initialization

# Pattern: find where the big JavaScript block ends (look for last </script>)
leaflet_js_end = content.rfind("\"</script>\" +")
if leaflet_js_end > 0:
    # Find the next newline after this
    insert_pos = content.find("\n", leaflet_js_end)
    if insert_pos > 0:
        content = content[:insert_pos] + websocket_client_js + content[insert_pos:]
        print("✅ WebSocket client code injected into buildLeafletHtml()")
else:
    print("⚠️ Could not find injection point - trying alternative method")
    # Alternative: Find buildLeafletHtml method and add at the end before return
    pattern = r'(return html;)(?=\s*})'
    if re.search(pattern, content):
        # Add WebSocket JS code in the HTML string before the return
        content = re.sub(
            pattern,
            websocket_client_js + '\n            + "\";\n        return html;',
            content
        )
        print("✅ WebSocket client code added (alternative method)")

# Write file
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"✅ File {file_path} updated with WebSocket client!")
