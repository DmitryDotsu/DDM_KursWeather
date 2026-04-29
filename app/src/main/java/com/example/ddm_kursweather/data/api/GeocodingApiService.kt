package com.example.ddm_kursweather.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
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
    val latitude: Double,
    val longitude: Double,
    @SerializedName("feature_code")
    val featureCode: String? = null
)