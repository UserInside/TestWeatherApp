package com.example.testweatherappcilation.domain

interface WeatherRepository {
    suspend fun request() : WeatherEntity
}