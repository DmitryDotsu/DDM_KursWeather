package com.example.ddm_kursweather.domain.repository

import com.example.ddm_kursweather.data.models.CurrentWeather

interface WeatherRepository {
    suspend fun getWeatherForCity(cityName: String): Result<CurrentWeather>
    suspend fun searchCity(cityName: String): Result<List<String>>
}