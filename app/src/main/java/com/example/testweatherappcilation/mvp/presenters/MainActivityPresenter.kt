package com.example.testweatherappcilation.mvp.presenters

import android.content.res.Resources
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.testweatherappcilation.mvp.models.MainRepository
import com.example.testweatherappcilation.mvp.models.WeatherUiModel
import com.example.testweatherappcilation.mvp.views.MainActivityView
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope

@InjectViewState
class MainActivityPresenter(
    dataStore: DataStore<Preferences>,
    val resources: Resources,
) : MvpPresenter<MainActivityView>() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    var model: WeatherUiModel = MainRepository(resources).repositoryModel

    init {
        presenterScope.launch {
            val lastShownWeather = loadLastWeatherEntity()
            lastShownWeather?.let { savedWeather ->
                _stateFlow.update { state -> // todo continue here
                    state.copy(
                        weatherUiModel = savedWeather,
                        contentState = ContentState.Done,
                    )
                }
            }
        }
    }


    fun showActualWeather() = viewState.showWeather(model)

    suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        try {
            _stateFlow.value.lat = lat
            _stateFlow.value.lon = lon
//            fetchData()

            model = MainRepository(resources).getModel(lat, lon)
            viewState.showWeather(model)
        } catch (throwable: IllegalArgumentException) {
            Log.e("TAG", "Wrong coordinates", throwable)
        }

    }

    suspend fun saveLastWeatherEntity(model: WeatherUiModel) {
        dataStoreRepository.saveLastWeatherEntity(model)
    }

    suspend fun loadLastWeatherEntity(): WeatherUiModel? { //todo
        return dataStoreRepository.loadLastWeatherEntity()
    }

    suspend fun getTokyoWeather() {
        model = MainRepository(resources).getModel(35.6895, 139.692)
        viewState.showWeather(model)
    }

    suspend fun getRostovWeather() {
        model = MainRepository(resources).getModel(47.222078, 39.720358)
        viewState.showWeather(model)
    }

    suspend fun getAbinskWeather() {
        model = MainRepository(resources).getModel(44.86623764, 38.15129089)
        viewState.showWeather(model)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
//        presenterScope.launch { }
    }

//    suspend fun fetchData() {
//        val weatherUiState = WeatherUiState()
//       // if (weatherUiState.contentState == ContentState.Loading) return //что это?
//
//        weatherUiState.weatherUiModel = DomainToPresentationMapper.map(
//            resources, getWeatherEntity(
//                weatherUiState.lat,
//                weatherUiState.lon
//            ))
//        weatherUiState.contentState = ContentState.Done
//    }

//        weatherUiState.weatherUiModel = coroutineScope {
//            val weatherEntity = getWeatherEntity(weatherUiState.lat, weatherUiState.lon)
//            val weatherUiModel = DomainToPresentationMapper.map(resources, weatherEntity)
////            saveLastWeatherEntity(weatherUiModel)
//            it = { state ->
//                state.copy(
//                    weatherUiModel = weatherUiModel,
//                    contentState = ContentState.Done,
//                )
//            }
//        }
//    }

//    suspend fun getWeatherEntity(lat: Double, lon: Double): WeatherEntity {
//        val weatherEntity = weatherInteractor.fetchData(lat, lon)
//        return weatherEntity
//    }

}

