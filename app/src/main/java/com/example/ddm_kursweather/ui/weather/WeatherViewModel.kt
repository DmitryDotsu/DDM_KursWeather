package com.example.ddm_kursweather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddm_kursweather.data.models.CurrentWeather
import com.example.ddm_kursweather.data.models.CityInfo
import com.example.ddm_kursweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    data object Initial : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(val weather: CurrentWeather, val cityName: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CityInfo>>(emptyList())
    val searchResults: StateFlow<List<CityInfo>> = _searchResults.asStateFlow()

    fun searchCity(query: String) {
        if (query.length < 3) {//ответ всеравно получим на запрос от трех символов (два крайняя редкость)
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val result = repository.searchCity(query)
            result.onSuccess { cities ->
                _searchResults.value = cities
            }.onFailure {
                _searchResults.value = emptyList()
                _uiState.value = WeatherUiState.Error(it.message ?: "Ошибка поиска городов")
            }
        }
    }

    fun loadWeather(city: CityInfo) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _searchResults.value = emptyList()

            val result = repository.getWeatherForCity(city.latitude, city.longitude)
            result.onSuccess { weather ->
                _uiState.value = WeatherUiState.Success(weather, city.name)
            }.onFailure { exception ->
                _uiState.value = WeatherUiState.Error(exception.message ?: "Ошибка загрузки погоды")
            }
        }
    }
}