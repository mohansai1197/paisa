# Decisions and progress

[Back to the file directory](../README.md)

**Last updated:** 2026-11-21, preparation stage.

## Agreed direction

**Latest instruction:** planning only. The user wants the full agreed project when implementation starts, with system design and a clear build workflow established first. Application coding/implementation is on hold until requested. The current entry point is `16-system-design-and-build-workflow.md`.

| Decision | Reason / context | Related file |
| --- | --- | --- |
| Propose Paisa for the supplied finance brief | Brief now available; user retains feature decisions | `14-finance-project-scope.md` |
| Use the required React/Java/Spring Boot/MySQL stack with JPA, Security, and backend LLM integration | These are requirements in the supplied brief | `00-event-brief.md` |
| Prioritize visual quality and first impression | The interface should look polished and communicate clearly | `02-frontend-design.md` |
| Pair a working demo with a strong explanation | Explain the approach, implementation, and value | `08-demo-and-pitch.md` |
| Keep separate, clearly named topic files and a central address directory | Make changes and presentation preparation easy to find | `../README.md` |
| User leads feature decisions; assistant handles implementation and supporting work | User explicitly delegated the remaining work, without reliance on teammate coding contributions | `06-workflow-and-team.md` |
| Follow documented coding standards and ethics | User requested ethical code; keep code readable, respect data and licenses, follow event rules, and report results honestly | `11-coding-standards-and-ethics.md` |
| Prefer Medium for routine tasks and High for difficult reasoning | User requested automatic effort selection; direct switching of this active chat is unavailable, so the assistant can recommend changes but must not claim to apply them | `12-model-and-reasoning-guide.md` |
| Include a brief next-effort recommendation after each completed task or meaningful execution milestone | User wants a clear reminder of which reasoning level to select for the next task | `12-model-and-reasoning-guide.md` |
| Plan a five-hour build with Medium for routine work and scoped High use | User corrected the duration; Pro plan reported, remaining allowance unknown | `13-day-schedule-and-usage-budget.md` |

## Current progress

- Completed: preparation documents and central file directory created.
- Reported by user: VS Code installed.
- Verified: Java/Javac 25.0.4, Node.js 22.18.0, npm 10.9.3, Maven 3.9.16, MySQL client 9.6.0, and Git 2.55.0 installed.
- MySQL: authenticated Workbench connection verified with a read-only query; server 8.0.37, account root@localhost, port 3306. No credentials captured and no database data/settings changed. CLI/backend credential configuration remains pending.
- Reported by user: five-person team (user and four teammates).
- Reported by user: **five-hour build**, superseding the earlier ten-hour estimate; Pro plan. Schedule revised; exact remaining allowance and credits unknown.
- Completed: all three PDF pages read and visually inspected; proposed finance scope and required submission artifacts documented. Four-vs-two AI requirement ambiguity identified; proposal covers all four named capabilities.
- User confirmed: include all four named AI capabilities or more. Each will have a distinct visible result and acceptance check; unusual-spend alerts are a fifth candidate after the required scope works.
- User clarified: do not start coding; plan a basic version first and refine it gradually. A short phased plan is saved in `15-basic-build-plan.md`.
- Latest clarification: plan the full project, beginning with system design and implementation order. Architecture, relationships, API groups, runtime flows, and delivery checkpoints are saved in `16-system-design-and-build-workflow.md`.
- Pipeline clarified: a simple build sequence and feature-by-feature input → backend processing → result map are saved in `17-project-pipeline.md`; application implementation remains on hold.
- Latest answers: submission approximately 3 p.m. IST today; no LLM API or hosting accounts; keep the proposed design/INR/fictional data/CSV defaults. No additional event rules supplied.
- Latest timing preference: first review after one hour of actual implementation, then discuss changes; full required scope remains. Access candidates and aggressive sprint targets are documented in `18-first-hour-and-access-setup.md`; no coding or service setup has started.
- Prepared: user-directed feature planning with assistant-led implementation, documented coding standards, and four suggested teammate support roles; teammate assignments and skills are pending.
- Confirmed during plugin discovery: Figma and Canva installed.
- Pending: final feature priorities, application database/account configuration, LLM API and hosting access, backend connectivity, project compatibility/build checks, exact deadline, team roles, code, tests, deployment, and submission.

## Next actions

1. Finalize the basic screen outline, opening dashboard content, feature priorities, and visual direction with the user.
2. Plan the data flow and identify LLM API/hosting prerequisites; confirm the exact deadline and any additional rubric/rules.
3. Wait for the user's instruction to begin coding. No source scaffolding or application/database changes during the planning-only stage.
4. After that instruction, implement in phases and maintain actual source paths, verification results, and explanations in the topic documents.

## Maintenance rule

When we complete work, update its topic file with actual source paths, how it works, how it was verified, and speaking points. Add new topic files to the root directory. Record material decisions here so later work can resume from the files.

| Date | New decision or milestone | Why | Related paths |
| --- | --- | --- | --- |
| 2026-11-21 | Preparation file structure created | User requested separate files and a directory of addresses | `../README.md`, `docs/` |
| 2026-11-21 | Recorded five-person team and proposed role split | User reported four other teammates | `06-workflow-and-team.md`, `00-event-brief.md` |
| 2026-11-21 | Replaced the proposed multi-developer split with a one-human-developer workflow | User expects to handle implementation with this assistant; teammate support tasks are suggestions | `06-workflow-and-team.md` |
| 2026-11-21 | Clarified ownership and added coding standards | User will decide features and delegated remaining implementation work to the assistant, following ethical coding practices | `06-workflow-and-team.md`, `11-coding-standards-and-ethics.md` |
| 2026-11-21 | Checked local development tools; identified database authentication as the remaining access check | Tools are installed; local MySQL rejected a passwordless connection | `01-setup.md`, `04-database.md` |
| 2026-11-21 | Verified authenticated Workbench access without retrieving credentials | A read-only query returned MySQL 8.0.37, root@localhost, and port 3306 | `01-setup.md`, `04-database.md` |
| 2026-11-21 | Reviewed finance brief and corrected duration to five hours | User supplied PDF and corrected the earlier schedule; proposed Paisa scope covers required technologies, five tables, all four named AI capabilities, deployment, and deliverables | `00-event-brief.md`, `13-day-schedule-and-usage-budget.md`, `14-finance-project-scope.md` |

## First implementation checkpoint — 21 November 2026

User explicitly authorized coding and renamed the project **Paisa**. React build and two Spring integration tests passed. Browser registration, persisted transaction creation/reload/edit/search/deletion, and recalculated totals verified against local H2. Real MySQL and Gemini verification await credentials. User chose to review the app before credential setup. Current guide: [implementation and review](19-implementation-and-review.md).

User explicitly approved uploading the React frontend source to the private Paisa Sites project for a hosted sample preview. Backend credentials remain local. Source publication is now in progress; see document 19 for the final deployment status.

Private frontend preview published successfully: https://paisa-finance-hackquest.violetkoala8.chatgpt.site. Native deployment verified; automated browser handoff blocked at OpenAI authentication. Full Java/MySQL production hosting is still pending.
