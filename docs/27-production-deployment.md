# GitHub and Railway deployment

The application ships as one Java 17 container with React assets included. The browser calls relative `/api` URLs, so frontend, session cookies, and API share one HTTPS origin. Do not deploy only `frontend/` for the connected application.

## Current checkpoint

Local release and finance/auth/AI regression checks pass. Source is uploaded to https://github.com/mohansai1197/paisa on `main` after explicit approval for public source with credentials excluded. Production hosting and remote end-to-end verification remain pending Railway setup. All 35 backend tests also pass on Java 17; 13 frontend tests pass. A temporary production-profile instance verified MySQL startup and Secure CSRF cookies behind HTTPS proxy headers, then was stopped. Docker is not installed locally; the GitHub workflow builds the image once source is uploaded.

## Repository

Create a private, empty GitHub repository named `paisa`, without generated README or license. Upload only Git-visible source. `.env`, `.local`, generated static assets, logs, databases, and output artifacts are excluded. `scripts/package_source.py` checks source against configured Gemini, SMTP, and database secrets before packaging. Never upload `.local/mysql-setup.sql` or local account data.

`.github/workflows/verify.yml` runs frontend tests, builds the complete release with backend tests, and builds the deployment Docker image on Java 17 / Node 22. Successful CI is evidence of a container build, not proof of a live deployment.

## Railway

1. Sign in at https://railway.com/dashboard and create a project. Handle account terms or payment choices yourself.
2. Add MySQL to this project. Keep database networking private.
3. Add the GitHub repository as a service with the repository root as build context. Railway detects `Dockerfile` and `railway.toml`.
4. Set these service variables (the database service name below is `MySQL`; adapt if renamed):

```text
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
GEMINI_API_KEY=<enter privately>
GEMINI_MODEL=gemini-3.5-flash-lite
```

Use a dedicated limited database account when provisioning one; do not reuse your local database password. A cloud database starts empty. No local user records are migrated by this deployment.

5. The production profile honors HTTPS proxy headers and requires Secure session cookies. The container uses Asia/Kolkata for month/date calculations. Keep one application replica because sessions and request-rate counters are in memory.
6. Configure email before declaring registration ready. **Railway Free, Trial, and Hobby block SMTP**, including Gmail port 587. Existing Gmail SMTP support requires Railway Pro or above; otherwise an HTTPS email adapter/provider must be added and verified. Do not turn off verification to bypass this limitation.
7. For a Railway plan supporting SMTP, privately set `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, and `MAIL_FROM` from the approved email sender. Gmail uses port 587 with STARTTLS. Never paste secrets into GitHub, logs, or chat.
8. Deploy and generate the Railway HTTPS domain. The health path is `/api/status`. Inspect build/runtime logs without sharing secret values.

## Verification

Run `python3 scripts/check_deployment.py https://YOUR-RAILWAY-DOMAIN`. This checks frontend HTML, JSON status, MySQL mode, CSRF, Secure cookies when returned, and unauthenticated workspace rejection. Configuration flags do not prove provider connectivity.

Then verify in the browser: register → receive email → verify → sign in → load demo records → refresh to confirm persistence → generate monthly AI insights → check balances/reports → request password reset and complete it privately. Confirm previous sessions are rejected after reset. No live URL is considered verified before these steps pass.

References: https://docs.railway.com/databases/mysql, https://railway.com/guides/spring-boot, https://docs.railway.com/networking/outbound-networking.
