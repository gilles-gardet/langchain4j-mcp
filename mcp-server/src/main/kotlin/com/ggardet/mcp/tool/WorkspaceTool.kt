package com.ggardet.mcp.tool

import io.modelcontextprotocol.spec.McpSchema
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springaicommunity.mcp.context.McpSyncRequestContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

@Service
class WorkspaceService {

    @PreAuthorize("isAuthenticated()")
    @McpTool(description = "List the workspace roots configured by the client (directories the client has granted access to)")
    fun listWorkspaceRoots(ctx: McpSyncRequestContext): String {
        if (!ctx.rootsEnabled()) return "The client does not support workspace roots."
        val roots = ctx.roots().roots()
        ctx.info("Listing ${roots.size} workspace root(s)")
        if (roots.isEmpty()) return "No workspace roots configured by the client."
        return roots.joinToString("\n") { root ->
            val name = root.name()?.takeIf { it.isNotBlank() }
            if (name != null) "- ${root.uri()} ($name)" else "- ${root.uri()}"
        }
    }

    @PreAuthorize("isAuthenticated()")
    @McpTool(description = "List files and subdirectories at the given path, which must be within a configured workspace root")
    fun listFiles(
        @McpToolParam(required = true, description = "Absolute path of the directory to list") path: String,
        ctx: McpSyncRequestContext
    ): String {
        val roots = if (ctx.rootsEnabled()) ctx.roots().roots() else emptyList()
        val target = Path.of(path)
        if (!isWithinRoots(target, roots)) {
            ctx.warn("Access denied: $path is outside all configured roots")
            return "Access denied: '$path' is not within any configured workspace root."
        }
        if (!target.isDirectory()) return "'$path' is not a directory."
        ctx.info("Listing files in $path")
        val entries = target.listDirectoryEntries()
            .sortedWith(compareBy({ !it.isDirectory() }, { it.name }))
        if (entries.isEmpty()) return "Directory '$path' is empty."
        return entries.joinToString("\n") { entry ->
            if (entry.isDirectory()) "[dir]  ${entry.name}" else "[file] ${entry.name}"
        }
    }

    @PreAuthorize("isAuthenticated()")
    @McpTool(description = "Read the text content of a file within a configured workspace root")
    fun readFile(
        @McpToolParam(required = true, description = "Absolute path of the file to read") path: String,
        ctx: McpSyncRequestContext
    ): String {
        val roots = if (ctx.rootsEnabled()) ctx.roots().roots() else emptyList()
        val target = Path.of(path)
        if (!isWithinRoots(target, roots)) {
            ctx.warn("Access denied: $path is outside all configured roots")
            return "Access denied: '$path' is not within any configured workspace root."
        }
        if (target.isDirectory()) return "'$path' is a directory, not a file."
        if (!target.isReadable()) return "File '$path' is not readable."
        ctx.info("Reading file $path")
        return target.readText()
    }

    private fun isWithinRoots(path: Path, roots: List<McpSchema.Root>): Boolean {
        if (roots.isEmpty()) return false
        val normalized = path.toAbsolutePath().normalize()
        return roots.any { root ->
            runCatching {
                val rootPath = Path.of(URI.create(root.uri())).toAbsolutePath().normalize()
                normalized.startsWith(rootPath)
            }.getOrDefault(false)
        }
    }
}
