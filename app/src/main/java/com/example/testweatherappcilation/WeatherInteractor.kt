package com.example.testweatherappcilation

class WeatherInteractor(
    private val weatherRepository: WeatherRepository
) {

    suspend fun fetchData() : WeatherEntity {
        return weatherRepository.request()
    }
}