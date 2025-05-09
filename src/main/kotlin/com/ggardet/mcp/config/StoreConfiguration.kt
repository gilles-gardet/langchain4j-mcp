package com.ggardet.mcp.config

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import io.qdrant.client.QdrantClient
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Collections
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class StoreConfiguration(private val embeddingModel: EmbeddingModel) {
    private val collectionName: String = "documents"

    @Bean
    fun embeddingStore(): EmbeddingStore<TextSegment> {
        val dimensions = if (embeddingModel is DimensionAwareEmbeddingModel) embeddingModel.dimension() else 768
        val grpcClientBuilder = QdrantGrpcClient.newBuilder("localhost", 6334, false)
        val client = QdrantClient(grpcClientBuilder.build())
        client.listCollectionsAsync().get().contains(collectionName).takeUnless { it }?.let {
            Collections.VectorParams.newBuilder()
                .setDistance(Collections.Distance.Cosine)
                .setSize(dimensions.toLong())
                .build()
                .let { params -> client.createCollectionAsync(collectionName, params).get() }
        }
        return QdrantEmbeddingStore.builder()
            .collectionName(collectionName)
            .client(client)
            .build()
    }
}
