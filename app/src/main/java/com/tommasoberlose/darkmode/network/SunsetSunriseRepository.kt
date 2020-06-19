package com.tommasoberlose.darkmode.network

import com.google.gson.GsonBuilder
import com.tommasoberlose.darkmode.models.SunsetSunriseResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class SunsetSunriseRepository {
    companion object {
        private const val BASE_URL = "https://api.sunrise-sunset.org/"

        private val retrofit: SunriseSunsetApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build().create(SunriseSunsetApi::class.java)
        }

        suspend fun getSunsetSunriseTime(lat: Double, lon: Double): SunsetSunriseResponse? {
            return try {
                retrofit.getSunsetSunriseTime(lat, lon)
            } catch (e: Exception) {
                null
            }
        }
    }
}