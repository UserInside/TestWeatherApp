package com.example.testweatherappcilation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getWeatherButton = findViewById<Button>(R.id.btn_getWeather)
        val refreshButton = findViewById<Button>(R.id.btn_refresh)
        val regionText = findViewById<TextView>(R.id.text_region)

        lifecycleScope.launch {
            val data = getWeatherEntity()
            getWeatherButton.setOnClickListener {
                regionText.text = data.actualWeather.geo_object.locality.name
            }
            refreshButton.setOnClickListener{
                regionText.text = ""
            }

        }



    }

    suspend fun getWeatherEntity() : WeatherEntity {
        val dataHttpClient = DataHttpClient()
        val weatherGateway : WeatherGateway  = WeatherGatewayImplementation(dataHttpClient)
        val weatherInteractor = WeatherInteractor(weatherGateway)
        val weatherEntity = weatherInteractor.fetchData()
        return weatherEntity
        }
}