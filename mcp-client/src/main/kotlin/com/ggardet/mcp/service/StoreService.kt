package com.ggardet.mcp.service

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.UrlDocumentLoader
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val embeddingModel: EmbeddingModel,
    private val embeddingStore: EmbeddingStore<TextSegment>
) {
    fun ingestContentFromUrl(url: String) {
        val document: Document? =
            UrlDocumentLoader.load(url, TextDocumentParser())
        val ingestor = EmbeddingStoreIngestor.builder()
            .documentTransformer(HtmlToTextDocumentTransformer("body"))
            .documentSplitter(DocumentSplitters.recursive(300, 30))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()
        ingestor.ingest(document)
    }
}
