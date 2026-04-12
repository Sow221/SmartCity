filepath = r'C:/Users/MS/Desktop/SC/SmartCity/src/main/java/com/smartcity/service/GpsApiServer.java'

with open(filepath, 'rb') as f:
    content = f.read().decode('utf-8', errors='replace')

# 1. Remove all sun.security.x509 imports
import re
content = re.sub(r'import sun\.security\.x509\.[^;]+;\r?\r?\r?\r?\n', '', content)

# 2. Remove unused imports that came with sun.security usage
# BigInteger, Date are still used - keep them
# Remove java.math.BigInteger only if not used elsewhere - actually it IS used, keep it

# 3. Replace createSelfSignedKeyStore method
old_method = '''    private KeyStore createSelfSignedKeyStore() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X500Name owner = new X500Name("CN=SmartCity GPS API, OU=SmartCity, O=SmartCity, L=Local, ST=None, C=FR");
        Date from = new Date(System.currentTimeMillis() - 3600_000L);
        Date to = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger serial = new BigInteger(64, new SecureRandom());

        X509CertInfo info = new X509CertInfo();
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serial));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get("SHA256withRSA")));

        CertificateExtensions extensions = new CertificateExtensions();
        GeneralNames gns = new GeneralNames();
        gns.add(new GeneralName(new DNSName("localhost")));
        gns.add(new GeneralName(new IPAddressName("127.0.0.1")));
        String localIp = getLocalIp();
        if (!"localhost".equals(localIp) && !"127.0.0.1".equals(localIp)) {
            gns.add(new GeneralName(new IPAddressName(localIp)));
        }
        extensions.set(SubjectAlternativeNameExtension.NAME, new SubjectAlternativeNameExtension(gns));
        extensions.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(false, -1));
        info.set(X509CertInfo.EXTENSIONS, extensions);

        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(keyPair.getPrivate(), "SHA256withRSA");
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM,
                cert.get(X509CertImpl.SIG_ALG));
        cert = new X509CertImpl(info);
        cert.sign(keyPair.getPrivate(), "SHA256withRSA");

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setKeyEntry("gps", keyPair.getPrivate(), password, new java.security.cert.Certificate[]{cert});
        return ks;
    }'''

new_method = '''    private KeyStore createSelfSignedKeyStore() throws Exception {
        char[] password = "changeit".toCharArray();
        java.io.File ksFile = java.io.File.createTempFile("smartcity-gps", ".p12");
        ksFile.deleteOnExit();
        ProcessBuilder pb = new ProcessBuilder(
            "keytool", "-genkeypair",
            "-alias", "gps",
            "-keyalg", "RSA", "-keysize", "2048",
            "-validity", "365",
            "-dname", "CN=SmartCity GPS API, OU=SmartCity, O=SmartCity, L=Local, ST=None, C=FR",
            "-storetype", "PKCS12",
            "-keystore", ksFile.getAbsolutePath(),
            "-storepass", "changeit",
            "-keypass", "changeit",
            "-noprompt"
        );
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.waitFor();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(ksFile)) {
            ks.load(fis, password);
        }
        return ks;
    }'''

# Normalize line endings for matching (the file has \r\r\r\r\n)
# Try to find and replace with flexible whitespace matching
if 'createSelfSignedKeyStore' in content:
    # Find the method start and end
    start_marker = '    private KeyStore createSelfSignedKeyStore() throws Exception {'
    end_marker = '        return ks;\n    }'
    
    start_idx = content.find(start_marker)
    if start_idx == -1:
        # Try with \r\r\r\r\n
        start_marker2 = '    private KeyStore createSelfSignedKeyStore() throws Exception {\r\r\r\r\n'
        start_idx = content.find('    private KeyStore createSelfSignedKeyStore()')
    
    print('start_idx:', start_idx)
    
    if start_idx != -1:
        # Find the closing } of this method
        # Look for the pattern: return ks;\n    }
        search_from = start_idx
        end_patterns = ['        return ks;\r\r\r\r\n    }\r\r\r\r\n', '        return ks;\n    }\n', '        return ks;\r\n    }\r\n']
        end_idx = -1
        for ep in end_patterns:
            idx = content.find(ep, search_from)
            if idx != -1:
                end_idx = idx + len(ep)
                print('Found end with pattern:', repr(ep[:20]))
                break
        
        print('end_idx:', end_idx)
        
        if end_idx != -1:
            content = content[:start_idx] + new_method + '\n' + content[end_idx:]
            print('Method replaced successfully')
        else:
            print('ERROR: could not find end of method')
    else:
        print('ERROR: could not find start of method')
else:
    print('ERROR: createSelfSignedKeyStore not found in file')

with open(filepath, 'wb') as f:
    f.write(content.encode('utf-8'))

print('Done, size:', len(content))
