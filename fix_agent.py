filepath = r'C:/Users/MS/Desktop/SC/SmartCity/src/main/java/com/smartcity/controller/AgentDashboardController.java'

with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
    lines = f.readlines()

print('Total lines:', len(lines))

# Find the duplicate getSmartGpsMapJs and getWebSocketClientJs methods
# They are at lines 3428-3482 (1-indexed), i.e. indices 3427-3481
# Verify:
print('Line 3428:', repr(lines[3427].rstrip()[:60]))
print('Line 3482:', repr(lines[3481].rstrip()[:60]))
print('Line 3483:', repr(lines[3482].rstrip()[:60]))
print('Line 3489:', repr(lines[3488].rstrip()[:60]))

# Remove lines 3427..3484 (the two duplicate methods + blank lines after)
# Keep everything before 3427 and from 3485 onward
# Find exact end: after line 3482 (closing }) there are blank lines until indexOfMission
# Lines 3483-3488 are blank, line 3489 starts indexOfMission
# So remove indices 3427..3487 (lines 3428..3488)

new_lines = lines[:3427] + lines[3488:]
print('New total lines:', len(new_lines))

with open(filepath, 'wb') as f:
    for line in new_lines:
        f.write(line.encode('utf-8') if isinstance(line, str) else line)

print('DONE')
