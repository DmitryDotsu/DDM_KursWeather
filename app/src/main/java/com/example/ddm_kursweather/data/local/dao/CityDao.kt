package com.example.ddm_kursweather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ddm_kursweather.data.local.entity.SavedCity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM saved_cities ORDER BY name ASC")
    fun getAllSavedCities(): Flow<List<SavedCity>>

    @Insert
    suspend fun insertCity(city: SavedCity)

    @Query("DELETE FROM saved_cities WHERE id = :cityId")
    suspend fun deleteCity(cityId: Int)

    @Query("SELECT COUNT(*) FROM saved_cities WHERE name = :name")
    suspend fun isCitySaved(name: String): Int
}