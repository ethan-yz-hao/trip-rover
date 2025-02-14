#!/bin/bash

# Get the project root directory (assuming script is in project_root/script/)
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

echo "PROJECT_ROOT: $PROJECT_ROOT"

# Choose environment
ENV_FILE=${1}

# Check if environment is provided
if [ -z "$ENV_FILE" ]; then
    echo "Usage: $0 <environment>"
    exit 1
fi

# Check if environment file exists
ENV_FILE="${PROJECT_ROOT}/${ENV_FILE}"

if [ ! -f "$ENV_FILE" ]; then
    echo "Environment file $ENV_FILE not found!"
    exit 1
fi

# Load environment variables
source "$ENV_FILE"

# Directory for temporary files
TEMP_DIR="$PROJECT_ROOT/script/temp"
mkdir -p "$TEMP_DIR"

# Function to replace placeholders in JSON templates
setup_connector() {
    local template=$1
    local output=$2
    
    # Check if template exists
    local template_path="$PROJECT_ROOT/script/connector-templates/$template"
    if [ ! -f "$template_path" ]; then
        echo "Template file $template not found at $template_path!"
        return 1
    fi
    
    # Create temp file with environment variables replaced
    sed -e "s/{{POSTGRES_HOST}}/$POSTGRES_HOST/g" \
        -e "s/{{POSTGRES_PORT}}/$POSTGRES_PORT/g" \
        -e "s/{{POSTGRES_USER}}/$POSTGRES_USER/g" \
        -e "s/{{POSTGRES_PASSWORD}}/$POSTGRES_PASSWORD/g" \
        -e "s/{{POSTGRES_DB}}/$POSTGRES_DB/g" \
        -e "s/{{ELASTICSEARCH_HOST}}/$ELASTICSEARCH_HOST/g" \
        -e "s/{{ELASTICSEARCH_PORT}}/$ELASTICSEARCH_PORT/g" \
        -e "s/{{ELASTIC_USERNAME}}/$ELASTIC_USERNAME/g" \
        -e "s/{{ELASTIC_PASSWORD}}/$ELASTIC_PASSWORD/g" \
        "$template_path" > "$TEMP_DIR/$output"
    
    # Create connector
    curl -i -X POST -H "Accept:application/json" \
         -H "Content-Type:application/json" \
         http://${DEBEZIUM_HOST}:${DEBEZIUM_PORT}/connectors/ \
         -d @"$TEMP_DIR/$output"
    
    echo "Created connector from $template"
}

# Setup each connector
setup_connector "postgres-source-template.json" "postgres-source.json"
setup_connector "elasticsearch-user-sink-template.json" "elasticsearch-user-sink.json"
setup_connector "elasticsearch-plan-sink-template.json" "elasticsearch-plan-sink.json"

# Clean up
rm -rf "$TEMP_DIR" 