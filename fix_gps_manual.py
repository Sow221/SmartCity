path = 'src/main/java/com/smartcity/service/GpsApiServer.java'

with open(path, 'rb') as f:
    lines = f.readlines()

# Lines to completely remove (0-indexed): 541,542,544,552,553,555,556,557,558,590,591,592,593,594,595,596
# Lines to modify: 578,582,587,597
remove_lines = {541, 542, 543, 544, 552, 553, 554, 555, 556, 557, 558, 590, 591, 592, 593, 594, 595, 596}

new_lines = []
for i, line in enumerate(lines):
    if i in remove_lines:
        print(f"REMOVED line {i+1}: {repr(line.strip()[:80])}", flush=True)
        continue

    decoded = line.decode('utf-8', errors='replace')

    # Fix tryGps: remove showManual() call when not HTTPS - just show status message
    if i == 578:  # "if(location.protocol!=='https:'){showManual();"
        new_line = b'            + \"  if(location.protocol!==\\'https:\\'){document.getElementById(\\'status\\').innerHTML=\\'\\\\u26A0\\\\uFE0F GPS n\\\\u00e9cessite HTTPS\\';return;}\"\r\r\r\r\n'
        new_lines.append(new_line)
        print(f"MODIFIED line {i+1}", flush=True)
        continue

    # Fix: if no geolocation - just show message, no showManual
    if i == 582:  # "if(!navigator.geolocation){showManual();return;}"
        new_line = b'            + \"  if(!navigator.geolocation){document.getElementById(\\'status\\').innerHTML=\\'GPS non disponible\\';return;}\"\r\r\r\r\n'
        new_lines.append(new_line)
        print(f"MODIFIED line {i+1}", flush=True)
        continue

    # Fix: GPS refused - just show message
    if i == 587:  # "showManual();"
        new_line = b'            + \"      document.getElementById(\\'status\\').innerHTML=\\'\\\\u26A0\\\\uFE0F GPS refus\\\\u00e9\\';}\"\r\r\r\r\n'
        new_lines.append(new_line)
        print(f"MODIFIED line {i+1}", flush=True)
        continue

    # Fix last line: remove else{showManual()...} branch
    if i == 597:  # "if(location.protocol==='https:'){tryGps();}else{showManual();"
        new_line = b'            + \"if(location.protocol===\\'https:\\'){tryGps();}else{document.getElementById(\\'status\\').innerHTML=\\'\\\\u26A0\\\\uFE0F Connexion HTTP \\\\u2014 GPS indisponible\\';}}\"\r\r\r\r\n'
        new_lines.append(new_line)
        print(f"MODIFIED line {i+1}", flush=True)
        continue

    new_lines.append(line)

with open(path, 'wb') as f:
    f.writelines(new_lines)

print(f"DONE. Lines: {len(lines)} -> {len(new_lines)}", flush=True)
