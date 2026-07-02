# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**My Weather** is an Android application for displaying current weather by city name. It's published on Google Play: https://play.google.com/store/apps/details?id=com.home.myweather

The app follows Clean Architecture with layered organization:
- **Presentation Layer** (`ui/`): Fragments, adapters, and UI controllers
- **Data Layer** (`data/`): Network API calls, repositories, and data models  
- **Utility Layer** (`utils/`): Data processing and formatting utilities
- **Helper Layer** (`helpers/`): Location services, favorites management

## Build and Development Commands

### Environment Setup
1. Copy `local.properties.example` to `local.properties`
2. Add your OpenWeatherMap API key: `OPENWEATHER_API_KEY=your_api_key_here`
3. Sync Gradle: File → Sync Project with Gradle Files (Android Studio)

### Build Commands
```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run Android tests
./gradlew connectedAndroidTest

# Install debug version on connected device
./gradlew installDebug
```

### Key Dependencies
- **Retrofit 2.11.0** + GSON converter for API calls
- **MapLibre GL 11.5.2** for map functionality  
- **Play Services Location 21.3.0** for GPS
- **Material Design 1.12.0** for UI components
- **ViewPager2 1.1.0** + **RecyclerView 1.4.0** for navigation

## Architecture

### Core Flow
1. **MainActivity**: Entry point with ViewPager2 for 5-tab navigation
   - Now (current weather)
   - Forecast (5-day forecast)
   - Map (interactive map with weather overlay)
   - Cities (favorites management)
   - Settings

2. **Data Flow**:
   ```
   UI Fragment → WeatherRepository → API Service → Retrofit Client
        ↓
   API Response → Data Model → UI Display
   ```

3. **Two-Step API Process**:
   - **Geocoding**: City name → coordinates via OpenWeatherMap Geo API
   - **Weather Data**: Coordinates → current weather + 5-day forecast

### Key Classes

**Data Layer:**
- `WeatherRepository.java`: Orchestrates weather data fetching (current + forecast)
- `GeocodingRepository.java`: Handles city→coordinates conversion
- `RetrofitClient.java`: Singleton Retrofit instance
- `WeatherApiService.java`: Retrofit interface for OpenWeatherMap API

**UI Layer:**
- `NowFragment.java`: Current weather display with hourly forecast
- `ForecastFragment.java`: 5-day forecast with expandable day details
- `MapFragment.java`: Interactive map with weather tile overlay
- `CitiesFragment.java`: Favorite cities management
- `SettingsFragment.java`: App preferences (theme, units)

**Models:**
- `WeatherResponse.java`: Current weather API response
- `ForecastResponse.java`: 5-day forecast API response  
- `GeoLocation.java`: City coordinates and metadata
- `DailyData.java`: Aggregated daily forecast data

### API Integration

The app uses two OpenWeatherMap endpoints:
1. **Geocoding**: `api.openweathermap.org/geo/1.0/direct?q={city},{country}&limit=1`
2. **Current Weather**: `api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang=ru`
3. **5-Day Forecast**: `api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&units=metric&lang=ru`

API key is injected via `BuildConfig.OPENWEATHER_API_KEY` from `local.properties`.

## Code Organization Principles

1. **Single Responsibility**: Each class has one clear purpose
2. **Separation of Concerns**: UI, data, and business logic are separated
3. **Dependency Inversion**: High-level modules don't depend on low-level implementations
4. **Package-by-Feature**: Related functionality grouped together

### Directory Structure
```
src/main/java/com/home/myweather/
├── ui/                          # Presentation layer
│   ├── fragments/               # Screen fragments
│   └── adapters/                # RecyclerView adapters
├── data/                        # Data layer
│   ├── network/                 # API communication
│   ├── repository/              # Data access abstraction
│   └── model/                   # Data models
├── utils/                       # Utility functions
│   ├── WeatherFormatter.java    # Data formatting
│   ├── ComfortIndex.java        # Weather comfort calculations
│   └── WeatherIcon.java         # Icon mapping
└── helpers/                     # Helper services
    ├── LocationHelper.java      # GPS location
    ├── FavoriteCitiesManager.java
    └── UiController.java        # Common UI operations
```

## Development Notes

### Theme System
- Uses `AppCompatDelegate.setDefaultNightMode()` for dark/light theme
- Theme preference stored in `SharedPreferences` under key `"dark_theme"`
- Applied in `MainActivity.applyStoredTheme()`

### Location Services
- `LocationHelper.java` handles GPS permissions and location updates
- Falls back to manual city search if GPS unavailable
- Last known location cached via `WeatherStorage`

### Map Integration
- MapLibre GL for interactive maps
- `WeatherTileLayer.java` adds weather overlay
- Pin placement for selected cities
- Gesture handling integrated with ViewPager navigation

### Testing
- JUnit 4 for unit tests
- Mockito for mocking dependencies
- Espresso for UI tests (configured but may need setup)

## Common Tasks

### Adding a New API Feature
1. Add endpoint to `WeatherApiService.java`
2. Create response model in `data/model/`
3. Add method to `WeatherRepository.java`
4. Update relevant fragment to display data

### Modifying UI Layouts
- Layout files are in `app/src/main/res/layout/`
- Follow Material Design guidelines
- Use `ConstraintLayout` for responsive design
- Support both Russian (`values-ru/`) and default locales

### Debugging
- Check `BuildConfig.OPENWEATHER_API_KEY` is properly set
- Enable Retrofit logging interceptor in `RetrofitClient.java`
- Verify GPS permissions in AndroidManifest.xml
- Check network connectivity for API calls

## Recent Changes

The project was recently restructured to follow Clean Architecture principles:
- Files moved to logical packages based on layer
- Import statements updated to reflect new structure
- Separation of UI, data, and utility layers
- Documentation created in `ARCHITECTURE.md` and `PROJECT_STRUCTURE.md`

## Resources
- OpenWeatherMap API: https://openweathermap.org/api
- MapLibre Android SDK: https://github.com/maplibre/maplibre-gl-native
- Material Design Components: https://material.io/develop/android