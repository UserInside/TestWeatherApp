package com.example.testweatherappcilation.mvp.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherEntity(
    val timeZoneName: String?,
    val yesterdayTemp: Int?,
    val actualTemp: Int?,
    val feelsLike: Int?,
    val icon: String?,
    val condition: WeatherCondition?,
    val windSpeed: Double?,
    val windDirection: WindDirection?,
    val humidity: Int?,
    val pressure: Int?,
    val dateTime: String?,
    val districtName: String?,
    val localityName: String?,

    val forecasts: List<Forecasts?>?,
)

@Serializable
data class Forecasts(
    val forecastsDate: String?,
    val forecastsTempDay: Int?,
    val forecastsTempNight: Int?,
    val forecastsIcon: String?,
    val forecastsCondition: WeatherCondition?,
)

