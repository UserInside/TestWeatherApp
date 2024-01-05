package com.example.testweatherappcilation.mvp.presenters

import com.example.testweatherappcilation.mvp.models.WeatherEntity
import com.example.testweatherappcilation.mvp.models.WeatherRepository

class WeatherInteractor(
    private val weatherRepository: WeatherRepository
) {

    suspend fun fetchData(lat: Double, lon: Double) : WeatherEntity {
        return weatherRepository.request(lat, lon)
    }
}