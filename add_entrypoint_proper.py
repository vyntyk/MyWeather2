#!/usr/bin/env python3
"""Add @AndroidEntryPoint to MainActivity.java with proper handling."""

import re

def fix_main_activity():
    filepath = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/MainActivity.java"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Check if already has @AndroidEntryPoint
    for line in lines:
        if '@AndroidEntryPoint' in line:
            print("MainActivity already has @AndroidEntryPoint")
            return
    
    # Find the import section and add the annotation
    import_line_idx = None
    for i, line in enumerate(lines):
        if line.startswith('import '):
            import_line_idx = i
    
    # Add the import after other imports
    if import_line_idx is not None:
        # Find the last import line
        last_import_idx = import_line_idx
        for i in range(import_line_idx, len(lines)):
            if lines[i].startswith('import '):
                last_import_idx = i
        
        # Add import after the last import
        lines.insert(last_import_idx + 1, 'import dagger.hilt.android.AndroidEntryPoint;\n')
        print(f"Added import at line {last_import_idx + 1}")
    
    # Find the class declaration and add annotation before it
    class_idx = None
    for i, line in enumerate(lines):
        if 'public class MainActivity' in line:
            class_idx = i
            break
    
    if class_idx is not None:
        lines.insert(class_idx, '@AndroidEntryPoint\n')
        print(f"Added @AndroidEntryPoint at line {class_idx}")
    
    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    
    print("Successfully added @AndroidEntryPoint to MainActivity")

if __name__ == '__main__':
    fix_main_activity()
