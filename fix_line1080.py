path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

with open(path, encoding='utf-8', errors='replace') as f:
    lines = f.readlines()

# Ligne 1079 (index 1078) contient une string corrompue coupee sur 2 lignes
# On la remplace par la version correcte
print(f"Avant 1079: {repr(lines[1078])}")
print(f"Avant 1080: {repr(lines[1079])}")

# Remplacer les deux lignes par une seule ligne correcte
lines[1078] = '            showCitizenMessage("Lien GPS copie : " + url, true);\n'
lines[1079] = ''  # vider la ligne suivante qui etait la suite de la string corrompue

print(f"Apres 1079: {repr(lines[1078])}")

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("Fichier corrige.")
