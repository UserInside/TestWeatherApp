package com.example.testweatherappcilation.domain

import android.content.res.Resources

import com.example.testweatherappcilation.R
import com.example.testweatherappcilation.presentaion.WeatherUiModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DomainToPresentationMapper {
    fun map(
        resources: Resources,
        from: WeatherEntity,
    ): WeatherUiModel {
        val actualTempData = from.actualTemp
        val location = from.localityName ?: resources.getString(R.string.location_not_idetified)

        return WeatherUiModel(
            textLocation = if (from.districtName == null || from.districtName == "") location else "${from.districtName}, $location" ,
            textActualTimeAndYesterdayTemp = resources.getString(
                R.string.actual_time_and_yesterday_temp,
                getActualTime(from),
                getYesterdayTemp(from)
            ),
            textActualTemp = if ((actualTempData != null) && (actualTempData > 0)) "+$actualTempData°"
            else "$actualTempData°",

            textCondition = from.condition?.let {resources.getString(it.textResource)},
            textFeelsLike = resources.getString(R.string.feels_like, from.feelsLike),
            textWind = from.windDirection?.let{resources.getString(
                R.string.wind,
                it.textResource,
                from.windSpeed)},
            textHumidity = resources.getString(R.string.humidity, from.humidity),
            textPressure = resources.getString(R.string.pressure, from.pressure),
        )
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