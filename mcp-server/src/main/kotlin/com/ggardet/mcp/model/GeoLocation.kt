package com.ggardet.mcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeocodingResponse(val results: List<GeoLocation>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    @JsonProperty("country_code") val countryCode: String
)
