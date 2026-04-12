import sys

filepath = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'

with open(filepath, 'rb') as f:
    d = f.read()

print('File size:', len(d), file=sys.stderr)

# Bytes helper
def b(*args):
    return bytes(list(args))

# checkmark emoji (double-encoded UTF-8): C3 83 C2 A2 C3 82 C2 9C
CHK = b(0xc3,0x83,0xc2,0xa2,0xc3,0x82,0xc2,0x9c)
# star emoji: C3 83 C2 A2 C3 82 C2 98
STAR = b(0xc3,0x83,0xc2,0xa2,0xc3,0x82,0xc2,0x98)
# e accent: C3 83 C2 83 C3 82 C2 A9
EA = b(0xc3,0x83,0xc2,0x83,0xc3,0x82,0xc2,0xa9)
# E accent (capital): C3 83 C2 83 C3 82 C2 89
ECA = b(0xc3,0x83,0xc2,0x83,0xc3,0x82,0xc2,0x89)
CRLF = b(0x0d, 0x0a)

fixes = [
    # Fix 1 line 455: "checkmark\r\n Photo selectionnee: "
    (
        b'"' + CHK + CRLF + b' Photo s' + EA + b'lectionn' + EA + b'e: "',
        b'"\\u2705 Photo s\\u00e9lectionn\\u00e9e: "'
    ),
    # Fix 2 line 632: "checkmark\r\n Signalement supprime."
    (
        b'"' + CHK + CRLF + b' Signalement supprim' + EA + b'."',
        b'"\\u2705 Signalement supprim\\u00e9."'
    ),
    # Fix 3 line ~1073: positionLabel "checkmark\r\n %.5f, %.5f"
    (
        b'"' + CHK + CRLF + b' %.5f, %.5f"',
        b'"\\u2705 %.5f, %.5f"'
    ),
    # Fix 4 line ~1646: heuresResolution + "h checkmark\r\n"
    (
        b'"h ' + CHK + CRLF + b'"',
        b'"h \\u2705"'
    ),
    # Fix 5 line ~1646 (colEval title): "star\r\n Evaluer"
    (
        b'"' + STAR + CRLF + b' ' + ECA + b'valuer"',
        b'"\\u2605 \\u00c9valuer"'
    ),
    # Fix 6 line ~1646 (btn label): same pattern
    (
        b'"' + STAR + CRLF + b' ' + ECA + b'valuer"',
        b'"\\u2605 \\u00c9valuer"'
    ),
    # Fix 7 line ~1931 (dialog title): "star\r\n Evaluer la collecte"
    (
        b'"' + STAR + CRLF + b' ' + ECA + b'valuer la collecte"',
        b'"\\u2605 \\u00c9valuer la collecte"'
    ),
    # Fix 8 lblValeur: "star\r\nstar\r\nstar\r\n"
    (
        b'"' + STAR + CRLF + STAR + CRLF + STAR + CRLF + b'"',
        b'"\\u2605\\u2605\\u2605"'
    ),
    # Fix 9 slider repeat: "star\r\n".repeat(...)
    (
        b'"' + STAR + CRLF + b'"',
        b'"\\u2605"'
    ),
    # Fix 10 line ~1646: "checkmark\r\n Merci pour votre evaluation !"
    (
        b'"' + CHK + CRLF + b' Merci pour votre ' + EA + b'valuation !"',
        b'"\\u2705 Merci pour votre \\u00e9valuation !"'
    ),
    # Fix 11 line ~1931: "checkmark\r\n Lien GPS copie : "
    (
        b'"' + CHK + CRLF + b' Lien GPS copi' + EA + b' : "',
        b'"\\u2705 Lien GPS copi\\u00e9 : "'
    ),
]

for old, new in fixes:
    count = d.count(old)
    print(f'  {repr(old[:20])}... found {count}x', file=sys.stderr)
    d = d.replace(old, new)

# Verify no more CRLF inside string literals at the known problem spots
remaining = []
for pattern in [b'\r\n Photo s', b'\r\n Signalement supprim', b'\r\n %.5f', b'\r\n Merci', b'\r\n Lien GPS']:
    if pattern in d:
        remaining.append(pattern)
        print(f'STILL PRESENT: {repr(pattern)}', file=sys.stderr)

with open(filepath, 'wb') as f:
    f.write(d)

print('Done. Remaining issues:', len(remaining), file=sys.stderr)
