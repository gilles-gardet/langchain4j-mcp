package com.ggardet.mcp.protocol.weather.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherResponse(@JsonProperty("current_weather") val currentWeather: CurrentWeather)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    @JsonProperty("weathercode") val weatherCode: Int
)
