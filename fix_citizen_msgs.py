import sys

path = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}", flush=True)

fixes = {
    520: b'                showCitizenMessage("Nom de fichier invalide.", false);\r\n',
    528: b'            showCitizenMessage("Photo s\xc3\xa9lectionn\xc3\xa9e : " + fileName, true);\r\n',
    554: b'            showCitizenMessage("Description requise", false);\r\n',
    566: b'            showCitizenMessage("Veuillez choisir une cat\xc3\xa9gorie", false);\r\n',
    576: b'            showCitizenMessage("Veuillez choisir une zone", false);\r\n',
    606: b'                showCitizenMessage("Cliquez sur la carte ou scannez le QR code pour obtenir votre position", false);\r\n',
    630: b'            showCitizenMessage(validation.getMessage(), false);\r\n',
    702: b'            showCitizenMessage("Seuls les signalements en attente peuvent \xc3\xaatre supprim\xc3\xa9s.", false);\r\n',
    722: b'            showCitizenMessage("Signalement supprim\xc3\xa9.", true);\r\n',
    728: b'            showCitizenMessage("Echec de la suppression.", false);\r\n',
    788: b'        showCitizenMessage("Formulaire remis \xc3\xa0 z\xc3\xa9ro", true);\r\n',
    846: b'            showCitizenMessage("Cet email est d\xc3\xa9j\xc3\xa0 utilis\xc3\xa9 par un autre compte.", false);\r\n',
    962: b'            showCitizenMessage("Mot de passe mis \xc3\xa0 jour.", true);\r\n',
    966: b'            showCitizenMessage("Echec de la mise \xc3\xa0 jour du mot de passe.", false);\r\n',
    1820: b'                showCitizenMessage("Merci pour votre \xc3\xa9valuation !", true);\r\n',
    1826: b'                showCitizenMessage("Erreur lors de l\'envoi.", false);\r\n',
}

for idx, newline in fixes.items():
    old = lines[idx]
    lines[idx] = newline
    print(f"Fixed line {idx+1}: {repr(old.strip()[:60])} -> {repr(newline.strip()[:60])}", flush=True)

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
