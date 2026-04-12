#!/usr/bin/env python3
import re
import sys

def clean_file(filepath):
    """Clean encoding corruption in Java files"""
    try:
        with open(filepath, 'rb') as f:
            content = f.read()
        
        # Try to decode as UTF-8 first
        try:
            text = content.decode('utf-8')
        except:
            # Fallback to latin1
            text = content.decode('latin-1')
        
        original_length = len(text)
        
        # Fix broken strings - replace "â\n" with proper characters
        # "â\n" appears to be corrupted within strings, causing them to break across lines
        text = text.replace('â\n', ' ')  # Remove broken strings
        
        # Replace other common corruption patterns
        text = re.sub(r'Ã©', 'é', text)  # Ã© -> é
        text = re.sub(r'Ã ', 'à', text)  # Ã  -> à
        text = re.sub(r'Ãª', 'ê', text)  # ªue -> ê
        text = re.sub(r'Ã¨', 'è', text)  # Ã¨ -> è
        text = re.sub(r'Ã§', 'ç', text)  # Ã§ -> ç
        text = re.sub(r'Ã´', 'ô', text)  # Ã´ -> ô
        text = re.sub(r'ð', '🔍', text)  # ð -> 🔍 (search icon)
        text = re.sub(r'â', '✓', text)   # â -> ✓ (checkmark, where it makes sense)
        
        # Write back with UTF-8 encoding
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(text)
        
        print(f"✓ Fixed: {filepath} ({original_length} -> {len(text)} chars)")
        return True
    except Exception as e:
        print(f"✗ Error processing {filepath}: {e}")
        return False

if __name__ == '__main__':
    files = [
        'src/main/java/com/smartcity/controller/CitizenDashboardController.java',
        'src/main/java/com/smartcity/service/ZoneService.java',
        'src/main/java/com/smartcity/service/RealTimeGPSService.java',
        'src/main/java/com/smartcity/service/GpsApiServer.java'
    ]
    
    for f in files:
        clean_file(f)
