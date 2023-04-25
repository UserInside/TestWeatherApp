package com.example.testweatherappcilation

interface WeatherGateway {
    suspend fun request() : WeatherEntity
}