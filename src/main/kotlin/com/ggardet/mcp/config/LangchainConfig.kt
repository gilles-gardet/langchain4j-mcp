package com.ggardet.mcp.config

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
        .modelName("phi4")
        .logRequests(true)
        .logResponses(true)
        .temperature(0.7)
        .build()
}
