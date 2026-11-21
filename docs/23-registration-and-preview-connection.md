# Registration and preview connection

The hosted `chatgpt.site` URL is a frontend-only sample preview. Its `/api` URLs
can return the HTML page instead of a JSON API response. The old CSRF loader
attempted to parse that HTML as JSON, causing the error shown during registration.
The Java/MySQL service is running locally; it has not yet been deployed online.

## Changes

- `frontend/src/api.js` verifies JSON content types, catches malformed responses,
  validates the CSRF token structure, and gives useful network/service errors.
- When the session endpoint serves HTML, registration stops before a POST sends
  the form credentials to that endpoint.
- `frontend/src/App.jsx` detects a missing service and offers **About this sample
  preview** and **Workspace connection** instead of the unavailable signup form.
- The connection dialog explains the limitation and links to the connected local
  app at `http://127.0.0.1:5173/`.
- CSV downloads also reject an HTML page rather than saving it as a report.

Source paths are relative to `/Users/mohansai/Documents/ChatGPT/hackathon`.

## Verification

Twelve frontend tests passed, including eight API response/registration cases and
four balance cases. The production frontend and combined Java release built.
A temporary local static preview with its API proxy disabled reproduced the
frontend-only hosting conditions and showed connection guidance instead of signup.
A fictional account was created through the local browser form, opened an empty MySQL workspace, and remained signed in after reload. The test account was then signed out, leaving a blank registration form ready for the user.

The same fix is published to the existing owner-private Sites preview. The
production preview itself has not been browser-verified because its authentication
handoff was previously blocked; native publication confirms the deployment result.

Real registration on the hosted URL remains unavailable until the Java backend
and production MySQL service are hosted and connected. This fix does not substitute
browser storage for secure user accounts or claim that backend hosting is complete.
