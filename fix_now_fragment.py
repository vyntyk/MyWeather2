#!/usr/bin/env python3
"""Fix NowFragment - add @Inject to weatherRepository field."""

def fix_now_fragment():
    filepath = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/ui/fragments/NowFragment.java"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Find the weatherRepository field and add @Inject if not present
    for i, line in enumerate(lines):
        if 'private WeatherRepository weatherRepository;' in line:
            # Check if previous line is @Inject
            if i > 0 and '@Inject' not in lines[i-1]:
                lines[i-1] = '    @Inject\n' + lines[i-1]
                print(f"Added @Inject to weatherRepository at line {i-1}")
            break
    
    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    
    print("Successfully fixed NowFragment")

if __name__ == '__main__':
    fix_now_fragment()
