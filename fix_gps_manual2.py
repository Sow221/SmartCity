path = 'src/main/java/com/smartcity/service/GpsApiServer.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Before: {len(lines)} lines", flush=True)

# Lines to remove (0-indexed) - CSS for manual, button, div, inputs, JS functions
# From scan: 541,542,543,544 = CSS manual styles
# 552 = button "Saisir manuellement"
# 553,554,555,556,557 = div#manual block
# 590 = showManual function
# 591,592,593,594,595 = sendManual function

# Also fix lines that call showManual:
# 578: if not https -> showManual
# 582: if no geolocation -> showManual
# 587: GPS refused -> showManual
# 597: else showManual

# Build set of lines to remove
to_remove = {541, 542, 543, 544, 552, 553, 554, 555, 556, 557, 590, 591, 592, 593, 594, 595}

# Lines to replace with simpler version (no showManual call)
replacements = {
    578: b'            + "  if(location.protocol!==\'https:\'){document.getElementById(\'status\').innerHTML=\'\\u26A0\\uFE0F GPS n\\u00e9cessite HTTPS\';return;}"\r\r\r\r\n',
    582: b'            + "  if(!navigator.geolocation){document.getElementById(\'status\').innerHTML=\'GPS non disponible\';return;}"\r\r\r\r\n',
    587: b'            + "      document.getElementById(\'status\').innerHTML=\'\\u26A0\\uFE0F GPS refus\\u00e9\';},"\r\r\r\r\n',
    597: b'            + "if(location.protocol===\'https:\'){tryGps();}else{document.getElementById(\'status\').innerHTML=\'\\u26A0\\uFE0F Connexion HTTP \\u2014 GPS indisponible\';}"\r\r\r\r\n',
}

new_lines = []
for i, line in enumerate(lines):
    if i in to_remove:
        print(f"REMOVED {i+1}: {repr(line.strip()[:60])}", flush=True)
        continue
    if i in replacements:
        new_lines.append(replacements[i])
        print(f"REPLACED {i+1}", flush=True)
        continue
    new_lines.append(line)

with open(path, 'wb') as f:
    f.writelines(new_lines)

print(f"After: {len(new_lines)} lines. DONE", flush=True)
