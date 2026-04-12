#!/usr/bin/env python3
import re

def fix_broken_strings(filepath):
    """Fix strings with embedded newlines by replacing them with proper content"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        fixed_lines = []
        i = 0
        while i < len(lines):
            line = lines[i]
            
            # Look for strings with embedded newlines
            # Pattern: quote,  optional corruption char, newline
            if '"' in line and ('\n' in line or (i + 1 < len(lines) and lines[i+1].strip() == '')):
                # Check if this line has an unclosed string
                if line.rstrip().endswith(',') or '+' in line:
                    # This might have an embedded newline issue
                    # Try to find and fix these cases
                    fixed_line = line
                    
                    # Fix specific patterns
                    fixed_line = re.sub(r'"[\u2500-\u2600\u00a0-\u00ff]*\n\s*', '"', fixed_line)
                    
                    # Also handle cases like: "...\n..." -> "... ..."
                    if line.count('"') > 0 and '\n' in line:
                        # Reconstruct by joining next line if it's a continuation
                        if i + 1 < len(lines) and not lines[i+1].strip().startswith('//'):
                            # This might be a broken string that continues
                            combined = line.rstrip() + '  ' + lines[i+1].lstrip()
                            # Clear out any embedded newlines/corruption
                            combined = re.sub(r'[\u2500-\u2600\u00a0-\u00ff]\n', ' ', combined)
                            fixed_lines.append(combined)
                            i += 2
                            continue
                    
                    fixed_lines.append(fixed_line)
                else:
                    fixed_lines.append(line)
            else:
                fixed_lines.append(line)
            
            i += 1
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(fixed_lines)
        
        print(f"✓ Fixed broken strings: {filepath}")
        return True
    except Exception as e:
        print(f"✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    fix_broken_strings('src/main/java/com/smartcity/controller/CitizenDashboardController.java')
