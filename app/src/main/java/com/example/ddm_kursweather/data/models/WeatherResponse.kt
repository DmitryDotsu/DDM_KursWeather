package com.example.ddm_kursweather.data.models

data class WeatherResponse(
    val current_weather: CurrentWeather,
    val hourly: Hourly? = null
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    val weathercode: Int,
    val time: String
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>
)