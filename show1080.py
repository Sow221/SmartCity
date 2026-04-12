path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'
with open(path, encoding='utf-8', errors='replace') as f:
    lines = f.readlines()
for i in range(1073, min(1090, len(lines))):
    print(f"{i+1}: {repr(lines[i].rstrip())}")
