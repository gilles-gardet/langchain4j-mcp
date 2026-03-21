package com.ggardet.mcp.repository

import com.ggardet.mcp.model.People
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PeopleRepository : PanacheRepository<People> {
    fun findByName(name: String): People? = find("name", name).firstResult()
    fun findByCountry(country: String): List<People> = find("country", country).list()
}
