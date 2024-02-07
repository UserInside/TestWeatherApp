package com.example.testweatherappcilation.mvp.models.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.testweatherappcilation.mvp.models.entity.WeatherUiModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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
        val lat = dataStore.data.first()[PreferencesKeys.lastLatitude] ?: 0.0
        val lon = dataStore.data.first()[PreferencesKeys.lastLongitude] ?: 0.0 //todo изменить нули
        return LatLng(lat, lon)
    }
}