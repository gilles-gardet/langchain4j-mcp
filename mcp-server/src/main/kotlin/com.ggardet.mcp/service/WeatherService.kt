package com.ggardet.mcp.service

import com.ggardet.mcp.model.GeocodingResponse
import com.ggardet.mcp.model.WeatherResponse
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private const val WEATHER_API_URL =
    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true"
private const val GEO_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json"

@Service
class WeatherService {
    private val restClient = RestClient.builder().build()

    @Tool(description = "Get the current weather for a given city and country code")
    fun getWeather(
        @ToolParam(description = "The city") city: String,
        @ToolParam(description = "The country code") countryCode: String
    ): String {
        val coords = getCoordinates(city, countryCode)
            ?: return "Could not find location for $city, $countryCode"
        val url = WEATHER_API_URL.format(coords.first, coords.second)
        val weatherResponse = restClient.get()
            .uri(url)
            .retrieve()
            .body<WeatherResponse>() ?: return "Weather data not available"
        return with(weatherResponse.currentWeather) {
            "Temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s".format(
                temperature, windspeed, getWeatherDescription(weatherCode)
            )
        }
    }

    private fun getCoordinates(city: String, countryCode: String): Pair<Double, Double>? {
        val url = GEO_API_URL.format(city)
        val geoResponse = restClient.get()
            .uri(url)
            .retrieve()
            .body<GeocodingResponse>()
        return geoResponse?.results
            ?.firstOrNull { it.countryCode.equals(countryCode, ignoreCase = true) }
            ?.let { it.latitude to it.longitude }
    }

    private fun getWeatherDescription(code: Int): String = when (code) {
        0 -> "Clear sky"
        in 1..3 -> "Partly cloudy"
        in listOf(45, 48) -> "Foggy"
        in 51..55 -> "Drizzle"
        in 61..65 -> "Rainy"
        in 71..75 -> "Snowy"
        in 95..99 -> "Stormy"
        else -> "Unknown conditions"
    }
}
