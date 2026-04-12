filepath = r'C:/Users/MS/Desktop/SC/SmartCity/src/main/java/com/smartcity/controller/AgentDashboardController.java'
with open(filepath, 'rb') as f:
    data = f.read()
if data[:3] == b'\xef\xbb\xbf':
    data = data[3:]
with open(filepath, 'wb') as f:
    f.write(data)
print('done size=' + str(len(data)) + ' bom=' + str(data[:3] == b'\xef\xbb\xbf'))
