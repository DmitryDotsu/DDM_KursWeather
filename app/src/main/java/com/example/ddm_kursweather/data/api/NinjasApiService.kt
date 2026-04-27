package com.example.ddm_kursweather.data.api

import com.example.ddm_kursweather.data.models.CityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NinjasApiService {
    @GET("city")
    suspend fun getCity(
        @Query("name") name: String
    ): List<CityResponse>
}