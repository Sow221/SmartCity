#!/usr/bin/env python3
import os
import re

def surgical_fix(filepath):
    """Apply targeted fixes for encoding issues"""
    try:
        # Read file
        with open(filepath, 'rb') as f:
            content = f.read()
        
        # Remove BOM if present
        if content.startswith(b'\xef\xbb\xbf'):
            content = content[3:]
        
        # Decode with UTF-8
        try:
            text = content.decode('utf-8')
        except:
            text = content.decode('latin-1', errors='replace')
        
        # Fix package declaration if needed
        text = re.sub(r'^ackage ', 'package ', text, flags=re.MULTILINE)
        
        # Fix specific UTF-8 corruption patterns ONLY (don't touch all newlines)
        text = re.sub(r'Ã©', 'é', text)
        text = re.sub(r'Ã ', 'à', text)
        text = re.sub(r'Ãª', 'ê', text)
        text = re.sub(r'Ã¨', 'è', text)
        text = re.sub(r'Ã§', 'ç', text)
        text = re.sub(r'Ã´', 'ô', text)
        
        # Remove only problematic control characters that break strings
        # But be very conservative - only remove actual garbage
        text = re.sub(r'[\x00-\x08\x0b-\x0c\x0e-\x1f\x7f-\x9f]+', '', text)
        
        # Remove BOM character if it exists in text
        text = text.replace('\ufeff', '')
        
        # Write back
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(text)
        
        print(f"✓ Surgically fixed: {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    files = [
        'src/main/java/com/smartcity/controller/CitizenDashboardController.java',
        'src/main/java/com/smartcity/controller/AgentDashboardController.java',
        'src/main/java/com/smartcity/service/ZoneService.java',
        'src/main/java/com/smartcity/service/RealTimeGPSService.java',
        'src/main/java/com/smartcity/service/GpsApiServer.java'
    ]
    
    for f in files:
        if os.path.exists(f):
            surgical_fix(f)
        else:
            print(f"⚠ File not found: {f}")
