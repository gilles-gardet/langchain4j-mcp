package com.ggardet.mcp.core.config

import com.ggardet.mcp.protocol.people.service.PeopleService
import com.ggardet.mcp.protocol.weather.service.WeatherService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfig {

    @Bean
    fun tools(peopleService: PeopleService, weatherService: WeatherService): ToolCallbackProvider =
        MethodToolCallbackProvider.builder()
            .toolObjects(peopleService, weatherService)
            .build()
}
