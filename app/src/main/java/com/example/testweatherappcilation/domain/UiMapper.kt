package com.example.testweatherappcilation.domain

import android.content.res.Resources
import android.util.Log

import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.presentation.WeatherUiModel
import com.example.testweatherappcilation.presentation.WeatherUiModelForecasts
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DomainToPresentationMapper {
    fun map(resources: Resources, from: WeatherEntity): WeatherUiModel {
        Log.e("UIMapper", "UI MAPPER map started")

        val actualTempData = from.actualTemp
        val location =
            if (from.localityName == null || from.localityName == "") resources.getString(R.string.location_not_idetified) else from.localityName
        val model = WeatherUiModel(
            textLocation = if (from.districtName == null || from.districtName == "") location else "${from.districtName}, $location",
            textActualTimeAndYesterdayTemp = resources.getString(
                R.string.actual_time_and_yesterday_temp,
                getActualTime(from),
                getYesterdayTemp(from)
            ),
            textActualTemp = if ((actualTempData != null) && (actualTempData > 0)) "+$actualTempData°"
            else "$actualTempData°",
            icon = from.icon,
            textCondition = from.condition?.let { resources.getString(it.textResource) },
            textFeelsLike = resources.getString(R.string.feels_like, from.feelsLike),
            textWind = resources.getString(
                R.string.wind,
                from.windDirection?.let { resources.getString(it.textResource) },
                from.windSpeed
            ),
            textHumidity = resources.getString(R.string.humidity, from.humidity),
            textPressure = resources.getString(R.string.pressure, from.pressure),

            forecasts = mapWeatherUiModelForecasts(resources, from.forecasts)

        )
        Log.e("UIMapper", "UI MAPPER map finished")

        return model
    }

    private fun mapWeatherUiModelForecasts(
        resources: Resources,
        forecasts: List<Forecasts?>?
    ): List<WeatherUiModelForecasts> {
        val weatherUiModelForecasts = mutableListOf<WeatherUiModelForecasts>()
        for (i in 0 until (forecasts?.size ?: 0)) {
            weatherUiModelForecasts.add(
                WeatherUiModelForecasts(
                    forecastsDay = forecasts?.get(i)?.forecastsDate?.let {
                        LocalDate.parse(it).dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale("ru", "RU")
                        )
                    }?.replaceFirstChar { it.uppercase() },
                    forecastsDate = forecasts?.get(i)?.forecastsDate?.let {
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                        ?.format(DateTimeFormatter.ofPattern("dd MMM", Locale("ru", "RU")))
                        .toString(),
                    forecastsTempDay = forecasts?.get(i)?.forecastsTempDay?.let { dayTemp -> if (dayTemp > 0) "+$dayTemp°" else "$dayTemp°" },
                    forecastsTempNight = forecasts?.get(i)?.forecastsTempNight?.let { nightTemp -> if (nightTemp > 0) "+$nightTemp°" else "$nightTemp°" },
                    forecastsIcon = forecasts?.get(i)?.forecastsIcon,
                    forecastsCondition = forecasts?.get(i)?.forecastsCondition?.let {
                        Log.e("COND", "condition ---- ${it}")
                        //todo не определяется partly-cloudy
                        resources.getString(it.textResource)
                    }
                )
            )
        }
        return weatherUiModelForecasts
    }

    fun getActualTime(from: WeatherEntity): String? {
        val offsetFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val actualTime = from.dateTime?.let {
            OffsetDateTime.parse(it)
                .atZoneSameInstant(ZoneId.of(from.timeZoneName))
                .toLocalTime().format(offsetFormatter)
        }
        return actualTime
    }

    fun getYesterdayTemp(from: WeatherEntity): String {
        val yesterdayTempData = from.yesterdayTemp
        val yesterdayTemp: String =
            if ((yesterdayTempData != null) && (yesterdayTempData > 0)) "+$yesterdayTempData" else "$yesterdayTempData"
        return yesterdayTemp
    }

}
