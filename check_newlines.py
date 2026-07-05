#!/usr/bin/env python3
"""Check if files have literal \n instead of actual newlines."""

import os

def check_file(filepath):
    try:
        with open(filepath, 'rb') as f:
            content = f.read()
        
        # Check if it contains literal backslash-n sequence
        if b'\\n' in content:
            # Count how many
            count = content.count(b'\\n')
            print(f"FOUND {count} literal \\n in: {filepath}")
            return True
        else:
            # Check if starts with package (good sign)
            if content.startswith(b'package'):
                print(f"OK: {os.path.basename(filepath)}")
            return False
    except Exception as e:
        print(f"ERROR: {filepath} - {e}")
        return False

def main():
    base_dir = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather"
    
    found_issues = 0
    
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            if file.endswith('.java') or file.endswith('.kt'):
                filepath = os.path.join(root, file)
                if check_file(filepath):
                    found_issues += 1
    
    print(f"\n=== Total files with issues: {found_issues} ===")

if __name__ == '__main__':
    main()
