package com.example.testweatherappcilation.domain

class WeatherInteractor(
    private val weatherRepository: WeatherRepository
) {

    suspend fun fetchData() : WeatherEntity {
        return weatherRepository.request()
    }
}