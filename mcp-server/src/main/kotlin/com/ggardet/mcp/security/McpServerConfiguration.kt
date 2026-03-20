package com.ggardet.mcp.security

import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository
import org.springaicommunity.mcp.security.server.config.McpServerOAuth2Configurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
internal class McpServerConfiguration {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/mcp").permitAll();
                it.anyRequest().authenticated()
            }
//            .with(McpApiKeyConfigurer.mcpServerApiKey()) {
//                it.apiKeyRepository(apiKeyRepository())
//            }
            .with(McpServerOAuth2Configurer.mcpServerOAuth2()) {
                it.authorizationServer("http://localhost:9000") // issuer URI
                it.validateAudienceClaim(true) // enforce the `aud` claim in the JWT token
            }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .build()
    }

    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
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
