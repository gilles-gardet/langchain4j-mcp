package com.ggardet.mcp.provider

import com.ggardet.mcp.repository.PeopleRepository
import org.springaicommunity.mcp.annotation.McpResource
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component

@Component
class ResourceProvider(private val peopleRepository: PeopleRepository) {

    /**
     * Static resource exposing the full people catalogue.
     * The user selects it explicitly in their MCP client (e.g. Claude Desktop, VS Code Copilot)
     * to inject the complete directory as context before interacting with the model.
     * This is the canonical Resource use case: user-driven context injection, not model-driven action.
     */
    @McpResource(
        uri = "people://directory",
        name = "People Directory",
        description = "Read-only catalogue of all registered people with their name, age and country"
    )
    fun listPeople(): String {
        val people = peopleRepository.findAll().toList()
        if (people.isEmpty()) return "No people registered yet."
        return people.joinToString("\n") { "- ${it.name}, age ${it.age}, from ${it.country}" }
    }

    /**
     * Template resource exposing a single person's profile by name.
     * The user selects it to provide focused context about one specific individual
     * without loading the entire directory into the conversation.
     */
    @McpResource(
        uri = "people://{name}/profile",
        name = "Person Profile",
        description = "Read-only profile card for a specific person identified by name"
    )
    fun getPersonProfile(
        @McpToolParam(description = "Name of the person") name: String
    ): String {
        val person = peopleRepository.findByName(name)
            ?: return "No person found with name '$name'."
        return """
            Name: ${person.name}
            Age: ${person.age}
            Country: ${person.country}
            Registered: ${person.createdAt}
        """.trimIndent()
    }
}
