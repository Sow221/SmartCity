f = open('src/main/java/com/smartcity/controller/CitizenDashboardController.java', 'rb')
lines = f.readlines()
f.close()
print(f"Before: {len(lines)}", flush=True)

# Line 789 (index 788) is orphan showCitizenMessage outside any method - remove it
# Also remove the surrounding blank lines (787, 788 = indices 786, 787)
print(f"Line 787: {repr(lines[786].strip()[:80])}", flush=True)
print(f"Line 788: {repr(lines[787].strip()[:80])}", flush=True)
print(f"Line 789: {repr(lines[788].strip()[:80])}", flush=True)
print(f"Line 790: {repr(lines[789].strip()[:80])}", flush=True)

# Remove only line 789 (index 788) - the orphan statement
del lines[788]

print(f"After: {len(lines)}", flush=True)
print(f"New line 789: {repr(lines[788].strip()[:80])}", flush=True)

with open('src/main/java/com/smartcity/controller/CitizenDashboardController.java', 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
