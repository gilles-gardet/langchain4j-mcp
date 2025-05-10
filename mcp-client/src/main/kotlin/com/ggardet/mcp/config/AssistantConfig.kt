package com.ggardet.mcp.config

import com.ggardet.mcp.model.Assistant
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AssistantConfig {

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
    fun assistant(
        chatLanguageModel: ChatLanguageModel,
        contentRetriever: EmbeddingStoreContentRetriever,
        mcpToolProvider: McpToolProvider
    ): Assistant = AiServices.builder(Assistant::class.java)
        .chatLanguageModel(chatLanguageModel)
        .toolProvider(mcpToolProvider)
        .contentRetriever(contentRetriever)
        .build()

    @Bean
    fun contentRetriever(
        embeddingStore: EmbeddingStore<TextSegment>,
        embeddingModel: EmbeddingModel
    ): EmbeddingStoreContentRetriever = EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 3)

    @Bean
    fun chatMemory(): ChatMemory = MessageWindowChatMemory.withMaxMessages(10)

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
