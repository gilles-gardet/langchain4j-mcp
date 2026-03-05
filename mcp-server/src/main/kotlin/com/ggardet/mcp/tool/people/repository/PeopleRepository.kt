package com.ggardet.mcp.tool.people.repository

import com.ggardet.mcp.tool.people.model.People
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PeopleRepository : JpaRepository<People, UUID> {
    fun findByName(name: String): People?
    fun findByCountry(country: String): List<People>?
}
