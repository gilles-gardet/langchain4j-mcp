package com.ggardet.mcp.provider

import com.ggardet.mcp.repository.PeopleRepository
import io.quarkiverse.mcp.server.Resource
import io.quarkiverse.mcp.server.ResourceTemplate
import io.quarkiverse.mcp.server.ResourceTemplateArg
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ResourceProvider {
    @Inject
    lateinit var peopleRepository: PeopleRepository

    /**
     * Static resource exposing the full people catalogue.
     * The user selects it explicitly in their MCP client (e.g. Claude Desktop, VS Code Copilot)
     * to inject the complete directory as context before interacting with the model.
     * This is the canonical Resource use case: user-driven context injection, not model-driven action.
     */
    @Resource(
        uri = "people://directory",
        name = "People Directory",
        description = "Read-only catalogue of all registered people with their name, age and country"
    )
    fun listPeople(): String {
        val people = peopleRepository.listAll()
        if (people.isEmpty()) return "No people registered yet."
        return people.joinToString("\n") { "- ${it.name}, age ${it.age}, from ${it.country}" }
    }

    /**
     * Template resource exposing a single person's profile by name.
     * The user selects it to provide focused context about one specific individual
     * without loading the entire directory into the conversation.
     */
    @ResourceTemplate(
        uriTemplate = "people://{name}/profile",
        name = "Person Profile",
        description = "Read-only profile card for a specific person identified by name"
    )
    fun getPersonProfile(@ResourceTemplateArg(name = "name") name: String): String {
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
