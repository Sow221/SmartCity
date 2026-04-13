path = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Lines: {len(lines)}", flush=True)

# ── FIX 1: Add @FXML ComboBox field after the existing @FXML fields block ──
# Insert after line 499 (after "private Button btnAgentDashboard..." declaration area)
# Find the line with "private Button btnAgentDashboard"
insert_field_after = None
for i, line in enumerate(lines):
    if b'btnAgentDashboard' in line and b'Button' in line:
        insert_field_after = i
        print(f"Found btnAgentDashboard at line {i+1}: {repr(line.strip()[:80])}", flush=True)
        break

if insert_field_after is not None:
    new_field = b'\r\n    @FXML\r\n    private ComboBox<String> filterMissionsStatutCombo;\r\n'
    lines.insert(insert_field_after + 1, new_field)
    print(f"Inserted ComboBox field after line {insert_field_after+1}", flush=True)

# ── FIX 1b: Add listener in initialize() after tableMesMissions.setItems(missions) ──
# Find "tableMesMissions.setItems(missions);" line
with open(path, 'rb') as f:
    lines = f.readlines()

# Re-search after potential shift
for i, line in enumerate(lines):
    if b'btnAgentDashboard' in line and b'Button' in line and b'@FXML' not in line:
        # Already has field? check next line
        pass

# Find initialize block and tableMesMissions.setItems
set_items_line = None
for i, line in enumerate(lines):
    if b'tableMesMissions.setItems(missions)' in line:
        set_items_line = i
        print(f"Found setItems at line {i+1}", flush=True)
        break

# Find the @FXML field block - insert ComboBox field declaration
# Find line with "private Button btnAgentDashboard"
field_insert = None
for i, line in enumerate(lines):
    if b'btnAgentDashboard' in line and b'Button' in line:
        field_insert = i
        break

print(f"field_insert={field_insert}, set_items_line={set_items_line}", flush=True)

# Apply fix 1: insert field
if field_insert is not None:
    lines.insert(field_insert + 1, b'\r\n    @FXML private ComboBox<String> filterMissionsStatutCombo;\r\n')
    # Adjust set_items_line index
    if set_items_line is not None:
        set_items_line += 1

# Apply fix 1b: insert listener after setItems
if set_items_line is not None:
    listener_code = (
        b'\r\n'
        b'        // Filtre statut missions\r\n'
        b'        if (filterMissionsStatutCombo != null) {\r\n'
        b'            filterMissionsStatutCombo.setItems(javafx.collections.FXCollections.observableArrayList(\r\n'
        b'                "Tous", "En attente", "Affect\xc3\xa9", "En cours", "Termin\xc3\xa9"));\r\n'
        b'            filterMissionsStatutCombo.setValue("Tous");\r\n'
        b'            filterMissionsStatutCombo.valueProperty().addListener((obs, old, val) -> {\r\n'
        b'                if (val == null || "Tous".equals(val)) {\r\n'
        b'                    tableMesMissions.setItems(missions);\r\n'
        b'                } else {\r\n'
        b'                    javafx.collections.ObservableList<com.smartcity.model.Signalement> filtered =\r\n'
        b'                        javafx.collections.FXCollections.observableArrayList(\r\n'
        b'                            missions.stream()\r\n'
        b'                                .filter(s -> com.smartcity.model.SignalementStatut.fromAny(val)\r\n'
        b'                                    == com.smartcity.model.SignalementStatut.fromAny(s.getStatut()))\r\n'
        b'                                .collect(java.util.stream.Collectors.toList()));\r\n'
        b'                    tableMesMissions.setItems(filtered);\r\n'
        b'                }\r\n'
        b'            });\r\n'
        b'        }\r\n'
    )
    lines.insert(set_items_line + 1, listener_code)
    print(f"Inserted listener after line {set_items_line+1}", flush=True)

with open(path, 'wb') as f:
    f.writelines(lines)

print("Agent controller DONE", flush=True)

# ── FIX 2: Remove manual entry from GpsApiServer.buildGpsHtml() ──
path2 = 'src/main/java/com/smartcity/service/GpsApiServer.java'
with open(path2, 'rb') as f:
    content = f.read()

# Remove the manual button line
content = content.replace(
    b'            + \"<button class=\\'btn\\' onclick=\\'showManual()\\'>\\\\u270F\\\\uFE0F Saisir manuellement</button>\"',
    b''
)
# Remove the manual div block
content = content.replace(
    b'            + \"<div id=\\'manual\\'>\"\r\r\r\r\n'
    b'            + \"  <p style=\\'margin-bottom:8px;font-size:13px;\\'>Coordonn\\\\u00e9es :</p>\"\r\r\r\r\n'
    b'            + \"  <input id=\\'mlat\\' type=\\'number\\' step=\\'0.00001\\' placeholder=\\'Latitude (ex: 14.76460)\\'/>\"\r\r\r\r\n'
    b'            + \"  <input id=\\'mlon\\' type=\\'number\\' step=\\'0.00001\\' placeholder=\\'Longitude (ex: -17.39200)\\'/>\"\r\r\r\r\n'
    b'            + \"  <button onclick=\\'sendManual()\\'>\\\\u2705 Confirmer</button>\"\r\r\r\r\n'
    b'            + \"</div>\"',
    b''
)
# Remove sendManual JS function
content = content.replace(
    b'            + \"function sendManual(){\"\r\r\r\r\n'
    b'            + \"  var lat=parseFloat(document.getElementById(\\'mlat\\').value);\"\r\r\r\r\n'
    b'            + \"  var lon=parseFloat(document.getElementById(\\'mlon\\').value);\"\r\r\r\r\n'
    b'            + \"  if(isNaN(lat)||isNaN(lon)||lat<-90||lat>90||lon<-180||lon>180){alert(\\'Invalide\\');return;}\"\r\r\r\r\n'
    b'            + \"  send(lat,lon,\\'Manuel\\');\"\r\r\r\r\n'
    b'            + \"}\"',
    b''
)
# Remove showManual JS function
content = content.replace(
    b'            + \"function showManual(){document.getElementById(\\'manual\\').style.display=\\'block\\';}\"',
    b''
)

with open(path2, 'wb') as f:
    f.write(content)

print("GpsApiServer DONE", flush=True)
