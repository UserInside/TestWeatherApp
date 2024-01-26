package com.example.testweatherappcilation.mvp.models.repository

interface WeatherRepository {
    suspend fun request(lat: Double, lon: Double) : WeatherEntity
}

// todo правильно ли расположение интерфейса этого?