import re

path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8') as f:
    content = f.read()

# Ajouter le guard json.has avant json.get("lat")
old = '                        double lat = json.get("lat").getAsDouble();\n'
new = (
    '                        if (!json.has("lat") || !json.has("lon")) return;\n'
    '                        double lat = json.get("lat").getAsDouble();\n'
)

if 'json.has("lat")' not in content:
    if old in content:
        content = content.replace(old, new, 1)
        print("OK: guard json.has ajoute")
    else:
        # Chercher variante
        alt = '                        double lat = json.get("lat").getAsDouble();'
        if alt in content:
            content = content.replace(alt,
                '                        if (!json.has("lat") || !json.has("lon")) return;\n' + alt, 1)
            print("OK: guard json.has ajoute (variante)")
        else:
            print("WARN: pattern json.get lat non trouve")
            # Afficher contexte startGpsPolling
            idx = content.find('private void startGpsPolling')
            if idx >= 0:
                print(content[idx:idx+800])
else:
    print("OK: guard json.has deja present")

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fichier ecrit.")
print("json.has present:", 'json.has("lat")' in content)
