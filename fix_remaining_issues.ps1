# Fix #2: Add table refresh in updateLiveDistances()
$agentFile = "c:\Users\bmd tech\Desktop\sc\SmartCity\src\main\java\com\smartcity\controller\AgentDashboardController.java"
$content = Get-Content $agentFile -Raw

# FIX #2: Add tableMesMissions.refresh() after refreshTourneeSummary()
$pattern = 'refreshTourneeSummary\(\);(\s*})'
$replacement = 'refreshTourneeSummary();`r`n            if (tableMesMissions != null) tableMesMissions.refresh(); // ✅ Maj distances en temps-réel$1'
$content = [regex]::Replace($content, $pattern, $replacement)

# FIX #5: Preserve filter on chargerDonnees() 
# Find missions.setAll(missionList); and add filter preservation logic
$pattern2 = 'missions\.setAll\(missionList\);'
$before = 'String currentFilter = (filterMissionsStatutCombo != null) ? filterMissionsStatutCombo.getValue() : "Tous";`r`n            missions.setAll(missionList);'
$content = $content -replace $pattern2, $before

# Add filter reapplication AFTER missionsUrgentes.setAll()
$pattern3 = 'missionsUrgentes\.setAll\(missionList\.stream.*?\);'
$after = '$& `r`n            // ✅ Réappliquer le filtre après rechargement`r`n            if ("Tous" -ne currentFilter && filterMissionsStatutCombo != null) { filterMissionsStatutCombo.setValue(currentFilter); }'
$content = [regex]::Replace($content, $pattern3, $after, [System.Text.RegularExpressions.RegexOptions]::Singleline)

Set-Content -Path $agentFile -Value $content -Encoding UTF8

Write-Host "✅ Fixes appliquées!"  -ForegroundColor Green
