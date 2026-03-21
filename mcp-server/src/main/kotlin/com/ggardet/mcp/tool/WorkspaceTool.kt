package com.ggardet.mcp.tool

import io.quarkiverse.mcp.server.McpLog
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

@ApplicationScoped
class WorkspaceService {
    /**
     * Workspace roots are not available in this Quarkus MCP implementation (first draft).
     * Root discovery requires a client that advertises roots capability.
     */
    @Tool(description = "List the workspace roots configured by the client (directories the client has granted access to)")
    fun listWorkspaceRoots(): String = "Workspace roots are not supported in this server configuration."

    @Tool(description = "List files and subdirectories at the given path")
    fun listFiles(
        @ToolArg(required = true, description = "Absolute path of the directory to list") path: String,
        log: McpLog
    ): String {
        val target = Path.of(path)
        if (!target.isDirectory()) return "'$path' is not a directory."
        log.info("Listing files in $path")
        val entries = target.listDirectoryEntries()
            .sortedWith(compareBy({ !it.isDirectory() }, { it.name }))
        if (entries.isEmpty()) return "Directory '$path' is empty."
        return entries.joinToString("\n") { entry ->
            if (entry.isDirectory()) "[dir]  ${entry.name}" else "[file] ${entry.name}"
        }
    }

    @Tool(description = "Read the text content of a file")
    fun readFile(
        @ToolArg(required = true, description = "Absolute path of the file to read") path: String,
        log: McpLog
    ): String {
        val target = Path.of(path)
        if (target.isDirectory()) return "'$path' is a directory, not a file."
        if (!target.isReadable()) return "File '$path' is not readable."
        log.info("Reading file $path")
        return target.readText()
    }
}
