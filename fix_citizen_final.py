import re, sys

path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

# Lire en latin-1 pour preserver tous les octets originaux
with open(path, 'rb') as f:
    raw = f.read()
content = raw.decode('latin-1')

# --- Nettoyer les lignes vides consecutives ---
lines = content.splitlines()
cleaned = []
prev_empty = False
for line in lines:
    is_empty = line.strip() == ''
    if is_empty and prev_empty:
        continue
    cleaned.append(line)
    prev_empty = is_empty
content = '\n'.join(cleaned) + '\n'

# --- Corriger les caracteres corrompus (latin-1 -> UTF-8 equivalents) ---
# Ces sequences sont des caracteres accentues mal encodes
fixes = [
    # Messages utilisateur corrompus -> versions simples
    (r'[^\x00-\x7F\n]+Description requise', '"Description requise"'),
    # Garder les chaines Java valides, remplacer les sequences non-ASCII par equivalents
]

# Remplacer caracteres non-ASCII hors strings Java par leur equivalent Unicode
# On va faire une passe ligne par ligne
result_lines = []
for line in content.splitlines():
    # Remplacer les bytes latin-1 > 127 par leur equivalent unicode direct
    # (latin-1 est identique a unicode pour les 256 premiers caracteres)
    # Donc on garde tel quel - le fichier est deja en unicode via latin-1 decode
    result_lines.append(line)
content = '\n'.join(result_lines) + '\n'

# --- Supprimer mapBridgeInstalled ---
content = re.sub(r'\s*private boolean mapBridgeInstalled = false;', '', content)
content = re.sub(r'\s*mapBridgeInstalled = false;', '', content)
content = re.sub(r'\s*mapBridgeInstalled = true;', '', content)

# --- Remplacer le bloc bridge if(!mapBridgeInstalled) ---
# Trouver et remplacer
marker = 'if (!mapBridgeInstalled)'
if marker in content:
    s = content.index(marker)
    # Remonter pour inclure le commentaire
    comment = '        // Bridge Java'
    cs = content.rfind(comment, max(0, s-300), s)
    if cs >= 0:
        s = cs
    # Trouver la fin du bloc (accolade fermante correspondante)
    depth = 0
    i = content.index('{', s)
    start_brace = i
    while i < len(content):
        if content[i] == '{':
            depth += 1
        elif content[i] == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                # Consommer le \n suivant
                if end < len(content) and content[end] == '\n':
                    end += 1
                break
        i += 1
    
    new_bridge = (
        '        // Bridge Java <- JavaScript : reinstalle a chaque loadContent\n'
        '        signalementMapView.getEngine().getLoadWorker().stateProperty().addListener((obs, o, n) -> {\n'
        '            if (n == javafx.concurrent.Worker.State.SUCCEEDED) {\n'
        '                try {\n'
        '                    netscape.javascript.JSObject win =\n'
        '                        (netscape.javascript.JSObject) signalementMapView.getEngine().executeScript("window");\n'
        '                    win.setMember("javaCitizen", new MapBridgeCitizen());\n'
        '                } catch (Exception ex) {\n'
        '                    logger.warn("Bridge carte citoyen non installe", ex);\n'
        '                }\n'
        '            }\n'
        '        });\n'
    )
    content = content[:s] + new_bridge + content[end:]
    print("OK: bridge remplace")
else:
    # Verifier si le nouveau bridge est deja present
    if 'Bridge Java <- JavaScript' in content:
        print("OK: nouveau bridge deja present")
    else:
        print("WARN: bridge non trouve")

# --- Ecrire en UTF-8 ---
with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Fichier ecrit: {content.count(chr(10))} lignes")

# --- Verifications ---
checks = [
    ('mapBridgeInstalled', False),
    ('class CitizenDashboardController', True),
    ('private void loadInteractiveMap', True),
    ('win.setMember("javaCitizen"', True),
    ('private void startGpsPolling', True),
    ('json.has("lat")', True),
]
all_ok = True
for pattern, should_exist in checks:
    found = pattern in content
    ok = found == should_exist
    all_ok = all_ok and ok
    print(f"  {'OK' if ok else 'FAIL'}: '{pattern}' {'present' if found else 'absent'}")

sys.exit(0 if all_ok else 1)
