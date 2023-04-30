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

    private fun fetchData() {
        try {
            viewModelScope.launch {
                val weatherEntity = getWeatherEntity()
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

    suspend fun getWeatherEntity(lat: Double = 55.75396, lon: Double = 37.620393): WeatherEntity {
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

}