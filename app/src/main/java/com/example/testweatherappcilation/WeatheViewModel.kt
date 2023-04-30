package com.example.testweatherappcilation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _stateFlow =
        MutableStateFlow(WeatherEntity(null)) //todo может аргумент другой. и можно ли тут нал ставить?
    val stateFlow: StateFlow<WeatherEntity> = _stateFlow.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData(lat: Double = 55.75396, lon: Double = 37.620393) {
        try {
            viewModelScope.launch {
                val weatherEntity = getWeatherEntity(lat, lon)
                _stateFlow.update {
                    it.copy(
                        actualWeather = weatherEntity.actualWeather

                    )
                }
            }
        } catch (e: Throwable) {
            Log.e("TAG", "weather request does not work")
        }
    }

    suspend fun getWeatherEntity(lat: Double, lon: Double): WeatherEntity {
        val dataHttpClient = DataHttpClient(lat, lon)
        val weatherGateway: WeatherGateway = WeatherGatewayImplementation(dataHttpClient)
        val weatherInteractor = WeatherInteractor(weatherGateway)
        val weatherEntity = weatherInteractor.fetchData()
        return weatherEntity
    }

    fun resetFields() {
        _stateFlow.value.actualWeather?.geo_object?.district?.name = ""
        _stateFlow.value.actualWeather?.geo_object?.locality?.name = ""

    }

    fun getWeatherByCoordinates(lat: Double, lon: Double) {
        fetchData(lat, lon)
    }

    fun getTokyoWeather() {
        fetchData(35.6895, 139.692)
    }

    fun getOttawaWeather() {
        fetchData(	45.4112, -75.6981)
    }

    fun getKigaliWeather() {
        fetchData(-1.94995, 30.0588)
    }

}