#!/bin/bash

export TEMPLATE_FILE="docker-compose.template.yml"
export GENERATED_FILE=".docker-compose.generated.yml"
export CACHED_TAG_FILE=".cached-tag.txt"
export TAG_FILE_LOCATION=$LOCAL_DIR/"$CACHED_TAG_FILE"
export DOCKER_COMMAND="docker-compose -f $GENERATED_FILE -p issue_board"