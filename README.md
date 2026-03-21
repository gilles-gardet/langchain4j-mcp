# LangChain4j MCP Demo

A demonstration project integrating Large Language Models (LLMs) with Model Context Protocol (MCP) servers, featuring RAG (Retrieval-Augmented Generation), external API calls, and database interactions.

## Goals

- [x] Implement a chat UI with Vaadin
- [x] Use a local LLM with LangChain4j
- [x] Implement RAG using Qdrant as vector DB
- [x] Integrate a call to an external API
- [x] Integrate a call to a database
- [x] Integrate the GitHub MCP Server (Docker + OpenAI LLM)
- [x] Expose resources from the MCP server
- [x] Expose prompts from the MCP server
- [x] Migrate MCP server to Quarkus
- [x] Secure MCP server with OAuth2/OIDC (Keycloak)

## Technology Stack

| Module | Framework | Port |
|--------|-----------|------|
| `mcp-client` | Spring Boot 4 + LangChain4j + Vaadin | 8080 |
| `mcp-server` | Quarkus + Quarkiverse MCP Server | 8090 |

**Other dependencies:** Kotlin, LangChain4j, OpenAI (LLM + embeddings), Qdrant (vector DB), PostgreSQL, Keycloak (OAuth2/OIDC)

## Architecture

```
User → Vaadin UI (mcp-client:8080) → LangChain4j → MCP Tools
                                           ↓
                          KeycloakTokenSupplier → Keycloak:8180 (client_credentials)
                                           ↓  Bearer token
                                    mcp-server:8090/mcp  (Quarkus, HTTP/SSE, OIDC-protected)
                                    ├── Weather tool     (Open-Meteo API)
                                    ├── People tools     (PostgreSQL)
                                    └── Workspace tools  (filesystem)
```

### mcp-client

Spring Boot application providing the chat UI and AI orchestration:

- **Vaadin** chat interface with markdown rendering
- **LangChain4j** orchestrates tool selection and RAG retrieval
- Connects to the local `mcp-server` via HTTP/SSE at `http://localhost:8090/mcp`, authenticated with a Bearer token from Keycloak
- Connects to the **GitHub MCP server** via Docker stdio transport
- Embeddings via **OpenAI** (`text-embedding-3-small`)
- Vector search via **Qdrant**

### mcp-server

Quarkus application exposing MCP tools, resources, prompts, and completions:

- **Tools:** weather lookup, people CRUD (PostgreSQL), workspace file access
- **Resources:** people directory (static), person profile (template)
- **Prompts:** weather-lookup, people-by-name, people-by-country
- **Completions:** autocomplete for people names, countries, and ISO country codes
- **Transport:** HTTP Streamable (SSE) at `/mcp`
- **Security:** OIDC bearer token validation via `quarkus-oidc` (Keycloak realm `mcp`)
- **Database:** PostgreSQL with Liquibase migrations

## Prerequisites

- sdkman
- Docker & docker-compose
- OpenAI API key

## Setup

```bash
sdk env install  # Install required JDK, Maven versions via sdkman
docker compose -f mcp-client/compose.yaml -f mcp-server/compose.yaml up -d
mvn clean install -DskipTests
```

The `docker compose` command starts:
- **Qdrant** (port 6333/6334) — vector store for RAG
- **PostgreSQL** (port 5432) — people database for mcp-server
- **Keycloak** (port 8180) — OAuth2/OIDC provider, auto-imports the `mcp` realm on first start

## Running

Start the MCP server first, then the client:

```bash
# Terminal 1 — MCP server (Quarkus)
mvn quarkus:dev -pl mcp-server

# Terminal 2 — MCP client (Spring Boot, requires OPENAI_API_KEY)
OPENAI_API_KEY=<your-key> mvn spring-boot:run -pl mcp-client
```

Open the chat UI at **http://localhost:8080**.

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | Required | Used for chat (GPT-4o-mini) and embeddings (`text-embedding-3-small`) |
| `GITHUB_PERSONAL_ACCESS_TOKEN` | Optional | Enables GitHub MCP server integration |

## Security

The MCP server endpoints (`/mcp/*`) require a valid Bearer token issued by Keycloak.

**Keycloak admin console:** http://localhost:8180 (admin / admin)

**Realm:** `mcp` — auto-imported from `mcp-server/src/main/resources/keycloak/realm-export.json`

| Client | Type | Purpose |
|--------|------|---------|
| `mcp-server` | Bearer-only | Resource server — validates incoming tokens |
| `mcp-client` | Service account | Obtains tokens via `client_credentials` grant |

The `mcp-client` Spring Boot app automatically fetches and refreshes tokens via `KeycloakTokenSupplier`, which is injected into `StreamableHttpMcpTransport` via `.customHeaders()`.

To obtain a token manually (e.g. for curl or MCP Inspector):

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/mcp/protocol/openid-connect/token \
  -d 'grant_type=client_credentials&client_id=mcp-client&client_secret=mcp-client-secret' \
  | jq -r .access_token)
```

## Testing the MCP Server

Use the MCP Inspector to test tools, resources, and prompts interactively:

```bash
npx @modelcontextprotocol/inspector
```

Connect using **Streamable HTTP** transport, URL `http://localhost:8090/mcp`, and add the header `Authorization: Bearer <token>` obtained from the command above.

## MCP Features

### Tools

| Tool | Description |
|------|-------------|
| `getWeather` | Current weather via Open-Meteo API |
| `fetchPeopleByName` | Find a person by name (PostgreSQL) |
| `fetchPeopleByCountry` | Find people by country |
| `fetchAllPeople` | List all registered people |
| `savePeople` | Save a new person to the database |
| `listWorkspaceRoots` | List client-configured workspace roots |
| `listFiles` | List directory contents |
| `readFile` | Read a file's text content |

### Resources

| URI | Description |
|-----|-------------|
| `people://directory` | Full catalogue of all registered people |
| `people://{name}/profile` | Profile card for a specific person |

### Prompts

| Name | Description |
|------|-------------|
| `weather-lookup` | Template for weather queries |
| `people-by-name` | Template for person lookup |
| `people-by-country` | Template for country-based search |

## Adding New MCP Tools

Create a Kotlin class in `mcp-server/src/main/kotlin/com/ggardet/mcp/tool/` and annotate methods with `@Tool`:

```kotlin
import io.quarkiverse.mcp.server.McpLog
import io.quarkiverse.mcp.server.Tool
import io.quarkiverse.mcp.server.ToolArg
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MyService {
    @Tool(description = "Fetch data by ID")
    fun fetchData(
        @ToolArg(description = "The data ID", required = true) id: String,
        log: McpLog
    ): String {
        log.info("Fetching data for id=$id")
        return "result"
    }
}
```

Quarkus auto-discovers annotated beans — no manual registration needed. In dev mode (`mvn quarkus:dev`), the server hot-reloads on save and exposes a Dev UI at `http://localhost:8090/q/dev` for interactive testing.

## Known Limitations

- **Workspace roots** — root discovery is not yet implemented; `listWorkspaceRoots` returns a placeholder
- **Sampling & elicitation** — LLM recommendation sampling (weather tool) and user confirmation prompts (save tool) have been removed pending Quarkus MCP context API integration
