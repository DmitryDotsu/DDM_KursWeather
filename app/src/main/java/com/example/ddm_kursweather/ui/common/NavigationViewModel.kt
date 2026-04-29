package com.example.ddm_kursweather.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddm_kursweather.data.models.CityInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel : ViewModel() {

    private val _selectedCity = MutableStateFlow<CityInfo?>(null)
    val selectedCity: StateFlow<CityInfo?> = _selectedCity.asStateFlow()

    private val _lastLoadedCityName = MutableStateFlow<String?>(null)
    val lastLoadedCityName: StateFlow<String?> = _lastLoadedCityName.asStateFlow()

    fun setSelectedCity(city: CityInfo?) {
        viewModelScope.launch {
            _selectedCity.emit(city)
        }
    }

    fun setLastLoadedCityName(name: String?) {
        viewModelScope.launch {
            _lastLoadedCityName.emit(name)
        }
    }

    fun restoreFromLastLoaded(fullName: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val city = CityInfo(
                name = lastLoadedCityName.value ?: "",
                fullName = fullName,
                latitude = latitude,
                longitude = longitude,
                isCapital = false,
                isRegionalCenter = false,
                adminLevelIcon = "📍"
            )
            _selectedCity.emit(city)
        }
    }
}