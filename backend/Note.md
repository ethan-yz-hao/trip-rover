JWT based authentication
    Redis Caching authentication (authenticated user) to improve verification efficiency (when authenticated user access resources)
    Authenticate User at websocket handshake
RBAC authorization
    Authorize/check user role during channel subscription and message handling (controller)
Websocket
    optimistic update, versioning + acknowledgement + re-fetching
    update plan places: add, remove, reorder, update places
OAuth2
    Google, create user if not exist
RESTful
    User
        Auth: login, logout, 
        User：register, update user, delete user, change password
        Avatar: upload avatar / delete avatar
    Plan
        check role (owner, editor, viewer from plan member)
        get plan places (for websocket sync)
        plan Member: add member, delete member, update member
        get plan summaries of user (projection interface), create plan, delete plan (including members and places), update plan summaries (name isPublic, description)
Exception with global exception handler and custom exception class
DTO for validation, update
    User
    plan
S3 storage for avatar

## Local Development
Start the application
```
docker-compose up -d
```

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