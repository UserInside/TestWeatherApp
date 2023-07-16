package com.example.testweatherappcilation.presentaion

import com.example.testweatherappcilation.ContentState
import com.example.testweatherappcilation.domain.WeatherEntity

data class WeatherUiState(
    val weatherUiModel: WeatherUiModel? = null,
    val contentState: ContentState = ContentState.Idle
) {
}