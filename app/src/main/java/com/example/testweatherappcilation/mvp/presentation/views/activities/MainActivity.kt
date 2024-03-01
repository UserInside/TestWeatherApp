package com.example.testweatherappcilation.mvp.presentation.views.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.example.testweatherappcilation.mvp.domain.repository.MainRepository
import com.example.testweatherappcilation.mvp.domain.entity.WeatherUiModel
import com.example.testweatherappcilation.mvp.domain.repository.DataStoreRepository
import com.example.testweatherappcilation.mvp.domain.repository.LocationRepositoryImpl
import com.example.testweatherappcilation.mvp.common.loadImageFromUrl
import com.example.testweatherappcilation.mvp.common.toastLocationAccessDenied
import com.example.testweatherappcilation.mvp.common.toastWrongCoordinates
import com.example.testweatherappcilation.mvp.presentation.presenter.MainActivityPresenter
import com.example.testweatherappcilation.mvp.presentation.views.adapters.ForecastRecyclerViewAdapter
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class MainActivity : MvpAppCompatActivity(), MainActivityView {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var recyclerAdapter: ForecastRecyclerViewAdapter

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("last shown weather")
    private val presenter by moxyPresenter {
        MainActivityPresenter(
            DataStoreRepository(dataStore),
            MainRepository(resources),
            LocationRepositoryImpl(this),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = requestPermissionLauncher()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetWeatherAround.setOnClickListener {
            lifecycleScope.launch {
                presenter.showWeatherAround()
                binding.contentWeatherView.visibility = View.VISIBLE
            }
        }

        binding.btnGetWeatherByCoordinates.setOnClickListener {
            try {
                val lat = binding.editLatitude.text.toString().toDouble()
                val lon = binding.editLongitude.text.toString().toDouble()
                if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                    val coordinates = LatLng(lat, lon)
                    lifecycleScope.launch {
                        presenter.showWeather(coordinates)
                    }
                } else {
                    toastWrongCoordinates()
                }
            } catch (e: Throwable) {
                toastWrongCoordinates()
            }
        }

        binding.btnTokyo.setOnClickListener {
            lifecycleScope.launch {
                presenter.showWeather(LatLng(35.6895, 139.692))
            }
        }
        binding.btnRostov.setOnClickListener {
            lifecycleScope.launch {
                presenter.showWeather(LatLng(47.222078, 39.720358))
            }
        }
        binding.btnAbinsk.setOnClickListener {
            lifecycleScope.launch {
                presenter.showWeather(LatLng(44.86623764, 38.15129089))
            }
        }

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(getColor(R.color.purple_700))
        swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                presenter.showWeather()
            }
            swipeRefresh.isRefreshing = false
        }
    }

    override fun showWeather(model: WeatherUiModel) {
        binding.apply {
            contentWeatherView.visibility = View.VISIBLE
            includeProgressLayout.root.visibility = View.GONE
            includeErrorLayout.root.visibility = View.GONE

            textLocation.text = model.textLocation
            textActualTimeAndYesterdayTemp.text = model.textActualTimeAndYesterdayTemp
            textActualTemp.text = model.textActualTemp

            imageCondition.loadImageFromUrl(getString(R.string.condition_icon_link, model.icon))

            textCondition.text = model.textCondition
            textFeelsLike.text = model.textFeelsLike
            wind.text = model.textWind
            humidity.text = model.textHumidity
            pressure.text = model.textPressure

            recyclerAdapter = ForecastRecyclerViewAdapter(model.forecasts, this@MainActivity)
            recyclerForecasts.adapter = recyclerAdapter
        }
    }

    override fun showWelcome() {
        binding.contentWeatherView.visibility = View.GONE
    }

    override fun showLoading() {
        binding.apply {
            contentWeatherView.visibility = View.INVISIBLE
            includeErrorLayout.root.visibility = View.INVISIBLE
            includeProgressLayout.root.visibility = View.VISIBLE
        }
    }

    override fun showError() {
        binding.apply {
            contentWeatherView.visibility = View.INVISIBLE
            includeErrorLayout.root.visibility = View.VISIBLE
            includeProgressLayout.root.visibility = View.INVISIBLE
        }

        val buttonRetry = findViewById<Button>(R.id.buttonRetry)
        buttonRetry.setOnClickListener {
            lifecycleScope.launch {
                presenter.showWeather()
            }
        }
    }

    override fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun requestPermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PERMISSION_GRANTED
            ) {
                lifecycleScope.launch {
                    presenter.showWeatherAround()
                }
            } else {
                toastLocationAccessDenied()
            }
        }
    }
}
