package com.ggardet.mcp.core

import io.modelcontextprotocol.server.McpSyncServerExchange
import io.modelcontextprotocol.spec.McpSchema

fun McpSyncServerExchange?.logInfo(logger: String, message: String) =
    this?.loggingNotification(
        McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .logger(logger)
            .data(message)
            .build()
    )

fun McpSyncServerExchange?.logWarning(logger: String, message: String) =
    this?.loggingNotification(
        McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.WARNING)
            .logger(logger)
            .data(message)
            .build()
    )

fun McpSyncServerExchange?.progress(token: String, progress: Double, total: Double, message: String) =
    this?.progressNotification(McpSchema.ProgressNotification(token, progress, total, message))
