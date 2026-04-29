package com.example.ddm_kursweather.data.repository

import com.example.ddm_kursweather.data.api.GeocodingApiService
import com.example.ddm_kursweather.data.api.OpenMeteoApiService
import com.example.ddm_kursweather.data.api.GeocodingResult
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val geocodingApi: GeocodingApiService,
    private val openMeteoApi: OpenMeteoApiService
) : WeatherRepository {

    override suspend fun searchCity(query: String): Result<List<CityInfo>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }

            val response = geocodingApi.searchLocation(
                name = query,
                count = 10,
                language = "ru"
            )

            val cities = response.results.map { result ->
                CityInfo(
                    name = result.name,
                    fullName = buildFullName(result),
                    latitude = result.latitude,
                    longitude = result.longitude
                )
            }

            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildFullName(result: GeocodingResult): String {
        return buildString {
            append(result.name)
            if (!result.region.isNullOrEmpty()) {
                append(", ${result.region}")
            }
            append(", ${result.country}")
        }
    }

    override suspend fun getWeatherForCity(latitude: Double, longitude: Double): Result<CurrentWeather> {
        return try {
            val weather = openMeteoApi.getWeather(
                latitude = latitude,
                longitude = longitude
            )
            Result.success(weather.currentWeather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}