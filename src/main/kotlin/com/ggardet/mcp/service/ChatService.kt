package com.ggardet.mcp.service

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import org.springframework.stereotype.Service

@Service
class ChatService {

    private val model: ChatLanguageModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("phi4")
        .temperature(0.7)
        .build()

    fun sendMessage(message: String): String {
        try {
            val userMessage = UserMessage.from(message)
            val response = model.chat(userMessage)
            return response.aiMessage().text()
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}
