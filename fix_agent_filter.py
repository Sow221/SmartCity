path = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Lines: {len(lines)}", flush=True)

# Find line with "private Button btnAgentDashboard" for field insertion
field_insert = None
for i, line in enumerate(lines):
    if b'btnAgentDashboard' in line and b'Button' in line:
        field_insert = i
        print(f"Field insert after line {i+1}: {repr(line.strip()[:80])}", flush=True)
        break

# Find "tableMesMissions.setItems(missions)" for listener insertion
set_items_line = None
for i, line in enumerate(lines):
    if b'tableMesMissions.setItems(missions)' in line:
        set_items_line = i
        print(f"setItems at line {i+1}: {repr(line.strip()[:80])}", flush=True)
        break

# Insert ComboBox field declaration
if field_insert is not None:
    lines.insert(field_insert + 1, b'    @FXML private javafx.scene.control.ComboBox<String> filterMissionsStatutCombo;\r\n')
    if set_items_line is not None:
        set_items_line += 1  # shift by 1

# Insert listener after setItems
if set_items_line is not None:
    listener = (
        b'        if (filterMissionsStatutCombo != null) {\r\n'
        b'            filterMissionsStatutCombo.setItems(javafx.collections.FXCollections.observableArrayList(\r\n'
        b'                "Tous", "En attente", "Affect\xc3\xa9", "En cours", "Termin\xc3\xa9"));\r\n'
        b'            filterMissionsStatutCombo.setValue("Tous");\r\n'
        b'            filterMissionsStatutCombo.valueProperty().addListener((obs, old, val) -> {\r\n'
        b'                if (val == null || "Tous".equals(val)) {\r\n'
        b'                    tableMesMissions.setItems(missions);\r\n'
        b'                } else {\r\n'
        b'                    tableMesMissions.setItems(javafx.collections.FXCollections.observableArrayList(\r\n'
        b'                        missions.stream().filter(s -> com.smartcity.model.SignalementStatut.fromAny(val)\r\n'
        b'                            == com.smartcity.model.SignalementStatut.fromAny(s.getStatut()))\r\n'
        b'                        .collect(java.util.stream.Collectors.toList())));\r\n'
        b'                }\r\n'
        b'            });\r\n'
        b'            missions.addListener((javafx.collections.ListChangeListener<com.smartcity.model.Signalement>) c -> {\r\n'
        b'                String current = filterMissionsStatutCombo.getValue();\r\n'
        b'                if (current != null && !"Tous".equals(current)) {\r\n'
        b'                    filterMissionsStatutCombo.valueProperty().set(null);\r\n'
        b'                    filterMissionsStatutCombo.setValue(current);\r\n'
        b'                }\r\n'
        b'            });\r\n'
        b'        }\r\n'
    )
    lines.insert(set_items_line + 1, listener)
    print(f"Listener inserted after line {set_items_line+1}", flush=True)

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
