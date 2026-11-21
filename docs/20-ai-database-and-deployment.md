# Paisa integration checkpoint

## Real Gemini API — verified

The backend now uses the supplied Gemini key from private `.env` (mode 0600).
The key is not in React, provider URLs, source control, logs, or the Sites upload.
The configured model is `gemini-3.5-flash-lite`; Google model metadata returned HTTP 200.

Real backend calls using fictional local demo records verified all four features:
expense categorization, spending patterns, budget recommendations, monthly summary.
The category request returned an allowed Food & dining category. Monthly output
returned patterns, four validated budget targets, and a narrative matching the
calculated income/expense/net totals. The browser rendered the generated monthly
results successfully. No synthetic fixtures are presented as real AI results.

## Source files and flow

- `backend/src/main/java/com/paisa/service/GeminiClient.java`: authenticated HTTPS,
  JSON schema, timeouts, quota/model/authorization errors, response parsing.
- `backend/src/main/java/com/paisa/service/AiService.java`: user data context,
  allowed category IDs, validated recommendation amounts, monthly result assembly.
- `backend/src/main/resources/ai/category.schema.json`: classification response contract.
- `backend/src/main/resources/ai/monthly.schema.json`: patterns/budgets/summary contract.
- `backend/src/test/java/com/paisa/service/GeminiClientTest.java`: eight synthetic
  transport cases, including header handling, malformed output, and provider errors.
- `backend/src/test/java/com/paisa/service/AiServiceTest.java`: four validation cases.
- `scripts/local_setup.py`: loopback-only private configuration form with Origin,
  Host, token, frame, and no-cache protections. It never returns credential values.
- `scripts/setup_mysql.py`: prepare dedicated local account SQL, then verify and
  activate its private backend connection settings.

## Checks

Nineteen backend tests passed: seven finance integration cases, eight provider
transport cases, and four AI validation cases. Synthetic transport cases do not
claim Google availability; separate real Google calls above establish the live path.
The private setup endpoint rejected foreign origins and invalid tokens without
changing the existing credentials.

## MySQL — connected and verified

The prepared `.local/mysql-setup.sql` creates the `paisa` database and a unique
application account limited to that database on localhost. It grants SELECT,
INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES for application operation
and Hibernate schema updates. It does not change existing account passwords.
The file contains a generated password and must remain private.

The user explicitly approved execution, and the prepared script ran successfully
in the authenticated Workbench connection. `python3 scripts/setup_mysql.py --verify`
authenticated the dedicated application account against MySQL 8.0.37 and activated
its private settings in `.env`. The backend now runs without `--demo`. No saved
root password was extracted. The original H2 demonstration remains separate.

All five tables exist: `users`, `accounts`, `categories`, `transactions`, `budgets`.
Workbench is left with **SCHEMAS → paisa → Tables** expanded and a successful
`SHOW TABLES FROM paisa;` result. To inspect records, right-click a table and choose
**Select Rows - Limit 1000**. Avoid sharing user password hashes or private setup SQL.

A live API check verified registration/session, seeded 28 fictional transactions,
saved an expense of ₹125.25, reloaded it, checked the matching account balance and
monthly expense changes, and removed the temporary test entry. A real monthly
Gemini request using these MySQL records also passed, returning two patterns and
four validated budget recommendations. Its fictional-data result is stored in
`output/mysql-ai-verification.json`. Table proof: `output/paisa-mysql-tables.png`.

## Deployment and submission — still pending

The private Sites preview is a browser sample preview. It does not run Java,
MySQL, or the Gemini backend. A full deployment needs an application host and a
MySQL service; the root Dockerfile serves React through Spring Boot on one origin.
No paid hosting or billing upgrade is authorized. A GitHub submission repository,
full application URL, and submission confirmation are still outstanding.

The supplied key appeared in chat. Rotate it before public launch, then update
private backend configuration. Never copy it into README, screenshots, or GitHub.

## References checked

- [Gemini key setup](https://aistudio.google.com/docs/api-key)
- [Gemini structured outputs](https://aistudio.google.com/docs/structured-output?generate-content=true)
- [Available models](https://aistudio.google.com/docs/models)

## Combined application release — verified

`python3 scripts/build_release.py` builds React into Spring Boot. The resulting
`backend/target/paisa-1.0.0.jar` was started on Java 17.0.19 with a disposable H2
database on port 8081. HTML, both bundled assets, and `/api/status` returned HTTP
200. The temporary verification server was stopped afterward; the main app remains
on ports 5173/8080. Docker itself is still unverified because it is not installed.

`python3 scripts/package_source.py` creates `output/paisa-source.zip` from
Git-visible source only. Private environment files, generated account credentials,
builds, databases, and dependencies are excluded. A check also prevents including
the supplied Gemini key in the source package.

Balance validation is now enforced for cash, bank, and wallet mutations, including concurrent spending. Four frontend checks and a separate live MySQL run also passed. See [balance validation](22-balance-validation.md).

Email verification is now configured with Gmail SMTP; the local app reports both AI and mail configured. New signups require a one-time code, and a verified account was confirmed in MySQL. Existing accounts retain their prior access. See [email verification](24-email-verification.md).
