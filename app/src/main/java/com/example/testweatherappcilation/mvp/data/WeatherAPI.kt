package com.example.testweatherappcilation.mvp.data

import com.example.testweatherappcilation.BuildConfig
import com.example.testweatherappcilation.mvp.data.mapper.ApiToEntityMapper
import com.example.testweatherappcilation.mvp.data.model.WeatherResponseModel
import com.example.testweatherappcilation.mvp.domain.entity.WeatherEntity
import com.example.testweatherappcilation.mvp.domain.repository.WeatherRepository
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
    suspend fun request(coordinates: LatLng): WeatherResponseModel {
        val apiKey: String = BuildConfig.ApiKey
        return httpClient.get("https://api.weather.yandex.ru/v2/forecast?lat=${coordinates.latitude}&lon=${coordinates.longitude}") {
            header("X-Yandex-API-Key", apiKey)
        }.body()
    }
}


