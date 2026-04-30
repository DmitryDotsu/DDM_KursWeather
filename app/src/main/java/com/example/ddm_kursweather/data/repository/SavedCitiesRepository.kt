package com.example.ddm_kursweather.data.repository

import com.example.ddm_kursweather.data.local.dao.CityDao
import com.example.ddm_kursweather.data.local.entity.SavedCity
import kotlinx.coroutines.flow.Flow

class SavedCitiesRepository(
    private val cityDao: CityDao
) {
    fun getAllCities(): Flow<List<SavedCity>> = cityDao.getAllSavedCities()

    suspend fun addCity(city: SavedCity) {
        if (cityDao.isCitySaved(city.name) == 0) {
            cityDao.insertCity(city)
        }
    }

    suspend fun removeCity(cityId: Int) = cityDao.deleteCity(cityId)
    suspend fun isCitySaved(name: String): Boolean = cityDao.isCitySaved(name) > 0


}
