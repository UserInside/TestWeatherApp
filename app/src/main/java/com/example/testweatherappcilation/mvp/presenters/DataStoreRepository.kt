package com.example.testweatherappcilation.mvp.presenters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.testweatherappcilation.mvp.models.WeatherUiModel
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

    suspend fun saveLastWeatherEntity(weatherUiModel: WeatherUiModel) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.lastWeatherEntity] = Json.encodeToString(weatherUiModel)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json{
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun loadLastWeatherEntity(): WeatherUiModel? {
        return dataStore.data.first().get(PreferencesKeys.lastWeatherEntity)
            ?.let { json.decodeFromString<WeatherUiModel>(it) }
    }

}