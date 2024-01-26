package com.example.testweatherappcilation.mvp.presenters

import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.testweatherappcilation.mvp.models.entity.MainRepository
import com.example.testweatherappcilation.mvp.models.entity.WeatherUiModel
import com.example.testweatherappcilation.mvp.views.MainActivityView
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope

@InjectViewState
class MainActivityPresenter(
    dataStore: DataStore<Preferences>,
    private val resources: Resources,
) : MvpPresenter<MainActivityView>() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var model: WeatherUiModel = WeatherUiModel()

    init {
        presenterScope.launch {
            val lastShownWeather = loadLastWeatherEntity()
            lastShownWeather?.let {
                viewState.showWeather(lastShownWeather)
            } ?: viewState.showWelcome()
        }
    }

    suspend fun showWeather(lat: Double = this.lat, lon: Double = this.lon) {
        try {
            viewState.showLoading()
            getWeatherByCoordinates(lat, lon)
            viewState.showWeather(model)
        } catch (throwable: IllegalArgumentException) {
            viewState.showError()
        }
    }

    suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
        model = MainRepository(resources).getModel(lat, lon)
    }

    suspend fun saveLastWeatherEntity(model: WeatherUiModel) {
        dataStoreRepository.saveLastWeatherEntity(model)
    }

    suspend fun loadLastWeatherEntity(): WeatherUiModel? {
        return dataStoreRepository.loadLastWeatherEntity()
    }

    suspend fun getTokyoWeather() {
        getWeatherByCoordinates(35.6895, 139.692)
        showWeather()
    }

    suspend fun getRostovWeather() {
        getWeatherByCoordinates(47.222078, 39.720358)
        showWeather()
    }

    suspend fun getAbinskWeather() {
        getWeatherByCoordinates(44.86623764, 38.15129089)
        showWeather()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
//        presenterScope.launch { }  //todo надо ли сюда перенести инит?
    }
}

