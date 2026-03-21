package com.ggardet.mcp.provider

import io.quarkiverse.mcp.server.Prompt
import io.quarkiverse.mcp.server.PromptArg
import io.quarkiverse.mcp.server.PromptMessage
import io.quarkiverse.mcp.server.TextContent
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PromptProvider {

    @Prompt(name = "weather-lookup", description = "Look up the current weather for a given city and country")
    fun weatherLookup(
        @PromptArg(name = "city", description = "The city name", required = true) city: String,
        @PromptArg(name = "countryCode", description = "The ISO country code (e.g. FR, US, DE)", required = true) countryCode: String
    ): List<PromptMessage> = listOf(
        PromptMessage.withUserRole(TextContent("What is the current weather in $city, $countryCode?"))
    )

    @Prompt(name = "people-by-name", description = "Look up a person by their name")
    fun peopleByName(
        @PromptArg(name = "name", description = "The person's name", required = true) name: String
    ): List<PromptMessage> = listOf(
        PromptMessage.withUserRole(TextContent("Find the person named $name and show me their details (name, age, country)."))
    )

    @Prompt(name = "people-by-country", description = "Find all people from a given country")
    fun peopleByCountry(
        @PromptArg(name = "country", description = "The country name", required = true) country: String
    ): List<PromptMessage> = listOf(
        PromptMessage.withUserRole(TextContent("Find all the people from $country and list their details (name, age, country)."))
    )
}
