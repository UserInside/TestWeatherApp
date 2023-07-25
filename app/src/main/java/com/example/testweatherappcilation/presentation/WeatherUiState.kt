package com.example.testweatherappcilation.presentation

import com.example.testweatherappcilation.common.ContentState

data class WeatherUiState(
    var weatherUiModel: WeatherUiModel? = null,
    val contentState: ContentState = ContentState.Idle,
    var lat: Double = 0.0,
    var lon: Double = 0.0,
) {
}