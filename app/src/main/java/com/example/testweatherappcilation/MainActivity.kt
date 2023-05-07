package com.example.testweatherappcilation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: WeatherViewModel by viewModels()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow.collect { it ->
                    val actualWeather = it.actualWeather

                    binding.textLocation.text =
                        "${actualWeather?.geo_object?.district?.name ?: ""}, ${actualWeather?.geo_object?.locality?.name ?: ""}" //todo поравить, чтобы при нал уходила запятая

                    val dataTemp = actualWeather?.yesterday?.temp
                    val formatter = DateTimeFormatter.ISO_TIME

//                    val offset = actualWeather?.info?.tzinfo?.offset?.div(360)

                    val actualTime = LocalTime.parse(actualWeather?.now_dt?.subSequence(11,16) ?: "09:54", formatter) //todo добавить сдвиг на пояс

                    val yesterdayTemp: String =
                        if ((dataTemp != null) && (dataTemp > 0)) "+$dataTemp" else "$dataTemp"

                    val degreeSymbol = "\u00B0"
                    binding.textActualTimeAndYesterdayTemp.text =
                        "Сейчас ${actualTime}. Вчера в это время $yesterdayTemp$degreeSymbol" //todo время взять из АПИ или из Форматтер даты ?

                    val temp = actualWeather?.fact?.temp
                    val actualTemperature: String =
                        if (temp != null && temp > 0) "+$temp$degreeSymbol" else "$temp$degreeSymbol"
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
                    binding.textFeelsLike.text = "Ощущается как ${actualWeather?.fact?.feels_like}"

                    binding.btnGetWeatherAround.setOnClickListener {
                        when (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            ACCESS_COARSE_LOCATION
                        )) {
                            PERMISSION_GRANTED -> {
                                fusedLocationClient =
                                    LocationServices.getFusedLocationProviderClient(this@MainActivity)

                                fusedLocationClient.getCurrentLocation(
                                    CurrentLocationRequest.Builder().build(),
                                    CancellationTokenSource().token //todo изучить
                                ).addOnSuccessListener { location ->
                                    try {
                                        val lat: Double = location.latitude
                                        val lon: Double = location.longitude

                                        Log.i("WIIW", "${lat}, ${lon}")

                                        if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                                            viewModel.getWeatherByCoordinates(lat, lon)
                                        } else {
                                            toastWrongCoordinates()
                                        }

                                    } catch (e: Throwable) {
                                        toastWrongCoordinates()
                                    }
                                }

//


                            }
//todo check GPS enable
                            PERMISSION_DENIED -> {

                                val requestPermissionLauncher = registerForActivityResult(
                                    ActivityResultContracts.RequestPermission()
                                ) {
                                    if (ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            ACCESS_COARSE_LOCATION
                                        ) == PERMISSION_GRANTED
                                    ) {
//                                        getWeatherAround()


                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Нужно разремение на локацию",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                requestPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
                            }
                        }
                    }


                    binding.btnGetWeatherByCoordinates.setOnClickListener {
                        try {
                            val lat: Double = binding.editLatitude.text.toString().toDouble()
                            val lon: Double = binding.editLongitude.text.toString().toDouble()

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
        Toast.makeText(
            this@MainActivity,
            "Wrong coordinates",
            Toast.LENGTH_LONG
        )
            .show()
    }
}