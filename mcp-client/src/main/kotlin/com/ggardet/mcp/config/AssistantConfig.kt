package com.ggardet.mcp.config

import com.ggardet.mcp.model.Assistant
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import java.util.function.Supplier

@Configuration
class AssistantConfig {

    @Bean
    fun embeddingModel(): EmbeddingModel = OpenAiEmbeddingModel.builder()
        .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL)
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .logRequests(true)
        .logResponses(true)
        .build()

    @Bean
    fun openaiChatModel(): ChatModel = OpenAiChatModel.builder()
        .modelName(OpenAiChatModelName.GPT_4_O_MINI)
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .logRequests(true)
        .logResponses(true)
        .temperature(0.7)
        .build()

    @Bean
    fun assistant(
        chatModel: ChatModel,
        contentRetriever: EmbeddingStoreContentRetriever,
        mcpToolProvider: McpToolProvider,
        chatMemory: ChatMemory
    ): Assistant = AiServices.builder(Assistant::class.java)
        .chatModel(chatModel)
        .toolProvider(mcpToolProvider)
        .contentRetriever(contentRetriever)
        .chatMemory(chatMemory)
        .build()

    @Bean
    fun contentRetriever(
        embeddingStore: EmbeddingStore<TextSegment>,
        embeddingModel: EmbeddingModel
    ): EmbeddingStoreContentRetriever = EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 3)

    @Bean
    fun chatMemory(): ChatMemory = MessageWindowChatMemory.withMaxMessages(10)

    @Bean(name = ["serverMcpTransport"])
    fun serverMcpTransport(keycloakTokenSupplier: KeycloakTokenSupplier): McpTransport =
        StreamableHttpMcpTransport.Builder()
            .url("http://localhost:8090/mcp")
            .customHeaders(Supplier { keycloakTokenSupplier.get() })
            .logRequests(true)
            .logResponses(true)
            .build()

    /**
     * Background-safe OAuth2 client manager that works outside of an HTTP request context.
     * The default Spring Boot auto-configured manager requires an active servlet request,
     * making it unsuitable for use in application beans like [KeycloakTokenSupplier].
     */
    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        )
        manager.setAuthorizedClientProvider(
            OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
        )
        return manager
    }

    /**
     * Permits all requests to the Vaadin UI and REST endpoints.
     * Spring Security is pulled in transitively by spring-boot-starter-oauth2-client
     * and would otherwise block the entire UI without this configuration.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { it.anyRequest().permitAll() }
        http.csrf { it.disable() }
        return http.build()
    }

    @Bean(name = ["serverMcpClient"])
    fun serverMcpClient(serverMcpTransport: McpTransport): McpClient = DefaultMcpClient.Builder()
        .transport(serverMcpTransport)
        .build()

    @Bean(name = ["githubMcpTransport"])
    fun githubTransport(): McpTransport = StdioMcpTransport.Builder()
        .command(
            listOf(
                "/usr/local/bin/docker",
                "run",
                "--rm",
                "--name",
                "github-mpc-server",
                "-e",
                "GITHUB_PERSONAL_ACCESS_TOKEN",
                "-i",
                "mcp/github"
            )
        )
        .logEvents(true)
        .build()

    @Bean(name = ["githubMcpClient"])
    fun githubMcpClient(githubMcpTransport: McpTransport): McpClient = DefaultMcpClient.Builder()
        .transport(githubMcpTransport)
        .build()

    @Bean
    fun mcpToolProvider(
        githubMcpClient: McpClient,
        serverMcpClient: McpClient,
    ): McpToolProvider = McpToolProvider.builder()
        .mcpClients(listOf(githubMcpClient, serverMcpClient))
        .build()
}
