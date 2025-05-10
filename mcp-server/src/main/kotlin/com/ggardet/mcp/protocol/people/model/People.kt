package com.ggardet.mcp.protocol.people.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.sql.Types
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "people")
class People(
    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    val id: UUID? = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val age: Int,

    @Column(nullable = true)
    val country: String,

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    var createdAt: Instant? = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: Instant? = Instant.now(),
)
