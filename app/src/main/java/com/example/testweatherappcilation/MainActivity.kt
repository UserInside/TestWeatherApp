package com.example.testweatherappcilation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.repeatOnLifecycle
import com.ahmadrosid.svgloader.SvgLoader
import com.example.testweatherappcilation.databinding.ActivityMainBinding
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
                        "${actualWeather?.geo_object?.district?.name ?: ""}, ${actualWeather?.geo_object?.locality?.name ?: ""}" //todo поравить, чтобы при нал уходила запятая

                    val dataTemp = actualWeather?.yesterday?.temp
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    val current = LocalDateTime.now().format(formatter)
                    val actualTime = actualWeather?.now_dt?.subSequence(
                        11,
                        16
                    ) //todo сделать что учитывало пояс локации

                    val yesterdayTemp: String =
                        if ((dataTemp != null) && (dataTemp > 0)) "+$dataTemp" else "$dataTemp"  //todo delete non-null assert
//                    binding.textActualTimeAndYesterdayTemp.text =
//                        "Сейчас ${current}. Вчера в это время $yesterdayTemp o"

                    val degreeSymbol = "\u00B0"
                    binding.textActualTimeAndYesterdayTemp.text =
                        "Сейчас ${actualTime}. Вчера в это время $yesterdayTemp$degreeSymbol" //todo время взять из АПИ или из Форматтер даты ?

                    val temp = actualWeather?.fact?.temp
                    val actualTemperature: String =
                        if (temp != null && temp > 0) "+$temp$degreeSymbol" else "$temp$degreeSymbol"  //todo delete non-null assert
                    binding.textActualTemp.text = actualTemperature

                    //load condition image
                    SvgLoader.pluck()
                        .with(this@MainActivity)
                        .load("https://yastatic.net/weather/i/icons/funky/dark/${actualWeather?.fact?.icon}.svg", binding.imageCondition);

                    binding.textCondition.text =
                        "${actualWeather?.fact?.conditionsMap?.get(actualWeather.fact.condition)}"
                    binding.textFeelsLike.text = "Ощущается как ${actualWeather?.fact?.feels_like}"

                    binding.btnGetWeatherAround.setOnClickListener {
                        when(ContextCompat.checkSelfPermission(this@MainActivity, ACCESS_COARSE_LOCATION)) {
                            PERMISSION_GRANTED -> {
                                Toast.makeText(this@MainActivity, "YAHOO", Toast.LENGTH_LONG).show()
                            }
                            PERMISSION_DENIED -> {
                                Toast.makeText(this@MainActivity, "no way", Toast.LENGTH_LONG).show()

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