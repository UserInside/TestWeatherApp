package com.example.testweatherappcilation


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class WeatherViewModelFactory(
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(dataStore) as T
    }
}

class WeatherViewModel(
    dataStore: DataStore<Preferences>
) : ViewModel() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    private val _stateFlow = MutableStateFlow(WeatherEntity(null))
    val stateFlow: StateFlow<WeatherEntity> = _stateFlow.asStateFlow()

    var lat: Double = 0.0
    var lon: Double = 0.0

    init {
        Log.i("WDataStore", "1")
        viewModelScope.launch {
            val lastWeatherEntity = loadLasWeatherEntity()
            Log.i("WDataStore", "$lastWeatherEntity")
            lastWeatherEntity?.let {actualWeather ->
                _stateFlow.update {
                    it.copy(
                        weather = actualWeather
                    )
                }
            }
        }
    }

    private fun fetchData(lat: Double, lon: Double) {
        try {
            viewModelScope.launch {
                val weatherEntity = getWeatherEntity(lat, lon)
                weatherEntity.weather?.let { saveLastWeatherEntity(it)
                    Log.i("WDataStore", "data saved to store $it")}
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

    suspend fun saveLastWeatherEntity(actualWeather: ActualWeather) {
        dataStoreRepository.saveLastWeatherEntity(actualWeather)
    }

    suspend fun loadLasWeatherEntity() :ActualWeather? {
        return dataStoreRepository.loadLastWeatherEntity()?.let { Json.decodeFromString<ActualWeather>(it) }
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

    fun getRostovWeather() {
        fetchData(47.222078, 39.720358)
    }

    fun getAbinskWeather() {
        fetchData(44.86623764, 38.15129089)
    }

    fun getActualTime(): String? {
        val offsetFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val actualTime = _stateFlow.value.weather?.nowDateTime?.let {
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