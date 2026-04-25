$file = 'src\main\java\com\smartcity\controller\AgentDashboardController.java'
$bytes = [System.IO.File]::ReadAllBytes($file)
# Supprimer BOM si present
if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    $bytes = $bytes[3..($bytes.Length-1)]
}
$content = [System.Text.Encoding]::UTF8.GetString($bytes)

$fixes = 0

# Fix 1: profilStatMissionsLabel - ajouter @FXML
$old1 = '    private Label profilStatMissionsLabel;'
$new1 = '    @FXML private Label profilStatMissionsLabel;'
if ($content.Contains($old1)) { $content = $content.Replace($old1, $new1); $fixes++ }

# Fix 2: profilStatScoreLabel - ajouter @FXML
$old2 = '    private Label profilStatScoreLabel;'
$new2 = '    @FXML private Label profilStatScoreLabel;'
if ($content.Contains($old2)) { $content = $content.Replace($old2, $new2); $fixes++ }

# Fix 3: tourneeProchaineLabel - afficher zone + ID
$q = [char]34
$old3 = 'tourneeProchaineLabel.setText(' + $q + '#' + $q + ' + next.mission.getIdSignalement());'
$new3 = 'tourneeProchaineLabel.setText(next.mission.getZoneNom() + ' + $q + ' #' + $q + ' + next.mission.getIdSignalement());'
if ($content.Contains($old3)) { $content = $content.Replace($old3, $new3); $fixes++ }

# Fix 4: statusColorHex Termine sans accent
$old4 = 'if (' + $q + 'Termine' + $q + '.equalsIgnoreCase(statut))'
$new4 = 'if (com.smartcity.model.SignalementStatut.TERMINE.matches(statut))'
if ($content.Contains($old4)) { $content = $content.Replace($old4, $new4); $fixes++ }

# Fix 5: ComboBox filtres - valeurs tronquees
# Chercher le pattern avec ? (0x3F) pour Affecte et Termine
$old5a = $q + 'Tous' + $q + ', ' + $q + 'En attente' + $q + ', ' + $q + 'Affect' + [char]0x3F + $q + ', ' + $q + 'En cours' + $q + ', ' + $q + 'Termin' + [char]0x3F + $q
$new5 = $q + 'Tous' + $q + ', ' + $q + 'En attente' + $q + ', ' + $q + 'Affect' + [char]0xE9 + $q + ', ' + $q + 'En cours' + $q + ', ' + $q + 'Termin' + [char]0xE9 + $q
if ($content.Contains($old5a)) { $content = $content.Replace($old5a, $new5); $fixes++ }

# Ecrire sans BOM
$outBytes = [System.Text.Encoding]::UTF8.GetBytes($content)
[System.IO.File]::WriteAllBytes($file, $outBytes)
"Fixes applied: $fixes" | Out-File fix_result.txt
