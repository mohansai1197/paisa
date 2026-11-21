# First-hour review checkpoint and access setup

> Status update: the user authorized implementation. Paisa now has a working first version. See [the implementation and review guide](19-implementation-and-review.md) for verified behavior and pending integrations. Planning-only statements below record an earlier stage and no longer apply.

[Back to the file directory](../README.md)

## Latest decisions

- User reports submission is approximately **3 p.m. IST on 21 November 2026**. Confirm the exact organizer cutoff.
- User wants a **first build review after about one hour of implementation**, then discussion and improvements. The submission deadline is not the first-review target.
- Keep Paisa, the light navy/emerald direction, INR, fictional demo data, and CSV reports.
- User reports no LLM API or hosting accounts. No additional event rules were supplied.
- All core requirements and all four named AI features remain in scope.
- Application coding has not started. The existing planning-only instruction remains in force until implementation is requested; the one-hour clock is measured from actual implementation start.

## One-hour target, not a completion guarantee

| Elapsed build time | Target |
| --- | --- |
| 0–10 minutes | Freeze essential screen/data/API choices; check access prerequisites; establish project structure and shared UI foundation |
| 10–30 minutes | Implement authentication, the five-table schema, and account/category/transaction handling |
| 30–45 minutes | Connect the dashboard, budgets, history, analytics, and CSV report |
| 45–55 minutes | Connect the four AI capabilities to real configured backend provider access, if available |
| 55–60 minutes | Run focused checks, open the running app, and prepare the first review with a precise completion/blocker list |

Validate deployment connectivity as early as credentials permit rather than postponing discovery until the end. The targets are aggressive for the full brief and depend on downloads, builds, database access, provider configuration, and hosting. Reassess at the one-hour checkpoint even if some work is incomplete.

## Access prerequisites

- **Local database:** Workbench is authenticated, but the application still needs its own database connection configuration. A GUI session does not automatically authenticate Spring Boot.
- **Application AI:** requires a provider API account/key and usable quota. Keep secrets local/server-side. Without it, show an honest unavailable state and list real-AI verification as pending.
- **Live deployment:** requires a usable hosting account and hosted MySQL connectivity. Local success alone does not satisfy the live-deployment requirement.
- **Source submission:** GitHub access/repository still needs verification.

## Practical candidate setup path

These are researched candidates, not accounts or services already created:

- **Gemini API via Google AI Studio:** [official API-key setup](https://aistudio.google.com/docs/api-key). [Pricing](https://ai.google.dev/gemini-api/docs/pricing) lists limited free-tier access; actual project eligibility, models, and quota must be checked. Use fictional data.
- **Railway for application hosting and MySQL:** [official Spring Boot guide](https://railway.com/guides/spring-boot) and [Free/Trial documentation](https://docs.railway.com/pricing/free-plan). Trial resources and account verification/network restrictions require checking before committing to it. One candidate arrangement serves the compiled React frontend through Spring Boot, with MySQL as a separate service, reducing deployments and origin configuration.

No accounts, API keys, billing arrangements, or deployments have been created. Paid services are not included in the current plan. An available teammate could handle account onboarding while implementation progresses, if the team agrees.

## First review

At the checkpoint, show the running app and identify **working and verified**, **implemented but unverified**, **blocked on access**, and **remaining** items. Review the interface, feature behavior, all four AI outputs, and next changes with the user. Do not delay this review until 3 p.m. or claim unconfigured services work.
