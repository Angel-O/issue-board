#!/bin/bash

# repliclates settings in decker-compose.template.yml file
# note: the context in the docker build command is passed as a parameter
# because this script is executed in the build.sbt file (in that scenario it is relative to the root of the project)

IMAGE_NAME="dev-db:latest"

DOCKER_CONTEXT=$1
CONTAINER_NAME=$2

docker container rm -f "$CONTAINER_NAME" &> /dev/null
docker build -t $IMAGE_NAME "$DOCKER_CONTEXT"
docker run --name "$CONTAINER_NAME" --volume issue_board_data:/home/dynamodblocal --volume-driver=local -p 8000:8000 -d $IMAGE_NAME
echo "running database init script..."
docker exec "$CONTAINER_NAME" sh db-entrypoint.sh