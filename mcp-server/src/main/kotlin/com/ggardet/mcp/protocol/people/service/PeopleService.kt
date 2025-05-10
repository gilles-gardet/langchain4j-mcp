package com.ggardet.mcp.protocol.people.service

import com.ggardet.mcp.protocol.people.model.People
import com.ggardet.mcp.protocol.people.repository.PeopleRepository
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class PeopleService(private val peopleRepository: PeopleRepository) {
    @Tool(name = "fetchPeopleByName", description = "Find a person by his/her name")
    fun fetchByName(@ToolParam(required = true, description = "The name of the person to find") name: String): People? =
        peopleRepository.findByName(name)

    @Tool(name = "fetchPeopleByCountry", description = "Find people by their country")
    fun fetchByCountry(
        @ToolParam(
            required = true,
            description = "The country of the people to find"
        ) country: String
    ): List<People>? =
        peopleRepository.findByCountry(country)

    @Tool(name = "fetchAllPeople", description = "Fetch all people")
    fun fetchAll(): List<People> = peopleRepository.findAll()

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
