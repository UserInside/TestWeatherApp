package com.example.testweatherappcilation.mvp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Condition(
    val condition: String,
) {
    @SerialName("clear")
    CLEAR("clear"),

    @SerialName("partly-cloudy")
    PARTLY_CLOUDY("partlyCloudy"),

    @SerialName("cloudy")
    CLOUDY("cloudy"),

    @SerialName("overcast")
    OVERCAST("overcast"),

    @SerialName("drizzle")
    DRIZZLE("drizzle"),

    @SerialName("light-rain")
    LIGHT_RAIN("lightRain"),

    @SerialName("rain")
    RAIN("rain"),

    @SerialName("moderate-rain")
    MODERATE_RAIN("moderateRain"),

    @SerialName("heavy-rain")
    HEAVY_RAIN("heavyRain"),

    @SerialName("continuous-heavy-hain")
    CONTINUOUS_HEAVY_RAIN("continuousHeavyRain"),

    @SerialName("showers")
    SHOWERS("showers"),

    @SerialName("wet-snow")
    WET_SNOW("wetSnow"),

    @SerialName("light-snow")
    LIGHT_SNOW("lightSnow"),

    @SerialName("snow")
    SNOW("snow"),

    @SerialName("snow-showers")
    SNOW_SHOWERS("snowShowers"),

    @SerialName("hail")
    HAIL("hail"),

    @SerialName("thunderstorm")
    THUNDERSTORM("thunderstorm"),

    @SerialName("thunderstorm-with-rain")
    THUNDERSTORM_WITH_RAIN("thunderstormWithRain"),

    @SerialName("thunderstorm-with-hail")
    THUNDERSTORM_WITH_HAIL("thunderstormWithHail"),

}