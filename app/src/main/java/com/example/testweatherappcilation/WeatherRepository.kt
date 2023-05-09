package com.example.testweatherappcilation

interface WeatherRepository {
    suspend fun request() : WeatherEntity
}