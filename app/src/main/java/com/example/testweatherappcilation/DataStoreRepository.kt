package com.example.testweatherappcilation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
    ) {

    private object PreferencesKeys {
        val lastWeatherEntity = stringPreferencesKey("last weather entity")
    }

    suspend fun saveLastWeatherEntity(actualWeather: ActualWeather) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.lastWeatherEntity] = Json.encodeToString(actualWeather)
        }
    }

    suspend fun loadLastWeatherEntity(): String? {
        return dataStore.data.first().get(PreferencesKeys.lastWeatherEntity)
    }

}