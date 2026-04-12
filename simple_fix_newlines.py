#!/usr/bin/env python3

def fix_simple(filepath):
    """Direct fix: remove embedded newlines between quotes"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            text = f.read()
        
        original_len = len(text)
        
        # Read file in binary to handle issues precisely
        with open(filepath, 'rb') as f:
            data = f.read()
        
        # Look for pattern: " + [garbage char] + \n within a line
        # Replace with just " "
        # Using hex codes for non-ASCII
        data = data.replace(b'"\xc3\xa2\n', b'" ')  # â
        data = data.replace(b'"\xc3\xb0\n', b'" ')  # ð (using UTF-8 encoding)
        data = data.replace(b'"\xe2\x9c\x93\n', b'" ')  # ✓ (using UTF-8 encoding)
        
        with open(filepath, 'wb') as f:
            f.write(data)
        
        print(f"✓ Fixed {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

if __name__ == '__main__':
    fix_simple('src/main/java/com/smartcity/controller/CitizenDashboardController.java')
    fix_simple('src/main/java/com/smartcity/controller/AgentDashboardController.java')
