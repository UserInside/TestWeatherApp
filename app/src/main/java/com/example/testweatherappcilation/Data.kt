package com.example.testweatherappcilation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.content.res.Resources

class WeatherGatewayImplementation(
    val dataHttpClient: DataHttpClient
) : WeatherRepository {

    override suspend fun request(): WeatherEntity {
        return WeatherEntity(dataHttpClient.request())
    }
}

class DataHttpClient(
    val lat: Double = 55.75396,
    val lon: Double = 37.620393,
) {

    suspend fun request(): ActualWeather {
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
        val response: HttpResponse =
            client.get("https://api.weather.yandex.ru/v2/forecast?lat=$lat&lon=$lon") {
                header("X-Yandex-API-Key", "cf8d0cd5-1645-4cc1-ae25-b6846212ef08")
            }

        val weather: ActualWeather = response.body()

        client.close()

        return weather
    }
}

@Serializable
data class ActualWeather(
    val now_dt: String?,
    val info: Info?,
    val geo_object: GeoObject?,
    val yesterday: Yesterday?,
    val fact: Fact?,
    val forecasts: List<ForecastsDate>?,
//    val condition: Condition,
)

@Serializable
data class Info(
    val tzinfo: TzInfo?,
)

@Serializable
data class TzInfo(
    val name: String?,
//    val offset: Int?,
)

@Serializable
data class GeoObject(
    val district: District?,
    val locality: Locality?,
)

@Serializable
data class District(
    var name: String?,
)

@Serializable
data class Locality(
    var name: String?,
)

@Serializable
data class Yesterday(
    val temp: Int?,
)

@Serializable
data class Fact(
    val temp: Int?,
    val feels_like: Int?,
    val icon: String?,
    val condition: Condition,
    val wind_speed: Double?,
    val wind_dir: String?,
    val windDirMap: Map<String, String> = mapOf(
        "nw" to "cеверо-западный",
        "n" to "северный",
        "ne" to "северо-восточный",
        "e" to "восточный",
        "se" to "юго-восточный",
        "s" to "южный",
        "sw" to "юго-западный",
        "w" to "западное",
        "c" to "штиль",
    ),
    val humidity: Int?,
    val pressure_mm: Int?,
)

@Serializable
data class ForecastsDate(
    val date: String?,
    val parts: ForecastsDayPart?,
)

@Serializable
data class ForecastsDayPart(
    val day_short: DayPart,
    val night_short: DayPart,
)

@Serializable
data class DayPart(
    val temp: Int?,
    val icon: String?,
    val condition: Condition,
)

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
