#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
import sys

def clean_corrupted_strings(filepath):
    """Nettoyer les strings corrompues avec des sauts de ligne et caractères corrompus"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_len = len(content)
        
        # Stratégie: trouver les patterns "...\n\n\n..." dans les strings
        # et les remplacer par des espaces
        lines = content.split('\n')
        fixed_lines = []
        i = 0
        
        while i < len(lines):
            line = lines[i]
            
            # Si la ligne a une chaîne non fermée
            if line.count('"') % 2 == 1:
                # Chercher le caractère de fermeture
                j = i + 1
                combined = line
                
                # Joindre les lignes jusqu'à trouver la fermeture
                while j < len(lines) and combined.count('"') % 2 == 1:
                    combined += ' ' + lines[j].strip()
                    j += 1
                
                # Nettoyer les espaces multiples
                combined = re.sub(r' +', ' ', combined)
                fixed_lines.append(combined)
                i = j
            else:
                fixed_lines.append(line)
                i += 1
        
        fixed_content = '\n'.join(fixed_lines)
        
        # Deuxième passe: nettoyer les caractères corrompus résiduels
        # Remplacer les symboles corrompus par des équivalents valides
        fixed_content = re.sub(r'[\x00-\x08\x0b-\x0c\x0e-\x1f]', '', fixed_content)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(fixed_content)
        
        print(f"✓ Nettoyé {filepath} ({original_len} -> {len(fixed_content)} caractères)")
        return True
    except Exception as e:
        print(f"✗ Erreur: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    files = [
        'src/main/java/com/smartcity/controller/CitizenDashboardController.java',
        'src/main/java/com/smartcity/controller/AgentDashboardController.java',
    ]
    
    for f in files:
        clean_corrupted_strings(f)
