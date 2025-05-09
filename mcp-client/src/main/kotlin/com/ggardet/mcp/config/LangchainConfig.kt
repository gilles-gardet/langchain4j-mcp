package com.ggardet.mcp.config

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LangchainConfig {

    @Bean
    fun embeddingModel(): EmbeddingModel = OllamaEmbeddingModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("nomic-embed-text")
        .logRequests(true)
        .logResponses(true)
        .build()

    @Bean
    fun chatLanguageModel(): ChatLanguageModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("qwen3:14b")
        .logRequests(true)
        .logResponses(true)
        .temperature(0.7)
        .build()

    @Bean
    fun mcpTransport(): McpTransport = HttpMcpTransport.Builder()
        .sseUrl("http://localhost:8081/sse")
        .logRequests(true)
        .logResponses(true)
        .build()

    @Bean
    fun mcpClient(mcpTransport: McpTransport): McpClient = DefaultMcpClient.Builder()
        .transport(mcpTransport)
        .build()

    @Bean
    fun mcpToolProvider(mcpClient: McpClient): McpToolProvider = McpToolProvider.builder()
        .mcpClients(listOf(mcpClient))
        .build()
}
