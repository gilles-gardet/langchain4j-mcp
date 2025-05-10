package com.ggardet.mcp.protocol.people.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "people")
class People() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null

    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    var age: Int = 0

    @Column(nullable = true)
    lateinit var country: String

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    val createdAt: Instant? = Instant.now()

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: Instant? = Instant.now()

    @Version
    @Column(name = "version", nullable = false)
    var version: Long? = 0

    constructor(name: String, age: Int, country: String) : this() {
        this.name = name
        this.age = age
        this.country = country
    }
}

