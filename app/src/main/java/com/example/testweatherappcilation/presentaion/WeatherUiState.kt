package com.example.testweatherappcilation.presentaion

import com.example.testweatherappcilation.ContentState
import com.example.testweatherappcilation.domain.WeatherEntity

data class WeatherUiState(
    val weatherEntity: WeatherEntity? = null,
    val contentState: ContentState = ContentState.Idle
) {
}