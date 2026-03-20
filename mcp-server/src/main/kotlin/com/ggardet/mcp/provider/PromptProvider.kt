package com.ggardet.mcp.provider

import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpArg
import org.springaicommunity.mcp.annotation.McpPrompt
import org.springframework.stereotype.Component

@Component
class PromptProvider {

    @McpPrompt(name = "weather-lookup", description = "Look up the current weather for a given city and country")
    fun weatherLookup(
        @McpArg(name = "city", description = "The city name", required = true) city: String,
        @McpArg(name = "countryCode", description = "The ISO country code (e.g. FR, US, DE)", required = true) countryCode: String
    ): McpSchema.GetPromptResult = McpSchema.GetPromptResult(
        "Weather lookup for $city, $countryCode",
        listOf(
            McpSchema.PromptMessage(
                McpSchema.Role.USER,
                McpSchema.TextContent("What is the current weather in $city, $countryCode?")
            )
        )
    )

    @McpPrompt(name = "people-by-name", description = "Look up a person by their name")
    fun peopleByName(
        @McpArg(name = "name", description = "The person's name", required = true) name: String
    ): McpSchema.GetPromptResult = McpSchema.GetPromptResult(
        "Person lookup for $name",
        listOf(
            McpSchema.PromptMessage(
                McpSchema.Role.USER,
                McpSchema.TextContent("Find the person named $name and show me their details (name, age, country).")
            )
        )
    )

    @McpPrompt(name = "people-by-country", description = "Find all people from a given country")
    fun peopleByCountry(
        @McpArg(name = "country", description = "The country name", required = true) country: String
    ): McpSchema.GetPromptResult = McpSchema.GetPromptResult(
        "People from $country",
        listOf(
            McpSchema.PromptMessage(
                McpSchema.Role.USER,
                McpSchema.TextContent("Find all the people from $country and list their details (name, age, country).")
            )
        )
    )
}
