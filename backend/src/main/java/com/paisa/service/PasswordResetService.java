package com.paisa.service;

import com.paisa.api.Requests;
import com.paisa.model.AppUser;
import com.paisa.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final VerificationMailSender mail;
  private final VerificationRateLimiter limiter;
  private final SecureRandom random = new SecureRandom();

  public PasswordResetService(
      UserRepository users,
      PasswordEncoder passwords,
      VerificationMailSender mail,
      VerificationRateLimiter limiter) {
    this.users = users;
    this.passwords = passwords;
    this.mail = mail;
    this.limiter = limiter;
  }

  @Transactional
  public void requestCode(String email, String address) {
    mail.requireConfigured();
    limiter.consume(address);
    users
        .findForBalanceUpdate(EmailVerificationService.normalize(email))
        .filter(AppUser::canAccessWorkspace)
        .ifPresent(this::issue);
  }

  private void issue(AppUser user) {
    Instant now = Instant.now();
    // Suppress repeat sends without revealing whether the address exists.
    if (user.getPasswordResetSentAt() != null
        && user.getPasswordResetSentAt().plusSeconds(60).isAfter(now)) return;
    if (user.getPasswordResetWindowAt() == null
        || !user.getPasswordResetWindowAt().plusSeconds(3600).isAfter(now)) {
      user.setPasswordResetWindowAt(now);
      user.setPasswordResetSends(0);
    }
    if (user.getPasswordResetSends() >= 5) return;
    String code;
    do {
      code = String.format(Locale.ROOT, "%06d", random.nextInt(1000000));
    } while ((user.getPasswordResetHash() != null
            && passwords.matches(code, user.getPasswordResetHash()))
        || (user.getVerificationHash() != null
            && passwords.matches(code, user.getVerificationHash())));
    user.setPasswordResetHash(passwords.encode(code));
    user.setPasswordResetExpiresAt(now.plusSeconds(600));
    user.setPasswordResetSentAt(now);
    user.setPasswordResetAttempts(0);
    user.setPasswordResetSends(user.getPasswordResetSends() + 1);
    mail.sendResetCode(user.getEmail(), code);
  }

  @Transactional(noRollbackFor = ResponseStatusException.class)
  public void reset(Requests.ResetPassword input) {
    AppUser user =
        users
            .findForBalanceUpdate(EmailVerificationService.normalize(input.email()))
            .filter(AppUser::canAccessWorkspace)
            .orElseThrow(this::invalid);
    if (user.getPasswordResetHash() == null
        || user.getPasswordResetExpiresAt() == null
        || !user.getPasswordResetExpiresAt().isAfter(Instant.now())
        || user.getPasswordResetAttempts() >= 5) throw invalid();
    user.setPasswordResetAttempts(user.getPasswordResetAttempts() + 1);
    if (!passwords.matches(input.code(), user.getPasswordResetHash())) throw invalid();
    user.setPasswordHash(passwords.encode(input.password()));
    user.setAuthVersion(user.getAuthVersion() + 1);
    user.setPasswordResetHash(null);
    user.setPasswordResetExpiresAt(null);
    user.setPasswordResetAttempts(0);
    user.setVerificationHash(null);
    user.setVerificationExpiresAt(null);
  }

  private ResponseStatusException invalid() {
    return new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "The reset code is invalid, expired, or has too many attempts. Request another code and try again.");
  }
}
