package com.example.ddm_kursweather.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddm_kursweather.data.local.entity.SavedCity
import com.example.ddm_kursweather.data.repository.SavedCitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data class Success(val cities: List<SavedCity>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(
    private val savedCitiesRepository: SavedCitiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading

            try {
                val cities = savedCitiesRepository.getAllCities().first()
                _uiState.value = FavoritesUiState.Success(cities)
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun removeCity(cityId: Int) {
        viewModelScope.launch {
            savedCitiesRepository.removeCity(cityId)
        }
    }
}