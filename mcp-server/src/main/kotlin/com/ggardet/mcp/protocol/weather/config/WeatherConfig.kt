package com.ggardet.mcp.protocol.weather.config

import com.ggardet.mcp.protocol.weather.service.WeatherService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WeatherConfig {

    @Bean
    fun weatherTools(weatherService: WeatherService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build()
    }
}
