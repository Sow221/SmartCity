#!/usr/bin/env python3
import re

def fix_string_newlines(filepath):
    """Fix unclosed strings by removing newlines within string literals"""
    try:
        with open(filepath, 'rb') as f:
            text = f.read().decode('utf-8')
        
        # Pattern to find strings with embedded newlines (the corruption pattern)
        # This matches: opening quote, optional whitespace, newline(s), content, optional newline(s), closing quote
        # We want to collapse these into: opening quote, spaces to preserve formatting, content, closing quote
        
        # Fix pattern: "\\n\\n string content" -> "string content"
        text = re.sub(r'"(\s*\n\s*)+', '"', text)
        
        # Also handle cases like: "text\\n\\n more text" -> "text more text"
        text = re.sub(r'(\n\s*)+', ' ', text)
        
        # But don't corrupt the code - restore normal newlines outside strings
        # This is tricky, so let's just clean up the most obvious broken strings
        lines = text.split('\n')
        fixed_lines = []
        in_string = False
        current_line = ''
        
        for line in lines:
            # Check for unclosed quotes
            quote_count = line.count('"') - line.count('\\"')
            if quote_count % 2 == 1:  # Odd number of quotes
                in_string = not in_string
            
            if in_string and line.strip() == '':
                # Skip empty lines inside strings
                continue
            elif in_string and line.strip() and current_line:
                # Continue the previous string
                current_line += ' ' + line.strip()
            elif in_string and line.strip() and not current_line:
                # Start of a string continuation
                current_line = line
            else:
                if current_line:
                    fixed_lines.append(current_line)
                    current_line = ''
                fixed_lines.append(line)
        
        if current_line:
            fixed_lines.append(current_line)
        
        fixed_text = '\n'.join(fixed_lines)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(fixed_text)
        
        print(f"✓ Fixed strings in: {filepath}")
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
    ]
    
    for f in files:
        fix_string_newlines(f)
