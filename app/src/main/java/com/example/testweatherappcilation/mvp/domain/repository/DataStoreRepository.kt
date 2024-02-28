package com.example.testweatherappcilation.mvp.domain.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val lastLatitude = doublePreferencesKey("last latitude")
        val lastLongitude = doublePreferencesKey("last longitude")
    }

    suspend fun saveLastWeatherEntity(coordinates: LatLng) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.lastLatitude] = coordinates.latitude
            preferences[PreferencesKeys.lastLongitude] = coordinates.longitude
        }
    }

    suspend fun loadLastWeatherEntity(): LatLng? {
        val lat = dataStore.data.first()[PreferencesKeys.lastLatitude]
        val lon = dataStore.data.first()[PreferencesKeys.lastLongitude]
        return if (lat != null && lon != null) LatLng(lat, lon) else null
    }
}