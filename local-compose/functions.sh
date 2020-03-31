#!/bin/bash

source "local-compose/variables.sh"

function shutdown() {
  # shellcheck disable=SC2164
  cd "local-compose" 2> /dev/null #defensive cd (runUiDevMode will cd outside the current dir)
  $DOCKER_COMMAND down --rmi local --remove-orphans
  rm "$GENERATED_FILE" &> /dev/null
}

function tailLogs() {
  service=$1
  if [[ $service == "all" ]]
  then
    $DOCKER_COMMAND logs -f backend db ui
  else
    $DOCKER_COMMAND logs -f "$service"
  fi

}

function silent() {
  echo "CTRL + C to clean up"
  tail -f /dev/null
}

function keepShellAlive() {
  mode=$1
  case $mode in
    backend | db | ui | all) tailLogs "$mode"
      ;;
    *) silent
      ;;
  esac
}

function generateDockerComposeFile() {
  patterns=$1
  TMP_DIR="temp_data"
  docker run --rm -v "$(pwd):/$TMP_DIR" alpine:3.10 sed "$patterns" "$TMP_DIR/$TEMPLATE_FILE" > "$GENERATED_FILE"
}

function buildAndReadBackendProjectTag() {
  sbt --error docker:publishLocal containerTag | tail -1
}

function readCachedTag() {
  cat "$TAG_FILE_LOCATION"
}

function cacheTag() {
  tag=$1
  echo "$tag" > "$TAG_FILE_LOCATION"
}

function usage() {
  echo "USAGE: local [-b] [ui | backend | db | all]"
}

function boot() {
  buildBackend=$1
  if [ "$buildBackend" ]; then
    echo "building backend local image...";
    containerTag=$(buildAndReadBackendProjectTag);
    cacheTag "$containerTag"
  fi

  cachedTag=$(readCachedTag 2> /dev/null) || ""

  # if cache is empty
  if [ -z "${cachedTag}" ]; then
    echo "no cached image found: rebuilding...";
    rebuiltTag=$(buildAndReadBackendProjectTag);
    cacheTag "$rebuiltTag"
  fi
}

function parseCommandLineOptions() {

  while getopts "bh" opt; do
    case ${opt} in
      b )
        buildBackend=true
        boot $buildBackend
        ;;
      h )
        usage
        exit 0
        ;;
      \? )
        usage >&2
        exit 1
        ;;
    esac
  done
}