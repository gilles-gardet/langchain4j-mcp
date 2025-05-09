package com.ggardet.mcp.web

import com.ggardet.mcp.model.Tools
import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ToolsController(private val toolCallbackProvider: ToolCallbackProvider) {

    @GetMapping("/tools/list")
    fun tools(): Tools {
        val toolList = toolCallbackProvider
            .toolCallbacks
            .map { it as ToolCallback }
        return Tools(toolList.size, toolList)
    }
}
