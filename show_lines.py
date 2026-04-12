path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8', errors='replace') as f:
    lines = f.readlines()

# Afficher lignes 1270-1340
for i in range(1269, min(1340, len(lines))):
    print(f"{i+1}: {lines[i].rstrip()}")
