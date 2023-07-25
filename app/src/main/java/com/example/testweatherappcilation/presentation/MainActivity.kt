package com.example.testweatherappcilation.presentation


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.common.ContentState
import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.common.HttpClientHolder
import com.example.testweatherappcilation.data.WeatherDataSource
import com.example.testweatherappcilation.data.WeatherRepositoryImplementation
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.example.testweatherappcilation.domain.WeatherInteractor
import com.example.testweatherappcilation.domain.WeatherRepository
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("last weather entity")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var recyclerAdapter: ForecastRecyclerViewAdapter

    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModel.factory(
            dataStore,
            resources,
            WeatherInteractor(
                WeatherRepositoryImplementation(
                    WeatherDataSource(HttpClientHolder.httpClient)
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()

        binding.contentWeatherView.visibility = View.GONE

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow
                    .collect { state ->
                        when (state.contentState) {
                            ContentState.Idle, ContentState.Loading -> {
                                binding.contentWeatherView.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.GONE
                                binding.includeProgressLayout.root.visibility = View.VISIBLE
                            }

                            ContentState.Error.Common, ContentState.Error.Network -> {
                                binding.contentWeatherView.visibility = View.GONE
                                binding.includeProgressLayout.root.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.VISIBLE

                                val buttonRetry = findViewById<Button>(R.id.buttonRetry)
                                buttonRetry.setOnClickListener {
                                    viewModel.fetchData()
                                }

                                val errorMessage = findViewById<TextView>(R.id.errorMessage)
                                if (state.contentState == ContentState.Error.Network) {
                                    errorMessage.text = getString(R.string.error_message_network)
                                }
                            }

                            ContentState.Done -> {
                                binding.contentWeatherView.visibility = View.VISIBLE
                                binding.includeProgressLayout.root.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.GONE
                            }
                        }

                        val uiModel = state.weatherUiModel

                        if (uiModel != null) {
                            binding.contentWeatherView.visibility = View.VISIBLE //todo попробовать это выше в фигурную вставить
                        }

                        binding.textLocation.text = uiModel?.textLocation
                        binding.textActualTimeAndYesterdayTemp.text = uiModel?.textActualTimeAndYesterdayTemp
                        binding.textActualTemp.text = uiModel?.textActualTemp

                        SvgLoader.pluck()
                            .with(this@MainActivity)
                            .load(
                                getString(
                                    R.string.condition_icon_link,
                                    uiModel?.icon
                                ),
                                binding.imageCondition
                            )

                        binding.textCondition.text = uiModel?.textCondition
                        binding.textFeelsLike.text = uiModel?.textFeelsLike
                        binding.wind.text = uiModel?.textWind
                        binding.humidity.text = uiModel?.textHumidity
                        binding.pressure.text = uiModel?.textPressure

                        recyclerAdapter = ForecastRecyclerViewAdapter(
                            uiModel?.forecasts,
                            this@MainActivity
                        ) // todo создаем адаптер вверху и далее диффутилз.
                        val recyclerForecasts = findViewById<RecyclerView>(R.id.recycler_forecasts)
                        recyclerForecasts.adapter = recyclerAdapter
                    }
            }
        }

        binding.btnGetWeatherAround.setOnClickListener {
            if (isGPSEnable()) {
                when {
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        binding.contentWeatherView.visibility = View.VISIBLE
                        getWeatherAround()
                    }

                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                        showMessageLocatonPermissionRequirement()
                    }

                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }
            } else {
                showMessageGPSRequirement()
            }
        }
        binding.btnGetWeatherByCoordinates.setOnClickListener {
            try {
                val lat = binding.editLatitude.text.toString().toDouble()
                val lon = binding.editLongitude.text.toString().toDouble()
                if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                    viewModel.getWeatherByCoordinates(lat, lon)
                } else {
                    toastWrongCoordinates()
                }
            } catch (e: Throwable) {
                toastWrongCoordinates()
            }
        }
        binding.btnTokyo.setOnClickListener {
            viewModel.getTokyoWeather()
        }
        binding.btnRostov.setOnClickListener {
            viewModel.getRostovWeather()
        }
        binding.btnAbinsk.setOnClickListener {
            viewModel.getAbinskWeather()
        }

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(getColor(R.color.purple_700))
        swipeRefresh.setOnRefreshListener {
            viewModel.fetchData()
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        SvgLoader.pluck().close();
    }

    fun toastWrongCoordinates() {
        Toast.makeText(this@MainActivity, getString(R.string.wrong_coordinates), Toast.LENGTH_LONG)
            .show()
    }

    fun requestLocationPermission() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PERMISSION_GRANTED
            ) {
                getWeatherAround()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.location_access_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showMessageLocatonPermissionRequirement() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.message_location_permission_requirement))
            .setPositiveButton(getString(R.string.button_ok)) { _: DialogInterface, _: Int ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .create()
            .show()
    }

    private fun isGPSEnable(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false
        }
        return true
    }

    private fun showMessageGPSRequirement() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.gps_turn_on))
            .setPositiveButton(getString(R.string.button_ok)) { _: DialogInterface, _: Int
                ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                requestLocationPermission()
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .create()
            .show()
    }

    private fun getWeatherAround() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder().build(),
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                viewModel.getWeatherByCoordinates(location.latitude, location.longitude)
            }
        }
    }



}