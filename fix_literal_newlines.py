#!/usr/bin/env python3
"""Fix files that have literal \\n instead of actual newlines."""

def fix_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Count literal \n in the file
        literal_count = content.count('\\n')
        
        if literal_count > 0:
            # Replace literal \n with actual newlines
            fixed_content = content.replace('\\n', '\n')
            
            # Write back
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(fixed_content)
            
            print(f"FIXED {filepath}: replaced {literal_count} literal \\n")
            return True
        else:
            print(f"OK: {filepath}")
            return False
    except Exception as e:
        print(f"ERROR processing {filepath}: {e}")
        return False

def main():
    files_to_fix = [
        "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/ui/fragments/DayDetailFragment.java",
        "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/ui/fragments/ForecastFragment.java",
    ]
    
    for filepath in files_to_fix:
        fix_file(filepath)

if __name__ == '__main__':
    main()
