path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8') as f:
    lines = f.readlines()

depth = 0
in_string = False
in_char = False
prev_char = ''

for i, line in enumerate(lines, 1):
    for j, ch in enumerate(line):
        if in_string:
            if ch == '"' and prev_char != '\\':
                in_string = False
        elif in_char:
            if ch == "'" and prev_char != '\\':
                in_char = False
        else:
            if ch == '"':
                in_string = True
            elif ch == "'":
                in_char = True
            elif ch == '{':
                depth += 1
            elif ch == '}':
                depth -= 1
                if depth < 0:
                    print(f"ERREUR: accolade fermante en trop a la ligne {i}: {line.rstrip()}")
                    depth = 0
        prev_char = ch
    
    # Afficher les changements de profondeur importants
    if depth == 0 and i > 10:
        print(f"Ligne {i}: depth=0 (fin de classe?) - {line.rstrip()[:60]}")
        if i < len(lines) - 5:
            print(f"  -> Lignes suivantes: {lines[i].rstrip()[:60] if i < len(lines) else 'EOF'}")
            break

print(f"\nDepth final: {depth}")
print(f"Total lignes: {len(lines)}")
