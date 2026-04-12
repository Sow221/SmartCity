path = 'src/main/java/com/smartcity/controller/CitizenDashboardController.java'

with open(path, 'rb') as f:
    lines = f.readlines()

# Lines 1793 and 1794 are the duplicate - remove them (0-indexed: 1792, 1793)
# Line 1792 = b'        slider.valueProperty().addListener((obs, o, n) ->\r\n'  (duplicate)
# Line 1793 = b'\r\n'  (empty line inserted by mistake)
print("Before fix:")
for i in range(1790, 1800):
    print(str(i+1)+': '+repr(lines[i]))

del lines[1792]  # remove duplicate slider line
del lines[1792]  # remove the empty line that was after it (now at same index)

print("\nAfter fix:")
for i in range(1790, 1798):
    print(str(i+1)+': '+repr(lines[i]))

with open(path, 'wb') as f:
    f.writelines(lines)

print("DONE", flush=True)
