package com.example.testweatherappcilation.presentaion

import com.example.testweatherappcilation.domain.WeatherCondition

data class WeatherUiRenderState(
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
) {


}