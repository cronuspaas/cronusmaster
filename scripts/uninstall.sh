#!/bin/bash

echo "clean all services installed."
curl -sSk -H "content-type:application/json" -H "Authorization:Basic $agent_auth" -X POST https://localhost:19000/agent/cleanup
sleep 2
echo "Done"
echo ""

