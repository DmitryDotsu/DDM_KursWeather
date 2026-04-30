package com.example.ddm_kursweather.data.repository

import com.example.ddm_kursweather.data.api.GeocodingApiService
import com.example.ddm_kursweather.data.api.OpenMeteoApiService
import com.example.ddm_kursweather.data.api.GeocodingResult
import com.example.ddm_kursweather.data.api.isCapital
import com.example.ddm_kursweather.data.api.isRegionalCenter
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.domain.repository.WeatherRepository
import com.example.ddm_kursweather.data.api.getAdminLevelIcon


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

    // Приоритетные страны (для сортировки)
    private val priorityCountries = listOf("RU", "BY", "KZ", "UA")

    // Сопоставление исторических названий с современными, косяк в ОпенМетео
    private val knownCityMapping = mapOf(
        "Молотов" to "Пермь"
    )

    override suspend fun searchCity(query: String): Result<List<CityInfo>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }

            val response = geocodingApi.searchLocation(
                name = query,
                count = 50,
                language = "ru"
            )

            // Сначала фильтруем по разрешённым странам
            val allowedResults = response.results
                .filter { result ->
                    result.countryCode?.let { allowedCountries.contains(it) } == true
                }

            // Сортируем по приоритетам
            val sortedResults = allowedResults.sortedWith { a, b ->

                // ПРИОРИТЕТ 1: Столицы стран (PPLC)
                val aIsCapital = a.isCapital()
                val bIsCapital = b.isCapital()
                if (aIsCapital != bIsCapital) {
                    return@sortedWith if (aIsCapital) -1 else 1
                }

                // ПРИОРИТЕТ 2: Административные центры регионов (PPLA) — Пермь, Красноярск и т.д.
                val aIsRegional = a.isRegionalCenter()
                val bIsRegional = b.isRegionalCenter()
                if (aIsRegional != bIsRegional) {
                    return@sortedWith if (aIsRegional) -1 else 1
                }

                // ПРИОРИТЕТ 3: Города из приоритетных стран (Россия, Беларусь, Казахстан, Украина)
                val aPriorityCountry = if (priorityCountries.contains(a.countryCode)) 1 else 2
                val bPriorityCountry = if (priorityCountries.contains(b.countryCode)) 1 else 2
                if (aPriorityCountry != bPriorityCountry) {
                    return@sortedWith aPriorityCountry.compareTo(bPriorityCountry)
                }

                // ПРИОРИТЕТ 4: Точное совпадение с запросом
                val aExactMatch = a.name.equals(query, ignoreCase = true)
                val bExactMatch = b.name.equals(query, ignoreCase = true)
                if (aExactMatch != bExactMatch) {
                    return@sortedWith if (aExactMatch) -1 else 1
                }

                // ПРИОРИТЕТ 5: Начинается с запроса
                val aStarts = a.name.startsWith(query, ignoreCase = true)
                val bStarts = b.name.startsWith(query, ignoreCase = true)
                if (aStarts != bStarts) {
                    return@sortedWith if (aStarts) -1 else 1
                }

                // ПРИОРИТЕТ 6: Специальные случаи (исторические названия)
                val aIsSpecial = knownCityMapping.keys.contains(a.name)
                val bIsSpecial = knownCityMapping.keys.contains(b.name)
                if (aIsSpecial != bIsSpecial) {
                    return@sortedWith if (aIsSpecial) -1 else 1
                }

                // ПРИОРИТЕТ 7: По населению (чем больше, тем выше)
                val aPop = a.population ?: 0
                val bPop = b.population ?: 0
                if (aPop != bPop) {
                    return@sortedWith bPop.compareTo(aPop)
                }

                // ПРИОРИТЕТ 8: По алфавиту
                a.name.compareTo(b.name)
            }

            val cities = sortedResults.take(15).map { result ->
                val displayName = knownCityMapping[result.name] ?: result.name
                CityInfo(
                    name = displayName,
                    fullName = buildFullName(result, displayName),
                    latitude = result.latitude,
                    longitude = result.longitude,
                    isCapital = result.isCapital(),
                    isRegionalCenter = result.isRegionalCenter(),
                    adminLevelIcon = result.getAdminLevelIcon()
                )
            }

            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildFullName(result: GeocodingResult, displayName: String): String {
        return buildString {
            append(displayName)
            if (!result.region.isNullOrEmpty()) {
                append(", ${result.region}")
            }
            append(", ${getCountryName(result.countryCode)}")
        }
    }

    //Тут хардкод оставлю,нет необходимости переносить контекст
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