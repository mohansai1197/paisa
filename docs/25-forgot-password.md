# Forgot password

**Sign in → Forgot password? → Send reset code → enter the inbox code and a new
password twice → Reset password → Sign in with the new password.**

The existing Gmail sender delivers recovery emails. The flow is available in the
connected local app. The hosted frontend preview still needs backend hosting.

## What leads where

1. `POST /api/auth/forgot-password` accepts the email. It returns the same HTTP 202
   message for eligible, unknown, or pending signup addresses. Only an eligible
   existing account gets an email. Pending new signups must verify first.
2. The backend stores a BCrypt hash of a random six-digit reset code. Codes expire
   in 10 minutes, allow five wrong attempts, and are replaced by a resend. Sending
   has a 60-second cooldown and a five-per-address hourly limit. Signup, verification,
   and recovery share the bounded network-address send limiter.
3. `POST /api/auth/reset-password` validates the email, reset code, and new password.
   The frontend also requires matching password confirmation. A successful reset
   clears outstanding recovery and verification codes and updates the password hash.
4. Reset increments the user's persistent `auth_version`. Every authenticated API
   request compares that version with the version captured at login. Older sessions
   are rejected on their next request. Reset does not automatically sign the user in.
5. Login locks the user record while checking credentials and capturing the version,
   preventing a concurrent reset from giving an old password a fresh session version.

Requesting recovery alone does not change the password, lock an account, or sign
out existing sessions. Legacy access and email-verification policy are preserved.
Recovery codes are independent of signup verification codes and are never returned
in responses or application logs.

## Files

- `frontend/src/components.jsx`: Forgot password, reset-code, confirmation, resend,
  and return-to-sign-in states in `AuthModal`.
- `backend/src/main/java/com/paisa/service/PasswordResetService.java`: code creation,
  hashing, expiry, attempts, send limits, password changes, and version increments.
- `backend/src/main/java/com/paisa/service/VerificationMailSender.java`: distinct
  recovery email subject and text using the configured SMTP sender.
- `backend/src/main/java/com/paisa/config/SessionVersionFilter.java`: previous-session
  rejection at the API boundary.
- `backend/src/main/java/com/paisa/api/AuthController.java`: public CSRF-protected
  recovery endpoints and session-version capture at login.
- `backend/src/main/java/com/paisa/model/AppUser.java`: recovery fields in the existing
  users table; no extra application table is needed.
- `backend/src/test/java/com/paisa/PasswordResetIntegrationTest.java`: recovery,
  replay, expiry, guess limits, resend, failed delivery, legacy and session tests.
- `database/schema.sql`, `database/er-diagram.mmd`, and
  `postman/Paisa.postman_collection.json`: updated schema and API examples.

Paths are relative to `/Users/mohansai/Documents/ChatGPT/hackathon`.

## Checks and limits

Thirty-four backend and thirteen frontend tests pass. The production frontend build
passed. The backend suite verifies that the old password fails after reset, previous
sessions lose API access, replay fails, delivery failure leaves no usable challenge,
and pending signup verification cannot be bypassed through recovery.

A full transport test with a loopback fake SMTP inbox and disposable H2 database
verified delivery, code consumption, old-password rejection, session revocation,
new-password login, and a preserved ₹500 balance. No real email or existing user
password was changed by that check. Browser testing on the updated local app
confirmed the Forgot password entry point and the code/new-password/confirmation
screen using an unregistered test address, which does not send email. A separate
live MySQL login preserved the fictional review account and its 28 transactions.
Screenshots: `output/paisa-forgot-password.png` and `output/paisa-reset-password.png`.

The request body/status is generic, but synchronous SMTP delivery can create timing
differences. Production deployment should use queued delivery and a shared rate
limiter across backend instances. A full security audit remains outside this local
hackathon check.

Reference: [OWASP password recovery guidance](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html).
