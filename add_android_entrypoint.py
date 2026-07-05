#!/usr/bin/env python3
"""Add @AndroidEntryPoint to MainActivity.java."""

def fix_main_activity():
    filepath = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/MainActivity.java"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Check if already has @AndroidEntryPoint
    if '@AndroidEntryPoint' in content:
        print("MainActivity already has @AndroidEntryPoint")
        return
    
    # Add import if not present
    if 'import dagger.hilt.android.AndroidEntryPoint;' not in content:
        # Find the import section and add after other imports
        lines = content.split('\n')
        import_idx = None
        for i, line in enumerate(lines):
            if line.startswith('import '):
                import_idx = i
        if import_idx is not None:
            lines.insert(import_idx + 1, 'import dagger.hilt.android.AndroidEntryPoint;')
            content = '\n'.join(lines)
    
    # Add @AndroidEntryPoint annotation before class declaration
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if line.startswith('public class MainActivity'):
            # Insert annotation before the class
            lines.insert(i, '@AndroidEntryPoint')
            break
    
    content = '\n'.join(lines)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print("Added @AndroidEntryPoint to MainActivity")

if __name__ == '__main__':
    fix_main_activity()
