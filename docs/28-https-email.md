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

## Live configuration checkpoint

Brevo sender is verified. The supplied API key is stored in private local configuration and Railway service variables, with `MAIL_FROM` set to the verified sender. No credential is committed. The packager passed its secret scan after this configuration. Brevo accepted one connection-test email to the sender’s own inbox (HTTP 201); this is provider acceptance, not inbox-delivery confirmation. Railway deployment `4c53f69c-8b75-4049-98dd-822aebd3d677` succeeded, and remote smoke checks report AI configured true and email configured true. Real signup verification and password reset still need private browser/inbox testing. No phone-verification gate was returned by the tested HTTPS send.
