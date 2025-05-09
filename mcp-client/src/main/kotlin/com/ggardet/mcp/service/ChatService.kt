package com.ggardet.mcp.service

import com.ggardet.mcp.contract.RagAssistant
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val storeService: StoreService,
    private val chatLanguageModel: ChatLanguageModel,
    mcpToolProvider: McpToolProvider,
    embeddingModel: EmbeddingModel,
    embeddingStore: EmbeddingStore<TextSegment>,
) {
    private val ragAssistant: RagAssistant = AiServices.builder(RagAssistant::class.java)
        .chatLanguageModel(chatLanguageModel)
        .toolProvider(mcpToolProvider)
        .contentRetriever(EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 3))
        .build()

    fun sendMessage(message: String): String = ragAssistant.augmentedChat(message)

    fun ingestContentFromUrl(url: String) = storeService.ingestContentFromUrl(url)
}
