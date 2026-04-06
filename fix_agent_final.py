# -*- coding: utf-8 -*-
import sys
sys.stdout.reconfigure(encoding='utf-8')

f = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
c = open(f, encoding='utf-8').read()
print('Initial size:', len(c))

# Corrections essentielles
changes = 0

# 1. Imports
if 'import com.smartcity.service.AffectationService;' not in c:
    c = c.replace('import com.smartcity.service.GeolocationService;',
                  'import com.smartcity.service.AffectationService;\nimport com.smartcity.service.GeolocationService;')
    changes += 1

if 'import javafx.collections.transformation.FilteredList;' not in c:
    c = c.replace('import javafx.collections.ObservableList;',
                  'import javafx.collections.ObservableList;\nimport javafx.collections.transformation.FilteredList;')
    changes += 1

if 'import javafx.scene.chart.PieChart;' not in c:
    c = c.replace('import javafx.scene.layout.BorderPane;',
                  'import javafx.scene.chart.PieChart;\nimport javafx.scene.layout.BorderPane;')
    changes += 1

# 2. Statut Collecte -> Terminé
before = c.count('"Collecte"')
c = c.replace('"Collecte"', '"Termin\u00e9"')
changes += before

# 3. AffectationService field
if 'affectationService' not in c:
    c = c.replace(
        'private SignalementService signalementService = new SignalementService();',
        'private SignalementService signalementService = new SignalementService();\n    private AffectationService affectationService = new AffectationService();'
    )
    changes += 1

# 4. missionsZone + filteredMissions + bienvenuAffiche
if 'missionsZone' not in c:
    c = c.replace(
        'private ObservableList<Signalement> historique = FXCollections.observableArrayList();',
        'private ObservableList<Signalement> historique = FXCollections.observableArrayList();\n    private ObservableList<Signalement> missionsZone = FXCollections.observableArrayList();\n    private FilteredList<Signalement> filteredMissions;\n    private boolean bienvenuAffiche = false;'
    )
    changes += 1

# 5. isTermine simplifié
old_is = 'String s = statut.toLowerCase().trim();\n        return s.equals("collecte") || s.equals("termine") || s.equals("termin\u00e9") || s.startsWith("collect");'
if old_is in c:
    c = c.replace(old_is, 'return "Termin\u00e9".equalsIgnoreCase(statut);')
    changes += 1

# 6. getDisplayStatut simplifié
old_disp = 'if (statut.toLowerCase().startsWith("collect") || "Termin\u00e9".equalsIgnoreCase(statut)) return "Termin\u00e9";\n        return statut;'
if old_disp in c:
    c = c.replace(old_disp, 'return statut;')
    changes += 1

# 7. getAgentZone null check
c = c.replace(
    'return current != null ? zoneService.getZoneById(current.getIdZone()).getNomZone() : "Pikine";',
    'if (current == null) return "Pikine";\n        com.smartcity.model.Zone z = zoneService.getZoneById(current.getIdZone());\n        return z != null ? z.getNomZone() : "Pikine";'
)

# 8. refreshProfil null check
c = c.replace(
    'profilZoneField.setText(zoneService.getZoneById(current.getIdZone()).getNomZone());',
    'com.smartcity.model.Zone zp = zoneService.getZoneById(current.getIdZone());\n        profilZoneField.setText(zp != null ? zp.getNomZone() : "");'
)

# 9. zoneCenter utilise gpsService
c = c.replace(
    'if (zone != null && zone.equalsIgnoreCase("Guediawaye")) return new Point(14.7765, -17.4047);\n        return new Point(14.7646, -17.3920);',
    'com.smartcity.service.RealTimeGPSService.Coordinates coords = gpsService.getZoneCenter(zone);\n        return new Point(coords.lat, coords.lon);'
)

# 10. chargerDonnees - utiliser getSignalementsByAgent
old_charge = 'List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());\n        missions.setAll(missionList);'
if old_charge in c:
    c = c.replace(old_charge,
        'List<Signalement> missionList = signalementService.getSignalementsByZone(current.getIdZone());\n'
        '        List<Signalement> mesMissions = signalementService.getSignalementsByAgent(current.getIdUser());\n'
        '        missions.setAll(mesMissions);\n'
        '        missionsZone.setAll(missionList);\n'
        '        if (!bienvenuAffiche) {\n'
        '            bienvenuAffiche = true;\n'
        '            long nb = mesMissions.stream().filter(s -> !"Termin\u00e9".equalsIgnoreCase(s.getStatut())).count();\n'
        '            showAgentMessage("Bonjour " + current.getNom() + " - " + nb + " mission(s) active(s)", true);\n'
        '        }'
    )
    changes += 1

# Sauvegarder
open(f, 'w', encoding='utf-8').write(c)
print('Changes applied:', changes)
print('Final size:', len(c))
print('package OK:', c.startswith('package com.smartcity'))
print('Collecte remaining:', '"Collecte"' in c)
print('affectationService:', 'affectationService' in c)
print('missionsZone:', 'missionsZone' in c)
print('isTermine simplified:', 'equalsIgnoreCase(statut)' in c)
