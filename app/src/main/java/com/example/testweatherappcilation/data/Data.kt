package com.example.testweatherappcilation.data



import com.example.testweatherappcilation.BuildConfig
import com.example.testweatherappcilation.domain.WeatherEntity
import com.example.testweatherappcilation.domain.WeatherRepository
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


class WeatherRepositoryImplementation(
    private val dataHttpClient: DataHttpClient
) : WeatherRepository {

    override suspend fun request(): WeatherEntity {
        return ApiToEntityMapper.map(dataHttpClient.request())
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
        val apiKey : String = BuildConfig.ApiKey
        val response: HttpResponse =
            client.get("https://api.weather.yandex.ru/v2/forecast?lat=$lat&lon=$lon") {
                header("X-Yandex-API-Key", apiKey)
            }

        val weather: ActualWeather = response.body()


        client.close()

        return weather
    }
}

@Serializable
data class ActualWeather(
    val info: Info?,
    val yesterday: Yesterday?,
    val fact: Fact?,
    val forecasts: List<ForecastsDate>?,
    @SerialName("now_dt")
    val nowDateTime: String?,
    @SerialName("geo_object")
    val geoObject: GeoObject?,
)

@Serializable
data class Info(
    val tzinfo: TzInfo?,
)

@Serializable
data class TzInfo(
    val name: String?,
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
    @SerialName("feels_like")
    val feelsLike: Int?,
    val icon: String?,
    val condition: Condition,
    @SerialName("wind_speed")
    val windSpeed: Double?,
    @SerialName("wind_dir")
    val windDirection: String?,
    val humidity: Int?,
    @SerialName("pressure_mm")
    val pressure: Int?,
)

@Serializable
data class ForecastsDate(
    val date: String?,
    val parts: ForecastsDayPart?,
)

@Serializable
data class ForecastsDayPart(
    @SerialName("day_short")
    val dayShort: DayPart,
    @SerialName("night_short")
    val nightShort: DayPart,
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

enum class WindDirection(
    @SerialName("wind_dir")
    val windDirection: String?
) {
    NORTH_WEST("nw"),
    NORTH("n"),
    NORTH_EAST("ne"),
    EAST("e"),
    SOUTH_WEST("sw"),
    SOUTH("s"),
    SOUTH_EAST("se"),
    WEST("w"),
    CALM("c"),
}
