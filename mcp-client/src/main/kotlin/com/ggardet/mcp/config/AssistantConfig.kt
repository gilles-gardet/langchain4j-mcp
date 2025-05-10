package com.ggardet.mcp.config

import com.ggardet.mcp.model.Assistant
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class AssistantConfig {

    @Bean
    fun embeddingModel(): EmbeddingModel = OllamaEmbeddingModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("nomic-embed-text")
        .logRequests(true)
        .logResponses(true)
        .build()

    @Profile("!openai")
    @Bean
    fun localChatModel(): ChatModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("qwen3:14b")
        .logRequests(true)
        .logResponses(true)
        .temperature(0.7)
        .build()

    @Profile("openai")
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
        mcpToolProvider: McpToolProvider
    ): Assistant = AiServices.builder(Assistant::class.java)
        .chatModel(chatModel)
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

    @Bean(name = ["serverMcpTransport"])
    fun serverMcpTransport(): McpTransport = HttpMcpTransport.Builder()
        .sseUrl("http://localhost:8081/sse")
        .logRequests(true)
        .logResponses(true)
        .build()

    @Bean(name = ["serverMcpClient"])
    fun serverMcpClient(serverMcpTransport: McpTransport): McpClient = DefaultMcpClient.Builder()
        .transport(serverMcpTransport)
        .build()

    @Bean(name = ["githubMcpTransport"])
    fun githubTransport(): McpTransport = StdioMcpTransport.Builder()
        .command(listOf("/usr/local/bin/docker", "run", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN", "-i", "mcp/github"))
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
