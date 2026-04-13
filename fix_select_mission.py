import sys

path = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Lines: {len(lines)}", flush=True)

# ── FIX 1: Add selectMission method inside JSBridgeAgent, after setAgentPosition closing brace (line 2894, index 2893) ──
# Insert after line 2894 (index 2893) which is the closing } of setAgentPosition
select_method = (
    b'\r\n'
    b'        public void selectMission(int id) {\r\n'
    b'            Platform.runLater(() -> {\r\n'
    b'                missions.stream()\r\n'
    b'                    .filter(m -> m.getIdSignalement() == id)\r\n'
    b'                    .findFirst()\r\n'
    b'                    .ifPresent(m -> {\r\n'
    b'                        tableMesMissions.getSelectionModel().select(m);\r\n'
    b'                        updateMapDetails(m);\r\n'
    b'                        updateDistanceAndTime();\r\n'
    b'                    });\r\n'
    b'            });\r\n'
    b'        }\r\n'
)
lines.insert(2894, select_method)
print("Inserted selectMission after line 2894", flush=True)

# ── FIX 2: Add .on('click') to circleMarker — line 3272 is now shifted by 1 → index 3272 ──
# Find the circleMarker append line
for i, line in enumerate(lines):
    if b"fillOpacity:0.88,weight:2}).addTo(map).bindPopup('" in line:
        old = line
        # Replace: add .on('click', function(){if(window.javaAgent)window.javaAgent.selectMission(ID);}) after bindPopup
        # The mission id is in the loop variable - we need to inject it via the markers StringBuilder
        # Current: .append("',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('").append(popup).append("');");
        # New:     .append("',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('").append(popup).append("').on('click',function(){if(window.javaAgent)window.javaAgent.selectMission(").append(mission.getIdSignalement()).append(");});");
        new_line = line.replace(
            b".append(\"',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('\").append(popup).append(\"');\");",
            b".append(\"',fillOpacity:0.88,weight:2}).addTo(map).bindPopup('\").append(popup)\r\n"
            b"                    .append(\"').on('click',function(){if(window.javaAgent)window.javaAgent.selectMission(\")\r\n"
            b"                    .append(mission.getIdSignalement())\r\n"
            b"                    .append(\");});\");"
        )
        if new_line != line:
            lines[i] = new_line
            print(f"Modified circleMarker click at line {i+1}", flush=True)
            break

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
