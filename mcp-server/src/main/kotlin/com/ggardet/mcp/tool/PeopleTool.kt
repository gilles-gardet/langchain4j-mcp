package com.ggardet.mcp.tool

import com.ggardet.mcp.model.People
import com.ggardet.mcp.repository.PeopleRepository
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class PeopleService(private val peopleRepository: PeopleRepository) {
    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchPeopleByName", description = "Find a person by his/her name")
    fun fetchByName(@ToolParam(required = true, description = "The name of the person to find") name: String): People? =
        peopleRepository.findByName(name)

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchPeopleByCountry", description = "Find people by their country")
    fun fetchByCountry(
        @ToolParam(
            required = true,
            description = "The country of the people to find"
        ) country: String
    ): List<People>? =
        peopleRepository.findByCountry(country)
    @PreAuthorize("isAuthenticated()")
    @Tool(name = "fetchAllPeople", description = "Fetch all people")
    fun fetchAll(): List<People> = peopleRepository.findAll()

    @PreAuthorize("isAuthenticated()")
    @Tool(name = "savePeople", description = "Save a person")
    fun save(
        @ToolParam(required = true, description = "The name of the person to save") name: String,
        @ToolParam(required = true, description = "The age of the person to save") age: Int,
        @ToolParam(required = true, description = "The country of the person to save") country: String
    ): People {
        val person = People(name = name, age = age, country = country)
        return peopleRepository.save(person)
    }
}
