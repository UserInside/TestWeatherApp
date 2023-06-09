package com.example.testweatherappcilation.data

import android.content.res.Resources
import com.example.testweatherappcilation.domain.Forecasts
import com.example.testweatherappcilation.domain.WeatherCondition
import com.example.testweatherappcilation.domain.WeatherEntity

object ApiToEntityMapper {

    fun map(item: ActualWeather): WeatherEntity {
        return WeatherEntity(
            timeZoneName = item.info?.tzinfo?.name ?: "",
            yesterdayTemp = item.yesterday?.temp ?: 0,
            actualTemp = item.fact?.temp ?: 0,
            feelsLike = item.fact?.feelsLike ?: 0,
            icon = item.fact?.icon ?: "",
            condition = mapWeatherCondition(item.fact?.condition?.condition),
            windSpeed = item.fact?.windSpeed ?: 0.0,
            windDirection = item.fact?.windDirection ?: "",
            humidity = item.fact?.humidity ?: 0,
            pressure = item.fact?.pressure ?: 0,
            dateTime = item.nowDateTime ?: "",
            districtName = item.geoObject?.district?.name ?: "",
            localityName = item.geoObject?.locality?.name ?: "",

            forecasts = mapForecasts(item)
        )
    }

    fun mapForecasts(item: ActualWeather): List<Forecasts> {
        val forecastsList = mutableListOf<Forecasts>()
        for (i in 0 until (item.forecasts?.size ?: 0)) {
            forecastsList.add(
                Forecasts(
                    forecastsDate = item.forecasts?.get(i)?.date,
                    forecastsTempDay = item.forecasts?.get(i)?.parts?.dayShort?.temp,
                    forecastsTempNight = item.forecasts?.get(i)?.parts?.nightShort?.temp,
                    forecastsIcon = item.forecasts?.get(i)?.parts?.dayShort?.icon,
                    forecastsCondition = item.forecasts?.get(i)?.parts?.dayShort?.condition?.condition,
                )
            )
        }
        return forecastsList
    }

    fun mapWeatherCondition(from: String?) : WeatherCondition{
        if (from.isNullOrBlank()) return WeatherCondition.Undefined
        return when (from.lowercase()) {                            //todo lowercase !!
            "clear" -> WeatherCondition.Clear
            "partlyCloudy" -> WeatherCondition.PartlyCloudy
            "cloudy" -> WeatherCondition.Cloudy
            "overcast" -> WeatherCondition.Overcast
            "drizzle" -> WeatherCondition.Drizzle
            "lightRain" -> WeatherCondition.LightRain
            "rain" -> WeatherCondition.Rain
            "moderateRain" -> WeatherCondition.ModerateRain
            "heavyRain" -> WeatherCondition.HeavyRain
            "continuousHeavyRain" -> WeatherCondition.ContinuousHeavyRain
            "showers" -> WeatherCondition.Showers
            "wetSnow" -> WeatherCondition.WetSnow
            "lightSnow" -> WeatherCondition.LightSnow
            "snow" -> WeatherCondition.Snow
            "snowShowers" -> WeatherCondition.SnowShowers
            "hail" -> WeatherCondition.Hail
            "thunderstorm" -> WeatherCondition.Thunderstorm
            "thunderstormWithRain" -> WeatherCondition.ThunderstormWithRain
            "thunderstormWithHail" -> WeatherCondition.ThunderstormWithHail
            else -> WeatherCondition.Undefined
        }


    }




}
