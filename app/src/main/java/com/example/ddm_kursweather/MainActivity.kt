package com.example.ddm_kursweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.ddm_kursweather.data.local.database.WeatherDatabase
import com.example.ddm_kursweather.data.local.entity.SavedCity
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.data.repository.SavedCitiesRepository
import com.example.ddm_kursweather.ui.favorites.FavoritesScreen
import com.example.ddm_kursweather.ui.theme.DDM_KursWeatherTheme
import com.example.ddm_kursweather.ui.weather.WeatherScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val savedCitiesRepository by lazy {
        SavedCitiesRepository(
            WeatherDatabase.getInstance(this).cityDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DDM_KursWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }

    @Composable
    fun WeatherApp() {
        var showFavorites by remember { mutableStateOf(false) }
        var selectedCityFromFavorites by remember { mutableStateOf<SavedCity?>(null) }
        var refreshFavoritesTrigger by remember { mutableStateOf(0) }

        if (showFavorites) {
            FavoritesScreen(
                savedCitiesRepository = savedCitiesRepository,
                refreshTrigger = refreshFavoritesTrigger,
                onCityClick = { city ->
                    selectedCityFromFavorites = city
                    showFavorites = false
                },
                onBack = { showFavorites = false }
            )
        } else {
            WeatherScreen(
                selectedCityFromFavorites = selectedCityFromFavorites,
                onClearSelectedCity = {
                    selectedCityFromFavorites = null
                },
                onSaveCity = { city ->
                    lifecycleScope.launch {
                        saveCityToFavorites(city)
                        refreshFavoritesTrigger++
                    }
                },
                onRemoveCity = { city ->
                    lifecycleScope.launch {
                        removeCityFromFavorites(city)
                        refreshFavoritesTrigger++
                    }
                },
                isCitySaved = { cityName ->
                    runBlocking {
                        isCityInFavorites(cityName)
                    }
                },
                onShowFavorites = {
                    refreshFavoritesTrigger++
                    showFavorites = true
                }
            )
        }
    }

    private suspend fun saveCityToFavorites(city: CityInfo) {
        val savedCity = SavedCity(
            name = city.name,
            fullName = city.fullName,
            latitude = city.latitude,
            longitude = city.longitude
        )
        savedCitiesRepository.addCity(savedCity)
    }

    private suspend fun removeCityFromFavorites(city: CityInfo) {
        val cities = savedCitiesRepository.getAllCities().first()
        val cityToRemove = cities.find { it.name == city.name }
        cityToRemove?.let {
            savedCitiesRepository.removeCity(it.id)
        }
    }

    private suspend fun isCityInFavorites(cityName: String): Boolean {
        val cities = savedCitiesRepository.getAllCities().first()
        return cities.any { it.name == cityName }
    }
}