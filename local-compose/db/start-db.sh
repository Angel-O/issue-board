#!/bin/bash

# replicates settings in decker-compose.template.yml file
# note: the context in the docker build command is passed as a parameter
# because this script is executed from the build.sbt file (in that scenario it is relative to the root of the project)


DOCKER_CONTEXT=$1
CONTAINER_NAME=$2
TAG=$3

IMAGE_NAME="issue-board-db:$TAG"

docker container rm -f "$CONTAINER_NAME" &> /dev/null
docker build -q -t "$IMAGE_NAME" "$DOCKER_CONTEXT" &> /dev/null
docker run --name "$CONTAINER_NAME" --volume issue_board_data:/home/dynamodblocal --volume-driver=local -p 8000:8000 -d "$IMAGE_NAME"