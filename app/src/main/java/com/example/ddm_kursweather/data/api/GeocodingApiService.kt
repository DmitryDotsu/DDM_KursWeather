package com.example.ddm_kursweather.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 30,
        @Query("language") language: String = "ru"
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList()
)

data class GeocodingResult(
    val id: Int = 0,
    val name: String,
    @SerializedName("admin1")
    val region: String? = null,
    val country: String,
    @SerializedName("country_code")
    val countryCode: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("feature_code")
    val featureCode: String? = null,
    val population: Int? = null
)

fun GeocodingResult.isCapital(): Boolean {
    return featureCode == "PPLC"
}

fun GeocodingResult.isRegionalCenter(): Boolean {
    return featureCode == "PPLA" // Центр региона (области, края)
}


fun GeocodingResult.getAdminLevelIcon(): String {
    return when (featureCode) {
        "PPLC" -> "🏛️"  // Столица страны
        "PPLA" -> "🏢"   // Центр региона
        else -> "📍"      // Обычный город
    }
}