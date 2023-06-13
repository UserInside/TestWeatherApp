package com.example.testweatherappcilation

data class WeatherUiState(
    val weatherEntity: WeatherEntity? = null,
    val contentState: ContentState = ContentState.Idle
) {
}