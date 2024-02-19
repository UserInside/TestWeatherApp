package com.example.testweatherappcilation.mvp.ui.presenter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.testweatherappcilation.mvp.data.repository.MainRepository
import com.example.testweatherappcilation.mvp.ui.views.activities.MainActivityView
import com.example.testweatherappcilation.mvp.data.repository.DataStoreRepository
import com.example.testweatherappcilation.mvp.data.repository.LocationRepository
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope
import com.google.android.gms.maps.model.LatLng

@InjectViewState
class MainActivityPresenter(
    dataStore: DataStore<Preferences>,
    private val repository: MainRepository,
    context: Context
) : MvpPresenter<MainActivityView>() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    private val locationRepository = LocationRepository(context, this)

    private var coordinates: LatLng = LatLng(0.0, 0.0)

    init {
        viewState.showWelcome()
        presenterScope.launch {
            loadLastLocation()?.let {
                viewState.showWeather(repository.getModel(coordinates))
            }
        }
    }
    //todo сделать загрузку без нулдей

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

    suspend fun updateLocation() {
        coordinates = locationRepository.getLocation()
        showWeather(coordinates)
    }

    fun requestLocationPermission() {
        viewState.requestLocationPermission()
    }
}

