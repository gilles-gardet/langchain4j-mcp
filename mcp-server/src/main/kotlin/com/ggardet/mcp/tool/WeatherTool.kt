package com.ggardet.mcp.tool

import com.ggardet.mcp.client.GeocodingClient
import com.ggardet.mcp.client.WeatherClient
import io.quarkiverse.mcp.server.McpLog
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class WeatherService(
    @RestClient private val weatherClient: WeatherClient,
    @RestClient private val geocodingClient: GeocodingClient
) {
    @Tool(description = "Get the current weather for a given city and country code")
    fun getWeather(
        @ToolArg(description = "The city", required = true) city: String,
        @ToolArg(description = "The country code", required = true) countryCode: String,
        log: McpLog
    ): String {
        log.info("Fetching weather for $city ($countryCode)")
        val coordinates = getCoordinates(city, countryCode) ?: run {
            log.error("Could not resolve coordinates for $city ($countryCode)")
            return "Could not find location for $city, $countryCode"
        }
        log.info("Coordinates found: lat=${coordinates.first}, lon=${coordinates.second}")
        return fetchWeatherData(coordinates) ?: "Weather data not available"
    }

    private fun fetchWeatherData(coords: Pair<Double, Double>): String? =
        runCatching {
            weatherClient.getWeather(coords.first, coords.second, true)
                .currentWeather?.let {
                    "Temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s"
                        .format(it.temperature, it.windspeed, getWeatherDescription(it.weatherCode))
                }
        }.getOrNull()

    private fun getCoordinates(city: String, countryCode: String): Pair<Double, Double>? =
        runCatching {
            geocodingClient.search(city, "json")
                .results
                ?.firstOrNull { it.countryCode.equals(countryCode, ignoreCase = true) }
                ?.let { it.latitude to it.longitude }
        }.getOrNull()

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
