package com.example.testweatherappcilation.mvp.data.repository

import com.example.testweatherappcilation.mvp.data.model.WeatherEntity
import com.google.android.gms.maps.model.LatLng

interface WeatherRepository {
    suspend fun request(coordinates: LatLng) : WeatherEntity
}
