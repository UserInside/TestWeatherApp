package com.example.testweatherappcilation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
                header("X-Yandex-API-Key", "9274936d-2c16-4189-a90c-f88b5cf4a034")
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

    val conditions: Map<String, String> = mapOf(
        "clear" to "Ясно",
        "partly-cloudy" to "Малооблачно",
        "cloudy" to "Облачно с прояснениями",
        "overcast" to "Пасмурно",
        "drizzle" to "Морось",
        "light-rain" to "Небольшой дождь",
        "rain" to "Дождь",
        "moderate-rain" to "Умеренно сильный дождь",
        "heavy-rain" to "Сильный дождь",
        "continuous-heavy-rain" to "Длительный сильный дождь",
        "showers" to "Ливень",
        "wet-snow" to "Дождь со снегом",
        "light-snow" to "Небольшой снег",
        "snow" to "Снег",
        "snow-showers" to "Снегопад",
        "hail" to "Град",
        "thunderstorm" to "Гроза",
        "thunderstorm-with-rain" to "Дождь с грозой",
        "thunderstorm-with-hail" to "Гроза с градом",
    ),
)

@Serializable
data class Info(
    val tzinfo: TzInfo?,
)

@Serializable
data class TzInfo(
    val name: String?,
    val offset: Int?,
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
    val condition: String?,
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


//@Serializable
//data class Forecasts(
//    val dates: List<ForecastsDate>,
//)

@Serializable
data class ForecastsDate(
    val date: String?,
//    val sunrise: String?,
//    val sunset: String?,
    val parts: ForecastsDayPart?,
//    val hours: List<ForecastsHour>,
)

@Serializable
data class ForecastsDayPart(
//    val morning: DayPart,
//    val day: DayPart,
//    val evening: DayPart,
//    val night: DayPart,
    val day_short: DayPart,
    val night_short: DayPart,
)

@Serializable
data class DayPart(
//    val temp_min: Int?,
//    val temp_avg: Int?,
//    val temp_max: Int?,
    val temp: Int?,
    val icon: String?,
    val condition: String?,
)

//@Serializable
//data class ForecastsHour(
//    val hour: String?,
//    val temp: Int?,
//    val condition: String,
//)

@Serializable
data class Conditions(
    val conditionsMap: Map<String, String> = mapOf(
        "clear" to "Ясно",
        "partly-cloudy" to "Малооблачно",
        "cloudy" to "Облачно с прояснениями",
        "overcast" to "Пасмурно",
        "drizzle" to "Морось",
        "light-rain" to "Небольшой дождь",
        "rain" to "Дождь",
        "moderate-rain" to "Умеренно сильный дождь",
        "heavy-rain" to "Сильный дождь",
        "continuous-heavy-rain" to "Длительный сильный дождь",
        "showers" to "Ливень",
        "wet-snow" to "Дождь со снегом",
        "light-snow" to "Небольшой снег",
        "snow" to "Снег",
        "snow-showers" to "Снегопад",
        "hail" to "Град",
        "thunderstorm" to "Гроза",
        "thunderstorm-with-rain" to "Дождь с грозой",
        "thunderstorm-with-hail" to "Гроза с градом",
    )
)