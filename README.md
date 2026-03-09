# Local LLM & MCP with langchain4j

The goal of this project is to:

- [x] implement a little chat UI with vaadin
- [x] test and use a local LLM with the help of langchain4j
- [x] implement some RAG using qdrant as vectorial db
- [x] integrate a call to an external API with a LCM
- [x] integrate a call to a database with a LCM
- [x] integrate a call to the GitHub LCM Server (docker + openai LLM)
- [x] expose resources from the MCP server
- [x] expose prompts from the MCP server
- [x] secure the MCP server with an API key
- [ ] secure the MCP server using Oauth2

To be noticed that 2 LLMs will be used:

- *GPT-4* as our chat model (which supports MCP tools and suits well for the GitHub LCM server)
- *nomic* as our local embedding model using Ollama (to generate the embeddings for the vectorial db)

## Prerequisites

- sdkman
- docker & docker-compose
- a good internet connection (to download the LLM and the webui)
- a good computer (to run the LLM and the vectorial db)

## Starting the project

To install the required tools and build the project, you can use the following commands:

```bash
sdk env install # install the required tools & versions (jdk, mvn, ...)
docker compose -f mcp-client/compose.yml -f mcp-server/compose.yml up -d # build and start the docker containers (ollama, qdrant, etc.)
mvn clean install -DskipTests # the first time fetch dependencies and build the project
```

> [!NOTE]
> To be noticed that the `docker compose` command will take a while to download the required images and build the
> containers.  
> Later spring-boot should start the docker containers for you, so no need to run this command each time we want to
> start the project.

Then, you can start the client and the server projects with the following commands (in a separate terminal):

```bash
# start first by running the MCP server
mvn clean compile && mvn spring-boot:run -pl mcp-server
# then start the MCP client
mvn clean compile && mvn spring-boot:run -pl mcp-client  
```

> [!WARNING]
> To run the project with the OpenAI LLM, you need to set the `OPENAI_API_KEY` environment variable with the content of
> your OpenAI API key.  
> You will also need to set `GITHUB_PERSONAL_ACCESS_TOKEN` with a GitHub Personal Access Token (PAT) to interact with
> the GitHub LCM server.

If needed you can also do a `mvn compile -DskipTests` before to re-compile the project.  
A chat window should be available at **localhost:8080** (client) and the MCP server at **localhost:8081**.

## Test the MCP server

We can use a client to test the tools, resources and prompt of our MCP server:

```bash
npx @modelcontextprotocol/inspector
```
And then connect using the **Streamable HTTP** transport type and the URL `http://localhost:8081/mcp`.

Since we secured our MCP server using an API key we will need to perform our calls using it.  
For that we need to set a header `X-API-key: api01.mycustomapikey`.  
**X-API-key** is the (default) header name for passing API keys, followed by the header value {id}.{secret}.  
