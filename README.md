# Local LLM & MCP with langchain4j

The goal of this project is to:

- [x] implement a little chat UI with vaadin
- [x] test and use a local LLM with the help of langchain4j
- [x] implement some RAG using qdrant as vectorial db
- [ ] integrate a use case with a LCM (TBD)

To be noticed that 2 LLMs will be used:

- *phi4* as our chat model
- *nomic* as our embedding model

## Prerequisites

- sdkman
- docker & docker-compose
- a good internet connection (to download the LLM and the webui)
- a good computer (to run the LLM and the vectorial db)

## Starting the project

To install the required tools, you can use sdkman:

```bash
sdk env install # install the required tools & versions (jdk, mvn, ...)
```

**The following is optional** as spring-boot should start the docker containers for you:

```bash
docker compose -f compose.yml up -d # start the docker containers (llm, vectorial db, ...)
```

Then, you can start the project with:

```bash
mvn clean install -DskipTests # the first time fetch dependencies and build the project
mvn spring-boot:run # start the spring boot app
# you can also use the following command once the dependencies are downloaded
mvn compile -DskipTests && mvn spring-boot:run
```

This should start a chat window at **localhost:8080**  

> [!NOTE]
> To be noticed that the docker initialization could be very long due to the LLM download and the webui setup.
