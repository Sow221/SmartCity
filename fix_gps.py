filepath = r'C:/Users/MS/Desktop/SC/SmartCity/src/main/java/com/smartcity/service/GpsApiServer.java'
with open(filepath, 'rb') as f:
    content = f.read().decode('utf-8', errors='replace')

if 'import java.security.SecureRandom' not in content:
    content = content.replace(
        'import java.security.KeyStore;',
        'import java.security.KeyStore;\nimport java.security.SecureRandom;'
    )
    with open(filepath, 'wb') as f:
        f.write(content.encode('utf-8'))
    print('Added SecureRandom import')
else:
    print('Already present')
