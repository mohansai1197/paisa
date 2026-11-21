# Event brief and problem

[Back to the file directory](../README.md)

**Status:** all three pages of the supplied finance brief have been read and visually inspected. Proposed implementation is recorded in [the finance scope](14-finance-project-scope.md).

## Information to capture

| Item | Value |
| --- | --- |
| Event name and link | VCUBE HACK QUEST 1.0; source `/Users/mohansai/Downloads/VCube-Hack-Quest-finance.pdf` |
| Problem statement / theme | Problem 5, Fintech: AI-Powered Personal Finance & Expense Intelligence System |
| Start, end, and timezone | Five-hour event window reported earlier; user now gives approximately **3 p.m. IST submission on 21 November 2026** and wants a first review after one hour of implementation. Exact start and organizer cutoff unconfirmed; PDF reporting time is 7:30 a.m. |
| Team size | Five people: you and four teammates; confirm the event's team-size limit |
| Judging criteria and weights | To be confirmed; do not assume weights |
| Rules on prior work, AI tools, libraries, and external services | To be confirmed |
| Required technologies | Java, Spring Boot, Spring Data JPA, Spring Security, REST APIs, MySQL, React, Spring AI or compatible LLM API |
| Submission requirements | Record in `09-submission.md` |

## Requirements and interpretation

- Core: registration, income/expenses, categories, accounts, monthly budgets, transaction history, analytics, downloadable reports.
- Database: at least five related tables, named `users`, `accounts`, `transactions`, `categories`, `budgets`.
- AI: backend integration using actual application data where relevant.
- Ambiguity: page 1 lists four mandatory AI capabilities, whereas page 2 says minimum two meaningful features. Proposed scope covers categorization, spending patterns, budget recommendations, and monthly summaries; seek clarification if available.
- Deployment: live frontend and backend with production database connectivity; Render or another suitable cloud environment.
- Submission: GitHub URL, live application URL, ER diagram, Postman collection, README, final demo.

## Proposed product definition

- Target user: someone tracking personal income/expenses who wants to understand category spending and budget progress.
- Problem they face: recorded transactions alone do not explain patterns or suggest practical budget changes.
- Current workaround and its limitation: manual logs/spreadsheets require users to interpret patterns themselves; this is a working hypothesis, not measured research.
- Our proposed approach: Paisa, a small finance dashboard with deterministic analytics and data-grounded AI explanations.
- Main user journey: log in → add an AI-categorized expense → see updated analytics/budget → generate monthly insights → export report.
- Essential features: thin implementations of every core requirement and all four named AI capabilities.
- Optional features: none until required scope, deployment, tests, and demo are complete.
- Evidence or assumptions supporting the approach: aligned with the supplied brief; real user demand and impact have not been measured.
- Success criterion we can demonstrate: one complete persisted, authenticated workflow on the live application with real backend AI calls.

## What to explain

“We help **[user]** accomplish **[outcome]** by **[approach]**. The main improvement over their current process is **[specific benefit]**.”

Support claims with evidence where available; describe assumptions as assumptions.
