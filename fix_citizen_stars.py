path = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

fixes = {
    # line 1229: positionLabel checkmark (c3 a2 = â corrupted, should be ✓ or plain text)
    1228: b'                positionLabel.setText(String.format("\xe2\x9c\x93 %.5f, %.5f", lat, lon));\r\n',
    # line 1791: lblValeur stars (c3 a2 = â corrupted, should be ★)
    1790: b'        Label lblValeur = new Label("\xe2\x98\x85 \xe2\x98\x85 \xe2\x98\x85");\r\n',
    # line 1795: star repeat
    1794: b'        slider.valueProperty().addListener((obs, o, n) ->\r\n'
          b'            lblValeur.setText("\xe2\x98\x85".repeat(n.intValue()) + "\xe2\x98\x86".repeat(5 - n.intValue())));\r\n',
}

# line 1795 is a single line, handle separately
with open(path, 'rb') as f:
    lines = f.readlines()

lines[1228] = b'                positionLabel.setText(String.format("\xe2\x9c\x93 %.5f, %.5f", lat, lon));\r\n'
lines[1790] = b'        Label lblValeur = new Label("\xe2\x98\x85 \xe2\x98\x85 \xe2\x98\x85");\r\n'
lines[1794] = b'        slider.valueProperty().addListener((obs, o, n) ->\r\n'
lines[1795] = b'            lblValeur.setText("\xe2\x98\x85".repeat(n.intValue()) + "\xe2\x98\x86".repeat(5 - n.intValue())));\r\n'

# Also fix colEval header (line 1635 - check if it was fixed)
print("Line 1635:", repr(lines[1634].strip()[:80]), flush=True)
print("Line 1229:", repr(lines[1228].strip()[:80]), flush=True)
print("Line 1791:", repr(lines[1790].strip()[:80]), flush=True)
print("Line 1795:", repr(lines[1794].strip()[:80]), flush=True)
print("Line 1796:", repr(lines[1795].strip()[:80]), flush=True)

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
