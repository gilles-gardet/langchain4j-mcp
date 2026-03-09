package com.ggardet.mcp.security.config

import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository
import org.springaicommunity.mcp.security.server.config.McpApiKeyConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
internal class McpServerConfiguration {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .with(McpApiKeyConfigurer.mcpServerApiKey()) {
                it.apiKeyRepository(apiKeyRepository())
            }
            .build()
    }

    private fun apiKeyRepository(): ApiKeyEntityRepository<ApiKeyEntityImpl> {
        val apiKey = ApiKeyEntityImpl.builder()
            .name("test api key")
            .id("api01")
            .secret("mycustomapikey")
            .build()
        return InMemoryApiKeyEntityRepository(listOf<ApiKeyEntityImpl>(apiKey))
    }
}
