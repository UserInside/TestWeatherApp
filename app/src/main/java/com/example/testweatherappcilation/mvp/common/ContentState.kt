package com.example.testweatherappcilation.mvp.common

sealed class ContentState {
    object Idle : ContentState()
    object Done : ContentState()
    object Loading : ContentState()
    sealed class Error : ContentState() {
        object Network : Error()
        object Common : Error()
    }
}