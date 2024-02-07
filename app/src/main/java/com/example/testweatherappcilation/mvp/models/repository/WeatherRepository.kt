package com.example.testweatherappcilation.mvp.models.repository

import com.google.android.gms.maps.model.LatLng

interface WeatherRepository {
    suspend fun request(coordinates: LatLng) : WeatherEntity
}
