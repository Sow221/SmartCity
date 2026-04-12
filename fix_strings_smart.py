#!/usr/bin/env python3
import re

def fix_java_string_newlines_smart(filepath):
    """Fix unclosed strings while preserving Java text blocks"""
    try:
        with open(filepath, 'rb') as f:
            text = f.read().decode('utf-8')
        
        # Don't collapse newlines inside text blocks (""" ... """)
        # Instead, just fix regular strings that have embedded newlines
        
        parts = []
        pos = 0
        
        while pos < len(text):
            # Look for text block start
            tb_start = text.find('"""', pos)
            
            # Look for regular string start
            str_start = text.find('"', pos)
            
            if tb_start == -1 and str_start == -1:
                # No more strings
                parts.append(text[pos:])
                break
            elif tb_start != -1 and (str_start == -1 or tb_start < str_start):
                # Found text block first
                # Find the end of the text block
                tb_content_start = tb_start + 3
                tb_end = text.find('"""', tb_content_start)
                if tb_end == -1:
                    # Unclosed text block - just append rest
                    parts.append(text[pos:])
                    break
                else:
                    # Include the whole text block as-is
                    parts.append(text[pos:tb_end + 3])
                    pos = tb_end + 3
            elif str_start != -1:
                # Found regular string first
                # Append everything before the string
                parts.append(text[pos:str_start])
                
                # Now fix the string content
                str_content_start = str_start + 1
                str_end = str_content_start
                
                # Find the closing quote, handling escapes
                while str_end < len(text):
                    if text[str_end] == '\\':
                        str_end += 2  # Skip escaped character
                    elif text[str_end] == '"':
                        break
                    elif text[str_end] == '\n':
                        # Found embedded newline - this is a problem
                        # Collapse whitespace/newlines
                        str_end += 1
                        while str_end < len(text) and text[str_end] in ' \t\n\r':
                            str_end += 1
                        # Now continue looking for the closing quote
                        continue
                    else:
                        str_end += 1
                
                if str_end < len(text) and text[str_end] == '"':
                    # Extract string content
                    content = text[str_content_start:str_end]
                    # Collapse internal newlines
                    content = re.sub(r'\n\s*', ' ', content)
                    content = re.sub(r'\s{2,}', ' ', content)
                    
                    # Add the fixed string
                    parts.append('"' + content + '"')
                    pos = str_end + 1
                else:
                    # Unclosed string - just append what we have
                    parts.append(text[str_start:])
                    break
            else:
                pos += 1
        
        fixed_text = ''.join(parts)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(fixed_text)
        
        print(f"✓ Smart fixed {filepath} ({len(text)} -> {len(fixed_text)} chars)")
        return True
    except Exception as e:
        print(f"✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    fix_java_string_newlines_smart('src/main/java/com/smartcity/controller/AgentDashboardController.java')
