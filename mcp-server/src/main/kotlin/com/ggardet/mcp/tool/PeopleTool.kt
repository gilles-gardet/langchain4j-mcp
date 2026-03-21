package com.ggardet.mcp.tool

import com.ggardet.mcp.model.People
import com.ggardet.mcp.repository.PeopleRepository
import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springaicommunity.mcp.context.McpSyncRequestContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class PeopleService(private val peopleRepository: PeopleRepository) {

    @PreAuthorize("isAuthenticated()")
    @McpTool(name = "fetchPeopleByName", description = "Find a person by his/her name")
    fun fetchByName(
        @McpToolParam(required = true, description = "The name of the person to find") name: String,
        ctx: McpSyncRequestContext
    ): People? {
        ctx.info("Fetching person by name: $name")
        return peopleRepository.findByName(name)
    }

    @PreAuthorize("isAuthenticated()")
    @McpTool(name = "fetchPeopleByCountry", description = "Find people by their country")
    fun fetchByCountry(
        @McpToolParam(required = true, description = "The country of the people to find") country: String,
        ctx: McpSyncRequestContext
    ): List<People>? {
        ctx.info("Fetching people by country: $country")
        return peopleRepository.findByCountry(country)
    }

    @PreAuthorize("isAuthenticated()")
    @McpTool(name = "fetchAllPeople", description = "Fetch all people")
    fun fetchAll(ctx: McpSyncRequestContext): List<People> {
        ctx.info("Fetching all people")
        return peopleRepository.findAll()
    }

    @PreAuthorize("isAuthenticated()")
    @McpTool(name = "savePeople", description = "Save a person")
    fun save(
        @McpToolParam(required = true, description = "The name of the person to save") name: String,
        @McpToolParam(required = true, description = "The age of the person to save") age: Int,
        @McpToolParam(required = true, description = "The country of the person to save") country: String,
        ctx: McpSyncRequestContext
    ): String {
        ctx.info("Requesting confirmation to save: $name, age=$age, country=$country")
        val elicitResult = ctx.elicit(
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
        ).takeIf { it.action() == McpSchema.ElicitResult.Action.ACCEPT }
            ?: run {
                ctx.warn("Save cancelled for: $name")
                return "Save cancelled"
            }
        if (elicitResult.content()["confirmed"] as? Boolean != true) {
            return "Save not confirmed by the user"
        }
        val saved = peopleRepository.save(People(name = name, age = age, country = country))
        val notes = elicitResult.content()["notes"] as? String
        ctx.info("Person saved: $name (id=${saved.id})${notes?.let { ", notes: $it" } ?: ""}")
        return saved.toString()
    }
}
