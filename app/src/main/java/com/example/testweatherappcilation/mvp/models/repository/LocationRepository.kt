package com.example.testweatherappcilation.mvp.models.repository

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService


class LocationRepository(
    val context: Context
) {

    fun getLocation() {
    }

    private fun isGPSEnable(): Boolean {
        val locationManager = getSystemServices(Context.Location_service) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        }
        return false
    }

}