package com.paisa.service;

import com.paisa.api.Requests;
import com.paisa.model.AppUser;
import com.paisa.repository.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailVerificationService {
  private final UserRepository users;
  private final CategoryRepository categories;
  private final FinanceService finance;
  private final PasswordEncoder passwords;
  private final VerificationMailSender mail;
  private final VerificationRateLimiter limiter;
  private final SecureRandom random = new SecureRandom();

  public EmailVerificationService(
      UserRepository users,
      CategoryRepository categories,
      FinanceService finance,
      PasswordEncoder passwords,
      VerificationMailSender mail,
      VerificationRateLimiter limiter) {
    this.users = users;
    this.categories = categories;
    this.finance = finance;
    this.passwords = passwords;
    this.mail = mail;
    this.limiter = limiter;
  }

  public static String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  @Transactional
  public void register(Requests.Register input, String address) {
    mail.requireConfigured();
    limiter.consume(address);
    String email = normalize(input.email());
    if (users.existsByEmail(email))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "That email is already registered. Sign in, or choose Verify email to request a code.");
    AppUser user = new AppUser();
    user.setName(input.name().trim());
    user.setEmail(email);
    user.setVerificationRequired(true);
    user.setPasswordHash(passwords.encode(input.password()));
    users.saveAndFlush(user);
    issue(user);
  }

  @Transactional
  public void resend(String email, String address) {
    mail.requireConfigured();
    limiter.consume(address);
    users
        .findForBalanceUpdate(normalize(email))
        .filter(user -> !user.isEmailVerified())
        .ifPresent(this::issue);
    // Same successful response for an unknown, pending, or already verified email.
  }

  private void issue(AppUser user) {
    Instant now = Instant.now();
    if (user.getVerificationSentAt() != null
        && user.getVerificationSentAt().plusSeconds(60).isAfter(now))
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Wait 60 seconds before requesting another code.");
    if (user.getVerificationWindowAt() == null
        || !user.getVerificationWindowAt().plusSeconds(3600).isAfter(now)) {
      user.setVerificationWindowAt(now);
      user.setVerificationSends(0);
    }
    if (user.getVerificationSends() >= 5)
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Too many verification emails. Please try again in an hour.");
    String code = String.format(Locale.ROOT, "%06d", random.nextInt(1000000));
    while (user.getVerificationHash() != null
        && passwords.matches(code, user.getVerificationHash()))
      code = String.format(Locale.ROOT, "%06d", random.nextInt(1000000));
    user.setVerificationHash(passwords.encode(code));
    user.setVerificationExpiresAt(now.plusSeconds(600));
    user.setVerificationSentAt(now);
    user.setVerificationAttempts(0);
    user.setVerificationSends(user.getVerificationSends() + 1);
    mail.sendCode(user.getEmail(), code);
  }

  @Transactional(noRollbackFor = ResponseStatusException.class)
  public void verify(Requests.VerifyEmail input) {
    AppUser user =
        users.findForBalanceUpdate(normalize(input.email())).orElseThrow(this::invalidCode);
    Instant now = Instant.now();
    if (user.isEmailVerified()
        || user.getVerificationHash() == null
        || user.getVerificationExpiresAt() == null
        || !user.getVerificationExpiresAt().isAfter(now)
        || user.getVerificationAttempts() >= 5) throw invalidCode();
    user.setVerificationAttempts(user.getVerificationAttempts() + 1);
    if (!passwords.matches(input.code(), user.getVerificationHash())) throw invalidCode();
    // The email owner chooses the password when proving ownership; an earlier pending
    // signup cannot retain an attacker's password after the legitimate owner verifies.
    user.setPasswordHash(passwords.encode(input.password()));
    user.setEmailVerified(true);
    user.setAuthVersion(user.getAuthVersion() + 1);
    user.setVerificationHash(null);
    user.setVerificationExpiresAt(null);
    user.setVerificationAttempts(0);
    if (categories.findByOwnerIdOrderByIdAsc(user.getId()).isEmpty())
      finance.defaultCategories(user);
  }

  private ResponseStatusException invalidCode() {
    return new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "The code is invalid, expired, or has too many attempts. Request a new code and try again.");
  }
}
