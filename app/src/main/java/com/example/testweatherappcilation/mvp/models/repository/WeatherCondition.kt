package com.example.testweatherappcilation.mvp.models.repository

import com.example.testweatherappcilation.R
import kotlinx.serialization.Serializable

@Serializable
sealed class WeatherCondition(val textResource: Int) {
    object Clear: WeatherCondition(R.string.clear)
    object PartlyCloudy: WeatherCondition(R.string.partlyCloudy)
    object Cloudy: WeatherCondition(R.string.cloudy)
    object Overcast: WeatherCondition(R.string.overcast)
    object Drizzle: WeatherCondition(R.string.drizzle)
    object LightRain: WeatherCondition(R.string.lightRain)
    object Rain: WeatherCondition(R.string.rain)
    object ModerateRain: WeatherCondition(R.string.moderateRain)
    object HeavyRain: WeatherCondition(R.string.heavyRain)
    object ContinuousHeavyRain: WeatherCondition(R.string.continuousHeavyRain)
    object Showers: WeatherCondition(R.string.showers)
    object WetSnow: WeatherCondition(R.string.wetSnow)
    object LightSnow: WeatherCondition(R.string.lightSnow)
    object Snow: WeatherCondition(R.string.snow)
    object SnowShowers: WeatherCondition(R.string.snowShowers)
    object Hail: WeatherCondition(R.string.hail)
    object Thunderstorm: WeatherCondition(R.string.thunderstorm)
    object ThunderstormWithRain: WeatherCondition(R.string.thunderstormWithRain)
    object ThunderstormWithHail: WeatherCondition(R.string.thunderstormWithHail)
    object Undefined: WeatherCondition(R.string.undefined)

}