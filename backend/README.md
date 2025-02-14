# TripRover Backend

A Spring Boot application that provides backend services for the TripRover travel planning platform.

## Features

### Plan Management

#### RESTful Operations
- Plan CRUD operations through DTOs
  - Create new plans
  - Retrieve plan summaries through projection interface
  - Retrieve plan places for websocket synchronization during concurrency exception
  - Update plan basic information
  - Delete plans, cascade delete members and places
- Member management with role-based permissions
  - Add/remove plan members
  - Update member roles (owner, editor, viewer)
  - Role-based access control for operations

#### Real-time Collaborative Operations (WebSocket STOMP)
- Real-time plan updates with optimistic concurrency control (Optimistic Locking)
  - Place management operations:
    - Add new places
    - Remove existing places
    - Reorder places in sequence
    - Update place details (Google Place ID, stay duration)
  - Versioning for optimistic concurrency control
  - Optimistic update at client side
  - Acknowledgment system for update confirmations
  - Re-fetch plan places to sync with server state when confirmation fails
  - Error handling for:
    - Version conflicts
    - Authorization failures
    - Invalid operations
- Client-specific update and acknowledgment channels
- Broadcast updates to all channel subscribers

### User Management
- Authentication
    - JWT-based authentication with Redis caching to improve verification efficiency
    - HTTP-only secure cookie for JWT storage
    - Logout through JWT invalidation
    - OAuth2 integration (Google) for authentication and registration
- Authorization
    - Role-Based Access Control (RBAC)
    - WebSocket authorization at handshake, channel subscription, and individual message handling
- User CRUD operations through DTOs
    - User registration and authentication
    - Profile management (user information update)
    - Password management
- Avatar upload/deletion with S3 storage

### Search and Autocomplete
- Elasticsearch/OpenSearch integration with Debezium CDC
  - Autocomplete suggestions for:
    - User names during member invitations
    - Plan names in dashboard search
  - Real-time data synchronization:
    - Change Data Capture (CDC) from PostgreSQL
    - Selective field synchronization for performance
    - Event streaming through Apache Kafka
  - Search features:
    - Fuzzy matching for typo tolerance
    - Relevance scoring based on:
      - Text similarity
      - User relationships
      - Plan recency
    - Contextual boosting based on user permissions
  - Performance optimization:
    - Selective table and column monitoring
    - Index aliases for zero-downtime updates
    - Kafka Connect transforms for data filtering
    - Request debouncing on client side
  - Reliability:
    - At-least-once delivery guarantee
    - Automatic failure recovery
    - Transaction log consistency

## Tech Stack

- **Framework**: Spring Boot
- **Security**: Spring Security, JWT, OAuth2
- **Database**: PostgreSQL
- **Caching**: Redis
- **Search Infrastructure**: Elasticsearch, Debezium, Kafka
- **Avatar Storage**: AWS S3
- **Real-time Collaboration**: WebSocket (STOMP)
- **Build Tool**: Maven
- **Containerization**: Docker

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 22 or higher
- Maven

### Local Development

Set the environment file
```bash
export ENV_FILE=.env.dev
```

```powershell
$env:ENV_FILE = ".env.dev"
```

Start the postgres and redis containers
```bash
docker-compose up -d
```

Create the connectors
```
# For local development
./script/setup-connectors.sh .env.dev
```

Start the application

<!-- Start the Postgres source connector:
```bash
# at ./backend
curl -X POST -H "Content-Type: application/json" -d @postgres-source-connector-config.json http://localhost:8083/connectors
```

Start the Elasticsearch sink connector:
```bash
# at ./backend
curl -X POST -H "Content-Type: application/json" -d @elasticsearch-sink-connector-config.json http://localhost:8083/connectors
```

Check connector status:
```bash
curl -X GET http://localhost:8083/connectors/postgres-source/status
curl -X GET http://localhost:8083/connectors/elasticsearch-sink/status
curl -X GET http://localhost:8083/connectors/elasticsearch-user-sink/status
``` -->

Down and remove volumes
```
docker-compose down -v
```

TODO:
- autocomplete search for user name and plan name
- websocket connection limit
- ml model for plan place recommendation
- spring cloud
- dual database: note on how to sync (in the future, dynamoDB)

## Commands

### Elasticsearch

Check all indices
```
curl -X GET "http://localhost:9200/_cat/indices?v"
```

Search users
```
curl -X GET "http://localhost:9200/users/_search?pretty"
```

