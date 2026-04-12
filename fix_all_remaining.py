import sys

def read_utf8(path):
    with open(path, 'rb') as f:
        return f.read().decode('utf-8', errors='replace')

def write_utf8(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def replace_lines(path, start_line, end_line, new_content):
    """Remplace les lignes [start_line, end_line] (1-based, inclusif) par new_content."""
    lines = read_utf8(path).splitlines(keepends=True)
    before = lines[:start_line - 1]
    after  = lines[end_line:]
    result = ''.join(before) + new_content + ''.join(after)
    write_utf8(path, result)
    print(f"  OK: lignes {start_line}-{end_line} remplacees dans {path}")

# ============================================================
# ETAPE 1 : AgentDashboardController
# ============================================================
agent_path = r'src\main\java\com\smartcity\controller\AgentDashboardController.java'

# 1a: Ajouter mapLoaded apres mapFallbackHandlersInstalled (ligne 152)
content = read_utf8(agent_path)
if 'private boolean mapLoaded' not in content:
    content = content.replace(
        '    private boolean mapFallbackHandlersInstalled;',
        '    private boolean mapFallbackHandlersInstalled;\n    private boolean mapLoaded = false;'
    )
    write_utf8(agent_path, content)
    print("  OK: mapLoaded ajoute dans AgentDashboardController")
else:
    print("  SKIP: mapLoaded deja present")

# 1b: Remplacer refreshMap (lignes 1886 a ~1930) par version propre
# Trouver les bornes exactes
lines = read_utf8(agent_path).splitlines(keepends=True)
start = None
end = None
brace_count = 0
for i, line in enumerate(lines):
    if 'private void refreshMap(Signalement selectedMission, boolean showRoute)' in line:
        start = i + 1  # 1-based
        brace_count = 0
    if start is not None:
        brace_count += line.count('{') - line.count('}')
        if brace_count == 0 and i + 1 > start:
            end = i + 1
            break

print(f"  refreshMap: lignes {start}-{end}")

new_refresh_map = '''    private void refreshMap(Signalement selectedMission, boolean showRoute) {
        if (mapWebView == null) return;
        installMapFallbackHandlers(mapWebView.getEngine(), "Carte agent indisponible.");
        String html = buildLeafletHtml(selectedMission, showRoute);
        if (!mapLoaded) {
            mapLoaded = true;
            mapWebView.getEngine().loadContent(html);
            mapWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        netscape.javascript.JSObject win = (netscape.javascript.JSObject)
                            mapWebView.getEngine().executeScript("window");
                        win.setMember("javaAgent", new JSBridgeAgent());
                    } catch (Exception e) {
                        logger.warn("Erreur bridge carte GPS", e);
                    }
                }
            });
        } else {
            mapWebView.getEngine().loadContent(html);
        }
    }
'''

replace_lines(agent_path, start, end, new_refresh_map)

# ============================================================
# ETAPE 2 : CitizenDashboardController
# Corriger mapBridgeInstalled : reinstaller le bridge a chaque load
# ============================================================
citizen_path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

lines = read_utf8(citizen_path).splitlines(keepends=True)
start_c = None
end_c = None
for i, line in enumerate(lines):
    if 'Bridge Java' in line and 'JavaScript' in line and 'clic' in line:
        start_c = i + 1
    if start_c and i + 1 >= start_c:
        if '});' in line and i + 1 > start_c + 2:
            end_c = i + 1
            break

print(f"  mapBridgeInstalled block: lignes {start_c}-{end_c}")

new_bridge = '''        // Bridge Java <- JavaScript : reinstalle a chaque loadContent
        signalementMapView.getEngine().getLoadWorker().stateProperty().addListener((obs, o, n) -> {
            if (n == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    netscape.javascript.JSObject win =
                        (netscape.javascript.JSObject) signalementMapView.getEngine().executeScript("window");
                    win.setMember("javaCitizen", new MapBridgeCitizen());
                } catch (Exception ex) {
                    logger.warn("Bridge carte citoyen non installe", ex);
                }
            }
        });
'''

replace_lines(citizen_path, start_c, end_c, new_bridge)

# Supprimer le champ mapBridgeInstalled et ses usages
content = read_utf8(citizen_path)
content = content.replace('    private boolean mapBridgeInstalled = false;\n', '')
content = content.replace('    private boolean mapBridgeInstalled = false;\r\n', '')
content = content.replace('        mapBridgeInstalled = false;\n', '')
content = content.replace('        mapBridgeInstalled = false;\r\n', '')
write_utf8(citizen_path, content)
print("  OK: mapBridgeInstalled supprime de CitizenDashboardController")

# ============================================================
# ETAPE 3 : MainApp — documenter start(0)
# ============================================================
main_path = r'src\main\java\com\smartcity\app\MainApp.java'
content = read_utf8(main_path)
if "// 0 = pas d'agent" not in content:
    content = content.replace(
        '            gpsApiServer.start(0);',
        "            gpsApiServer.start(0); // defaultAgentId=0 : tokens crees a la demande par chaque agent"
    )
    write_utf8(main_path, content)
    print("  OK: MainApp start(0) documente")

print("\nToutes les corrections appliquees.")
