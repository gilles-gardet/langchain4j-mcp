# Local LLM & MCP with langchain4j

The goal of this project is to:

- [x] implement a little chat UI with vaadin
- [x] test and use a local LLM with the help of langchain4j
- [x] implement some RAG using qdrant as vectorial db
- [x] integrate a call to an external API with a LCM
- [ ] integrate a call to a database with a LCM

To be noticed that 2 LLMs will be used:

- *qwen3* as our chat model (which supports MCP tools)
- *nomic* as our embedding model

## Prerequisites

- sdkman
- docker & docker-compose
- a good internet connection (to download the LLM and the webui)
- a good computer (to run the LLM and the vectorial db)

## Starting the project

To install the required tools and build the project, you can use the following commands:

```bash
sdk env install # install the required tools & versions (jdk, mvn, ...)
docker compose -f mcp-client/compose.yml -f mcp-server/compose.yml up -d # build and start the docker containers (ollama, webui, qdrant, etc.)
mvn clean install -DskipTests # the first time fetch dependencies and build the project
```

> [!NOTE]
> To be noticed that the `docker compose` command will take a while to download the required images and build the containers.  
> That is precisely why we use `-d` to run it in the background.  
> Later spring-boot should start the docker containers for you, so no need to run this command each time we want to start the project.

Then, you can start the client and the server projects with the following commands (in a separate terminal):

```bash
mvn spring-boot:run -pl mcp-client # start the LLM UI
mvn spring-boot:run -pl mcp-server # start the MCP server
```

If needed you can also do a `mvn compile -DskipTests` before to re-compile the project.  
A chat window should be available at **localhost:8080** (client) and the MCP server at **localhost:8081**.
