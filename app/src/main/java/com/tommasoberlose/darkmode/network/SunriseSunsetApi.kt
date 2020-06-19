package com.tommasoberlose.darkmode.network

import com.tommasoberlose.darkmode.models.SunsetSunriseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SunriseSunsetApi {
    @GET("json?formatted=0")
    suspend fun getSunsetSunriseTime(@Query("lat") lat: Double, @Query("lng") lon: Double): SunsetSunriseResponse
}