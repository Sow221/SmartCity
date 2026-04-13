import sys

# ── Fix CitizenDashboardController: remove wrongly inserted block ──
path_citizen = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'
with open(path_citizen, 'rb') as f:
    lines = f.readlines()

print(f"Citizen lines: {len(lines)}", flush=True)

# Find the inserted block - look for filterMissionsStatutCombo
start_remove = None
end_remove = None
for i, line in enumerate(lines):
    if b'filterMissionsStatutCombo' in line and start_remove is None:
        # Go back to find the blank line before it
        start_remove = i
        print(f"Found filterMissionsStatutCombo at line {i+1}", flush=True)
    if start_remove is not None and end_remove is None:
        if b'missions.addListener' in line:
            # Find closing brace of this block
            for j in range(i, min(i+25, len(lines))):
                if b'        }' in lines[j] and j > i+5:
                    end_remove = j
                    print(f"Block ends at line {j+1}", flush=True)
                    break
            break

if start_remove is not None and end_remove is not None:
    print(f"Removing lines {start_remove+1} to {end_remove+1}", flush=True)
    for i in range(start_remove, end_remove+1):
        print(f"  REMOVE {i+1}: {repr(lines[i].strip()[:80])}", flush=True)
    del lines[start_remove:end_remove+1]
    with open(path_citizen, 'wb') as f:
        f.writelines(lines)
    print(f"Citizen fixed. New lines: {len(lines)}", flush=True)
else:
    print(f"Block not found: start={start_remove}, end={end_remove}", flush=True)
    # Show context around line 789
    for i in range(780, 800):
        print(f"{i+1}: {repr(lines[i].strip()[:100])}", flush=True)

# ── Verify AgentDashboardController has the listener ──
path_agent = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
with open(path_agent, 'rb') as f:
    agent_lines = f.readlines()

print(f"\nAgent lines: {len(agent_lines)}", flush=True)
for i, line in enumerate(agent_lines):
    if b'filterMissionsStatutCombo' in line:
        print(f"Agent {i+1}: {repr(line.strip()[:120])}", flush=True)
