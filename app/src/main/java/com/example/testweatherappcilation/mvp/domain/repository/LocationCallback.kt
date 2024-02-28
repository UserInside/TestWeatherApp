package com.example.testweatherappcilation.mvp.domain.repository

import com.google.android.gms.maps.model.LatLng

interface LocationCallback {
    fun onCoordinatesReceived(coordinates: LatLng)
    fun requestLocationPermission()
}