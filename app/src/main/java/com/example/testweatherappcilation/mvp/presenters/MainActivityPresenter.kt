package com.example.testweatherappcilation.mvp.presenters

import android.content.res.Resources

import com.example.testweatherappcilation.mvp.common.HttpClientHolder
import com.example.testweatherappcilation.mvp.models.ApiToEntityMapper
import com.example.testweatherappcilation.mvp.models.DomainToPresentationMapper
import com.example.testweatherappcilation.mvp.models.MainRepository
import com.example.testweatherappcilation.mvp.models.WeatherDataSource
import com.example.testweatherappcilation.mvp.models.WeatherEntity
import com.example.testweatherappcilation.mvp.models.WeatherRepository
import com.example.testweatherappcilation.mvp.models.WeatherRepositoryImplementation
import com.example.testweatherappcilation.mvp.models.WeatherUiModel
import com.example.testweatherappcilation.mvp.views.MainActivityView
import com.example.testweatherappcilation.mvp.views.WeatherUiState
import com.example.testweatherappcilation.mvp.views.WeatherViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope

@InjectViewState
class MainActivityPresenter(
    val resources: Resources,
) : MvpPresenter<MainActivityView>() {

    var model: WeatherUiModel = MainRepository(resources).repositoryModel

    suspend fun getWeatherByCoordinates(lat: Double, lon: Double) {
        model = MainRepository(resources).getModel(lat, lon)
        viewState.showCityWeather(model)
    }

    suspend fun getTokyoWeather() {
        model = MainRepository(resources).getModel(35.6895, 139.692)
        viewState.showCityWeather(model)
    }

    suspend fun getRostovWeather() {
        model = MainRepository(resources).getModel(47.222078, 39.720358)
        viewState.showCityWeather(model)
    }

    suspend fun getAbinskWeather() {
        model = MainRepository(resources).getModel(44.86623764, 38.15129089)
        viewState.showCityWeather(model)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        presenterScope.launch { }
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

