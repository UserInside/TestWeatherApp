package com.example.testweatherappcilation.domain

class WeatherInteractor(
    private val weatherRepository: WeatherRepository
) {

    suspend fun fetchData(lat: Double, lon: Double) : WeatherEntity {
        return weatherRepository.request(lat, lon)
    }
}