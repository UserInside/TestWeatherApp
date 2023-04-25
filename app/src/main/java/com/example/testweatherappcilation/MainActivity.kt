package com.example.testweatherappcilation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getWeatherButton = findViewById<Button>(R.id.btn_getWeather)
        val regionText = findViewById<TextView>(R.id.text_region)

        val dataHttpClient = DataHttpClient()
        val weatherGatewayImplementation = WeatherGatewayImplementation(dataHttpClient)



    }
}