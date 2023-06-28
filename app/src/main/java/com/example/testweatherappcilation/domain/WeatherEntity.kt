package com.example.testweatherappcilation.domain

import kotlinx.serialization.Serializable

@Serializable
data class WeatherEntity(
    val timeZoneName: String?,
    val yesterdayTemp: Int?,
    val actualTemp: Int?,
    val feelsLike: Int?,
    val icon: String?,
    val condition: String?,
    val windSpeed: Double?,
    val windDirection: String?,
    val humidity: Int?,
    val pressure: Int?,
    val dateTime: String?,
    val districtName: String?,
    val localityName: String?,

    val forecastsDate: List<String?>?,
    val forecastsTempDay: List<Int?>?,
    val forecastsTempNight: List<Int?>?,
    val forecastsIcon: List<String?>?,
    val forecastsCondition: List<String?>?
)


