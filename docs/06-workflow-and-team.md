# Work plan, time allocation, and team roles

[Back to the file directory](../README.md)

**Status:** five-person team: you and four teammates. The user corrected the build duration to **five hours**. The user leads feature decisions and delegates implementation to this assistant. Plan without depending on teammate coding contributions; support roles remain provisional. Individual skills remain unknown.

## Build order

1. Read the brief, rubric, rules, and submission requirements.
2. Define the main journey, essential features, and API data shapes.
3. Choose a visual direction and build the React interface with sample data.
4. Implement the Spring Boot endpoints and MySQL persistence for that journey.
5. Connect the interface and backend, then test the full journey.
6. Polish the interface, prepare demo data, and rehearse the explanation.
7. Submit all required materials before the deadline.

Develop in small, manageable steps with the assistant handling implementation and the user directing features. Agree on data shapes, polish the main screen with sample data, then connect one real backend operation and verify persistence before expanding the flow. Aim to complete one working journey early.

## Starting time budget

| Work | Share of available time |
| --- | --- |
| Planning and design | 15% |
| Building and integration | 50% |
| Testing and polish | 20% |
| Pitch and submission preparation | 15% |

These were initial estimates. Use the [corrected five-hour milestone schedule and usage plan](13-day-schedule-and-usage-budget.md); its elapsed-time table is the current plan. Capture submission requirements immediately, validate hosting/API access early, and draft the explanation during development.

## Implementation ownership

- **User:** decides feature priorities and desired behavior, gives feedback on the running application, and learns to explain the main flow. Coding contributions are not assumed.
- **Assistant:** handles technical planning, UI design, frontend/backend/database implementation, integration, debugging, appropriate verification, documentation, and demo/pitch preparation with available tools. Make routine implementation choices autonomously within the agreed scope. External account access and inaccessible local steps may require user action; report such limitations clearly.
- Keep scope realistic for the available time and workflow. Choose one polished main journey and add optional features only after integration is reliable.
- Add a feature explanation and actual source paths as each feature is completed. Pause at milestones for a short walkthrough so the user can explain the work.
- Confirm the event's AI-tool rules when announced and follow them.
- Follow the [coding standards and ethics agreement](11-coding-standards-and-ethics.md).

## Suggested support roles for four teammates

These are suggestions to discuss with the team, not commitments they have made. Implementation must not depend on their availability or coding skills.

| Support role | Responsibility | Useful deliverable | Person |
| --- | --- | --- | --- |
| Problem and rubric research | Read the brief, check requirements, research the current workflow and alternatives | Concise problem notes with sources and rubric checklist | To be agreed |
| User-flow testing | Try the main journey and report reproducible issues; review laptop and mobile layouts | Steps to reproduce issues with screenshots and expected behavior | To be agreed |
| Demo content | Prepare fictional, realistic sample records and clear interface wording | Demo-data sheet and reviewed copy, using the agreed fields | To be agreed |
| Presentation and submission | Draft slides, track required artifacts and deadline, rehearse the pitch with the user | Pitch draft, demo checklist, and submission checklist | To be agreed |

## Coordination rules

- Agree on the main journey, API shapes, and shared UI conventions before implementation grows.
- Keep one implementation owner and coordinate any teammate code contributions explicitly if they become possible.
- Integrate at milestones: visual foundation, first real API response, first persisted action, then demo-ready flow. Keep the working version runnable.
- Ask teammates to work on agreed support tasks while implementation progresses; collect issues in a shared list with clear reproduction steps.
- Each person must understand the main journey and be able to explain their contribution using actual file paths.
- Reserve time for visual review, integration fixes, rehearsal, and submission before adding optional features.

Use Git checkpoints for coherent changes. At each milestone, update the relevant document and the progress tracker.

## What to explain

Describe how responsibilities were divided and how completing the main journey before optional features kept the project demonstrable.
