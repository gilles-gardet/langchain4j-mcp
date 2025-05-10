package com.ggardet.mcp.service

import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.spring.AiService

@AiService
fun interface AssistantService {

    @SystemMessage(fromResource = "/prompts/system.st")
    fun chat(userMessage: String): String
}
