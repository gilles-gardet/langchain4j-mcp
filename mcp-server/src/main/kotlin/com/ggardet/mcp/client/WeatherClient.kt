package com.ggardet.mcp.client

import com.ggardet.mcp.model.GeocodingResponse
import com.ggardet.mcp.model.WeatherResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "weather-api")
@Path("/v1")
interface WeatherClient {
    @GET
    @Path("/forecast")
    fun getWeather(
        @QueryParam("latitude") latitude: Double,
        @QueryParam("longitude") longitude: Double,
        @QueryParam("current_weather") currentWeather: Boolean
    ): WeatherResponse
}

@RegisterRestClient(configKey = "geocoding-api")
@Path("/v1")
interface GeocodingClient {
    @GET
    @Path("/search")
    fun search(
        @QueryParam("name") name: String,
        @QueryParam("format") format: String
    ): GeocodingResponse
}
