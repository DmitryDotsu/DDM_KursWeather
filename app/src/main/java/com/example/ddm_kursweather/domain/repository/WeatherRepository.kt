package com.example.ddm_kursweather.domain.repository

import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo

interface WeatherRepository {
    suspend fun searchCity(query: String): Result<List<CityInfo>>
    suspend fun getWeatherForCity(latitude: Double, longitude: Double): Result<CurrentWeather>
    suspend fun getWeatherForCity(cityInfo: CityInfo): Result<CurrentWeather> {
        return getWeatherForCity(cityInfo.latitude, cityInfo.longitude)
    }
}