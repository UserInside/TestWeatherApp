package com.example.testweatherappcilation.presentation


import android.content.res.Resources
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.testweatherappcilation.common.ContentState
import com.example.testweatherappcilation.data.ApiToEntityMapper
import com.example.testweatherappcilation.domain.DataStoreRepository
import com.example.testweatherappcilation.domain.DomainToPresentationMapper
import com.example.testweatherappcilation.domain.WeatherEntity
import com.example.testweatherappcilation.domain.WeatherInteractor
import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherViewModel(
    dataStore: DataStore<Preferences>,
    val resources: Resources,
    val weatherInteractor: WeatherInteractor,
) : ViewModel() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    private val _stateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState())
    val stateFlow: StateFlow<WeatherUiState> = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val lastShownWeather = loadLastWeatherEntity()
            lastShownWeather?.let { savedWeather ->
                _stateFlow.update { state ->
                    state.copy(
                        weatherUiModel = savedWeather,
                        contentState = ContentState.Done,
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
                    ContentState.Error.Common
                }
            )
        }
    }

    fun fetchData() {
        Log.e("VIEWMODEL", "VEIWMODEL Fetch data")
        if (_stateFlow.value.contentState == ContentState.Loading) return

        _stateFlow.update { state -> state.copy(contentState = ContentState.Loading) }
        viewModelScope.launch(exceptionHandler) {
            Log.e("VIEWMODEL", "VIEWMODEL scope open")
            val weatherEntity = getWeatherEntity(_stateFlow.value.lat, _stateFlow.value.lon)
            Log.e("VIEWMODEL", "VIEWMODEL weatherEntity got")
            val weatherUiModel = DomainToPresentationMapper.map(resources, weatherEntity)
            Log.e("VIEWMODEL", "VIEWMODEL weatherUiModel mapped")
//            saveLastWeatherEntity(weatherUiModel)
            _stateFlow.update { state ->
                Log.e("VIEWMODEL", " Update weatherUIModel")
                state.copy(
                    weatherUiModel = weatherUiModel,
                    contentState = ContentState.Done,
                )
            }
        }
    }

    suspend fun getWeatherEntity(lat: Double, lon: Double): WeatherEntity {
        val weatherEntity = weatherInteractor.fetchData(lat, lon)
        return weatherEntity
    }

//    suspend fun saveLastWeatherEntity(weatherUiModel: WeatherUiModel) {
//        dataStoreRepository.saveLastWeatherEntity(weatherUiModel)
//    }

    suspend fun loadLastWeatherEntity(): WeatherUiModel? {
        return dataStoreRepository.loadLastWeatherEntity()
    }


    fun getWeatherByCoordinates(lat: Double, lon: Double) {
        try {
            _stateFlow.value.lat = lat
            _stateFlow.value.lon = lon
            fetchData()
        } catch (throwable: IllegalArgumentException) {
            Log.e("TAG", "Wrong coordinates", throwable)
        }
    }

    fun getTokyoWeather() {
        _stateFlow.value.lat = 35.6895
        _stateFlow.value.lon = 139.692
        Log.e("VIEWMODEL", "VIEWMODEL Tokio button clicked")
        fetchData()
    }

    fun getRostovWeather() {
        _stateFlow.value.lat = 47.222078
        _stateFlow.value.lon = 39.720358
        fetchData()
    }

    fun getAbinskWeather() {
        _stateFlow.value.lat = 44.86623764
        _stateFlow.value.lon = 38.15129089
        fetchData()
    }


    companion object {
        fun factory(
            dataStore: DataStore<Preferences>,
            resources: Resources,
            weatherInteractor: WeatherInteractor
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    WeatherViewModel(dataStore, resources, weatherInteractor)
                }
            }

        }
    }

}

