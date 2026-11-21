package com.paisa;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paisa.model.AppUser;
import com.paisa.repository.UserRepository;
import com.paisa.service.VerificationMailSender;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:paisa-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "paisa.gemini-key=",
      "paisa.verification.max-sends-per-hour=1000"
    })
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder passwords;
  @MockitoBean VerificationMailSender mail;
  final Map<String, String> codes = new HashMap<>();

  @BeforeEach
  void captureMail() {
    doAnswer(
            call -> {
              codes.put(call.getArgument(0), call.getArgument(1));
              return null;
            })
        .when(mail)
        .sendResetCode(anyString(), anyString());
  }

  AppUser account(boolean verified, boolean required) {
    AppUser user = new AppUser();
    user.setName("Disposable reset test");
    user.setEmail("reset-" + UUID.randomUUID() + "@example.test");
    user.setPasswordHash(passwords.encode("original-password-123"));
    user.setEmailVerified(verified);
    user.setVerificationRequired(required);
    return users.save(user);
  }

  ResultActions postApi(MockHttpSession session, String path, Object body) throws Exception {
    return mvc.perform(
        post("/api/auth/" + path)
            .session(session)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(body)));
  }

  MockHttpSession login(String email, String password) throws Exception {
    var result =
        postApi(new MockHttpSession(), "login", Map.of("email", email, "password", password))
            .andExpect(status().isOk())
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  Map<String, String> resetInput(String email, String code) {
    return Map.of("email", email, "code", code, "password", "new-password-456");
  }

  @Test
  void resetChangesPasswordConsumesCodeAndRevokesAllOldSessions() throws Exception {
    AppUser user = account(true, true);
    var one = login(user.getEmail(), "original-password-123");
    var two = login(user.getEmail(), "original-password-123");
    var anonymous = new MockHttpSession();
    String response =
        postApi(anonymous, "forgot-password", Map.of("email", user.getEmail()))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String code = codes.get(user.getEmail());
    assertThat(code).matches("[0-9]{6}");
    assertThat(response).doesNotContain(code);
    assertThat(users.findById(user.getId()).orElseThrow().getPasswordResetHash())
        .isNotEqualTo(code);
    // Requesting recovery does not change the password or revoke valid sessions.
    mvc.perform(get("/api/workspace").session(one)).andExpect(status().isOk());
    postApi(anonymous, "reset-password", resetInput(user.getEmail(), code))
        .andExpect(status().isOk());
    AppUser updated = users.findById(user.getId()).orElseThrow();
    assertThat(updated.getPasswordResetHash()).isNull();
    assertThat(updated.getAuthVersion()).isEqualTo(user.getAuthVersion() + 1);
    postApi(new MockHttpSession(), "reset-password", resetInput(user.getEmail(), code))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/workspace").session(one)).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/workspace").session(two)).andExpect(status().isUnauthorized());
    postApi(
            new MockHttpSession(),
            "login",
            Map.of("email", user.getEmail(), "password", "original-password-123"))
        .andExpect(status().isUnauthorized());
    var fresh = login(user.getEmail(), "new-password-456");
    mvc.perform(get("/api/workspace").session(fresh)).andExpect(status().isOk());
  }

  @Test
  void unknownPendingAndKnownAddressesReceiveTheSameRequestResponse() throws Exception {
    AppUser known = account(true, true), pending = account(false, true);
    String expected = null;
    for (String email :
        List.of(known.getEmail(), pending.getEmail(), "unknown-reset@example.test")) {
      String response =
          postApi(new MockHttpSession(), "forgot-password", Map.of("email", email))
              .andExpect(status().isAccepted())
              .andReturn()
              .getResponse()
              .getContentAsString();
      if (expected == null) expected = response;
      else assertThat(response).isEqualTo(expected);
    }
    verify(mail, never()).sendResetCode(eq(pending.getEmail()), anyString());
    verify(mail, never()).sendResetCode(eq("unknown-reset@example.test"), anyString());
    postApi(new MockHttpSession(), "reset-password", resetInput(pending.getEmail(), "123456"))
        .andExpect(status().isBadRequest());
    assertThat(users.findById(pending.getId()).orElseThrow().isEmailVerified()).isFalse();
  }

  @Test
  void expiredCodesAndFiveIncorrectGuessesCannotResetPassword() throws Exception {
    AppUser user = account(true, true);
    var session = new MockHttpSession();
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    String code = codes.get(user.getEmail());
    user = users.findById(user.getId()).orElseThrow();
    user.setPasswordResetExpiresAt(Instant.now().minusSeconds(1));
    users.save(user);
    postApi(session, "reset-password", resetInput(user.getEmail(), code))
        .andExpect(status().isBadRequest());
    user = users.findById(user.getId()).orElseThrow();
    user.setPasswordResetExpiresAt(Instant.now().plusSeconds(600));
    users.save(user);
    String wrong = code.equals("000000") ? "111111" : "000000";
    for (int n = 0; n < 5; n++)
      postApi(session, "reset-password", resetInput(user.getEmail(), wrong))
          .andExpect(status().isBadRequest());
    assertThat(users.findById(user.getId()).orElseThrow().getPasswordResetAttempts()).isEqualTo(5);
    postApi(session, "reset-password", resetInput(user.getEmail(), code))
        .andExpect(status().isBadRequest());
    assertThat(
            passwords.matches(
                "original-password-123",
                users.findById(user.getId()).orElseThrow().getPasswordHash()))
        .isTrue();
  }

  @Test
  void resendingEnforcesCooldownHourlyLimitAndInvalidatesTheOldCode() throws Exception {
    AppUser user = account(true, true);
    var session = new MockHttpSession();
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    String first = codes.get(user.getEmail());
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    verify(mail, times(1)).sendResetCode(eq(user.getEmail()), anyString());
    user = users.findById(user.getId()).orElseThrow();
    user.setPasswordResetSentAt(Instant.now().minusSeconds(61));
    users.save(user);
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    String second = codes.get(user.getEmail());
    assertThat(second).isNotEqualTo(first);
    postApi(session, "reset-password", resetInput(user.getEmail(), first))
        .andExpect(status().isBadRequest());
    user = users.findById(user.getId()).orElseThrow();
    user.setPasswordResetSentAt(Instant.now().minusSeconds(61));
    user.setPasswordResetSends(5);
    users.save(user);
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    verify(mail, times(2)).sendResetCode(eq(user.getEmail()), anyString());
    postApi(session, "reset-password", resetInput(user.getEmail(), second))
        .andExpect(status().isOk());
  }

  @Test
  void deliveryFailureRollsBackChallengeAndMissingConfigurationDoesNotSend() throws Exception {
    AppUser user = account(true, true);
    var session = new MockHttpSession();
    doThrow(
            new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Delivery unavailable"))
        .when(mail)
        .sendResetCode(anyString(), anyString());
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isServiceUnavailable());
    assertThat(users.findById(user.getId()).orElseThrow().getPasswordResetHash()).isNull();
    doThrow(
            new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Email unavailable"))
        .when(mail)
        .requireConfigured();
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void verificationCodesCannotBeUsedForRecoveryAndResetPreservesLegacyAccessPolicy()
      throws Exception {
    AppUser user = account(false, false);
    user.setVerificationHash(passwords.encode("654321"));
    users.save(user);
    var session = new MockHttpSession();
    postApi(session, "forgot-password", Map.of("email", user.getEmail()))
        .andExpect(status().isAccepted());
    String reset = codes.get(user.getEmail());
    assertThat(reset).isNotEqualTo("654321");
    postApi(session, "reset-password", resetInput(user.getEmail(), "654321"))
        .andExpect(status().isBadRequest());
    postApi(session, "reset-password", resetInput(user.getEmail(), reset))
        .andExpect(status().isOk());
    AppUser updated = users.findById(user.getId()).orElseThrow();
    assertThat(updated.isVerificationRequired()).isFalse();
    assertThat(updated.isEmailVerified()).isFalse();
    assertThat(updated.getVerificationHash()).isNull();
    login(user.getEmail(), "new-password-456");
  }
}
