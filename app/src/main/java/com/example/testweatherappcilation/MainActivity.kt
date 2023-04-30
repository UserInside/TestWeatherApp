package com.example.testweatherappcilation

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.example.testweatherappcilation.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: WeatherViewModel by viewModels()

        val calendar = Calendar.getInstance()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow.collect {
                    val actualWeather = it.actualWeather

                    binding.textLocation.text =
                        "${actualWeather?.geo_object?.district?.name}, ${actualWeather?.geo_object?.locality?.name}"


                    binding.textLocation.text =
                        "${actualWeather?.geo_object?.district?.name ?: ""}, ${actualWeather?.geo_object?.locality?.name}" //todo поравить, чтобы при нал уходила запятая

                    val dataTemp = actualWeather?.yesterday?.temp
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val current = LocalDateTime.now().format(formatter)

                    val yesterdayTemp: String =
                        if (dataTemp!! > 0) "+$dataTemp" else "$dataTemp"  //todo delete non-null assert
                    binding.textActualTimeAndYesterdayTemp.text =
                        "Сейчас ${current}. Вчера в это время $yesterdayTemp o"

                    val temp = actualWeather.fact?.temp
                    val actualTemperature: String =
                        if (temp!! > 0) "+$temp o" else "${temp}o"  //todo delete non-null assert
                    binding.textActualTemp.text = actualTemperature

                    binding.textCondition.text = "${actualWeather.fact.condition}"
                    binding.textFeelsLike.text = "Ощущается как ${actualWeather?.fact?.feels_like}"

                    binding.btnGetWeather.setOnClickListener {
                        try {
                            val lat: Double = binding.editLatitude.text.toString().toDouble()
                            val lon: Double = binding.editLongitude.text.toString().toDouble()

                            if (lat in -90.0 .. 90.0 && lon in -180.0 .. 180.0) {
                                viewModel.getWeatherByCoordinates(lat, lon,)
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

    fun toastWrongCoordinates() {
        Toast.makeText(
            this@MainActivity,
            "Wrong coordinates",
            Toast.LENGTH_LONG)
            .show()
    }
}