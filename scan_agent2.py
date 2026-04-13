f = open('src/main/java/com/smartcity/controller/AgentDashboardController.java', 'rb')
lines = f.readlines()
f.close()
print(f"Lines: {len(lines)}", flush=True)

# Print initialize block (lines 507-560)
print("\n--- initialize ---", flush=True)
for i in range(505, 560):
    print(f"{i+1}: {repr(lines[i].strip()[:120])}", flush=True)

# Search for filterMissions anywhere
print("\n--- filterMissions search ---", flush=True)
for i, line in enumerate(lines, 1):
    if b'filter' in line.lower() and b'mission' in line.lower():
        print(f"{i}: {repr(line.strip()[:120])}", flush=True)

# Search for ComboBox
print("\n--- ComboBox ---", flush=True)
for i, line in enumerate(lines, 1):
    if b'ComboBox' in line or b'comboBox' in line:
        print(f"{i}: {repr(line.strip()[:120])}", flush=True)
