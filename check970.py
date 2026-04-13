f = open('src/main/java/com/smartcity/controller/CitizenDashboardController.java', 'rb')
lines = f.readlines()
f.close()
print(f"Total: {len(lines)}", flush=True)
for i in range(955, 985):
    print(f"{i+1}: {repr(lines[i].strip()[:130])}", flush=True)
