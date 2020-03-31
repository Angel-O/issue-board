#!/bin/bash

FLAG_FILE=".initialized"

if [ ! -f "$FLAG_FILE" ]; then
    echo "Initializing local dynamo db"
    aws dynamodb create-table --table-name core-retro-issues-v2 \
--attribute-definitions AttributeName=id,AttributeType=S \
--key-schema AttributeName=id,KeyType=HASH \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000
    touch "$FLAG_FILE"
    else
      echo "db already initialized"
fi