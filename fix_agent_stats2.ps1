$path = 'c:\Users\bmd tech\Desktop\sc\SmartCity\src\main\java\com\smartcity\service\UtilisateurService.java'
$c = [System.IO.File]::ReadAllText($path)

$old = 'result.add(new AgentStats(' + "`n" + '                        rs.getInt("idUser"),' + "`n" + '                        rs.getString("nom"),' + "`n" + '                        rs.getString("nomZone") != null ? rs.getString("nomZone") : "Zone inconnue",' + "`n" + '                        rs.getInt("traites")' + "`n" + '                    ))'

$new = 'result.add(new AgentStats(' + "`n" + '                        rs.getInt("idUser"),' + "`n" + '                        rs.getString("nom"),' + "`n" + '                        rs.getString("email") != null ? rs.getString("email") : "",' + "`n" + '                        rs.getString("nomZone") != null ? rs.getString("nomZone") : "Zone inconnue",' + "`n" + '                        rs.getInt("traites")' + "`n" + '                    ))'

$result = $c.Replace($old, $new)
if ($result -eq $c) { Write-Host "ECHEC"; exit 1 }
[System.IO.File]::WriteAllBytes($path, [System.Text.Encoding]::UTF8.GetBytes($result))
Write-Host "OK"
