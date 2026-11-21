# Proposed project: Paisa

[Back to the file directory](../README.md)

**Status:** recommendation based on the supplied brief; implementation has not started. User retains feature-priority decisions.

**Source:** `/Users/mohansai/Downloads/VCube-Hack-Quest-finance.pdf`, all three pages inspected. Event: VCUBE HACK QUEST 1.0, Problem 5, Fintech. Document date: 21 November 2026. User corrected the build duration to **five hours**; exact start/deadline remain unconfirmed.

## Product and approach

Build a personal-finance dashboard that helps users record income and expenses, see where their money goes, and turn spending patterns into understandable budget suggestions.

Pitch: **“Paisa turns everyday transactions into a clear picture of your spending and an actionable monthly budget.”**

Make the main dashboard polished, then reuse its components across a small number of screens. Keep arithmetic deterministic in Java; use an LLM for categorization and explanations grounded in the user's stored data.

## Minimum working scope

| Requirement from brief | Proposed implementation |
| --- | --- |
| User registration | Register, log in, log out; Spring Security; hashed passwords and user-scoped access |
| Income and expense management | Add, edit, delete, and list income/expense transactions with amount, date, description, account, and category |
| Expense categorization | Category selector plus AI suggestion; user can correct suggestions |
| Account management | Simple cash/bank accounts with opening balance and derived current balance |
| Monthly budget management | Set a category budget for a month and compare spending against it |
| Transaction history | Search/filter by month, account, category, and type |
| Expense analytics | Income/expense totals, category breakdown, spending trend, and budget progress |
| Downloadable reports | Download the user's selected-month transactions and a summary as CSV; confirm if organizers specifically require PDF |
| Five related tables | `users`, `accounts`, `transactions`, `categories`, `budgets` |
| AI features | Cover all four named features with the two backend AI flows below |
| Deployment | Live React frontend, live Spring Boot backend, connected hosted MySQL database |
| Submission | GitHub URL, live URL, ER diagram, Postman collection, README, final demonstration |

## Four distinct AI capabilities with shared backend infrastructure

Page 1 calls four AI capabilities mandatory; page 2 gives a minimum of two meaningful AI features. The user explicitly chose **all four or more**, so all four named capabilities belong in the required scope. They must be visibly identifiable and demonstrable; shared backend calls do not reduce them to two features. Extra capabilities follow only after required functionality, live deployment, and verification are reliable.

| AI feature | User-visible result | Acceptance check |
| --- | --- | --- |
| Expense categorization | A suggested category that can be accepted or corrected | Real model call returns a valid allowed category; saving persists the chosen result |
| Spending pattern analyzer | Evidence-backed category/trend observations | Findings match stored data; missing historical data is handled honestly |
| Budget recommendation engine | Suggested category budgets with reasons and an apply action | Suggestions use actual spending; changes occur only after user selection |
| Monthly summary generator | A selected-month narrative with income, expenses, and net cash flow | The narrative agrees with deterministic backend totals |

**Fifth candidate:** unusual-spend alerts with AI explanations. Use comparable personal-history data and an explicit baseline; show insufficient-data states rather than fabricating anomalies. Treat this as additional scope after the required four, not a reason to postpone them.

### Flow 1: categorize a transaction

Spring Boot sends a minimal transaction description and the allowed category names to an LLM. Validate its structured response against those categories, show the suggested category, and let the user accept or change it. Store the result only when the transaction is saved. An unavailable model must leave manual entry usable; do not label a manual or rules-based fallback as an AI result.

### Flow 2: monthly intelligence supporting three separate features

Spring Boot loads the authenticated user's actual selected-month transactions, prior-period data if available, budgets, and deterministic aggregates. Send only the data needed for these outputs:

1. **Spending pattern analysis:** explain top categories and supported changes; use prior-month comparisons only when that data exists.
2. **Budget recommendations:** suggest category-level changes and explain the evidence; show an apply action that updates a budget only on explicit user selection.
3. **Monthly financial summary:** concise income, expenses, net cash flow, and key observations tied to the computed totals.

One structured response can supply three distinct, labeled dashboard sections, each with its own purpose and demo acceptance check. Use dedicated sections/actions so judges can identify all four AI capabilities, even if transport and analysis data are shared. Validate returned figures and category IDs. Keep money calculations in Java and handle sparse/no data explicitly. Refresh insights on request; invalidate or mark them stale when relevant transactions/budgets change.

LLM calls must run through Spring Boot. Keep API credentials server-side. No real personal data is required for the demo; use fictional seeded transactions. The app is for spending awareness and budgeting, with no investment or lending decisions in scope.

## Proposed screen organization

- **Register/login:** one compact authentication flow.
- **Overview:** month selector, income/expense/net-cash-flow cards, category chart, spending trend, budget progress, AI insights, and recent transactions.
- **Transactions:** searchable/filterable history with an add/edit form and AI category suggestion.
- **Budgets and accounts:** two focused sections reachable from the same navigation; simple forms and progress indicators.

Visual direction to explore: light neutral surfaces, dark navy text/navigation, emerald accents, readable currency figures, consistent spacing, restrained motion, and responsive cards/tables. This is a proposal, not an existing design.

## Data model

- `users`: identity and password hash.
- `accounts`: owned by a user; name, type, opening balance.
- `categories`: owned by a user; name and income/expense type; seed defaults per user.
- `transactions`: owned by a user; related account and category; type, exact-decimal amount, date, description.
- `budgets`: owned by a user; category, month, amount; one budget per user/category/month.

Validate that referenced accounts/categories belong to the authenticated user. Derive balances from opening balances plus transactions. Use exact-decimal money calculations and one currency, INR, for the first version.

## Core demonstration

1. Log in to a seeded fictional account.
2. Enter an expense with a recognizable description.
3. Ask AI to suggest a category, review it, then save.
4. Show the stored transaction updating the dashboard and category budget.
5. Generate monthly intelligence using the stored data; explain patterns, recommendations, and the summary.
6. Apply a budget recommendation after user selection and show the updated budget.
7. Export the selected-month report and refresh to demonstrate persistence.

Use a second user to verify data isolation before presenting. Clearly identify fictional demo data and any unavailable integration.

## Scope exclusions for five hours

Bank-account linking, payments, transfers between accounts, receipt OCR, multi-currency conversion, investment recommendations, SMS/email ingestion, and a generic finance chatbot are outside the initial scope. Protect the required features, real AI integration, deployment, and demonstration first.

## Early dependencies and risks

- Confirm permitted builder AI tools and any additional rubric details with organizers. The brief requires application AI but does not establish all rules on AI-assisted development.
- Need a working backend LLM provider/API configuration. Codex Pro usage does not establish application API access; validate separately and keep keys local/server-side.
- Need hosting and hosted MySQL access. Verify a minimal deployed frontend/backend/database connection early rather than waiting until the final hour.
- Five hours is tight for the full brief. Keep each required feature thin, use one visual system, and avoid optional infrastructure. A local-only or mocked-AI demo would leave stated requirements unmet.

See the [five-hour execution schedule](13-day-schedule-and-usage-budget.md). Add implementation paths, tests, and demo notes to each topic file as code is created.
