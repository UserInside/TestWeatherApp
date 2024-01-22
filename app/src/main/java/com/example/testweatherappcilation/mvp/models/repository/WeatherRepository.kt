package com.example.testweatherappcilation.mvp.models

import com.example.testweatherappcilation.mvp.models.WeatherEntity

interface WeatherRepository {
    suspend fun request(lat: Double, lon: Double) : WeatherEntity
}

// todo правильно ли расположение интерфейса этого?