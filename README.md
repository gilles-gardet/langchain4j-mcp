# Local LLM & MCP with langchain4j

The goal of this project is to:

1. implement a little chat UI with vaadin to test langchain4j
2. use a local LLM
3. integrate langchain4j with a LCM (TBD)

To start the project:

```bash
sdk env install
docker compose -f compase.yml up -d
mvn springboot:start
```

This should bootstrap a chat window at **localhost:8080**  
To be noticed that the docker initialization could be very long due to the LLM download and the webui setup.
