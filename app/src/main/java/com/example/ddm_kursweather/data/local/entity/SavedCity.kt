package com.example.ddm_kursweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cities")
data class SavedCity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double
)