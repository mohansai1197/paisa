# API contract and integration

[Back to the file directory](../README.md)

**Status:** no routes or data contracts have been chosen.

## Agree before connecting screens

For every endpoint, record the HTTP method, path, purpose, request fields, response fields, validation rules, and expected status codes. Keep frontend sample data consistent with that agreement.

| Method and route | Purpose | Request | Success response | Failure response | Source paths |
| --- | --- | --- | --- | --- | --- |
| To be decided | Awaiting brief | Pending | Pending | Pending | Not created |

Record example JSON below each endpoint when implemented. Decide consistent field names and date formats early.

## Integration workflow

1. Put frontend HTTP requests in a clearly named API module.
2. Configure the backend base URL in one place.
3. Configure a development proxy or explicit allowed origins as appropriate.
4. Replace sample-data operations one at a time.
5. Verify success, invalid input, missing data, and unavailable-backend behavior.
6. Confirm a write through React reaches MySQL and can be read back.

## How it works

User action → React event handler → API request → Spring Boot controller → service → repository → MySQL → JSON response → React state → updated interface.

Database credentials belong on the backend. The browser communicates with the backend API.

## What to explain

Walk through one real user action using the chain above. Point to its screen/component, API module, controller, service, repository, and table. Add their actual file paths here when implemented.
