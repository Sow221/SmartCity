f = open('src/main/java/com/smartcity/controller/AgentDashboardController.java', 'rb')
lines = f.readlines()
f.close()
print(f"Lines: {len(lines)}", flush=True)
for i, line in enumerate(lines, 1):
    s = line.strip().decode('utf-8', errors='replace')
    if any(x in s for x in ['JSBridgeAgent', 'selectMission', 'javaAgent', 'bindPopup', 'circleMarker', 'on(\'click']):
        print(f"{i}: {s[:120]}", flush=True)
