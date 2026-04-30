package com.example.ddm_kursweather.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ddm_kursweather.data.repository.SavedCitiesRepository

class FavoritesViewModelFactory(
    private val savedCitiesRepository: SavedCitiesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(savedCitiesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}