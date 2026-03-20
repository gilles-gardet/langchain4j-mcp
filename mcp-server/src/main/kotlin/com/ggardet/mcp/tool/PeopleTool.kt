package com.ggardet.mcp.tool

import com.ggardet.mcp.core.logInfo
import com.ggardet.mcp.core.logWarning
import com.ggardet.mcp.model.People
import com.ggardet.mcp.repository.PeopleRepository
import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema
import org.springframework.ai.chat.model.ToolContext
import org.springframework.ai.mcp.McpToolUtils
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

private const val LOGGER = "people-service"

@Service
class PeopleService(private val peopleRepository: PeopleRepository) {

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchPeopleByName", description = "Find a person by his/her name")
    fun fetchByName(
        @ToolParam(required = true, description = "The name of the person to find") name: String,
        toolContext: ToolContext
    ): People? {
        val exchange = McpToolUtils.getMcpExchange(toolContext).orElse(null)
        exchange.logInfo(LOGGER, "Fetching person by name: $name")
        return peopleRepository.findByName(name)
    }

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchPeopleByCountry", description = "Find people by their country")
    fun fetchByCountry(
        @ToolParam(required = true, description = "The country of the people to find") country: String,
        toolContext: ToolContext
    ): List<People>? {
        val exchange = McpToolUtils.getMcpExchange(toolContext).orElse(null)
        exchange.logInfo(LOGGER, "Fetching people by country: $country")
        return peopleRepository.findByCountry(country)
    }

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchAllPeople", description = "Fetch all people")
    fun fetchAll(toolContext: ToolContext): List<People> {
        val exchange = McpToolUtils.getMcpExchange(toolContext).orElse(null)
        exchange.logInfo(LOGGER, "Fetching all people")
        return peopleRepository.findAll()
    }

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "savePeople", description = "Save a person")
    fun save(
        @ToolParam(required = true, description = "The name of the person to save") name: String,
        @ToolParam(required = true, description = "The age of the person to save") age: Int,
        @ToolParam(required = true, description = "The country of the person to save") country: String,
        toolContext: ToolContext
    ): String {
        val exchange = McpToolUtils.getMcpExchange(toolContext).orElse(null)
        exchange.logInfo(LOGGER, "Requesting confirmation to save: $name, age=$age, country=$country")
        val elicitResult = exchange.elicitSaveConfirmation(name, age, country)
            ?: run {
                exchange.logWarning(LOGGER, "Save cancelled for: $name")
                return "Save cancelled"
            }
        if (elicitResult.content()["confirmed"] as? Boolean != true) {
            return "Save not confirmed by the user"
        }
        val saved = peopleRepository.save(People(name = name, age = age, country = country))
        val notes = elicitResult.content()["notes"] as? String
        exchange.logInfo(LOGGER, "Person saved: $name (id=${saved.id})${notes?.let { ", notes: $it" } ?: ""}")
        return saved.toString()
    }

    private fun McpSyncServerExchange?.elicitSaveConfirmation(
        name: String, age: Int, country: String
    ): McpSchema.ElicitResult? =
        this?.createElicitation(
            McpSchema.ElicitRequest.builder()
                .message("Confirm saving this person: $name, age $age, from $country")
                .requestedSchema(
                    mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "confirmed" to mapOf("type" to "boolean", "description" to "Confirm the save"),
                            "notes" to mapOf("type" to "string", "description" to "Optional notes about this person")
                        ),
                        "required" to listOf("confirmed")
                    )
                )
                .build()
        )?.takeIf { it.action() == McpSchema.ElicitResult.Action.ACCEPT }
}
