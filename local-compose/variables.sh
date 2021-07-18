#!/bin/bash

export TEMPLATE_FILE="docker-compose.template.yml"
export GENERATED_FILE=".docker-compose.generated.yml"
export CACHED_TAG_FILE=".cached-tag.txt"
export TAG_FILE_LOCATION=$LOCAL_DIR/"$CACHED_TAG_FILE" # LOCAL_DIR defined in 'local' script file
export DOCKER_COMMAND="docker-compose -f $GENERATED_FILE -p issue_board"
export UI_CONTAINER="ui"
export BACKEND_CONTAINER="backend"
export DB_CONTAINER="db"
export ALL_CONTAINERS="all"