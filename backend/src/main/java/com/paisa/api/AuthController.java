package com.paisa.api;

import com.paisa.config.SessionVersionFilter;
import com.paisa.model.AppUser;
import com.paisa.repository.UserRepository;
import com.paisa.service.EmailVerificationService;
import com.paisa.service.FinanceService;
import com.paisa.service.PasswordResetService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  final UserRepository users;
  final EmailVerificationService verification;
  final AuthenticationManager auth;
  final FinanceService finance;
  final PasswordResetService resets;

  public AuthController(
      UserRepository u,
      EmailVerificationService v,
      AuthenticationManager a,
      FinanceService f,
      PasswordResetService r) {
    users = u;
    verification = v;
    auth = a;
    finance = f;
    resets = r;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication a) {
    AppUser u = finance.user(a.getName());
    return Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail());
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> register(
      @Valid @RequestBody Requests.Register input,
      HttpServletRequest request,
      HttpServletResponse response) {
    verification.register(input, request.getRemoteAddr());
    return Map.of(
        "verificationRequired",
        true,
        "email",
        EmailVerificationService.normalize(input.email()),
        "message",
        "Check your inbox for a six-digit verification code. It expires in 10 minutes.");
  }

  @PostMapping("/resend-verification")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> resend(
      @Valid @RequestBody Requests.VerificationEmail input, HttpServletRequest request) {
    verification.resend(input.email(), request.getRemoteAddr());
    return Map.of(
        "verificationRequired",
        true,
        "message",
        "If this address has an unverified account, a code has been sent. Check your inbox and spam folder.");
  }

  @PostMapping("/verify")
  public Map<String, Object> verify(@Valid @RequestBody Requests.VerifyEmail input) {
    verification.verify(input);
    return Map.of(
        "emailVerified", true, "message", "Email verified. Sign in with your chosen password.");
  }

  @PostMapping("/login")
  @Transactional
  public Map<String, Object> login(
      @Valid @RequestBody Requests.Login input,
      HttpServletRequest request,
      HttpServletResponse response) {
    loginContext(input.email().trim().toLowerCase(), input.password(), request, response);
    return me(SecurityContextHolder.getContext().getAuthentication());
  }

  @PostMapping("/forgot-password")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> forgot(
      @Valid @RequestBody Requests.VerificationEmail input, HttpServletRequest request) {
    resets.requestCode(input.email(), request.getRemoteAddr());
    return Map.of(
        "message",
        "If an eligible account exists, a password reset code has been sent. Check your inbox and spam folder.");
  }

  @PostMapping("/reset-password")
  public Map<String, Object> reset(
      @Valid @RequestBody Requests.ResetPassword input, HttpServletRequest request) {
    resets.reset(input);
    HttpSession session = request.getSession(false);
    if (session != null) session.invalidate();
    SecurityContextHolder.clearContext();
    return Map.of(
        "message",
        "Password reset. Sign in with your new password. Previous sessions have been signed out.");
  }

  private void loginContext(
      String email, String password, HttpServletRequest request, HttpServletResponse response) {
    AppUser user =
        users
            .findForBalanceUpdate(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    Authentication authenticated =
        auth.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    request.getSession(true);
    request.changeSessionId();
    request.getSession().setAttribute(SessionVersionFilter.SESSION_VERSION, user.getAuthVersion());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authenticated);
    SecurityContextHolder.setContext(context);
    new HttpSessionSecurityContextRepository().saveContext(context, request, response);
  }
}
