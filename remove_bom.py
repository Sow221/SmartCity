#!/usr/bin/env python3
import os

def remove_bom_and_fix_file(filepath):
    """Remove BOM and fix basic corruption in Java files"""
    try:
        with open(filepath, 'rb') as f:
            content = f.read()
        
        # Remove BOM if present
        if content.startswith(b'\xef\xbb\xbf'):
            print(f"Removing BOM from {filepath}")
            content = content[3:]
        
        # Write back
        with open(filepath, 'wb') as f:
            f.write(content)
        
        print(f"✓ Fixed {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

if __name__ == '__main__':
    filepath = 'src/main/java/com/smartcity/controller/AgentDashboardController.java'
    remove_bom_and_fix_file(filepath)
