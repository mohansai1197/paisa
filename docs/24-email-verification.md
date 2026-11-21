# Email verification

New signups must prove ownership of their email before they can sign in or use a
workspace. Email format validation alone does not establish ownership.

## What leads where

1. **Create an account → Send verification code:** the backend checks delivery
   configuration, creates a pending user, hashes the password and six-digit code,
   and sends the code through Gmail SMTP. It returns HTTP 202, without a login session.
2. **Verify email:** enter the inbox code and choose the account password. The code
   expires after 10 minutes, allows five guesses, and is invalidated after use.
   The email owner chooses the final password so a prior pending registration cannot
   retain someone else's password. Default categories are created if needed.
3. **Sign in:** only a verified new signup can authenticate and load financial data.
4. **Send another code:** the old code is replaced. There is a 60-second cooldown,
   a database limit of five sends per address per hour, and an in-process limit of
   ten verification requests per network address per hour.

Delivery configuration or sending failures do not create an active account. Codes
are never returned in API responses, shown in the app, logged, or saved in plaintext
by the application. They are sent only in the requested verification email.

## Existing accounts

This change applies to **new signups**. Existing accounts keep their previous
access, and their data is preserved. They are not automatically labeled verified.
The `verification_required` flag defaults to false for legacy rows and is explicitly
set to true by the new registration service. `email_verified` remains false until
actual email proof. Existing owners can choose **Verify an existing email**.

An earlier attempt to require verification retroactively was stopped by approval
review because it could lock existing users out before mail was configured. The
implemented version preserves their access and was approved and verified separately.

## Files

- `backend/src/main/java/com/paisa/service/EmailVerificationService.java`: pending
  registration, code hashing, expiry, attempts, resend, and password finalization.
- `backend/src/main/java/com/paisa/service/VerificationMailSender.java`: SMTP mail
  delivery, configuration checks, and safe delivery errors.
- `backend/src/main/java/com/paisa/service/VerificationRateLimiter.java`: bounded
  in-process network-address rate limit. A multi-instance deployment needs a shared limiter.
- `backend/src/main/java/com/paisa/model/AppUser.java`: verification state stored in
  the existing users table; there are still five application tables.
- `backend/src/main/java/com/paisa/api/AuthController.java`: register, verify,
  resend-verification, login, and session endpoints.
- `backend/src/main/java/com/paisa/config/SecurityConfig.java`: blocks pending new users.
- `frontend/src/components.jsx`: registration, code entry, resend, and sign-in screens.
- `frontend/src/App.jsx`: pending registration does not load a workspace or claim a login.
- `scripts/local_setup.py`: private sender entry. It accepts embedded-browser opaque
  or missing Origin only with a valid unpredictable form nonce and exact loopback Host;
  foreign explicit origins, invalid tokens, and wrong hosts are rejected.
- `.env.example`, `database/schema.sql`, `database/er-diagram.mmd`, and the Postman
  collection document the required configuration, columns, and endpoints.

All file paths are relative to `/Users/mohansai/Documents/ChatGPT/hackathon`.

## Sender setup and current verification

The user saved a Gmail address and app password through the private loopback form.
They remain in the ignored owner-readable `.env`. No credential was copied into
the React source, Sites upload, source ZIP, screenshots, or this document.
An encrypted SMTP connection authenticated to Gmail successfully without sending
an email. The running local app reports `emailVerificationConfigured: true`,
`aiConfigured: true`, and `storage: MySQL`.

Twenty-seven backend and thirteen frontend tests passed. Coverage includes pending
access, expired and reused codes, persisted guess limits, cooldown and rotation,
failed delivery rollback, final password ownership, preserved legacy access, SMTP
message construction, safe provider errors, finance rules, and AI integration logic.

A separate full transport test used a loopback-only fake SMTP inbox and disposable
H2 database: register → SMTP delivery → pending access denied → verification →
replay denied → password login → workspace all passed. No real email was sent by
that test. The live MySQL check preserved the existing fictional account and 28
transactions while blocking signup before sender configuration.

After the user tested the flow, a read-only MySQL aggregate returned one verified
email account and zero pending new accounts. This confirms that an actual local
email verification was accepted. No email address or verification code was read
for this check. The SMTP authentication check alone was not treated as proof of
inbox delivery.

The hosted Sites preview remains frontend-only; its deployment does not host Java,
MySQL, Gemini, or SMTP. The connected flow runs in the local app until backend
hosting is completed.

## Official references

- [Spring Boot mail configuration](https://docs.spring.io/spring-boot/reference/io/email.html)
- [Google app passwords](https://support.google.com/accounts/answer/185833?hl=en)
- [Gmail SMTP transport](https://developers.google.com/workspace/gmail/imap/imap-smtp?hl=en)
- [OWASP email verification guidance](https://cheatsheetseries.owasp.org/cheatsheets/Email_Validation_and_Verification_Cheat_Sheet.html)

The owner-private frontend preview update was published successfully from source commit `9967bc41356d7ef6196fa75e88b4336ba2352751`. Its audience is unchanged.
