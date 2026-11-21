# Basic plan: Paisa

> Status update: the user authorized implementation. Paisa now has a working first version. See [the implementation and review guide](19-implementation-and-review.md) for verified behavior and pending integrations. Planning-only statements below record an earlier stage and no longer apply.

[Back to the file directory](../README.md)

**Current instruction:** planning only. Do not create application code or begin implementation until the user asks to start coding. Documentation changes are part of planning. No application code exists yet.

**Clarification:** the user wants the full agreed project when implementation begins, with a clear system design and workflow first. These phases describe work order, not a reduced final feature set. Use the [system design and build workflow](16-system-design-and-build-workflow.md) as the current planning entry point.

## Goal

A polished personal-finance app: record money coming in and going out, understand spending, manage budgets, and use four clearly identified AI features.

## Phase 1: agree on the experience

- Set the main user journey: log in → record a transaction → see totals/budget progress → generate insights → download a report.
- Plan login/register, overview, transactions, budgets/accounts, and AI insights sections.
- Choose a consistent visual direction and the most important information on the opening screen.
- Decide essential transaction fields: amount, income/expense type, date, description, account, and category.
- Identify usable LLM API, database, and hosting access early; keep credentials out of chat.

**Result:** a clear screen outline and data agreement. No implementation in this phase unless separately requested.

## Phase 2: build the foundation, when authorized

- Establish the React layout and shared components; clearly identify any temporary sample data.
- Implement registration/login and user-scoped access using Spring Security.
- Create the five related MySQL tables through the backend's schema-management approach.
- Make accounts, categories, income/expenses, and transaction history work through Spring Boot.
- Connect the overview to stored records; verify persistence and separation between users.
- Validate a minimal live frontend/backend/database connection early.

**Checkpoint:** one user can sign in, add a transaction, and see the correct updated totals after refreshing; another user cannot access those records.

## Phase 3: add the remaining required capabilities

- Monthly category budgets and progress indicators.
- Spending charts, history filters, and downloadable CSV reports.
- Four distinct AI features: expense categorization, spending pattern analysis, budget recommendations, and monthly summaries.
- Keep calculations deterministic in Java; use the backend LLM for data-grounded categorization and explanations.
- Let users correct categories and explicitly select any budget change.

**Checkpoint:** each named feature has a visible result and a testable real-data flow. Show unavailable-AI and insufficient-data states honestly.

## Phase 4: make it ready to present

- Review laptop/mobile layouts and interaction states.
- Test the full user journey and fix important failures.
- Complete live deployment and verify production database connectivity.
- Prepare README, ER diagram, Postman collection, demo data, and pitch.
- Rehearse one short demonstration and submit with a buffer.

**Checkpoint:** required artifacts and the live demonstration work together.

## Gradual upgrades

Once the required foundation and four AI features are reliable, consider unusual-spend alerts and further visual refinements. Decide extras with the user based on remaining time. Keep the corrected five-hour limit in mind; see the [execution schedule](13-day-schedule-and-usage-budget.md).

## Immediate next planning decisions

1. Finalize the screen outline and opening dashboard content.
2. Choose the visual direction.
3. Confirm essential fields and the first demo journey.

The user leads feature decisions. The assistant handles implementation only after the user requests coding, and documents how completed work functions.
