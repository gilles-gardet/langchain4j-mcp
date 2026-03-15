package com.ggardet.mcp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class McpOauthApplication

fun main(args: Array<String>) {
    runApplication<McpOauthApplication>(*args)
}
