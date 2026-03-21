package com.ggardet.mcp.tool

import com.ggardet.mcp.model.GeocodingResponse
import com.ggardet.mcp.model.WeatherResponse
import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springaicommunity.mcp.context.McpSyncRequestContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

private const val WEATHER_API_URL =
    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true"
private const val GEO_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&format=json"

@Service
class WeatherService {
    private val restClient = RestClient.builder().build()

    @PreAuthorize("isAuthenticated()")
    @McpTool(description = "Get the current weather for a given city and country code")
    fun getWeather(
        @McpToolParam(required = true, description = "The city") city: String,
        @McpToolParam(required = true, description = "The country code") countryCode: String,
        ctx: McpSyncRequestContext
    ): String {
        ctx.ping()
        ctx.info("Fetching weather for $city ($countryCode)")
        ctx.progress(McpSchema.ProgressNotification("weather-$city", 0.0, 1.0, "Resolving coordinates for $city"))
        val coordinates = getCoordinates(city, countryCode) ?: run {
            ctx.warn("Could not resolve coordinates for $city ($countryCode)")
            return "Could not find location for $city, $countryCode"
        }
        ctx.progress(McpSchema.ProgressNotification("weather-$city", 0.5, 1.0, "Coordinates resolved, fetching weather data"))
        ctx.info("Coordinates found: lat=${coordinates.first}, lon=${coordinates.second}")
        val rawWeather = fetchWeatherData(coordinates) ?: return "Weather data not available"
        ctx.progress(McpSchema.ProgressNotification("weather-$city", 1.0, 1.0, "Weather data retrieved"))
        return sampleRecommendation(city, rawWeather, ctx)
    }

    private fun fetchWeatherData(coords: Pair<Double, Double>): String? {
        val url = WEATHER_API_URL.format(coords.first, coords.second)
        return restClient.get().uri(url).retrieve().body<WeatherResponse>()
            ?.currentWeather?.let {
                "Temperature: %.1f°C, Windspeed: %.1f km/h, Condition: %s"
                    .format(it.temperature, it.windspeed, getWeatherDescription(it.weatherCode))
            }
    }

    private fun sampleRecommendation(city: String, rawWeather: String, ctx: McpSyncRequestContext): String {
        if (!ctx.sampleEnabled()) return rawWeather
        return runCatching {
            val result = ctx.sample(
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
            if (recommendation != null) "$rawWeather\n\nRecommendation: $recommendation" else rawWeather
        }.getOrDefault(rawWeather)
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
