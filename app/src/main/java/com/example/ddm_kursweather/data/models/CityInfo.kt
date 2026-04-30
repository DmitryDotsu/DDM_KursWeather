package com.example.ddm_kursweather.data.models

data class CityInfo(
    val name: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    val isCapital: Boolean = false,
    val isRegionalCenter: Boolean = false,
    val adminLevelIcon: String = "📍"
)