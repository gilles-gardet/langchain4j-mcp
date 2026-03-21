package com.ggardet.mcp.tool

import com.ggardet.mcp.model.People
import com.ggardet.mcp.repository.PeopleRepository
import io.quarkiverse.mcp.server.McpLog
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class PeopleService {
    @Inject
    lateinit var peopleRepository: PeopleRepository

    @Tool(name = "fetchPeopleByName", description = "Find a person by his/her name")
    fun fetchByName(
        @ToolArg(description = "The name of the person to find", required = true) name: String,
        log: McpLog
    ): People? {
        log.info("Fetching person by name: $name")
        return peopleRepository.findByName(name)
    }

    @Tool(name = "fetchPeopleByCountry", description = "Find people by their country")
    fun fetchByCountry(
        @ToolArg(description = "The country of the people to find", required = true) country: String,
        log: McpLog
    ): List<People> {
        log.info("Fetching people by country: $country")
        return peopleRepository.findByCountry(country)
    }

    @Tool(name = "fetchAllPeople", description = "Fetch all people")
    fun fetchAll(log: McpLog): List<People> {
        log.info("Fetching all people")
        return peopleRepository.listAll()
    }

    @Transactional
    @Tool(name = "savePeople", description = "Save a person")
    fun save(
        @ToolArg(description = "The name of the person to save", required = true) name: String,
        @ToolArg(description = "The age of the person to save", required = true) age: Int,
        @ToolArg(description = "The country of the person to save", required = true) country: String,
        log: McpLog
    ): String {
        val person = People().also {
            it.name = name
            it.age = age
            it.country = country
        }
        peopleRepository.persist(person)
        log.info("Person saved: $name (id=${person.id})")
        return person.toString()
    }
}
