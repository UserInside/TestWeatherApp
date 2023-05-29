package com.example.testweatherappcilation


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch


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

        binding.frameWeather.visibility =View.GONE

        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { it -> //переименовать  в  state  полем текст локешн в котором будет текст только.
                    val actualWeather = it.weather

                    val districtName = actualWeather?.geo_object?.district?.name
                    val localityName = actualWeather?.geo_object?.locality?.name
                    binding.textLocation.text = if (districtName == null) "$localityName" else "$districtName, $localityName"
                    binding.textActualTimeAndYesterdayTemp.text = getString(R.string.actual_time_and_yesterday_temp, viewModel.getActualTime(), viewModel.getYesterdayTemp()) //во ВМ в мапере сделать логику для
                    binding.textActualTemp.text = viewModel.getActualTemp()

                    //load condition image
                    SvgLoader.pluck()
                        .with(this@MainActivity)
                        .load(
                            getString(R.string.condition_icon_link, actualWeather?.fact?.icon), //"ovc" не работает ?
                            binding.imageCondition
                        );

                    binding.textCondition.text = actualWeather?.conditions?.get(actualWeather.fact?.condition)
                    binding.textFeelsLike.text = getString(R.string.feels_like, actualWeather?.fact?.feels_like)

                    binding.wind.text = getString(R.string.wind, actualWeather?.fact?.windDirMap?.get(actualWeather.fact.wind_dir), actualWeather?.fact?.wind_speed)
                    binding.humidity.text = getString(R.string.humidity, actualWeather?.fact?.humidity)
                    binding.pressure.text = getString(R.string.pressure, actualWeather?.fact?.pressure_mm)

                    val recyclerAdapter = ForecastRecyclerViewAdapter(actualWeather, this@MainActivity) // создаем адаптер вверху и далее диффутилз.
                    val recyclerForecasts = findViewById<RecyclerView>(R.id.recycler_forecasts)
                    recyclerForecasts.adapter = recyclerAdapter
                }
            }
        }

        binding.btnGetWeatherAround.setOnClickListener {
            if (isGPSEnable()){
                when {
                    ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                        binding.frameWeather.visibility = View.VISIBLE
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
                viewModel.lat = binding.editLatitude.text.toString().toDouble()
                viewModel.lon = binding.editLongitude.text.toString().toDouble()
                binding.frameWeather.visibility = View.VISIBLE
                viewModel.getWeatherByCoordinates()

            } catch (e: Throwable) {
                toastWrongCoordinates()
            }
        }

        binding.btnTokyo.setOnClickListener {
            binding.frameWeather.visibility = View.VISIBLE
            viewModel.getTokyoWeather()
        }
        binding.btnOttawa.setOnClickListener {
            binding.frameWeather.visibility = View.VISIBLE
            viewModel.getOttawaWeather()
        }
        binding.btnKigali.setOnClickListener {
            binding.frameWeather.visibility = View.VISIBLE
            viewModel.getAbinskWeather()
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        SvgLoader.pluck().close();
    }

    fun toastWrongCoordinates() {
        Toast.makeText(this@MainActivity, getString(R.string.wrong_coordinates), Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@MainActivity, getString(R.string.location_access_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMessageLocatonPermissionRequirement() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.message_location_permission_requirement))
            .setPositiveButton(getString(R.string.button_ok)) {_: DialogInterface, _: Int ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .create()
            .show()
    }

    private fun isGPSEnable() : Boolean {
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

        if (ContextCompat.checkSelfPermission(this@MainActivity,
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