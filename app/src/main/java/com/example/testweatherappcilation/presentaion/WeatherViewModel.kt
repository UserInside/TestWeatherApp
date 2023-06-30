package com.example.testweatherappcilation.presentaion


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.testweatherappcilation.ContentState
import com.example.testweatherappcilation.domain.DataStoreRepository
import com.example.testweatherappcilation.domain.WeatherRepository
import com.example.testweatherappcilation.data.DataHttpClient
import com.example.testweatherappcilation.data.WeatherRepositoryImplementation
import com.example.testweatherappcilation.domain.WeatherEntity
import com.example.testweatherappcilation.domain.WeatherInteractor
import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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

    private val _stateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState())
    val stateFlow: StateFlow<WeatherUiState> = _stateFlow.asStateFlow()

    var lat: Double = 0.0
    var lon: Double = 0.0

    init {
        viewModelScope.launch {
            val lastShownWeather = loadLasWeatherEntity()
            lastShownWeather?.let { savedWeather ->
                _stateFlow.update { state ->
                    state.copy(
                        weatherEntity = savedWeather,
                        contentState = ContentState.Done
                    )
                }
            }
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _stateFlow.update { state ->
            state.copy(
                contentState = if (throwable is ConnectTimeoutException
                    || throwable is ConnectException
                    || throwable is UnknownHostException
                    || throwable is SocketTimeoutException
                ) ContentState.Error.Network
                else {
//                    Log.e("Lolshto", "error", throwable)
                    ContentState.Error.Common
                }
            )
        }
    }

    fun fetchData(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
        if (_stateFlow.value.contentState == ContentState.Loading) return

        _stateFlow.update { state -> state.copy(contentState = ContentState.Loading) }
        viewModelScope.launch(exceptionHandler) {
            val weatherEntity = getWeatherEntity(lat, lon)
            saveLastWeatherEntity(weatherEntity)
            _stateFlow.update { state ->
                state.copy(
                    weatherEntity = weatherEntity,
                    contentState = ContentState.Done
                )
            }
        }
    }

    suspend fun saveLastWeatherEntity(weatherEntity: WeatherEntity) {
        dataStoreRepository.saveLastWeatherEntity(weatherEntity)
    }

    suspend fun loadLasWeatherEntity(): WeatherEntity? {
        return dataStoreRepository.loadLastWeatherEntity()
    }

    suspend fun getWeatherEntity(lat: Double, lon: Double): WeatherEntity {
        val dataHttpClient = DataHttpClient(lat, lon)
        val weatherGateway: WeatherRepository = WeatherRepositoryImplementation(dataHttpClient)
        val weatherInteractor = WeatherInteractor(weatherGateway)
        val weatherEntity = weatherInteractor.fetchData()
        return weatherEntity
    }

    fun getWeatherByCoordinates() {
        try {
            fetchData(lat, lon)
        } catch (throwable: IllegalArgumentException) {
            Log.e("TAG", "Wrong coordinates", throwable)
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
        val actualTime = _stateFlow.value.weatherEntity?.dateTime?.let {
            OffsetDateTime.parse(it)
                .atZoneSameInstant(ZoneId.of(_stateFlow.value.weatherEntity?.timeZoneName))
                .toLocalTime().format(offsetFormatter)
        }
        return actualTime
    }

    fun getYesterdayTemp(): String {
        val yesterdayTempData = _stateFlow.value.weatherEntity?.yesterdayTemp
        val yesterdayTemp: String =
            if ((yesterdayTempData != null) && (yesterdayTempData > 0)) "+$yesterdayTempData" else "$yesterdayTempData"
        return yesterdayTemp
    }

    fun getActualTemp(): String {
        val actualTempData = _stateFlow.value.weatherEntity?.actualTemp
        val actualTemperature: String =
            if ((actualTempData != null) && (actualTempData > 0)) "+$actualTempData°" else "$actualTempData°"
        return actualTemperature
    }
}

