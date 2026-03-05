#!/bin/bash
/bin/ollama serve &
pid=$!
sleep 5
echo "🔴 Retrieve Nomic model..."
ollama pull nomic-embed-text
echo "🟢 Done!"
wait $pid
