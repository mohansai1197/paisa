# GitHub and Railway deployment

The application ships as one Java 17 container with React assets included. The browser calls relative `/api` URLs, so frontend, session cookies, and API share one HTTPS origin. Do not deploy only `frontend/` for the connected application.

## Current checkpoint

Local release and finance/auth/AI regression checks pass. Source is uploaded to https://github.com/mohansai1197/paisa on `main` after explicit approval for public source with credentials excluded. Railway reports a successful deployment at https://paisa-production.up.railway.app. Production email delivery and authenticated remote end-to-end verification remain pending. The GitHub Docker image build passed: https://github.com/mohansai1197/paisa/actions/runs/44884593187. All 35 backend tests also pass on Java 17; 13 frontend tests pass. A temporary production-profile instance verified MySQL startup and Secure CSRF cookies behind HTTPS proxy headers, then was stopped. Docker is not installed locally; the GitHub workflow builds the image once source is uploaded.

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

## Live deployment checkpoint

- Project: `b9839d86-3b95-4e6c-8cd7-5cfb1e680db8` (Railway-generated name `inspiring-victory`).
- App service: `751803e1-5cb3-4b12-8512-c6c14c78f433`; MySQL: `e1a4089f-a744-4d43-88ab-ece737171198`.
- Successful app deployment: `83ac53db-7503-481f-b955-518425ed7368`.
- Domain: https://paisa-production.up.railway.app, port 8080.
- Production profile, MySQL variable references, Gemini key (explicitly authorized private transfer), Dockerfile builder/path, and `/api/status` health check with 180-second timeout configured through Railway UI.
- Railway now deprecates Config as Code for new services; the checked-in legacy `railway.toml` was not relied upon. Actual settings were applied directly.
- Remote read-only smoke checks passed: frontend HTML, JSON status, MySQL mode, CSRF token, Secure cookies, and unauthenticated workspace returning JSON 401. AI configured true; email configured false.
- The live browser renders the dashboard and Sign in action. No cloud registration, email, account persistence through authenticated requests, or real cloud Gemini generation is claimed verified yet. Local MySQL records were not migrated.
- Remaining blocker: Railway Trial disables Gmail SMTP. User must choose an HTTPS email sender integration or personally upgrade to a plan supporting SMTP. Verification stays enabled. No paid plan selected.

### HTTPS email enabled

The Brevo HTTPS adapter is deployed and privately configured. A sender-inbox connection-test send was accepted by Brevo with HTTP 201. Remote smoke checks now report email configured true; private inbox and authenticated end-to-end checks remain outstanding. See [HTTPS email](28-https-email.md) for current evidence.
