#!/usr/bin/env python3
"""Fix missionPoint() and zoneCenter() methods in AgentDashboardController"""

import re

file_path = r"src/main/java/com/smartcity/controller/AgentDashboardController.java"

# Read file
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix 1: Replace missionPoint() method
old_mission_point = r"""private Point missionPoint\(Signalement mission, int index\) \{
        Point base = zoneCenter\(mission\.getZoneNom\(\)\);
        double latOffset = \(index % 5\) \* 0\.004 \+ 0\.001;
        double lonOffset = \(\(index / 5\) % 5\) \* 0\.004 \+ 0\.001;
        return new Point\(base\.lat \+ latOffset, base\.lon \+ lonOffset\);
    \}"""

new_mission_point = """private Point missionPoint(Signalement mission, int index) {
        // 🎯 UTILISER D'ABORD LES VRAIES COORDONNÉES GPS SI DISPONIBLES
        if (mission.getLatitude() != 0.0 || mission.getLongitude() != 0.0) {
            return new Point(mission.getLatitude(), mission.getLongitude());
        }
        
        // SINON: Placer autour du centre de zone avec offset pour éviter superposition
        Point base = zoneCenter(mission.getZoneNom());
        double latOffset = (index % 5) * 0.004 + 0.001;  // ±0.004° = ~400m
        double lonOffset = ((index / 5) % 5) * 0.004 + 0.001;
        return new Point(base.lat + latOffset, base.lon + lonOffset);
    }"""

# Fix 2: Replace zoneCenter() method
old_zone_center = r"""private Point zoneCenter\(String zone\) \{
        if \(zone != null && zone\.equalsIgnoreCase\("Guediawaye"\)\)
            return new Point\(14\.7765, -17\.4047\);

        GeolocationService\.Coordinates c = zoneService\.getCenter\(zone\);
        return new Point\(c\.lat, c\.lon\);
    \}"""

new_zone_center = """private Point zoneCenter(String zone) {
        // ✅ DÉLÉGUER ENTIÈREMENT AU SERVICE (Pas de hardcode)
        GeolocationService.Coordinates c = zoneService.getCenter(zone);
        return new Point(c.lat, c.lon);
    }"""

# Apply fixes using simple string search (more robust)
if "private Point missionPoint(Signalement mission, int index)" in content:
    # Find the method body and replace it
    start_idx = content.find("private Point missionPoint(Signalement mission, int index)")
    if start_idx != -1:
        # Find the closing brace
        brace_count = 0
        start_body = content.find("{", start_idx)
        end_idx = start_body
        for i, char in enumerate(content[start_body:]):
            if char == "{":
                brace_count += 1
            elif char == "}":
                brace_count -= 1
                if brace_count == 0:
                    end_idx = start_body + i + 1
                    break
        
        content = content[:start_idx] + new_mission_point + content[end_idx:]
        print("✅ missionPoint() fixed")

if "private Point zoneCenter(String zone)" in content:
    start_idx = content.find("private Point zoneCenter(String zone)")
    if start_idx != -1:
        brace_count = 0
        start_body = content.find("{", start_idx)
        end_idx = start_body
        for i, char in enumerate(content[start_body:]):
            if char == "{":
                brace_count += 1
            elif char == "}":
                brace_count -= 1
                if brace_count == 0:
                    end_idx = start_body + i + 1
                    break
        
        content = content[:start_idx] + new_zone_center + content[end_idx:]
        print("✅ zoneCenter() fixed")

# Write file
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"✅ File {file_path} updated successfully!")
