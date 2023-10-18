#!/bin/bash

# Clear the terminal screen
clear

# Change to 'export all env variables' mode
set -o allexport

# This script's directory, relative to the running point
RELATIVE_SCRIPT_DIR=$( dirname -- ${BASH_SOURCE[0]} )

# This script's absolute directory
SCRIPT_DIR=$( cd -- ${RELATIVE_SCRIPT_DIR} &> /dev/null && pwd )

# Base directory of the Agnos programs (parent of the script's dir)
PROGRAM_DIR="$(dirname ${SCRIPT_DIR})"

# Load the personal settings
source ${SCRIPT_DIR}/environment.txt

# Restore to normal env variable mode
set +o allexport

cd ${SCRIPT_DIR}

# Start the docker compose process
docker-compose up -d

# Wait the docker container to start
sleep 10

# Copy the input, sql file and do the working steps
docker cp agnos_log_all.sql ${POSTGRES_CONTAINER_NAME}:/agnos_log_all.sql	# Copy the processing sql file to the container
docker cp ${ORIGINAL_ACCESS_LOGS} ${POSTGRES_CONTAINER_NAME}:/media/input.csv	# Copy the input csv to the container
docker exec ${POSTGRES_CONTAINER_NAME} psql -h localhost -p 5432 -U postgres -d postgres -w -a -f agnos_log_all.sql	# Run the loading&processing sql inside
docker run --name ${ROLLUP_CONTAINER_NAME} --add-host=host.docker.internal:host-gateway ruzsaz/agnos-logs-rollupbuilder:2.0	# Build the rollup table
docker run --name ${CUBEBUILDER_CONTAINER_NAME} --add-host=host.docker.internal:host-gateway -e RESULTCUBE=${TARGET_CUBE} ruzsaz/agnos-logs-cubebuilder:2.0	# Make the cube

# Get the result cube
docker cp ${CUBEBUILDER_CONTAINER_NAME}:/result/${TARGET_CUBE}.cube ./${TARGET_CUBE}.cube
mv ./${TARGET_CUBE}.cube "${CUBES_DIR}/${TARGET_CUBE}.cube"

# Stop the containers
docker-compose down
docker rm ${ROLLUP_CONTAINER_NAME} ${CUBEBUILDER_CONTAINER_NAME}

# Refresh the cube server
docker exec agnos-cube-nginx curl -X POST http://localhost:7979/acs/refresh
docker exec agnos-cube-nginx curl -X POST http://agnos-report-server:9091/ars/refresh