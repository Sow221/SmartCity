path = 'src/main/java/com/smartcity/service/GpsApiServer.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Lines: {len(lines)}", flush=True)

# Find lines containing manual-related content
for i, line in enumerate(lines, 1):
    decoded = line.decode('utf-8', errors='replace')
    if any(x in decoded for x in ['showManual', 'sendManual', 'manual', 'Saisir', 'mlat', 'mlon', 'Manuel']):
        print(f"{i}: {repr(decoded.strip()[:120])}", flush=True)
