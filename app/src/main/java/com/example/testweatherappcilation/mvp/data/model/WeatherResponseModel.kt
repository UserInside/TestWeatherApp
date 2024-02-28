package com.example.testweatherappcilation.mvp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseModel(
    val info: Info?,
    val yesterday: Yesterday?,
    val fact: Fact?,
    val forecasts: List<ForecastsDate>?,
    @SerialName("now_dt") val nowDateTime: String?,
    @SerialName("geo_object") val geoObject: GeoObject?,
) {
    @Serializable
    data class Info(
        val tzinfo: TzInfo?,
    ) {
        @Serializable
        data class TzInfo(
            val name: String?,
        )
    }

    @Serializable
    data class Yesterday(
        val temp: Int?,
    )

    @Serializable
    data class Fact(
        val temp: Int?,
        @SerialName("feels_like") val feelsLike: Int?,
        val icon: String?,
        val condition: Condition,
        @SerialName("wind_speed") val windSpeed: Double?,
        @SerialName("wind_dir") val windDirection: String?,
        val humidity: Int?,
        @SerialName("pressure_mm") val pressure: Int?,
    )

    @Serializable
    data class ForecastsDate(
        val date: String?,
        val parts: ForecastsDayPart?,
    ) {
        @Serializable
        data class ForecastsDayPart(
            @SerialName("day_short") val dayShort: DayPart,
            @SerialName("night_short") val nightShort: DayPart,
        ) {
            @Serializable
            data class DayPart(
                val temp: Int?,
                val icon: String?,
                val condition: Condition,
            )
        }
    }

    @Serializable
    data class GeoObject(
        val district: District?,
        val locality: Locality?,
    ) {
        @Serializable
        data class District(
            var name: String?,
        )

        @Serializable
        data class Locality(
            var name: String?,
        )
    }
}