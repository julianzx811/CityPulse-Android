package com.yulian.citypulse.repository

import com.yulian.citypulse.data.model.WeatherResponse
import com.yulian.citypulse.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository {

    private val api = RetrofitInstance.api

    fun getWeather(city: String, apiKey: String): Flow<Result<WeatherResponse>> = flow {
        try {
            val response = api.getWeatherByCity(city, apiKey)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}