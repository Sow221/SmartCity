import sys

# Agent controller
f = open('src/main/java/com/smartcity/controller/AgentDashboardController.java', 'rb')
d = f.read()
f.close()
lines = d.split(b'\n')
print(f"Agent controller: {len(lines)} lines", flush=True)
for i, line in enumerate(lines, 1):
    if b'filterMissions' in line or b'ComboBox' in line or b'configureMissions' in line or b'initialize' in line:
        print(f"{i}: {repr(line.strip()[:120])}", flush=True)
