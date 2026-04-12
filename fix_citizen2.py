import re

path = r'src\main\java\com\smartcity\controller\CitizenDashboardController.java'

# Lire avec latin-1 pour preserver tous les octets
with open(path, 'rb') as f:
    raw = f.read()

# Decoder en latin-1 (preserve tous les octets)
content = raw.decode('latin-1')

# 1. Supprimer les lignes vides consecutives
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

# 2. Corriger les caracteres corrompus connus
# "ﾇｽｶ?ｶO" = "❌" (U+274C)
# "ﾇｽｶo\n" = "é" suivi de saut de ligne
# Remplacer les sequences corrompues par leurs equivalents ASCII simples
replacements = [
    # Caracteres corrompus -> ASCII safe
    ('\uff87\uff7d\uff76\uff3f\uff76O', '!'),  # ❌ corrompu
    ('\uff87\uff7d\uff76o', 'e'),  # e accent corrompu
    ('\uff87\uff7d\uff76\uff3f', '!'),
    # Corriger les messages utilisateur corrompus
    ('"ﾇｽｶ?ｶO Description requise"', '"Description requise"'),
    ('"ﾇｽｶo\n Lien GPS copiﾇ涕ｸ : "', '"Lien GPS copie : "'),
    # Autres sequences corrompues communes
    ('ﾇｽｶ', ''),
    ('涕ｸ', 'e'),
]

for old, new in replacements:
    if old in content:
        content = content.replace(old, new)
        print(f"  Corrige: {repr(old[:20])} -> {repr(new)}")

# 3. Supprimer le champ mapBridgeInstalled
content = re.sub(r'    private boolean mapBridgeInstalled = false;\n', '', content)
content = re.sub(r'        mapBridgeInstalled = false;\n', '', content)
print("  mapBridgeInstalled supprime")

# 4. Remplacer le bloc bridge if(!mapBridgeInstalled) par bridge direct
# Chercher le pattern
bridge_pattern = re.compile(
    r'        // Bridge Java.*?clic sur carte\)\n'
    r'        if \(!mapBridgeInstalled\) \{.*?\n        \}\n',
    re.DOTALL
)

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

m = bridge_pattern.search(content)
if m:
    content = content[:m.start()] + new_bridge + content[m.end():]
    print("  Bridge remplace via regex")
else:
    # Chercher manuellement
    marker = 'if (!mapBridgeInstalled)'
    if marker in content:
        s = content.index(marker)
        # Trouver debut du commentaire
        comment_start = content.rfind('        // Bridge', 0, s)
        if comment_start > s - 300:
            s = comment_start
        # Trouver fin du bloc
        depth = 0
        i = s
        started = False
        end = s
        while i < len(content):
            if content[i] == '{':
                depth += 1
                started = True
            elif content[i] == '}':
                depth -= 1
                if started and depth == 0:
                    end = i + 2  # inclure \n
                    break
            i += 1
        content = content[:s] + new_bridge + content[end:]
        print("  Bridge remplace manuellement")
    else:
        print("  WARN: bridge pattern non trouve")

# 5. Ecrire en UTF-8 propre
with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

lines_count = content.count('\n')
print(f"Fichier ecrit: {lines_count} lignes")

# Verifications
checks = [
    ('mapBridgeInstalled', False),
    ('class CitizenDashboardController', True),
    ('private void loadInteractiveMap', True),
    ('Bridge Java <- JavaScript', True),
    ('win.setMember("javaCitizen"', True),
]
for pattern, should_exist in checks:
    found = pattern in content
    status = 'OK' if found == should_exist else 'FAIL'
    print(f"  {status}: '{pattern}' {'present' if found else 'absent'}")
