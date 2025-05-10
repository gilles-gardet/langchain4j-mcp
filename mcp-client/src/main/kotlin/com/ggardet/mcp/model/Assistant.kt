package com.ggardet.mcp.model

fun interface Assistant {
    fun chat(userMessage: String): String
}
