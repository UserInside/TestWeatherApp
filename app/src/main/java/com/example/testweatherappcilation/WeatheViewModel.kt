package com.example.testweatherappcilation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WeatherViewModel : ViewModel() {

    private val _stateFlow =
        MutableStateFlow(WeatherEntity(null))
    val stateFlow: StateFlow<WeatherEntity> = _stateFlow.asStateFlow()

    var lat: Double = 55.75396
    var lon: Double = 37.620393

    init {
        fetchData(lat, lon)
    }

    private fun fetchData(lat: Double, lon: Double) {
        try {
            viewModelScope.launch {
                val weatherEntity = getWeatherEntity(lat, lon)
                _stateFlow.update {
                    it.copy(
                        weather = weatherEntity.weather

                    )
                }
            }
        } catch (e: Throwable) {
            Log.e("TAG", "weather request does not work")
        }
    }

    suspend fun getWeatherEntity(lat: Double, lon: Double): WeatherEntity {
        val dataHttpClient = DataHttpClient(lat, lon)
        val weatherGateway: WeatherRepository = WeatherGatewayImplementation(dataHttpClient)
        val weatherInteractor = WeatherInteractor(weatherGateway)
        val weatherEntity = weatherInteractor.fetchData()
        return weatherEntity
    }

    fun getWeatherByCoordinates() {
        try {
            if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                fetchData(lat, lon)
            } else {
                throw IllegalArgumentException("Wrong coordinates")
            }

        } catch (e: IllegalArgumentException) {
            Log.e("TAG", "Wrong coordinates", e)
        }
    }

    fun getTokyoWeather() {
        fetchData(35.6895, 139.692)
    }

    fun getOttawaWeather() {
        fetchData(45.4112, -75.6981)
    }

    fun getKigaliWeather() {
        fetchData(-1.94995, 30.0588)
    }

    fun getActualTime() : String? {
        val offsetFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val actualTime = _stateFlow.value.weather?.now_dt?.let {
            OffsetDateTime.parse(it)
                .atZoneSameInstant(ZoneId.of(_stateFlow.value.weather?.info?.tzinfo?.name))
                .toLocalTime().format(offsetFormatter)
        }
        return actualTime
    }

    fun getYesterdayTemp(): String {
        val yesterdayTempData = _stateFlow.value.weather?.yesterday?.temp
        val yesterdayTemp: String =
            if ((yesterdayTempData != null) && (yesterdayTempData > 0)) "+$yesterdayTempData" else "$yesterdayTempData"
        return yesterdayTemp
    }

    fun getActualTemp(): String {
        val actualTempData = _stateFlow.value.weather?.fact?.temp
        val actualTemperature: String =
            if ((actualTempData != null) && (actualTempData > 0)) "+$actualTempData°" else "$actualTempData°"
        return actualTemperature
    }
}