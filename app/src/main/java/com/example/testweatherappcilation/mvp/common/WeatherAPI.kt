package com.example.testweatherappcilation.mvp.common

import com.example.testweatherappcilation.BuildConfig
import com.example.testweatherappcilation.mvp.data.model.ActualWeather
import com.example.testweatherappcilation.mvp.data.model.WeatherEntity
import com.example.testweatherappcilation.mvp.data.mapper.ApiToEntityMapper
import com.example.testweatherappcilation.mvp.data.repository.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*


class WeatherRepositoryImplementation(
    private val weatherDataSource: WeatherDataSource
) : WeatherRepository {

    override suspend fun request(coordinates: LatLng): WeatherEntity {
        return ApiToEntityMapper.map(weatherDataSource.request(coordinates))
    }
}

class WeatherDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun request(coordinates: LatLng): ActualWeather {
        val apiKey: String = BuildConfig.ApiKey
        return httpClient.get("https://api.weather.yandex.ru/v2/forecast?lat=${coordinates.latitude}&lon=${coordinates.longitude}") {
            header("X-Yandex-API-Key", apiKey)
        }.body()
    }
}


