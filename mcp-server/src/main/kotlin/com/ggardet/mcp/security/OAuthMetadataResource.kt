package com.ggardet.mcp.security

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

/**
 * Exposes OAuth 2.0 Protected Resource Metadata (RFC 9728) at the well-known endpoint.
 * MCP clients such as MCP Inspector use this endpoint to discover which authorization
 * server protects this resource, then fetch that server's own metadata from Keycloak directly.
 */
@Path("/.well-known")
@ApplicationScoped
class OAuthMetadataResource {

    /**
     * RFC 9728 - OAuth 2.0 Protected Resource Metadata.
     * Points MCP clients to the Keycloak realm as the authorization server.
     */
    @GET
    @Path("/oauth-protected-resource")
    @Produces(MediaType.APPLICATION_JSON)
    fun protectedResourceMetadata(): Map<String, Any> = mapOf(
        "resource" to "http://localhost:8090",
        "authorization_servers" to listOf("http://localhost:8180/realms/mcp"),
    )

    /**
     * RFC 8414 - OAuth 2.0 Authorization Server Metadata.
     * Kept as a convenience fallback for clients that skip RFC 9728 discovery.
     */
    @GET
    @Path("/oauth-authorization-server")
    @Produces(MediaType.APPLICATION_JSON)
    fun authorizationServerMetadata(): Map<String, Any> = mapOf(
        "issuer" to "http://localhost:8180/realms/mcp",
        "authorization_endpoint" to "http://localhost:8180/realms/mcp/protocol/openid-connect/auth",
        "token_endpoint" to "http://localhost:8180/realms/mcp/protocol/openid-connect/token",
        "response_types_supported" to listOf("code"),
        "grant_types_supported" to listOf("authorization_code", "client_credentials"),
        "code_challenge_methods_supported" to listOf("S256"),
        "scopes_supported" to listOf("openid", "profile", "email"),
    )
}
