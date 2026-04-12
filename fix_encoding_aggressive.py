#!/usr/bin/env python3
import re
import sys

def clean_file_aggressive(filepath):
    """Clean encoding corruption in Java files - aggressive version"""
    try:
        with open(filepath, 'rb') as f:
            content = f.read()
        
        # Remove BOM if present
        if content.startswith(b'\xef\xbb\xbf'):
            content = content[3:]
        
        # Try to decode as UTF-8 first
        try:
            text = content.decode('utf-8')
        except:
            # Fallback to latin1
            text = content.decode('latin-1', errors='replace')
        
        original_length = len(text)
        
        # Fix BOM character in text
        text = text.replace('\ufeff', '')
        
        # Fix broken strings - these cause unclosed string literal errors
        # Replace any ✓ followed by newline/escape sequences
        text = re.sub(r'✓[\x80-\xff]+', '', text)  # Remove checkmarks with binary corruption
        text = re.sub(r'\\u2713[\x80-\xff]+', '', text)  # Remove unicode checkmarks with corruption
        
        # Replace common corruption patterns
        text = re.sub(r'Ã©', 'é', text)  # Ã© -> é
        text = re.sub(r'Ã ', 'à', text)  # Ã  -> à
        text = re.sub(r'Ãª', 'ê', text)  # ªue -> ê
        text = re.sub(r'Ã¨', 'è', text)  # Ã¨ -> è
        text = re.sub(r'Ã§', 'ç', text)  # Ã§ -> ç
        text = re.sub(r'Ã´', 'ô', text)  # Ã´ -> ô
        text = re.sub(r'Ã¹', 'ù', text)  # Ã¹ -> ù
        text = re.sub(r'Û\x08', 'ü', text)  # Other corrupt sequences
        
        # Fix stray control characters that break strings
        text = re.sub(r'[\x80-\x9f]+', '', text)  # Remove control chars in 0x80-0x9f range
        
        # Replace problematic emoji/symbols with ASCII equivalents inside strings
        # These were causing illegal character errors
        text = text.replace('\u2713', '')  # Remove checkmark that was causing issues
        text = text.replace('ð', '')  # Remove corrupted symbols
        text = text.replace('ð', '[MAGNIFIER]')  # Keep the intent but as text
        
        # Write back with UTF-8 encoding
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(text)
        
        print(f"✓ Fixed: {filepath} ({original_length} -> {len(text)} chars)")
        return True
    except Exception as e:
        print(f"✗ Error processing {filepath}: {e}")
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
        try:
            clean_file_aggressive(f)
        except Exception as e:
            print(f"Failed to process {f}: {e}")
