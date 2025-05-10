package com.ggardet.mcp.core.config

import com.ggardet.mcp.protocol.people.model.People
import com.ggardet.mcp.protocol.people.repository.PeopleRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.Instant
import java.util.Optional

@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["com.ggardet.mcp.protocol.people"])
@Configuration
class PersistenceConfig {
    @Bean
    fun loadData(peopleRepository: PeopleRepository): CommandLineRunner {
        return CommandLineRunner {
            val john = People(
                name = "John Doe",
                age = 30,
                country = "Sweeden"
            )
            peopleRepository.save(john)
        }
    }

    @Bean
    fun dateTimeProvider(): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now()) }
}
