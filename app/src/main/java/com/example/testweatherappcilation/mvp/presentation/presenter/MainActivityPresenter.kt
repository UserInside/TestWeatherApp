package com.example.testweatherappcilation.mvp.presentation.presenter

import com.example.testweatherappcilation.mvp.domain.repository.MainRepository
import com.example.testweatherappcilation.mvp.presentation.views.activities.MainActivityView
import com.example.testweatherappcilation.mvp.domain.repository.DataStoreRepository
import com.example.testweatherappcilation.mvp.domain.repository.LocationCallback
import com.example.testweatherappcilation.mvp.domain.repository.LocationRepositoryImpl
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope
import com.google.android.gms.maps.model.LatLng

@InjectViewState
class MainActivityPresenter(
    private val dataStoreRepository: DataStoreRepository,
    private val repository: MainRepository,
    private val locationRepository: LocationRepositoryImpl,
) : MvpPresenter<MainActivityView>(), LocationCallback {

    private lateinit var coordinates: LatLng

    init {
        locationRepository.setLocationCallback(this)
        viewState.showWelcome()
        presenterScope.launch {
            loadLastLocation()?.let {
                showWeather(it)
            }
        }
    }

    suspend fun showWeather(coordinates: LatLng = this.coordinates) {
        try {
            viewState.showLoading()
            this.coordinates = coordinates
            saveLastLocation(coordinates)
            viewState.showWeather(repository.getModel(coordinates))
        } catch (e: Exception) {
            viewState.showError()
        }
    }

    private suspend fun saveLastLocation(coordinates: LatLng) {
        dataStoreRepository.saveLastWeatherEntity(coordinates)
    }

    private suspend fun loadLastLocation(): LatLng? {
        return dataStoreRepository.loadLastWeatherEntity()
    }

    override fun requestLocationPermission() {
        viewState.requestLocationPermission()
    }

    fun showWeatherAround() {
        locationRepository.getCoordinates()
    }

    override fun onCoordinatesReceived(coordinates: LatLng) {
        this.coordinates = coordinates
        if (locationRepository.isLocationEnable) {
            presenterScope.launch {
                showWeather(coordinates)
            }
        }
    }
}

