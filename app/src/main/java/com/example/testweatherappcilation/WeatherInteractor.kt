package com.example.testweatherappcilation

class WeatherInteractor(
    private val weatherGateway: WeatherGateway
) {

    private val weatherEntity : WeatherEntity? = null

    suspend fun fetchData() : WeatherEntity {
        return weatherGateway.request()
    }
}