import os, json, datetime

history_dir = r'C:\Users\MS\AppData\Roaming\Code\User\History'
target_start = datetime.datetime(2026, 4, 7, 20, 0)
target_end   = datetime.datetime(2026, 4, 8, 7, 0)
found = []
all_recent = []

for folder in os.listdir(history_dir):
    entries_path = os.path.join(history_dir, folder, 'entries.json')
    if not os.path.exists(entries_path):
        continue
    try:
        with open(entries_path, 'rb') as f:
            raw = f.read()
        decoded = raw.decode('utf-8', errors='ignore')
        data = json.loads(decoded)
        resource = data.get('resource', '')
        for entry in data.get('entries', []):
            ts = entry.get('timestamp', 0)
            dt = datetime.datetime.fromtimestamp(ts / 1000)
            all_recent.append((dt, resource.split('/')[-1], entry.get('id',''), folder))
            if target_start <= dt <= target_end:
                found.append((dt, resource.split('/')[-1], entry.get('id',''), folder, resource))
    except Exception as e:
        pass

found.sort()
all_recent.sort()

print(f"=== Entrees entre 20h07 et 6h08 : {len(found)} ===")
for dt, fname, eid, folder, res in found:
    print(dt.strftime('%Y-%m-%d %H:%M:%S'), '|', fname, '|', eid, '|', folder)
    print('  ->', res)

print()
print("=== 15 entrees les plus recentes (tous fichiers) ===")
for dt, fname, eid, folder in all_recent[-15:]:
    print(dt.strftime('%Y-%m-%d %H:%M:%S'), '|', fname, '|', eid, '|', folder)
