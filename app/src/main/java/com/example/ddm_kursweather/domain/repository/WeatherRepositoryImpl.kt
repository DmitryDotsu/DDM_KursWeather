package com.example.ddm_kursweather.data.repository

import com.example.ddm_kursweather.data.api.GeocodingApiService
import com.example.ddm_kursweather.data.api.OpenMeteoApiService
import com.example.ddm_kursweather.data.api.GeocodingResult
import com.example.ddm_kursweather.data.api.isCapital
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val geocodingApi: GeocodingApiService,
    private val openMeteoApi: OpenMeteoApiService
) : WeatherRepository {

    // Разрешённые страны
    private val allowedCountries = setOf(
        "RU", "US", "GB", "DE", "FR", "IT", "ES",
        "UA", "BY", "KZ", "PL", "TR", "CN", "JP",
        "CA", "AU", "BR", "IN", "MX"
    )

    override suspend fun searchCity(query: String): Result<List<CityInfo>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }

            val response = geocodingApi.searchLocation(
                name = query,
                count = 40,
                language = "ru"
            )

            // Сначала фильтруем по разрешённым странам
            val allowedResults = response.results
                .filter { result ->
                    result.countryCode?.let { allowedCountries.contains(it) } == true
                }

            // Сортируем по приоритетам
            val sortedResults = allowedResults.sortedWith { a, b ->
                // Приоритет 1: Столицы
                val aIsCapital = a.isCapital()
                val bIsCapital = b.isCapital()
                if (aIsCapital != bIsCapital) {
                    return@sortedWith if (aIsCapital) -1 else 1
                }

                // Приоритет 2: Точное совпадение с запросом
                val aExactMatch = a.name.equals(query, ignoreCase = true)
                val bExactMatch = b.name.equals(query, ignoreCase = true)
                if (aExactMatch != bExactMatch) {
                    return@sortedWith if (aExactMatch) -1 else 1
                }

                // Приоритет 3: Начинается с запроса
                val aStarts = a.name.startsWith(query, ignoreCase = true)
                val bStarts = b.name.startsWith(query, ignoreCase = true)
                if (aStarts != bStarts) {
                    return@sortedWith if (aStarts) -1 else 1
                }

                // Приоритет 4: Россия и США в приоритете
                val aPriority = when (a.countryCode) { "RU" -> 1; "US" -> 2; else -> 3 }
                val bPriority = when (b.countryCode) { "RU" -> 1; "US" -> 2; else -> 3 }
                if (aPriority != bPriority) {
                    return@sortedWith aPriority.compareTo(bPriority)
                }

                // Приоритет 5: Популярность (по населению или просто по алфавиту)
                a.name.compareTo(b.name)
            }

            val cities = sortedResults.take(10).map { result ->
                CityInfo(
                    name = result.name,
                    fullName = buildFullName(result),  // без звёздочки
                    latitude = result.latitude,
                    longitude = result.longitude,
                    isCapital = result.isCapital()
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
            append(", ${getCountryName(result.countryCode)}")
        }
    }

    private fun getCountryName(code: String?): String {
        return when (code) {
            "RU" -> "Россия"
            "US" -> "США"
            "GB" -> "Великобритания"
            "DE" -> "Германия"
            "FR" -> "Франция"
            "IT" -> "Италия"
            "ES" -> "Испания"
            "UA" -> "Украина"
            "BY" -> "Беларусь"
            "KZ" -> "Казахстан"
            "PL" -> "Польша"
            "TR" -> "Турция"
            "CN" -> "Китай"
            "JP" -> "Япония"
            "CA" -> "Канада"
            "AU" -> "Австралия"
            else -> code ?: "Unknown"
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