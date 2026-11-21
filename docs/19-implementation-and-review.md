# Paisa: first working version

This records the first implementation review. Document 20 contains the latest integration status. Earlier documents describe the planning
process; their proposed routes or names may differ. Coding was explicitly authorized
by the user. The project name is **Paisa**.

## Open and run

- Local review: http://127.0.0.1:5173
- Published private sample preview: https://paisa-finance-hackquest.violetkoala8.chatgpt.site (owner sign-in required)
- Backend status: http://127.0.0.1:8080/api/status
- Backend with the explicitly labeled H2 demo database: `python3 scripts/run_backend.py --demo`
- Frontend, in a second terminal: `cd frontend` then `npm ci` then `npm run dev`.
- Tests: run `mvn test` inside `backend`. Build frontend with `npm run build` inside `frontend`.
- MySQL/Gemini configuration: `python3 scripts/configure_local.py`. Enter credentials
  privately in the terminal. Then run the backend **without** `--demo` for MySQL.
- Local review account: `demo.review@paisa.example` / `PaisaDemo2026!` is fictional,
  local-only test data. It is not a production credential. Browser sample mode needs no login.

## What leads where

| Feature | Flow | Files | What to explain |
| --- | --- | --- | --- |
| Registration/login | Form → CSRF-protected API → BCrypt + session → private workspace | `frontend/src/components.jsx`, `backend/src/main/java/com/paisa/api/AuthController.java`, `backend/src/main/java/com/paisa/config/SecurityConfig.java` | Every financial request is scoped to the signed-in user. |
| Accounts | Add/edit account → save opening balance → derive live balance from all its transactions | `frontend/src/pages.jsx`, `backend/src/main/java/com/paisa/service/FinanceService.java` | Account balances include opening amounts; monthly cash flow does not. |
| Transactions | Add income/expense → validate category/account ownership → persist → refresh workspace | `frontend/src/components.jsx`, `frontend/src/App.jsx`, `backend/src/main/java/com/paisa/api/FinanceController.java` | A saved entry updates history, balances, charts, and budget progress. |
| Categories | Transaction form → New category → choose income/expense type → save | `frontend/src/components.jsx`, `backend/src/main/java/com/paisa/service/FinanceService.java` | Categories belong to each user; type must match the transaction. |
| Budgets | Choose expense category + month + limit → compare to actual spending | `frontend/src/pages.jsx`, `frontend/src/finance.js`, `backend/src/main/java/com/paisa/service/FinanceService.java` | Progress uses recorded expenses; alerts start at 85%. |
| Dashboard | Selected month → integer-cent client aggregation → charts and totals | `frontend/src/pages.jsx`, `frontend/src/finance.js` | The charts use actual workspace records, not decorative numbers. |
| Reports | Selected month → CSV → file download | `frontend/src/finance.js`, `backend/src/main/java/com/paisa/service/FinanceService.java` | Export includes entries and monthly totals; values are escaped for spreadsheet safety. |
| AI categorization | Expense description → backend → Gemini → allowed category ID → user reviews | `backend/src/main/java/com/paisa/service/AiService.java`, `frontend/src/components.jsx` | The model suggests; the user confirms before saving. |
| AI patterns | Backend monthly aggregates → Gemini → pattern cards | `backend/src/main/java/com/paisa/service/AiService.java`, `frontend/src/pages.jsx` | Model receives measured spending, not invented history. |
| AI budgets | Measured category spending → illustrative 10% reduction candidates → Gemini explanation → review budget form | Same AI service and pages | This is a starting target, not an affordability assessment or guaranteed saving. |
| AI summary | Income/expenses/net/count/category facts → Gemini → monthly narrative | Same AI service and pages | Numbers are calculated before generation; generated text still needs human review. |

All source paths in the table are relative to `/Users/mohansai/Documents/ChatGPT/hackathon`.
Each concern has its own file; related forms and screens currently share two modules
to keep the first version straightforward to change.

## Verified at the first review

- React production build passed.
- Two Spring integration tests passed with zero failures/errors: authentication,
  ownership isolation, decimal arithmetic, persistence, budgets, CSV escaping,
  validation, CSRF rejection, and missing-provider handling.
- Browser registration → backend → local H2 persistence → workspace succeeded.
- A new expense updated dashboard/category/budget figures, survived reload, could
  be edited and found through search, and was removed after the test.
- Budget editing changed the limit, remaining amount, and progress; the original demo limit was restored.
- CSV report returned HTTP 200 with the correct monthly net cash flow; browser download feedback confirmed the file was ready.
- Responsive navigation opens and routes correctly.
- AI screen clearly shows an unavailable state without provider credentials.
- Java runtime now uses a separate jar copy so rebuilding the source package does not replace the running application.

## Pending, with no completion claim

- Production Java/MySQL hosting, public submission URL, and GitHub repository URL.
- Docker image build: Docker is not installed locally; deployment configuration is prepared, unverified.
- Full accessibility/security audit, load testing, pagination, account recovery, and production migrations.

The user explicitly approved frontend source upload, resolving the earlier upload review rejection. The private Sites sample preview is published successfully. Automated browser handoff redirected to OpenAI authentication and was blocked by approval review; hosted UI verification is therefore pending. The Sites static preview only demonstrates browser sample mode.
It does not satisfy the full deployed-backend or MySQL requirement.

## Two-minute explanation

“Paisa brings personal income, expenses, accounts, and budgets into one understandable
workspace. Record an expense once and the entire financial picture updates. Four AI
features help classify entries, explain patterns, suggest budget starting points, and
summarize the month. Calculations and ownership checks happen in application code;
AI explains measured facts, and people review its suggestions before applying them.”

Demo: Overview → add an expense → show changed totals → transaction search → budgets
→ AI insights → CSV report. Describe unconfigured services honestly during this review.

## Private preview publication record

- Site: `appgprj_6b01277008748191aae787afef5107e0`
- Source commit: `d17d147e0a82bb1825d35054198f9fcf311b2c31`
- Deployment: `appgdep_6b012c6916608191bfcb0f38f2659a73`
- Native deployment result: `succeeded`, 21 November 2026.
- URL: https://paisa-finance-hackquest.violetkoala8.chatgpt.site
- Source upload was limited to the React frontend. Credentials and the Java backend were not uploaded to Sites.

## Integration update

Real Gemini responses for all four features are now verified, and fourteen backend checks pass. The backend now uses real MySQL 8.0.37: all five tables, registration/session, persisted transactions, account balances, and monthly AI generation from MySQL records passed live checks. See [the integration checkpoint](20-ai-database-and-deployment.md) for current status.
