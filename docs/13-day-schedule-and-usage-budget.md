# Five-hour execution schedule and usage budget

[Back to the file directory](../README.md)

**Latest timing update:** user reports submission approximately 3 p.m. IST and requests a first build review after **one hour of implementation**, then changes. Use [the first-hour plan](18-first-hour-and-access-setup.md) for the immediate checkpoint. The longer schedule below is an initial event-window planning reference, not a reason to postpone review until the submission deadline.

**Confirmed by user:** build duration is **five hours**, correcting the earlier ten-hour estimate; user has a Pro plan. The finance brief is now available, and the user wants all four named AI capabilities included.

**Timing:** use elapsed time from the actual build start because the corrected absolute start/deadline are unknown. The brief is dated 21 November 2026 and gives a reporting time, which does not establish the build start. Remaining usage, purchased credits, and reset times have not been inspected. This is a working plan, not a guaranteed credit forecast or an enforced spending cap.

## Day schedule

| Elapsed time | Work and concrete milestone | Suggested reasoning |
| --- | --- | --- |
| 0:00–0:20 | Confirm scope and deadline, choose the visual foundation, settle schema/API shapes, and validate LLM/hosting/database access | High for architecture, Medium for setup |
| 0:20–1:30 | Build authentication, five related tables, account/category basics, the main dashboard, and the persisted transaction flow; get a minimal deployment running early | Medium |
| 1:30–2:30 | Finish thin CRUD flows, monthly budgets, deterministic analytics, history filters, and downloadable CSV report | Medium |
| 2:30–3:20 | Complete and verify all four named AI features using actual application data; add an extra feature only if the main flow and deployment are already reliable | Medium; High for difficult integration diagnosis |
| 3:20–4:05 | Freeze optional features, complete live deployment checks, verify user isolation and errors, and polish laptop/mobile layouts | Medium; High for a defined critical blocker |
| 4:05–4:40 | Finalize README, ER diagram, Postman collection, explanation, demo rehearsal, and backup | Medium |
| 4:40–5:00 | Submit required artifacts and retain the remaining time for submission issues or critical demo fixes | Use only as needed |

These are aggressive targets for the required scope, not a completion guarantee. Start hosting and AI-access validation in the first block; do not defer those dependencies to the integration block. Adjust ordering around available accounts and real blockers.

Teammates can prepare research, test reports, fictional demo content, and slides while implementation progresses. Confirm support assignments rather than relying on unagreed work.

## Pro usage facts

The [official pricing documentation](https://learn.chatgpt.com/docs/pricing) states, as checked during preparation, that Pro plans have no five-hour limit. This does not establish the user's remaining allowance, other applicable limits, or purchased-credit balance. Use the account's usage dashboard for current limits and reset information; the event duration does not determine account reset windows.

Usage depends on the model, task complexity, context, reasoning, tools, retrieval, and caching; elapsed time and message count do not provide a fixed credit cost. [Official pricing documentation](https://learn.chatgpt.com/docs/pricing).

Higher reasoning effort can help complex tasks but takes longer and uses more tokens. [Official model guidance](https://learn.chatgpt.com/docs/models).

## Provisional allocation if a finite credit budget is set

Apply these percentages to an explicitly chosen available credit budget, not to the subscription price or an assumed balance. These are planning targets, not measured costs. Included usage is tracked using the account dashboard's own indicators; it cannot automatically be converted into this credit allocation.

| Purpose | Budget target |
| --- | --- |
| Initial scope and architecture | 10% |
| Core frontend/backend/database implementation | 40% |
| Integration and meaningful verification | 20% |
| Final visual polish | 5% |
| Pitch and explanation refinement | 5% |
| Reserve for critical fixes and submission | 20% |

Without a known balance, no exact number of credits or guaranteed number of runs can be budgeted. The reserve is a manual planning target; this chat cannot enforce or continuously measure account usage.

## Practical conservation rules

- Use Medium for routine work; recommend High for a defined architecture decision or difficult blocker, then return to Medium.
- Prefer Standard speed if available; faster modes can consume more allowance. Check the [official speed multipliers](https://learn.chatgpt.com/docs/pricing) for the selected model.
- Send focused feature batches with acceptance criteria; avoid requesting a complete regeneration for small changes.
- Inspect the existing code and make targeted edits. Keep tool output and repeated context concise.
- Use the topic documents as a concise record so decisions and explanations do not need to be repeatedly rebuilt.
- Run checks appropriate to changes; repeat or broaden them when new changes, failures, or unresolved concerns justify it.
- Keep initial custom imagery to what materially helps the demo, and iterate deliberately rather than generating many variants.
- At the start, around elapsed 2:00, and around elapsed 3:30, check the usage dashboard. Reduce optional scope if usage is running ahead of the plan. Do not assume the assistant can read that dashboard automatically.
- Codex development usage and the application's LLM-provider usage are separate dependencies. Set up backend-only API configuration and avoid regenerating monthly insights on every UI render.
- No credit purchases or paid upgrades are included in this plan.

## If time or usage becomes tight

Stop optional features, preserve the working main journey, fix demo-blocking defects, and rehearse. Use existing UI components and demo assets. Explain unfinished work accurately.

After completed work, follow the [next-effort recommendation rule](12-model-and-reasoning-guide.md).
