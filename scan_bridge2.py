import sys
import os
os.environ['PYTHONIOENCODING'] = 'utf-8'

f = open('src/main/java/com/smartcity/controller/AgentDashboardController.java', 'rb')
lines = f.readlines()
f.close()

for i, line in enumerate(lines, 1):
    s = line.strip().decode('utf-8', errors='replace')
    if any(x in s for x in ['JSBridgeAgent', 'selectMission', 'javaAgent', 'circleMarker', 'bindPopup']):
        sys.stdout.buffer.write(f"{i}: {s[:120]}\n".encode('utf-8'))
sys.stdout.buffer.flush()
