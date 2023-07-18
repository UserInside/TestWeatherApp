package com.example.testweatherappcilation.domain

interface WeatherRepository {
    suspend fun request(lat: Double, lon: Double) : WeatherEntity
}