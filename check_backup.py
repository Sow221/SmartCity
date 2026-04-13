import os

# Check if there's a backup or original file
paths_to_check = [
    'CitizenDashboardController_orig.java',
    'original_agent.java',
]
for p in paths_to_check:
    if os.path.exists(p):
        print(f"Found: {p}", flush=True)

# Check git status
import subprocess
result = subprocess.run(['git', 'log', '--oneline', '-3'], capture_output=True, text=True)
print("Git log:", result.stdout, flush=True)
result2 = subprocess.run(['git', 'diff', '--name-only'], capture_output=True, text=True)
print("Modified files:", result2.stdout, flush=True)
