package com.example.testweatherappcilation.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.testweatherappcilation.data.ActualWeather
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val lastWeatherEntity = stringPreferencesKey("last weather entity")
    }

    suspend fun saveLastWeatherEntity(weatherEntity: WeatherEntity) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.lastWeatherEntity] = Json.encodeToString(weatherEntity)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json{
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun loadLastWeatherEntity(): WeatherEntity? {
        return dataStore.data.first().get(PreferencesKeys.lastWeatherEntity)
            ?.let { json.decodeFromString<WeatherEntity>(it) }
    }

}