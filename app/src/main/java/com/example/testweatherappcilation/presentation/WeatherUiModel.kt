package com.example.testweatherappcilation.presentation

import kotlinx.serialization.Serializable

@Serializable
data class WeatherUiModel(
    val textLocation: String? = null,
    val textActualTimeAndYesterdayTemp: String? = null,
    val textActualTemp: String? = null,
    val icon: String? = null,
    val textCondition: String? = null,
    val textFeelsLike: String? = null,
    val textWind: String? = null,
    val textHumidity: String? = null,
    val textPressure: String? = null,

    val dateTime: String? = null,
    val districtName: String? = null,
    val localityName: String? = null,

    val forecasts: List<WeatherUiModelForecasts?>? = null


)

@Serializable
data class WeatherUiModelForecasts(
    val forecastsDate: String?,
    val forecastsTempDay: Int?,
    val forecastsTempNight: Int?,
    val forecastsIcon: String?,
    val forecastsCondition: String?,
)
