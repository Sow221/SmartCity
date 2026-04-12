#!/usr/bin/env python3
import re

def fix_embedded_newlines_in_strings(filepath):
    """Remove embedded newlines within string literals that cause compilation errors"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Find all string literals and replace embedded newlines/whitespace within them
        # Pattern: find quoted strings and normalize internal whitespace/newlines        
        def fix_string_content(match):
            full_match = match.group(0)
            # Don't touch raw strings or escaped quotes
            if full_match.startswith('"""') or full_match.startswith("'''"):
                return full_match
            
            # Remove embedded newlines and excessive whitespace, keeping at least one space if needed
            content = full_match[1:-1]  # Remove outer quotes
            content = re.sub(r'\n\s*', ' ', content)  # Replace newline+spaces with single space
            content = re.sub(r'\s{2,}', ' ', content)  # Replace multiple spaces with single space
            
            return '"' + content + '"'
        
        # Find and fix string literals while avoiding raw strings and special cases
        # Use a looser pattern to catch most strings
        before_len = len(content)
        content = re.sub(r'"(?:[^"\\]|\\.)*?"', fix_string_content, content, flags=re.MULTILINE | re.DOTALL)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Fixed {filepath} ({before_len} -> {len(content)} chars)")
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
    ]
    
    for f in files:
        fix_embedded_newlines_in_strings(f)
