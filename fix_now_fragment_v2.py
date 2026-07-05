#!/usr/bin/env python3
"""Add @AndroidEntryPoint and @Inject to NowFragment.java."""

def fix_now_fragment():
    filepath = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/ui/fragments/NowFragment.java"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Check if already has @AndroidEntryPoint
    if '@AndroidEntryPoint' in ''.join(lines):
        print("NowFragment already has @AndroidEntryPoint")
        return
    
    # Find the import section and add @AndroidEntryPoint
    import_line_idx = None
    for i, line in enumerate(lines):
        if line.startswith('import '):
            import_line_idx = i
    
    if import_line_idx is not None:
        # Find the last import line
        last_import_idx = import_line_idx
        for i in range(import_line_idx, len(lines)):
            if lines[i].startswith('import '):
                last_import_idx = i
        
        # Add import after the last import
        lines.insert(last_import_idx + 1, 'import dagger.hilt.android.AndroidEntryPoint;\n')
        lines.insert(last_import_idx + 1, 'import javax.inject.Inject;\n')
        print(f"Added imports at line {last_import_idx + 1}")
    
    # Add @AndroidEntryPoint before class declaration
    for i, line in enumerate(lines):
        if line.startswith('public class NowFragment'):
            lines.insert(i, '@AndroidEntryPoint\n')
            print(f"Added @AndroidEntryPoint annotation at line {i}")
            break
    
    # Find weatherRepository field and add @Inject
    for i, line in enumerate(lines):
        if 'private WeatherRepository weatherRepository;' in line:
            # Insert @Inject before the field
            lines.insert(i, '    @Inject\n')
            print(f"Added @Inject to weatherRepository at line {i}")
            break
    
    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    
    print("Successfully fixed NowFragment")

if __name__ == '__main__':
    fix_now_fragment()
