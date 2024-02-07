package com.example.testweatherappcilation.mvp.presenters

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.LocationManager
import android.view.View
import androidx.core.content.ContextCompat

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import com.example.testweatherappcilation.mvp.models.entity.MainRepository
import com.example.testweatherappcilation.mvp.models.entity.WeatherUiModel
import com.example.testweatherappcilation.mvp.models.repository.DataStoreRepository
import com.example.testweatherappcilation.mvp.views.MainActivityView
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import moxy.presenterScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

@InjectViewState
class MainActivityPresenter(
    dataStore: DataStore<Preferences>,
    private val repository: MainRepository
) : MvpPresenter<MainActivityView>() {

    private val dataStoreRepository = DataStoreRepository(dataStore)

    private var coordinates: LatLng = LatLng(0.0, 0.0)
    private var model: WeatherUiModel = WeatherUiModel()

    init {
        presenterScope.launch {
            loadLastLocation()?.let { coordinates ->
                viewState.showWeather(repository.getModel(coordinates))
            } ?: viewState.showWelcome()
        }
    }

    suspend fun showWeather(coordinates: LatLng = this.coordinates) {
        try {
            viewState.showLoading()
            getWeatherByCoordinates(coordinates)
            viewState.showWeather(model)
        } catch (throwable: IllegalArgumentException) {
            viewState.showError()
        }
    }

    private suspend fun getWeatherByCoordinates(coordinates: LatLng) {
        this.coordinates = coordinates
        saveLastLocation(coordinates)
        model = repository.getModel(coordinates)
    }

    private suspend fun saveLastLocation(coordinates: LatLng) {
        dataStoreRepository.saveLastWeatherEntity(coordinates)
    }

    private suspend fun loadLastLocation(): LatLng? {
        return dataStoreRepository.loadLastWeatherEntity()
    }

    suspend fun getTokyoWeather() {
        getWeatherByCoordinates(LatLng(35.6895, 139.692))
        showWeather()
    }

    suspend fun getRostovWeather() {
        getWeatherByCoordinates(LatLng(47.222078, 39.720358))
        showWeather()
    }

    suspend fun getAbinskWeather() {
        getWeatherByCoordinates(LatLng(44.86623764, 38.15129089))
        showWeather()
    }


    fun getWeatherAround() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        if (ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().build(), CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                lifecycleScope.launch {
                    presenter.showWeather(LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    if (isGPSEnable())    {
        when {
            ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                binding.contentWeatherView.visibility = View.VISIBLE
                getWeatherAround()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                viewState.showMessageLocationPermissionRequirement()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    } else
    {
        viewState.showMessageGPSRequirement()
    }

    private fun isGPSEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        }
        return false
    }


}

