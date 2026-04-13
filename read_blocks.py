import sys
f = open('src/main/java/com/smartcity/controller/AgentDashboardController.java', 'rb')
lines = f.readlines()
f.close()

sys.stdout.buffer.write(b"--- JSBridgeAgent (lines 2864-2900) ---\n")
for i in range(2863, 2900):
    sys.stdout.buffer.write(f"{i+1}: {lines[i].strip().decode('utf-8','replace')[:120]}\n".encode('utf-8'))

sys.stdout.buffer.write(b"\n--- circleMarker generation (lines 3260-3280) ---\n")
for i in range(3259, 3280):
    sys.stdout.buffer.write(f"{i+1}: {lines[i].strip().decode('utf-8','replace')[:120]}\n".encode('utf-8'))

sys.stdout.buffer.flush()
