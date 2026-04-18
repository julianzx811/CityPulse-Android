package com.yulian.citypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yulian.citypulse.BuildConfig
import com.yulian.citypulse.data.model.WeatherResponse
import com.yulian.citypulse.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState

    fun getWeather(city: String) {
        viewModelScope.launch {
            android.util.Log.d("API_KEY", "Key: ${BuildConfig.OPENWEATHER_API_KEY}")
            _weatherState.value = WeatherUiState.Loading
            repository.getWeather(city = city, apiKey = BuildConfig.OPENWEATHER_API_KEY).collect { result ->
                result.fold(
                    onSuccess = { weather ->
                        _weatherState.value = WeatherUiState.Success(weather)
                    },
                    onFailure = { error ->
                        _weatherState.value = WeatherUiState.Error(error.message ?: "Error desconocido")
                    }
                )
            }
        }
    }

    private val _city = MutableStateFlow("Medellin")
    val city: StateFlow<String> = _city

    fun searchCity(newCity: String) {
        _city.value = newCity
        getWeather(newCity)
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}