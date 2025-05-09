package com.ggardet.mcp

import com.ggardet.mcp.service.WeatherService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class McpServerApplication

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}

@Bean
fun weatherTools(weatherService: WeatherService): ToolCallbackProvider {
    return MethodToolCallbackProvider.builder().toolObjects(weatherService).build()
}
