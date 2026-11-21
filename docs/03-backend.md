# Java and Spring Boot backend

[Back to the file directory](../README.md)

**Status:** preferred architecture only. No backend code or endpoints exist yet.

## Responsibilities

| Part | Responsibility | Planned location |
| --- | --- | --- |
| Controller | Accept HTTP requests and return appropriate responses | Inside `backend/`; exact paths pending |
| Request/response models | Define data exchanged with React and validation rules | Exact paths pending |
| Service | Apply the application's business rules | Exact paths pending |
| Repository | Read and write persisted data | Exact paths pending |
| Entity | Represent persisted application data | Exact paths pending |
| Error handling | Return understandable, consistent failures | Exact paths pending |

## How a request works

React sends a request → the controller validates its input → the service applies rules → the repository accesses MySQL → the backend returns a response → React updates the screen.

Use only the layers and dependencies that help our actual feature scope. Add authentication if the chosen problem or event requires it.

## Implementation records to maintain

- Actual Java and Spring Boot versions:
- Application entry-point path:
- Feature names and controller/service/repository paths:
- Important business rules and where they are enforced:
- Configuration-variable names:
- Verified startup command and port: record in `01-setup.md`.
- API contract: maintain in `05-api-integration.md`.

## What to explain

“The backend validates requests, applies our rules, and controls access to stored data.” Be able to walk through one implemented request and identify the files involved.
