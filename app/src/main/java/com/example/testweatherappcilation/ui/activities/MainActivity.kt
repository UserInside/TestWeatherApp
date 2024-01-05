package com.example.testweatherappcilation.ui.activities

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.example.testweatherappcilation.mvp.models.WeatherUiModel
import com.example.testweatherappcilation.mvp.presenters.MainActivityPresenter
import com.example.testweatherappcilation.ui.adapters.ForecastRecyclerViewAdapter
import com.example.testweatherappcilation.mvp.views.MainActivityView
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class MainActivity : MvpAppCompatActivity(), MainActivityView {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var recyclerAdapter: ForecastRecyclerViewAdapter

    private val presenter by moxyPresenter { MainActivityPresenter(resources) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermission()

        binding.contentWeatherView.visibility = View.VISIBLE

        lifecycleScope.launch {

        }
//                presenter { state ->
//
//                    val uiModel = state.weatherUiModel
//                    if (uiModel != null) binding.contentWeatherView.visibility = View.VISIBLE // зачем оно нам?
        /**
        when (state.contentState) {
        ContentState.Idle, ContentState.Loading -> {
        Log.i("ERRORSTATE", "${state.contentState}") // todo почистить от логов
        binding.contentWeatherView.visibility = View.GONE
        binding.includeErrorLayout.root.visibility = View.GONE
        binding.includeProgressLayout.root.visibility = View.VISIBLE
        Log.i("ContentState", "${binding.contentWeatherView.visibility}")
        Log.i("ContentState", "${binding.includeErrorLayout.root.visibility}")
        Log.i(
        "ContentState",
        "${binding.includeProgressLayout.root.visibility}"
        )
        }

        ContentState.Error.Common, ContentState.Error.Network -> {
        Log.i("ERRORSTATE", "${state.contentState}")
        binding.contentWeatherView.visibility = View.GONE
        binding.includeErrorLayout.root.visibility = View.VISIBLE
        binding.includeProgressLayout.root.visibility = View.GONE
        Log.i("ContentState", "${binding.contentWeatherView.visibility}")
        Log.i("ContentState", "${binding.includeErrorLayout.root.visibility}")
        Log.i(
        "ContentState",
        "${binding.includeProgressLayout.root.visibility}"
        )

        val buttonRetry = findViewById<Button>(R.id.buttonRetry)
        buttonRetry.setOnClickListener {
        Log.i("ERRORSTATE", "retry button")
        viewModel.fetchData()
        }

        val errorMessage = findViewById<TextView>(R.id.errorMessage)
        if (state.contentState == ContentState.Error.Network) {
        errorMessage.text = getString(R.string.error_message_network)
        }
        }

        ContentState.Done -> {
        Log.i("ERRORSTATE", "${state.contentState}")
        binding.contentWeatherView.visibility = View.VISIBLE
        binding.includeProgressLayout.root.visibility = View.GONE
        binding.includeErrorLayout.root.visibility = View.GONE
        Log.i("ContentState", "${binding.contentWeatherView.visibility}")
        Log.i("ContentState", "${binding.includeErrorLayout.root.visibility}")
        Log.i(
        "ContentState",
        "${binding.includeProgressLayout.root.visibility}"
        )
        }
        }
         **/


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
                        showMessageLocationPermissionRequirement()
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
                    lifecycleScope.launch {
                        presenter.getWeatherByCoordinates(lat, lon)
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
                presenter.getTokyoWeather()
            }
        }

        binding.btnRostov.setOnClickListener {
            lifecycleScope.launch {
                presenter.getRostovWeather()
            }
        }
        binding.btnAbinsk.setOnClickListener {
            lifecycleScope.launch {
                presenter.getAbinskWeather()
            }
        }

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(getColor(R.color.purple_700))
        swipeRefresh.setOnRefreshListener {
            presenter.showActualWeather()
            swipeRefresh.isRefreshing = false
        }
    }

    override fun showWeather(model: WeatherUiModel) {
        binding.textLocation.text = model.textLocation
        binding.textActualTimeAndYesterdayTemp.text =
            model.textActualTimeAndYesterdayTemp
        binding.textActualTemp.text = model.textActualTemp

        SvgLoader.pluck()
            .with(this@MainActivity)
            .load(
                getString(
                    R.string.condition_icon_link,
                    model.icon
                ),
                binding.imageCondition
            )

        binding.textCondition.text = model.textCondition
        binding.textFeelsLike.text = model.textFeelsLike
        binding.wind.text = model.textWind
        binding.humidity.text = model.textHumidity
        binding.pressure.text = model.textPressure

        recyclerAdapter = ForecastRecyclerViewAdapter(model.forecasts, this@MainActivity)
        binding.recyclerForecasts.adapter = recyclerAdapter

    }

    private fun requestLocationPermission() {
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
    private fun showMessageLocationPermissionRequirement() {
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
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        }
        return false
    }

    private fun showMessageGPSRequirement() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.gps_turn_on))
            .setPositiveButton(getString(R.string.button_ok)) { _: DialogInterface, _: Int
                ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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
                lifecycleScope.launch {
                    presenter.getWeatherByCoordinates(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun toastWrongCoordinates() {
        Toast.makeText(
            this@MainActivity,
            getString(R.string.wrong_coordinates),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        SvgLoader.pluck().close()
    }

}