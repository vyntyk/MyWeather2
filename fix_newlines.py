#!/usr/bin/env python3
"""
Fix files that have literal \n instead of actual newlines.
This script processes Java and Kotlin files in the project.
"""

import os
import re

def fix_file(filepath):
    """Fix a single file by replacing literal \\n with actual newlines."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if file has literal \n characters (not already fixed)
        if '\\n' in content and not content.startswith('package'):
            # Replace literal \n with actual newlines
            fixed_content = content.replace('\\n', '\n')
            
            # Write back
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(fixed_content)
            
            print(f"FIXED: {filepath}")
            return True
        else:
            print(f"OK: {filepath}")
            return False
    except Exception as e:
        print(f"ERROR processing {filepath}: {e}")
        return False

def main():
    base_dir = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather"
    
    fixed_count = 0
    
    # Process Java files
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith('.java') or file.endswith('.kt'):
                filepath = os.path.join(root, file)
                if fix_file(filepath):
                    fixed_count += 1
    
    print(f"\n=== Summary: {fixed_count} files fixed ===")

if __name__ == '__main__':
    main()
