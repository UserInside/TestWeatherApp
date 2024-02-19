package com.example.testweatherappcilation.mvp.ui.views.activities

import com.example.testweatherappcilation.mvp.data.model.WeatherUiModel
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleTagStrategy::class)
interface MainActivityView : MvpView {
    fun showWeather(model: WeatherUiModel)
    fun showLoading()
    fun showError()
    fun showWelcome()
    fun requestLocationPermission()

}