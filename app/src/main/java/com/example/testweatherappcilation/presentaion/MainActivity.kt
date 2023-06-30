package com.example.testweatherappcilation.presentaion


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.ContentState
import com.example.testweatherappcilation.domain.ForecastRecyclerViewAdapter
import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("last weather entity")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var viewModel: WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()

        binding.contentWeatherView.visibility = View.GONE

        viewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(dataStore)
        ).get(WeatherViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow
                    .onEach { state ->
                        when (state.contentState) {
                            ContentState.Idle, ContentState.Loading -> {
                                binding.contentWeatherView.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.GONE
                                binding.includeProgressLayout.root.visibility = View.VISIBLE
                            }

                            ContentState.Error.Common, ContentState.Error.Network -> {
                                val buttonRetry = findViewById<Button>(R.id.buttonRetry)
                                val errorMessage = findViewById<TextView>(R.id.errorMessage)
                                if (state.contentState == ContentState.Error.Network) {
                                    errorMessage.text = getString(R.string.error_message_network)
                                }
                                buttonRetry.setOnClickListener {
                                    viewModel.fetchData(viewModel.lat, viewModel.lon)
                                }

                                binding.contentWeatherView.visibility = View.GONE
                                binding.includeProgressLayout.root.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.VISIBLE
                            }

                            ContentState.Done -> {
                                binding.contentWeatherView.visibility = View.VISIBLE
                                binding.includeProgressLayout.root.visibility = View.GONE
                                binding.includeErrorLayout.root.visibility = View.GONE
                            }
                        }
                    }
                    .collect { state -> //todo переименовать  в  state  полем текст локешн в котором будет текст только.

                        val actualWeather = state.weatherEntity
                        if (actualWeather != null) {
                            binding.contentWeatherView.visibility = View.VISIBLE
                        }

                        val districtName = actualWeather?.districtName
                        val localityName = actualWeather?.localityName ?: getString(R.string.location_not_idetified)
                        binding.textLocation.text =
                            if (districtName == null || districtName == "") localityName else "$districtName, $localityName"
                        binding.textActualTimeAndYesterdayTemp.text = getString(
                            R.string.actual_time_and_yesterday_temp,
                            viewModel.getActualTime(),
                            viewModel.getYesterdayTemp()
                        ) //во ВМ в мапере сделать логику для
                        binding.textActualTemp.text = viewModel.getActualTemp()


                        //load condition image
                        SvgLoader.pluck()
                            .with(this@MainActivity)
                            .load(
                                getString(
                                    R.string.condition_icon_link,
                                    actualWeather?.icon
                                ), //"ovc" не работает ?
                                binding.imageCondition
                            )

                        binding.textCondition.text = actualWeather?.condition?.let {
                            getString(resources.getIdentifier(it,"string",packageName))}
                        binding.textFeelsLike.text =
                            getString(R.string.feels_like, actualWeather?.feelsLike)
                        binding.wind.text = actualWeather?.windDirection?.let {
                            getString(R.string.wind,
                                getString(resources.getIdentifier(it,"string", packageName)),
                                actualWeather.windSpeed)}
                        binding.humidity.text =
                            getString(R.string.humidity, actualWeather?.humidity)
                        binding.pressure.text =
                            getString(R.string.pressure, actualWeather?.pressure)

                        val recyclerAdapter = ForecastRecyclerViewAdapter(
                            actualWeather?.forecasts,
                            this@MainActivity
                        ) // создаем адаптер вверху и далее диффутилз.
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
                    viewModel.lat = lat
                    viewModel.lon = lon
                    viewModel.getWeatherByCoordinates()
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
        binding.btnOttawa.setOnClickListener {
            viewModel.getRostovWeather()
        }
        binding.btnKigali.setOnClickListener {
            viewModel.getAbinskWeather()
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
                CancellationTokenSource().token //todo изучить
            ).addOnSuccessListener { location ->
                viewModel.lat = location.latitude
                viewModel.lon = location.longitude
                viewModel.getWeatherByCoordinates()
            }
        }
    }
}