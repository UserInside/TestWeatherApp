package com.example.testweatherappcilation.mvp.views

import com.example.testweatherappcilation.mvp.models.WeatherUiModel
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleTagStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleTagStrategy::class)
interface MainActivityView : MvpView {

    fun showWeatherByCoordinates(lat: Double, lon: Double)

    fun showCityWeather(model: WeatherUiModel)
}