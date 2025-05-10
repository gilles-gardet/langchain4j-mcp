package com.ggardet.mcp.protocol.people.repository

import com.ggardet.mcp.protocol.people.model.People
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PeopleRepository : JpaRepository<People, UUID> {
    fun findByName(name: String): People?
    fun findByCountry(country: String): List<People>?
}
