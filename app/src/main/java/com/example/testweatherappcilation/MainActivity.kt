package com.example.testweatherappcilation


import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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
                    val actualWeather = it.actualWeather

                    val districtName = actualWeather?.geo_object?.district?.name
                    val localityName = actualWeather?.geo_object?.locality?.name
                    binding.textLocation.text =
                        if (districtName == null) "$localityName" else "$districtName, $localityName"

                    val yesterdayTempData = actualWeather?.yesterday?.temp
                    val yesterdayTemp: String =
                        if ((yesterdayTempData != null) && (yesterdayTempData > 0)) "+$yesterdayTempData" else "$yesterdayTempData"

                    val formatter = DateTimeFormatter.ISO_TIME
//                    val offset = actualWeather?.info?.tzinfo?.offset?.div(360)
                    Log.e("WOW", "${actualWeather?.now_dt?.subSequence(11, 16)}")
                    val actualTime = LocalTime.parse(
                        actualWeather?.now_dt?.subSequence(11, 16) ?: "09:54", formatter
                    ) //todo добавить сдвиг на пояс . и провеить почеру без элвиса не работает
                    binding.textActualTimeAndYesterdayTemp.text = "Сейчас ${actualTime}. Вчера в это время $yesterdayTemp°"

                    val actualTempData = actualWeather?.fact?.temp
                    val actualTemperature: String =
                        if (actualTempData != null && actualTempData > 0) "+$actualTempData°" else "$actualTempData°"
                    binding.textActualTemp.text = actualTemperature

                    //load condition image
                    SvgLoader.pluck()
                        .with(this@MainActivity)
                        .load(
                            "https://yastatic.net/weather/i/icons/funky/dark/${actualWeather?.fact?.icon}.svg", //"ovc" не работает ?
                            binding.imageCondition
                        );

                    binding.textCondition.text =
                        "${actualWeather?.fact?.conditionsMap?.get(actualWeather.fact.condition)}"
                    binding.textFeelsLike.text = "Ощущается как ${actualWeather?.fact?.feels_like}°"

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
                Toast.makeText(this@MainActivity, "В доступе отказано", Toast.LENGTH_SHORT).show()
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
//        Log.i("WOW", "8")

    }
}