package com.example.testweatherappcilation.data

import com.example.testweatherappcilation.domain.WeatherEntity

object ApiToEntityMapper {
    fun map(item: ActualWeather): WeatherEntity {
        return WeatherEntity(
            timeZoneName = item.info?.tzinfo?.name ?: "",
            yesterdayTemp = item.yesterday?.temp ?: 0,
            actualTemp = item.fact?.temp ?: 0,
            feelsLike = item.fact?.feelsLike ?: 0,
            icon = item.fact?.icon ?: "",
            condition = item.fact?.condition?.condition ?: "",
            windSpeed = item.fact?.windSpeed ?: 0.0,
            windDirection = item.fact?.windDirection ?: "",
            humidity = item.fact?.humidity ?: 0,
            pressure = item.fact?.pressure ?: 0,
            dateTime = item.nowDateTime ?: "",
            districtName = item.geoObject?.district?.name ?: "",
            localityName = item.geoObject?.locality?.name ?: "",

            forecastsDate = item.forecasts?.map { it.date },
            // todo как-то херачить листы
            forecastsTempDay = item.forecasts?.map { it.parts?.dayShort?.temp },
            forecastsTempNight = item.forecasts?.map { it.parts?.nightShort?.temp },
            forecastsIcon = item.forecasts?.map { it.parts?.dayShort?.icon },
            forecastsCondition = item.forecasts?.map { it.parts?.dayShort?.condition?.condition },
        )

    }
}
