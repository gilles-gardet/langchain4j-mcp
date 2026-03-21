package com.ggardet.mcp.config

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import java.util.function.Supplier

/**
 * Supplies a Bearer token header for every MCP request by delegating to Spring's
 * [OAuth2AuthorizedClientManager], which transparently handles token caching and expiry.
 */
@Component
class KeycloakTokenSupplier(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
) : Supplier<Map<String, String>> {

    override fun get(): Map<String, String> {
        val request = OAuth2AuthorizeRequest
            .withClientRegistrationId("keycloak")
            .principal("mcp-client")
            .build()
        val client = authorizedClientManager.authorize(request)
            ?: error("Failed to obtain OAuth2 token from Keycloak")
        return mapOf("Authorization" to "Bearer ${client.accessToken.tokenValue}")
    }
}
