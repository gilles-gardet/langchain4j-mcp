# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a demonstration project integrating Large Language Models (LLMs) with Model Context Protocol (MCP) servers, featuring RAG (Retrieval-Augmented Generation), external API calls, and database interactions.

**Technology Stack:** Kotlin 2.1.20, Spring Boot 3.4.5, Vaadin 24.7.3, LangChain4j 1.0.0-beta4, Spring AI 1.0.0

**Multi-module Maven Project:**
- `mcp-client/` - Chat UI with LLM client (port 8080)
- `mcp-server/` - MCP protocol server exposing tools (port 8081)

## Common Development Commands

### Environment Setup
```bash
sdk env install  # Install required JDK 24, Maven 3.9.9, Spring Boot 3.4.5
docker compose -f mcp-client/compose.yml -f mcp-server/compose.yml up -d
```

### Building
```bash
mvn clean install -DskipTests  # Full build
mvn compile -DskipTests        # Compile only
```

### Running the Applications
```bash
# Terminal 1: Start MCP server
mvn spring-boot:run -pl mcp-server

# Terminal 2: Start client with local LLM (qwen3:14b via Ollama)
mvn spring-boot:run -pl mcp-client

# OR with OpenAI (requires OPENAI_API_KEY environment variable)
mvn spring-boot:run -pl mcp-client -Dspring-boot.run.profiles=openai
```

### Testing
```bash
mvn test -pl mcp-client   # Test client module
mvn test -pl mcp-server   # Test server module
mvn test                  # Test all modules
```

### Required Environment Variables
- `OPENAI_API_KEY` - For using OpenAI profile (GPT-4o-mini)
- `GITHUB_PERSONAL_ACCESS_TOKEN` - For GitHub MCP server integration

## Architecture

### MCP-Client: AI Chat Application

**Core Pattern:** Vaadin UI → AssistantService → LangChain4j → MCP Tools + RAG + LLM

**Key Components:**

1. **Configuration (`/config/AssistantConfig.kt`)**
   - Configures LLM providers (profile-based: Ollama default, OpenAI optional)
   - Sets up embedding model (nomic-embed-text)
   - Configures MCP clients:
     - HTTP transport for local mcp-server (port 8081)
     - StdIO transport for GitHub MCP server (Docker-based)
   - Aggregates tools from multiple MCP clients via ToolProvider
   - Configures RAG content retriever (Qdrant vector store)
   - Manages chat memory (10 message sliding window)

2. **UI Layer (`/ui/ChatView.kt`)**
   - Vaadin-based chat interface
   - Handles URL ingestion for RAG
   - Renders markdown responses using Flexmark

3. **Services**
   - `AssistantService.kt` - AI chat interface with system prompt
   - `StoreService.kt` - Handles URL content ingestion into Qdrant

4. **System Prompt (`/resources/prompts/system.st`)**
   - Defines assistant behavior for weather, RAG, and database queries
   - Modify this file to change assistant personality or capabilities

**Docker Dependencies:**
- Ollama (port 11434) - Auto-pulls qwen3:14b and nomic-embed-text models
- Qdrant (ports 6333, 6334) - Vector database for RAG

### MCP-Server: Tool Provider via Spring AI MCP

**Core Pattern:** Spring AI MCP Server → @Tool-annotated services → External APIs/Database

**Protocol Implementation:**
- MCP server exposing tools via SSE (Server-Sent Events)
- Tools automatically discovered through Spring AI's `@Tool` annotation
- Tool registration in `ToolConfig.kt` makes services available to MCP clients

**Available Tools (Protocols):**

1. **Weather Protocol (`/protocol/weather/`)**
   - `WeatherService.kt` - Calls Open-Meteo API for geocoding and weather data
   - Single `@Tool` method for retrieving weather by location

2. **People Protocol (`/protocol/people/`)**
   - `PeopleService.kt` - Database CRUD operations exposed as MCP tools
   - Four `@Tool` methods: fetchPeopleByName, fetchPeopleByCountry, fetchAllPeople, savePeople
   - `People.kt` - JPA entity with auditing (@CreatedDate, @LastModifiedDate)
   - `PeopleRepository.kt` - Spring Data JPA repository

**Database:**
- PostgreSQL (port 5432) - Note: application.yml has port mismatch (5342 vs 5432)
- Liquibase migrations in `/resources/db/changelog/`
- Schema includes people table with optimistic locking (version column)

### Key Integration Points

1. **MCP Communication Flow:**
   ```
   mcp-client → HTTP/SSE → mcp-server → Tools (@Tool services)
   ```

2. **LLM Tool Calling:**
   - LangChain4j orchestrates tool selection based on user queries
   - MCP clients expose tools to LangChain4j via ToolProvider
   - Tools are called with structured parameters and return typed results

3. **RAG Implementation:**
   - URL content → HTML parsing → Document chunking → Embedding (nomic) → Qdrant storage
   - Retrieval during chat via ContentRetriever configured in AssistantConfig
   - Vector similarity search enriches LLM context

### Adding New MCP Tools

To add a new tool to mcp-server:

1. Create a service in `/protocol/{domain}/service/`
2. Annotate methods with `@Tool(description="...")`
3. Register the service bean in `ToolConfig.kt` if needed
4. Restart mcp-server - tools are auto-discovered via Spring AI

Example:
```kotlin
@Service
class MyService {
    @Tool("Fetch data by ID")
    fun fetchData(id: String): String {
        // Implementation
    }
}
```

### Important Configuration Details

- **Virtual Threads:** Enabled in both modules for improved concurrency (Java 24)
- **Kotlin:** Source directory is `src/main/kotlin`, JVM target is 17
- **Spring Plugin:** All-open plugin enabled for Kotlin Spring classes
- **DevTools:** Available for hot reload during development
- **Docker Compose:** Spring Boot auto-starts containers defined in compose.yaml files

### Database Migrations

Use Liquibase for schema changes:
1. Create new changelog file in `mcp-server/src/main/resources/db/changelog/`
2. Reference it in `db.changelog-master.yml`
3. Use YAML format with proper changeSet structure
4. Restart mcp-server to apply migrations automatically
