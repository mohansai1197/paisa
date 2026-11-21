# Paisa

A personal-finance dashboard for the VCube Hack Quest. React + Java + Spring Boot + Spring Security + JPA, configured for MySQL with backend-only Gemini integration.

**Local app:** [Open Paisa locally](http://127.0.0.1:5173) or [open the private hosted sample preview](https://paisa-finance-hackquest.violetkoala8.chatgpt.site). The current connected workspace uses **MySQL 8.0.37**. The browser sample workspace is also available without login. All demo records are fictional.

**Implemented:** email-verified registration/login, accounts, categories, income/expenses, transaction history with search/filters/edit/delete, monthly budgets, charts, CSV downloads, and the four AI feature flows. **MySQL persistence and real Gemini calls are verified. Source:** [mohansai1197/paisa](https://github.com/mohansai1197/paisa). **Live Java/MySQL app:** [Paisa on Railway](https://paisa-production.up.railway.app). **Pending:** production email delivery and authenticated end-to-end verification; Railway Trial blocks Gmail SMTP. No provider outputs are fabricated.

## Run locally

Requirements: Java 17+, Maven, Node 22+, npm. From this folder:

```sh
python3 scripts/run_backend.py
```

In a second terminal:

```sh
cd frontend
npm ci
npm run dev
```

Open http://127.0.0.1:5173. Register a fictional review workspace and choose **Load sample records**, or explore the browser sample without registering. The backend runs on port 8080. MySQL credentials are already configured privately on this machine. For a separate H2 demonstration without MySQL, add `--demo` to the backend command. After source changes, run `mvn package` in `backend` before restarting the jar.

## Connect MySQL and Gemini

Create the application database using [database/schema.sql](database/schema.sql) in your authorized MySQL session. Use a dedicated database account limited to `paisa.*`. Run `python3 scripts/configure_local.py` to enter existing application credentials privately; it saves an ignored, owner-readable `.env`. Gemini is optional for manual features. Start `python3 scripts/run_backend.py` without `--demo` for MySQL. Keep keys server-side and use fictional data with the provider.

The [implementation guide](docs/19-implementation-and-review.md) maps each feature to its files and explains exactly what leads where. The [AI Studio setup guide](https://aistudio.google.com/docs/api-key) explains obtaining a key. Actual model availability/quota must be checked on the account.

## Verify and deploy

Run `mvn test` in `backend` and `npm run build` in `frontend`. Thirty-five backend checks and thirteen frontend checks pass, including finance integration and AI transport/validation cases. Real provider calls pass for all four AI features using fictional data. A separate live MySQL check verified registration/session, saved transactions, refreshed balances, test-record cleanup, all five tables, and monthly Gemini output using MySQL records. The Dockerfile compiles React into Spring Boot for one-origin hosting. `railway.toml` prepares a deployment; set database environment variables, `GEMINI_API_KEY`, and `SECURE_COOKIES=true` behind HTTPS. Docker is not installed locally; the GitHub Actions Docker image build passed, and Railway reports a successful deployment. A combined executable jar has been built and verified on Java 17: use `python3 scripts/build_release.py` to reproduce it. Use `python3 scripts/package_source.py` for a source-only ZIP that excludes private files.

The [private Sites sample preview](https://paisa-finance-hackquest.violetkoala8.chatgpt.site) is published successfully. It demonstrates browser sample mode only; the full Java/MySQL app is deployed separately on Railway; production email remains pending. The hosted preview requires owner sign-in. Automated browser handoff redirected to OpenAI authentication and was blocked by approval review, so hosted UI verification is pending; the same frontend was verified locally.

## Submission artifacts

- [ER diagram](database/er-diagram.svg), [diagram source](database/er-diagram.mmd) and [MySQL schema](database/schema.sql)
- [Postman collection](postman/Paisa.postman_collection.json)
- [Demo explanation and verification limits](docs/19-implementation-and-review.md)

## File directory

Each link below is the full address of a file. Update the relevant file when a decision or implementation changes.

| Topic | File | What you will find |
| --- | --- | --- |
| Event and problem | [00-event-brief.md](docs/00-event-brief.md) | Rules, judging criteria, problem, users, scope |
| Local setup | [01-setup.md](docs/01-setup.md) | Required tools, checks, startup instructions |
| Frontend and visual design | [02-frontend-design.md](docs/02-frontend-design.md) | Visual workflow, screen inventory, component organization |
| Backend | [03-backend.md](docs/03-backend.md) | Spring Boot responsibilities and request handling |
| Database | [04-database.md](docs/04-database.md) | MySQL tables, relationships, sample data |
| API and integration | [05-api-integration.md](docs/05-api-integration.md) | Frontend/backend agreement and request flow |
| Work plan and roles | [06-workflow-and-team.md](docs/06-workflow-and-team.md) | Build order, time allocation, ownership |
| Testing and polish | [07-testing-and-polish.md](docs/07-testing-and-polish.md) | Functional checks, visual review, known issues |
| Demo and explanation | [08-demo-and-pitch.md](docs/08-demo-and-pitch.md) | Demo script, speaking points, judge questions |
| Submission | [09-submission.md](docs/09-submission.md) | Event deliverables, deadline, final links |
| Decisions and progress | [10-decisions-and-progress.md](docs/10-decisions-and-progress.md) | Agreed choices, current status, next actions |
| Coding standards and ethics | [11-coding-standards-and-ethics.md](docs/11-coding-standards-and-ethics.md) | Readability, data handling, attribution, honest verification |
| Model and reasoning choices | [12-model-and-reasoning-guide.md](docs/12-model-and-reasoning-guide.md) | When Medium or High may help, and current switching limitations |
| Day schedule and usage budget | [13-day-schedule-and-usage-budget.md](docs/13-day-schedule-and-usage-budget.md) | Corrected five-hour milestones and a provisional usage budget |
| Finance project scope | [14-finance-project-scope.md](docs/14-finance-project-scope.md) | Proposed Paisa product, requirements, AI flows, data model, and demo |
| Basic phased plan | [15-basic-build-plan.md](docs/15-basic-build-plan.md) | Original phased roadmap and completion checkpoints |
| System design and build workflow | [16-system-design-and-build-workflow.md](docs/16-system-design-and-build-workflow.md) | Architecture, database relationships, API responsibilities, runtime flows, and full-project build order |
| Project and feature pipeline | [17-project-pipeline.md](docs/17-project-pipeline.md) | Build order plus each feature's input, processing, result, and connected updates |
| First-hour review and access | [18-first-hour-and-access-setup.md](docs/18-first-hour-and-access-setup.md) | One-hour review target, approximate deadline, and missing API/hosting prerequisites |
| First working version | [19-implementation-and-review.md](docs/19-implementation-and-review.md) | Actual files, verified flows, pending integrations, pitch |


New signups require email verification. Email delivery needs private SMTP configuration; if it is missing, registration is blocked. Existing accounts retain their previous access and are not marked verified automatically. See [email verification](docs/24-email-verification.md).

Current integration details: [AI, MySQL, and deployment checkpoint](docs/20-ai-database-and-deployment.md).

[Demo script and judge explanations](docs/21-demo-script.md).

[Balance validation and insufficient-funds checks](docs/22-balance-validation.md).

[Registration and hosted-preview connection](docs/23-registration-and-preview-connection.md).

[Forgot password and session recovery](docs/25-forgot-password.md).

## Source directory

| File or directory | Responsibility |
| --- | --- |
| `frontend/src/App.jsx` | Navigation, workspace state, API-backed/sample actions |
| `frontend/src/pages.jsx` | Overview, Transactions, Accounts, Budgets, AI insights, Reports |
| `frontend/src/components.jsx` | Shared controls, transaction/account/budget/authentication forms |
| `frontend/src/finance.js` | Integer-cent calculations, chart data, CSV formatting |
| `frontend/src/api.js` | Session requests and CSRF headers |
| `frontend/src/data.js` | Clearly labeled fictional browser sample data |
| `frontend/src/styles.css` | Responsive navy/emerald visual design |
| `backend/src/main/java/com/paisa/api/` | Validated REST requests and error handling |
| `backend/src/main/java/com/paisa/service/FinanceService.java` | User-scoped persistence, decimal totals, budgets, reports |
| `backend/src/main/java/com/paisa/service/AiService.java` | Provider requests and response validation |
| `backend/src/main/java/com/paisa/config/SecurityConfig.java` | Password hashing, sessions, CSRF and endpoint access |
| `backend/src/main/java/com/paisa/model/` | The five JPA entities |
| `backend/src/test/java/com/paisa/FinanceIntegrationTest.java` | Focused integration checks |
| `scripts/` | Private local setup and backend launcher |

Workspace address: `/Users/mohansai/Documents/ChatGPT/hackathon`. The documentation index retains planning history; document 19 records the first build and document 20 records the current integration status.

[Demo-data loading](docs/26-demo-data.md).

[Production deployment and verification](docs/27-production-deployment.md).
