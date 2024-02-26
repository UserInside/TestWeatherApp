package com.example.testweatherappcilation.mvp.domain.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getString
import com.example.testweatherappcilation.R
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

class LocationRepositoryImpl(
    private val context: Context,
) : LocationRepository {

    private var locationCallback: LocationCallback? = null

    var isLocationEnable = false

    fun setLocationCallback(callback: LocationCallback) {
        locationCallback = callback
    }

    override fun getCoordinates() {
        if (isGPSEnable()) {
            when {
                checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(
                        CurrentLocationRequest.Builder().build(), CancellationTokenSource().token
                    ).addOnSuccessListener { location ->
                        isLocationEnable = true
                        val coordinates = LatLng(location.latitude, location.longitude)
                        locationCallback?.onCoordinatesReceived(coordinates)
                    }
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) -> {
                    showMessageLocationPermissionRequirement()
                }

                else -> {
                    isLocationEnable = false
                    locationCallback?.requestLocationPermission()
                }
            }
        } else {
            isLocationEnable = false
            showMessageGPSRequirement()
        }
    }

    private fun isGPSEnable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showMessageGPSRequirement() {
        AlertDialog.Builder(context)
            .setMessage(context.resources.getString(R.string.gps_turn_on))
            .setPositiveButton(
                getString(
                    context, R.string.button_ok
                )
            ) { _: DialogInterface, _: Int ->
                ContextCompat.startActivity(
                    context,
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    null
                )
                locationCallback?.requestLocationPermission()
            }.setNegativeButton(getString(context, R.string.button_cancel), null)
            .create().show()
    }

    private fun showMessageLocationPermissionRequirement() {
        AlertDialog.Builder(context)
            .setMessage(
                getString(
                    context,
                    R.string.message_location_permission_requirement
                )
            )
            .setPositiveButton(
                getString(
                    context,
                    R.string.button_ok
                )
            ) { _: DialogInterface, _: Int ->
                locationCallback?.requestLocationPermission()
            }.setNegativeButton(getString(context, R.string.button_cancel), null)
            .create().show()
    }
}


