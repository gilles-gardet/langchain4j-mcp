package com.ggardet.mcp.tool

import com.ggardet.mcp.core.logInfo
import com.ggardet.mcp.core.logWarning
import com.ggardet.mcp.core.progress
import com.ggardet.mcp.model.GeocodingResponse
import com.ggardet.mcp.model.WeatherResponse
import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import org.springframework.ai.chat.model.ToolContext
import org.springframework.ai.mcp.McpToolUtils
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private const val WEATHER_API_URL =
    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true"
private const val GEO_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json"
private const val LOGGER = "weather-service"

@Service
class WeatherService {
    private val restClient = RestClient.builder().build()

    @PreAuthorize("isAuthenticated()")
    @Tool(description = "Get the current weather for a given city and country code")
    fun getWeather(
        @ToolParam(required = true, description = "The city") city: String,
        @ToolParam(required = true, description = "The country code") countryCode: String,
        toolContext: ToolContext
    ): String {
        val exchange = McpToolUtils.getMcpExchange(toolContext).orElse(null)
        exchange?.ping()
        exchange.logInfo(LOGGER, "Fetching weather for $city ($countryCode)")
        exchange.progress("weather-$city", 0.0, 1.0, "Resolving coordinates for $city")
        val coords = getCoordinates(city, countryCode) ?: run {
            exchange.logWarning(LOGGER, "Could not resolve coordinates for $city ($countryCode)")
            return "Could not find location for $city, $countryCode"
        }
        exchange.progress("weather-$city", 0.5, 1.0, "Coordinates resolved, fetching weather data")
        exchange.logInfo(LOGGER, "Coordinates found: lat=${coords.first}, lon=${coords.second}")
        val rawWeather = fetchWeatherData(coords) ?: return "Weather data not available"
        exchange.progress("weather-$city", 1.0, 1.0, "Weather data retrieved")
        return exchange?.sampleRecommendation(city, rawWeather) ?: rawWeather
    }

    private fun fetchWeatherData(coords: Pair<Double, Double>): String? {
        val url = WEATHER_API_URL.format(coords.first, coords.second)
        return restClient.get().uri(url).retrieve().body<WeatherResponse>()
            ?.currentWeather?.let {
                "Temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s"
                    .format(it.temperature, it.windspeed, getWeatherDescription(it.weatherCode))
            }
    }

    private fun McpSyncServerExchange.sampleRecommendation(city: String, rawWeather: String): String {
        val result = createMessage(
            McpSchema.CreateMessageRequest.builder()
                .messages(listOf(
                    McpSchema.SamplingMessage(
                        McpSchema.Role.USER,
                        McpSchema.TextContent(
                            "Based on this weather in $city: $rawWeather — " +
                            "give a one-sentence friendly recommendation (what to wear or activity advice)."
                        )
                    )
                ))
                .maxTokens(100)
                .build()
        )
        val recommendation = (result.content() as? McpSchema.TextContent)?.text()
        return if (recommendation != null) "$rawWeather\n\nRecommendation: $recommendation" else rawWeather
    }

    private fun getCoordinates(city: String, countryCode: String): Pair<Double, Double>? {
        val url = GEO_API_URL.format(city)
        return restClient.get().uri(url).retrieve().body<GeocodingResponse>()
            ?.results
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
