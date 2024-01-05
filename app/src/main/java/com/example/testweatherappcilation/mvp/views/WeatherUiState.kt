package com.example.testweatherappcilation.mvp.views

import com.example.testweatherappcilation.mvp.models.WeatherUiModel

data class WeatherUiState(
    var weatherUiModel: WeatherUiModel? = null,
   // val contentState: ContentState = ContentState.Idle,
    var lat: Double = 0.0,
    var lon: Double = 0.0,
) {
}