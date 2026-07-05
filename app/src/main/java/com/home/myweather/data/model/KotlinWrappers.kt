package com.home.myweather.data.model

/**
 * Kotlin wrapper for OpenMeteoForecastResponse.Hourly to avoid BackendException
 * with Kotlin 2.0.20 + Compose 1.7.5 + Java POJOs.
 * 
 * This wrapper converts Java POJO fields to Kotlin data class properties
 * to prevent IR lowering failures when capturing values in Compose lambdas.
 */
data class HourlyForecastData(
    val time: List<String>,
    val temperature2m: List<Double>,
    val relativeHumidity2m: List<Int>,
    val apparentTemperature: List<Double>,
    val precipitationProbability: List<Int>,
    val weatherCode: List<Int>,
    val windSpeed10m: List<Double>,
    val windDirection10m: List<Int>,
    val surfacePressure: List<Double>,
    val visibility: List<Double>
)

/**
 * Kotlin wrapper for OpenMeteoForecastResponse.Daily to avoid BackendException
 * with Kotlin 2.0.20 + Compose 1.7.5 + Java POJOs.
 */
data class DailyForecastData(
    val time: List<String>,
    val weatherCode: List<Int>,
    val temperature2mMax: List<Double>,
    val temperature2mMin: List<Double>,
    val precipitationProbabilityMax: List<Int>,
    val windSpeed10mMax: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>
)

/**
 * Kotlin wrapper for OpenMeteoForecastResponse.Current to avoid BackendException.
 */
data class CurrentWeatherData(
    val time: String,
    val temperature2m: Double,
    val relativeHumidity2m: Int,
    val apparentTemperature: Double,
    val precipitationProbability: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val cloudCover: Int,
    val surfacePressure: Double,
    val windSpeed10m: Double,
    val windDirection10m: Int,
    val isDay: Int
)

/**
 * Kotlin wrapper for OpenMeteoForecastResponse to avoid BackendException.
 */
data class OpenMeteoForecastData(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val utcOffsetSeconds: Int,
    val current: CurrentWeatherData,
    val hourly: HourlyForecastData,
    val daily: DailyForecastData
)
