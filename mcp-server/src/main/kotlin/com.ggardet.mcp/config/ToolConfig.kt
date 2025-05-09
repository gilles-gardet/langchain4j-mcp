package com.ggardet.mcp.config

import com.ggardet.mcp.service.WeatherService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfig {

    @Bean
    fun tools(weatherService: WeatherService): ToolCallbackProvider = MethodToolCallbackProvider.builder()
        .toolObjects(weatherService)
        .build()
}
