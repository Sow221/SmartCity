import re

path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8', errors='replace') as f:
    content = f.read()

# Trouver toutes les lignes avec des caracteres non-ASCII dans des strings Java
lines = content.split('\n')
issues = []
for i, line in enumerate(lines):
    # Chercher des caracteres non-ASCII (hors commentaires)
    has_non_ascii = any(ord(c) > 127 for c in line)
    if has_non_ascii:
        issues.append((i+1, line))

print(f"Lignes avec caracteres non-ASCII: {len(issues)}")
for lineno, line in issues[:30]:
    print(f"  {lineno}: {repr(line[:100])}")
