package com.example.testweatherappcilation


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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

        viewModel = ViewModelProvider(this).get(WeatherViewModel::class.java)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow.collect { it ->
                    val actualWeather = it.weather

                    val districtName = actualWeather?.geo_object?.district?.name
                    val localityName = actualWeather?.geo_object?.locality?.name
                    binding.textLocation.text = if (districtName == null) "$localityName" else "$districtName, $localityName"
                    binding.textActualTimeAndYesterdayTemp.text = "Сейчас ${viewModel.getActualTime()}. Вчера в это время ${viewModel.getYesterdayTemp()}°"
                    binding.textActualTemp.text = viewModel.getActualTemp()

                    //load condition image
                    SvgLoader.pluck()
                        .with(this@MainActivity)
                        .load(
                            "https://yastatic.net/weather/i/icons/funky/dark/${actualWeather?.fact?.icon}.svg", //"ovc" не работает ?
                            binding.imageCondition
                        );

                    binding.textCondition.text = "${actualWeather?.conditions?.get(actualWeather.fact?.condition)}"
                    binding.textFeelsLike.text = "Ощущается как ${actualWeather?.fact?.feels_like}°"

                    binding.wind.text = "Ветер ${actualWeather?.fact?.windDirMap?.get(actualWeather.fact.wind_dir)} ${actualWeather?.fact?.wind_speed} м/с"
                    binding.humidity.text = "Влажность ${actualWeather?.fact?.humidity} %"
                    binding.pressure.text = "Давление ${actualWeather?.fact?.pressure_mm} мм.рт.ст."


                    binding.btnGetWeatherAround.setOnClickListener {
                        if (isGPSEnable()){
                            when {
                                ContextCompat.checkSelfPermission(this@MainActivity,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
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

                    val recyclerAdapter = ForecastRecyclerViewAdapter(actualWeather, this@MainActivity)
                    val recyclerForecasts = findViewById<RecyclerView>(R.id.recycler_forecasts)
                    recyclerForecasts.adapter = recyclerAdapter

                    binding.btnGetWeatherByCoordinates.setOnClickListener {
                        try {
                            viewModel.lat = binding.editLatitude.text.toString().toDouble()
                            viewModel.lon = binding.editLongitude.text.toString().toDouble()
                            viewModel.getWeatherByCoordinates()

                        } catch (e: Throwable) {
                            toastWrongCoordinates()
                        }
                    }
                    binding.btnTokyo.setOnClickListener {
                        viewModel.getTokyoWeather()
                    }
                    binding.btnOttawa.setOnClickListener {
                        viewModel.getOttawaWeather()
                    }
                    binding.btnKigali.setOnClickListener {
                        viewModel.getKigaliWeather()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        SvgLoader.pluck().close();
    }

    fun toastWrongCoordinates() {
        Toast.makeText(this@MainActivity, "Wrong coordinates", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@MainActivity, "Использование геолокации запрещено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMessageLocatonPermissionRequirement() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.message_location_permission_requirement))
            .setPositiveButton("OK") {_: DialogInterface, _: Int ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            .setNegativeButton("Cancel", null)
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
            .setMessage("Please turn ON location service (GPS)")
            .setPositiveButton("OK") { _: DialogInterface, _: Int
                ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                requestLocationPermission()
            }
            .setNegativeButton("Cancel", null)
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