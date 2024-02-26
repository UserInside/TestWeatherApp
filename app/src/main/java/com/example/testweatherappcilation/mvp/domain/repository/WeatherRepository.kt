package com.example.testweatherappcilation.mvp.domain.repository

import com.example.testweatherappcilation.mvp.domain.entity.WeatherEntity
import com.google.android.gms.maps.model.LatLng

interface WeatherRepository {
    suspend fun request(coordinates: LatLng) : WeatherEntity
}
