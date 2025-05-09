package com.ggardet.mcp.model

import org.springframework.ai.tool.ToolCallback

data class Tools(
    val totalNoOfTools: Int,
    val tool: List<ToolCallback> = emptyList()
)
