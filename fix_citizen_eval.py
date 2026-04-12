path = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}", flush=True)

# Fix evaluation column/button/dialog titles (lines 1635, 1643, 1767)
fixes = {
    1634: b' TableColumn<Signalement, Void> colEval = new TableColumn<>("\xc3\x89valuer");\r\n',
    1642: b'            private final Button btn = new Button("\xc3\x89valuer");\r\n',
    1766: b'        dialog.setTitle("\xc3\x89valuer la collecte");\r\n',
}

# Also scan for star rating lines and positionLabel
for i, line in enumerate(lines):
    decoded = line.decode('utf-8', errors='replace')
    if 'lblValeur' in decoded or 'positionLabel.setText' in decoded:
        print(f"Line {i+1}: {repr(line.strip()[:120])}", flush=True)

for idx, newline in fixes.items():
    old = lines[idx]
    lines[idx] = newline
    print(f"Fixed line {idx+1}: {repr(old.strip()[:60])} -> {repr(newline.strip()[:60])}", flush=True)

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
