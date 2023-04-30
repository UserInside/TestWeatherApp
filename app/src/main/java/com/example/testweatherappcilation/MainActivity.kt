package com.example.testweatherappcilation

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.example.testweatherappcilation.databinding.ActivityMainBinding
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

                    binding.textLocation.text = "${actualWeather?.geo_object?.district?.name}, ${actualWeather?.geo_object?.locality?.name}"

                    binding.btnGetWeather.setOnClickListener {
                        binding.textLocation.text =
                            "${actualWeather?.geo_object?.district?.name}, ${actualWeather?.geo_object?.locality?.name}"

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
                    }
                    binding.btnTokyo.setOnClickListener {
                        viewModel.resetFields()
                    }
                }
            }
        }
    }
}