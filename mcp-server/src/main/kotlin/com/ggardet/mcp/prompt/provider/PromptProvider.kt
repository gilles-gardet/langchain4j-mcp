package com.ggardet.mcp.prompt.provider

import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest
import io.modelcontextprotocol.spec.McpSchema.PromptMessage
import io.modelcontextprotocol.spec.McpSchema.TextContent
import org.springaicommunity.mcp.annotation.McpPrompt
import org.springframework.stereotype.Component


@Component
class PromptProvider {

    @McpPrompt(name = "conversation-guide", description = "Provides guidance to interact with the system")
    fun conversationStarter(request: GetPromptRequest): List<PromptMessage> = listOf(
        PromptMessage(
            McpSchema.Role.ASSISTANT, TextContent(
                ("""You are a friendly AI assistant designed to help using a tools designed to:
                    - retriever weather information,
                    - consulting a store containing information about people's country, age, and name.
                    
                    If the user asks for the weather, retrieve and display the data without requesting additional information if it is available.
                    For questions about people, give the exact data (name, age, country) if it exists.
                    
                    Always respond professionally.
                    If you do not know the answer, politely inform the user and ask a clarifying question.
                    If you know the answer, provide it without asking any further questions.
                    For any uncertainty about the results, explain that there may be other data not returned.""")
            )
        )
    )
}
