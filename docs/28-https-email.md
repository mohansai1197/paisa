# Brevo HTTPS email delivery

Paisa supports Brevo's HTTPS transactional email endpoint for hosts that block SMTP. The same sender handles signup verification and password reset. Existing one-time codes, expiry, attempt limits, and session invalidation remain unchanged.

## Setup

1. Create the free Brevo account, complete onboarding, and verify a sender email. Account and sender approval are controlled by Brevo; delivery is not guaranteed until a real inbox test succeeds.
2. Create an **API key** under SMTP & API → API keys. Do not use an SMTP key.
3. Set `BREVO_API_KEY` and `MAIL_FROM` privately in the Railway `paisa` service. `MAIL_FROM` must match a verified sender. Keep these out of Git and chat. A local private entry form is available at http://127.0.0.1:8767 while its setup server is running; that form saves locally only.
4. Redeploy, then test a real registration email, verification, sign-in, and password reset privately.

When both Brevo variables are present, Brevo takes precedence over SMTP. Without them, existing SMTP behavior remains. Provider failures return a safe 503 and never expose responses, keys, recipients, or codes. Requests use a five-second connect timeout, fifteen-second total timeout, no redirect following, and no automatic retries.

Files: `backend/src/main/java/com/paisa/service/BrevoMailClient.java`, `VerificationMailSender.java`, `backend/src/main/resources/application.properties`, `backend/src/test/java/com/paisa/service/BrevoMailClientTest.java`, `scripts/local_setup.py`, and `.env.example`.

Verification: release build and 38 backend tests pass, including local HTTP transport tests using fictional data and a dummy API key. Real Brevo sender approval, delivery, and cloud end-to-end verification remain pending credentials. The source packager also scans against a configured Brevo secret before publishing.

Provider API reference: https://developers.brevo.com/reference/send-transac-email.
