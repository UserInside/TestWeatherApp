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
) : WeatherGateway {

    override suspend fun request(): WeatherEntity {
        return WeatherEntity(dataHttpClient.request())
    }
}

class DataHttpClient(
    val lat : Double = 55.75396,
    val lon : Double = 37.620393,
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
    val geo_object: GeoObject?,
    val yesterday: Yesterday?,
    val fact: Fact?,
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
)

data class Forecasts(
    val dates: List<ForecastDate>,
)

data class ForecastDate(
    val date: String?,
    val sunrise: String?,
    val sunset: String?,
    val parts: ForecastsDayParts?,
    val hours: List<ForecastsHour>,
)

data class ForecastsDayParts(
    val morning: DayPart,
    val day: DayPart,
    val evening: DayPart,
    val night: DayPart,
)

data class DayPart(
    val temp_min: Int?,
    val temp_avg: Int?,
    val temp_max: Int?,
    val icon: String?,
    val condition: String?,

    )

data class ForecastsHour(
    val hour: String?,
    val temp: Int?,
    val condition: String,
)