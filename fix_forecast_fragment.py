#!/usr/bin/env python3
"""Fix ForecastFragment - add @AndroidEntryPoint and @Inject WeatherRepository."""

def fix_forecast_fragment():
    filepath = "C:/Users/Viktor/Downloads/My_Weather_changes_1/app/src/main/java/com/home/myweather/ui/fragments/ForecastFragment.java"
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    content = ''.join(lines)
    
    # Check if already has @AndroidEntryPoint
    if '@AndroidEntryPoint' in content:
        print("ForecastFragment already has @AndroidEntryPoint")
    else:
        # Add import if not present
        if 'import dagger.hilt.android.AndroidEntryPoint;' not in content:
            # Find the last import line
            last_import_idx = None
            for i, line in enumerate(lines):
                if line.startswith('import '):
                    last_import_idx = i
            
            # Add import after the last import
            if last_import_idx is not None:
                lines.insert(last_import_idx + 1, 'import dagger.hilt.android.AndroidEntryPoint;\n')
                print(f"Added @AndroidEntryPoint import at line {last_import_idx + 1}")
        
        # Find the class declaration and add annotation before it
        class_idx = None
        for i, line in enumerate(lines):
            if line.startswith('public class ForecastFragment'):
                class_idx = i
                break
        
        if class_idx is not None:
            lines.insert(class_idx, '@AndroidEntryPoint\n')
            print(f"Added @AndroidEntryPoint annotation at line {class_idx}")
    
    # Add @Inject annotation for weatherRepository if not already injected
    if '@Inject\n    WeatherRepository weatherRepository' not in content and 'weatherRepository = new WeatherRepository()' in content:
        # Find the line with weatherRepository declaration
        for i, line in enumerate(lines):
            if 'weatherRepository;' in line and not line.strip().startswith('//'):
                # Check if it's already @Inject
                if '@Inject' not in lines[i-1]:
                    lines[i-1] = '    @Inject\n' + lines[i-1]
                    print(f"Added @Inject to weatherRepository at line {i-1}")
                break
        
        # Remove the line: weatherRepository = new WeatherRepository();
        for i, line in enumerate(lines):
            if 'weatherRepository = new WeatherRepository();' in line:
                lines[i] = line.replace('weatherRepository = new WeatherRepository();', '')
                print(f"Removed manual weatherRepository initialization at line {i}")
                break
    
    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    
    print("Successfully fixed ForecastFragment")

if __name__ == '__main__':
    fix_forecast_fragment()
