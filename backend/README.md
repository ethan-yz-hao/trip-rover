JWT based authentication
    Redis Caching authentication (authenticated user) to improve verification efficiency (when authenticated user access resources)
    Authenticate User at websocket handshake
RBAC authorization
    Authorize/check user role during channel subscription and message handling (controller)
Websocket
    optimistic update, versioning + acknowledgement + re-fetching
    update plan: add, remove, reorder, update places
OAuth2
    Google, create user if not exist
RESTful
    Auth
        login, logout, register, update user, delete user
    Plan
        get plan
        check role


## Local Development
Start the application
```
docker-compose up -d
```

Down and remove volumes
```
docker-compose down -v
```