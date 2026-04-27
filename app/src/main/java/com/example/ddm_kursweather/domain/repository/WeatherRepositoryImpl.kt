package com.example.ddm_kursweather.data.repository

import com.example.ddm_kursweather.data.api.NinjasApiService
import com.example.ddm_kursweather.data.api.OpenMeteoApiService
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val ninjasApi: NinjasApiService,
    private val openMeteoApi: OpenMeteoApiService
) : WeatherRepository {

    override suspend fun getWeatherForCity(cityName: String): Result<CurrentWeather> {
        return try {
            // 1. Ищем координаты города
            val cities = ninjasApi.getCity(cityName)
            if (cities.isEmpty()) {
                return Result.failure(Exception("Город не найден"))
            }

            val city = cities.first()

            // 2. Запрашиваем погоду
            val weather = openMeteoApi.getWeather(
                latitude = city.latitude,
                longitude = city.longitude
            )

            Result.success(weather.current_weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchCity(cityName: String): Result<List<String>> {
        return try {
            val cities = ninjasApi.getCity(cityName)
            Result.success(cities.map { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}