package com.home.myweather.data.mapper

import com.home.myweather.data.model.DailyForecastData
import com.home.myweather.data.model.HourlyForecastData
import com.home.myweather.data.model.OpenMeteoForecastData
import com.home.myweather.data.model.OpenMeteoForecastResponse
import com.home.myweather.data.model.CurrentWeatherData

/**
 * Mapper to convert Java POJO [OpenMeteoForecastResponse] to Kotlin wrapper [OpenMeteoForecastData].
 * 
 * This avoids BackendException with Kotlin 2.0.20 + Compose 1.7.5 + Java POJO capture
 * by using Kotlin data classes in Compose UI layers.
 */
object OpenMeteoResponseMapper {

    fun map(response: OpenMeteoForecastResponse): OpenMeteoForecastData {
        return OpenMeteoForecastData(
            latitude = response.latitude,
            longitude = response.longitude,
            timezone = response.timezone,
            utcOffsetSeconds = response.utcOffsetSeconds,
            current = mapCurrent(response.current),
            hourly = mapHourly(response.hourly),
            daily = mapDaily(response.daily)
        )
    }

    private fun mapCurrent(current: OpenMeteoForecastResponse.Current): CurrentWeatherData {
        return CurrentWeatherData(
            time = current.time,
            temperature2m = current.temperature2m,
            relativeHumidity2m = current.relativeHumidity2m,
            apparentTemperature = current.apparentTemperature,
            precipitationProbability = current.precipitationProbability,
            precipitation = current.precipitation,
            weatherCode = current.weatherCode,
            cloudCover = current.cloudCover,
            surfacePressure = current.surfacePressure,
            windSpeed10m = current.windSpeed10m,
            windDirection10m = current.windDirection10m,
            isDay = current.isDay
        )
    }

    private fun mapHourly(hourly: OpenMeteoForecastResponse.Hourly): HourlyForecastData {
        return HourlyForecastData(
            time = hourly.time,
            temperature2m = hourly.temperature2m,
            relativeHumidity2m = hourly.relativeHumidity2m,
            apparentTemperature = hourly.apparentTemperature,
            precipitationProbability = hourly.precipitationProbability,
            weatherCode = hourly.weatherCode,
            windSpeed10m = hourly.windSpeed10m,
            windDirection10m = hourly.windDirection10m,
            surfacePressure = hourly.surfacePressure,
            visibility = hourly.visibility
        )
    }

    private fun mapDaily(daily: OpenMeteoForecastResponse.Daily): DailyForecastData {
        return DailyForecastData(
            time = daily.time,
            weatherCode = daily.weatherCode,
            temperature2mMax = daily.temperature2mMax,
            temperature2mMin = daily.temperature2mMin,
            precipitationProbabilityMax = daily.precipitationProbabilityMax,
            windSpeed10mMax = daily.windSpeed10mMax,
            sunrise = daily.sunrise,
            sunset = daily.sunset
        )
    }
}
