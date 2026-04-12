path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8') as f:
    lines = f.readlines()

depth = 0
class_started = False
in_string = False
prev_char = ''
escape_next = False

for i, line in enumerate(lines, 1):
    stripped = line.strip()
    
    # Detecter debut de classe
    if 'public class CitizenDashboardController' in line:
        class_started = True
    
    if not class_started:
        continue
    
    # Compter accolades (simplifie - ignore strings)
    j = 0
    while j < len(line):
        ch = line[j]
        if escape_next:
            escape_next = False
        elif in_string:
            if ch == '\\':
                escape_next = True
            elif ch == '"':
                in_string = False
        else:
            if ch == '"':
                in_string = True
            elif ch == '{':
                depth += 1
            elif ch == '}':
                depth -= 1
                if depth == 0 and i < len(lines) - 3:
                    # Classe fermee avant la fin du fichier
                    print(f"CLASSE FERMEE a la ligne {i}: {line.rstrip()}")
                    # Afficher les 5 lignes suivantes
                    for k in range(i, min(i+5, len(lines))):
                        print(f"  {k+1}: {lines[k].rstrip()}")
                    print()
        j += 1

print(f"Depth final: {depth}")
print(f"Total lignes: {len(lines)}")
