package com.paisa;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.paisa.model.AppUser;
import com.paisa.repository.UserRepository;
import com.paisa.service.VerificationMailSender;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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
class FinanceIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserRepository users;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
  @MockitoBean VerificationMailSender mail;
  private final Map<String, String> sentCodes = new java.util.concurrent.ConcurrentHashMap<>();

  @BeforeEach
  void captureVerificationEmails() {
    doAnswer(
            call -> {
              sentCodes.put(call.getArgument(0), call.getArgument(1));
              return null;
            })
        .when(mail)
        .sendCode(anyString(), anyString());
  }

  private ResultActions send(MockHttpSession session, String method, String path, Object body)
      throws Exception {
    return mvc.perform(
        request(org.springframework.http.HttpMethod.valueOf(method), path)
            .session(session)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body == null ? "" : json.writeValueAsString(body)));
  }

  private MockHttpSession register(String name) throws Exception {
    String email = (name + UUID.randomUUID() + "@example.test").toLowerCase();
    MockHttpSession anonymous = new MockHttpSession();
    send(
            anonymous,
            "POST",
            "/api/auth/register",
            Map.of("name", name, "email", email, "password", "test-only-strong-password"))
        .andExpect(status().isAccepted());
    assertThat(sentCodes.get(email)).matches("[0-9]{6}");
    send(
            anonymous,
            "POST",
            "/api/auth/verify",
            Map.of(
                "email",
                email,
                "code",
                sentCodes.get(email),
                "password",
                "test-only-strong-password"))
        .andExpect(status().isOk());
    MvcResult result =
        send(
                anonymous,
                "POST",
                "/api/auth/login",
                Map.of("email", email, "password", "test-only-strong-password"))
            .andExpect(status().isOk())
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  private JsonNode body(ResultActions r) throws Exception {
    return json.readTree(r.andReturn().getResponse().getContentAsString());
  }

  @Test
  void fullPersistedFlowAndUserIsolation() throws Exception {
    MockHttpSession owner = register("Owner"), other = register("Other");
    JsonNode account =
        body(
            send(
                    owner,
                    "POST",
                    "/api/accounts",
                    Map.of("name", "Test cash", "type", "CASH", "openingBalance", 1000))
                .andExpect(status().isOk()));
    long aid = account.path("id").asLong();
    JsonNode workspace =
        body(mvc.perform(get("/api/workspace").session(owner)).andExpect(status().isOk()));
    long expenseCat = workspace.path("categories").get(0).path("id").asLong(),
        incomeCat = workspace.path("categories").get(7).path("id").asLong();
    Map<String, Object> expense =
        Map.of(
            "accountId",
            aid,
            "categoryId",
            expenseCat,
            "type",
            "EXPENSE",
            "description",
            "=SUM(1,2)",
            "amount",
            125.25,
            "date",
            "2026-11-10");
    long tid =
        body(send(owner, "POST", "/api/transactions", expense).andExpect(status().isOk()))
            .path("id")
            .asLong();
    send(
            owner,
            "POST",
            "/api/transactions",
            Map.of(
                "accountId",
                aid,
                "categoryId",
                incomeCat,
                "type",
                "INCOME",
                "description",
                "Income",
                "amount",
                500,
                "date",
                "2026-11-10"))
        .andExpect(status().isOk());
    send(
            owner,
            "POST",
            "/api/budgets",
            Map.of("categoryId", expenseCat, "month", "2026-11", "limitAmount", 300))
        .andExpect(status().isOk());
    JsonNode totals =
        body(
            mvc.perform(get("/api/analytics?month=2026-11").session(owner))
                .andExpect(status().isOk()));
    assertThat(totals.path("income").decimalValue()).isEqualByComparingTo("500");
    assertThat(totals.path("expenses").decimalValue()).isEqualByComparingTo("125.25");
    assertThat(totals.path("netCashFlow").decimalValue()).isEqualByComparingTo("374.75");
    JsonNode refreshed =
        body(mvc.perform(get("/api/workspace").session(owner)).andExpect(status().isOk()));
    assertThat(refreshed.path("accounts").get(0).path("balance").decimalValue())
        .isEqualByComparingTo("1374.75");
    assertThat(refreshed.path("transactions").size()).isEqualTo(2);
    String csv =
        mvc.perform(get("/api/reports?month=2026-11").session(owner))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(csv).contains("'=SUM(1,2)", "Net cash flow,374.75");
    send(other, "DELETE", "/api/transactions/" + tid, null).andExpect(status().isNotFound());
    send(other, "POST", "/api/transactions", expense).andExpect(status().isNotFound());
    send(owner, "DELETE", "/api/accounts/" + aid, null).andExpect(status().isConflict());
    send(owner, "POST", "/api/ai/categorize", Map.of("description", "Coffee"))
        .andExpect(status().isServiceUnavailable());
    send(owner, "DELETE", "/api/transactions/" + tid, null).andExpect(status().isNoContent());
    JsonNode after =
        body(
            mvc.perform(get("/api/analytics?month=2026-11").session(owner))
                .andExpect(status().isOk()));
    assertThat(after.path("expenses").decimalValue()).isEqualByComparingTo("0");
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(strings = {"CASH", "BANK", "WALLET"})
  void balancesCannotBeOverdrawnByAnyMutation(String type) throws Exception {
    MockHttpSession session = register("Balances");
    send(session, "POST", "/api/accounts", Map.of("name", type, "type", type, "openingBalance", -1))
        .andExpect(status().isBadRequest());
    long account =
        body(send(
                    session,
                    "POST",
                    "/api/accounts",
                    Map.of("name", type, "type", type, "openingBalance", 100))
                .andExpect(status().isOk()))
            .path("id")
            .asLong();
    long empty =
        body(send(
                    session,
                    "POST",
                    "/api/accounts",
                    Map.of("name", "Empty", "type", type, "openingBalance", 0))
                .andExpect(status().isOk()))
            .path("id")
            .asLong();
    JsonNode workspace = body(mvc.perform(get("/api/workspace").session(session)));
    long expenseCategory = workspace.path("categories").get(0).path("id").asLong();
    long incomeCategory = workspace.path("categories").get(7).path("id").asLong();
    Map<String, Object> expense =
        new HashMap<>(
            Map.of(
                "accountId",
                account,
                "categoryId",
                expenseCategory,
                "type",
                "EXPENSE",
                "description",
                "Balance test",
                "amount",
                100,
                "date",
                "2026-11-21"));
    long expenseId =
        body(send(session, "POST", "/api/transactions", expense).andExpect(status().isOk()))
            .path("id")
            .asLong();
    expense.put("amount", 0.01);
    send(session, "POST", "/api/transactions", expense).andExpect(status().isConflict());
    expense.put("amount", 100.01);
    send(session, "PUT", "/api/transactions/" + expenseId, expense)
        .andExpect(status().isConflict());
    expense.put("amount", 100);
    expense.put("accountId", empty);
    send(session, "PUT", "/api/transactions/" + expenseId, expense)
        .andExpect(status().isConflict());
    send(
            session,
            "PUT",
            "/api/accounts/" + account,
            Map.of("name", type, "type", type, "openingBalance", 99.99))
        .andExpect(status().isConflict());
    Map<String, Object> income =
        new HashMap<>(
            Map.of(
                "accountId",
                account,
                "categoryId",
                incomeCategory,
                "type",
                "INCOME",
                "description",
                "Funding",
                "amount",
                50,
                "date",
                "2026-11-21"));
    long incomeId =
        body(send(session, "POST", "/api/transactions", income).andExpect(status().isOk()))
            .path("id")
            .asLong();
    expense.put("accountId", account);
    expense.put("amount", 25);
    long secondExpense =
        body(send(session, "POST", "/api/transactions", expense).andExpect(status().isOk()))
            .path("id")
            .asLong();
    send(session, "DELETE", "/api/transactions/" + incomeId, null).andExpect(status().isConflict());
    income.put("amount", 20);
    send(session, "PUT", "/api/transactions/" + incomeId, income).andExpect(status().isConflict());
    income.put("amount", 50);
    income.put("accountId", empty);
    send(session, "PUT", "/api/transactions/" + incomeId, income).andExpect(status().isConflict());
    JsonNode unchanged = body(mvc.perform(get("/api/workspace").session(session)));
    assertThat(unchanged.path("accounts").get(0).path("balance").decimalValue())
        .isEqualByComparingTo("25");
    assertThat(unchanged.path("accounts").get(1).path("balance").decimalValue())
        .isEqualByComparingTo("0");
    assertThat(unchanged.path("transactions").size()).isEqualTo(3);
    send(session, "DELETE", "/api/transactions/" + secondExpense, null)
        .andExpect(status().isNoContent());
    send(session, "DELETE", "/api/transactions/" + incomeId, null)
        .andExpect(status().isNoContent());
    send(session, "DELETE", "/api/transactions/" + expenseId, null)
        .andExpect(status().isNoContent());
  }

  @Test
  void simultaneousExpensesCannotSpendTheSameFundsTwice() throws Exception {
    MockHttpSession session = register("Concurrent");
    long account =
        body(send(
                session,
                "POST",
                "/api/accounts",
                Map.of("name", "Shared funds", "type", "CASH", "openingBalance", 100)))
            .path("id")
            .asLong();
    long category =
        body(mvc.perform(get("/api/workspace").session(session)))
            .path("categories")
            .get(0)
            .path("id")
            .asLong();
    Map<String, Object> expense =
        Map.of(
            "accountId",
            account,
            "categoryId",
            category,
            "type",
            "EXPENSE",
            "description",
            "Concurrent spend",
            "amount",
            80,
            "date",
            "2026-11-21");
    java.util.concurrent.ExecutorService pool =
        java.util.concurrent.Executors.newFixedThreadPool(2);
    java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
    try {
      java.util.concurrent.Callable<Integer> attempt =
          () -> {
            start.await();
            return send(session, "POST", "/api/transactions", expense)
                .andReturn()
                .getResponse()
                .getStatus();
          };
      var one = pool.submit(attempt);
      var two = pool.submit(attempt);
      start.countDown();
      assertThat(
              List.of(
                  one.get(10, java.util.concurrent.TimeUnit.SECONDS),
                  two.get(10, java.util.concurrent.TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(200, 409);
      JsonNode workspace = body(mvc.perform(get("/api/workspace").session(session)));
      assertThat(workspace.path("accounts").get(0).path("balance").decimalValue())
          .isEqualByComparingTo("20");
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void demoRecordsPreserveExistingAccountAndPreventDuplicateLoads() throws Exception {
    MockHttpSession session = register("ExistingDemo");
    JsonNode original = body(send(session, "POST", "/api/accounts",
        Map.of("name", "My own account", "type", "BANK", "openingBalance", 321))
        .andExpect(status().isOk()));
    JsonNode before = body(mvc.perform(get("/api/workspace").session(session)));
    long category = before.path("categories").get(0).path("id").asLong();
    send(session, "POST", "/api/budgets", Map.of("categoryId", category,
        "month", java.time.YearMonth.now().toString(), "limitAmount", 9876))
        .andExpect(status().isOk());
    JsonNode after = body(send(session, "POST", "/api/demo/seed", null)
        .andExpect(status().isOk()));
    assertThat(after.path("accounts").size()).isEqualTo(3);
    assertThat(after.path("transactions").size()).isEqualTo(28);
    assertThat(after.path("budgets").size()).isEqualTo(5);
    assertThat(after.path("accounts").get(0).path("id")).isEqualTo(original.path("id"));
    assertThat(after.path("accounts").get(0).path("balance").decimalValue())
        .isEqualByComparingTo("321");
    assertThat(after.path("budgets").get(0).path("limitAmount").decimalValue())
        .isEqualByComparingTo("9876");
    send(session, "POST", "/api/demo/seed", null).andExpect(status().isConflict());
    JsonNode repeated = body(mvc.perform(get("/api/workspace").session(session)));
    assertThat(repeated.path("transactions").size()).isEqualTo(28);
  }

  @Test
  void sampleAccountsAreFundedForTheirSampleExpenses() throws Exception {
    MockHttpSession session = register("SeedBalances");
    JsonNode workspace =
        body(send(session, "POST", "/api/demo/seed", null).andExpect(status().isOk()));
    for (JsonNode account : workspace.path("accounts")) {
      assertThat(account.path("balance").decimalValue())
          .isGreaterThanOrEqualTo(java.math.BigDecimal.ZERO);
    }
  }

  @Test
  void pendingSignupCannotAccessWorkspaceUntilOneTimeVerification() throws Exception {
    String email = "pending-" + UUID.randomUUID() + "@example.test";
    MockHttpSession session = new MockHttpSession();
    String response =
        send(
                session,
                "POST",
                "/api/auth/register",
                Map.of("name", "Pending", "email", email, "password", "original-pending-password"))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String code = sentCodes.get(email);
    assertThat(response).doesNotContain(code);
    AppUser pending = users.findByEmail(email).orElseThrow();
    assertThat(pending.isEmailVerified()).isFalse();
    assertThat(pending.isVerificationRequired()).isTrue();
    assertThat(pending.getVerificationHash()).isNotEqualTo(code);
    mvc.perform(get("/api/workspace").session(session)).andExpect(status().isUnauthorized());
    send(
            session,
            "POST",
            "/api/auth/login",
            Map.of("email", email, "password", "original-pending-password"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.verificationRequired").value(true));
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", code, "password", "email-owner-chosen-password"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/workspace").session(session)).andExpect(status().isUnauthorized());
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", code, "password", "email-owner-chosen-password"))
        .andExpect(status().isBadRequest());
    AppUser verified = users.findByEmail(email).orElseThrow();
    assertThat(verified.isEmailVerified()).isTrue();
    assertThat(verified.getVerificationHash()).isNull();
    send(
            session,
            "POST",
            "/api/auth/login",
            Map.of("email", email, "password", "original-pending-password"))
        .andExpect(status().isUnauthorized());
    send(
            session,
            "POST",
            "/api/auth/login",
            Map.of("email", email, "password", "email-owner-chosen-password"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/workspace").session(session)).andExpect(status().isOk());
  }

  @Test
  void expiryGuessLimitsResendCooldownAndRotationAreEnforced() throws Exception {
    String email = "expiry-" + UUID.randomUUID() + "@example.test";
    MockHttpSession session = new MockHttpSession();
    send(
            session,
            "POST",
            "/api/auth/register",
            Map.of("name", "Limits", "email", email, "password", "test-password-123"))
        .andExpect(status().isAccepted());
    String original = sentCodes.get(email);
    send(session, "POST", "/api/auth/resend-verification", Map.of("email", email))
        .andExpect(status().isTooManyRequests());
    AppUser pending = users.findByEmail(email).orElseThrow();
    pending.setVerificationExpiresAt(java.time.Instant.now().minusSeconds(1));
    users.save(pending);
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", original, "password", "test-password-123"))
        .andExpect(status().isBadRequest());
    pending = users.findByEmail(email).orElseThrow();
    pending.setVerificationSentAt(java.time.Instant.now().minusSeconds(61));
    users.save(pending);
    send(session, "POST", "/api/auth/resend-verification", Map.of("email", email))
        .andExpect(status().isAccepted());
    String current = sentCodes.get(email);
    assertThat(current).isNotEqualTo(original);
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", original, "password", "test-password-123"))
        .andExpect(status().isBadRequest());
    String wrong = current.equals("000000") ? "111111" : "000000";
    for (int i = 0; i < 4; i++)
      send(
              session,
              "POST",
              "/api/auth/verify",
              Map.of("email", email, "code", wrong, "password", "test-password-123"))
          .andExpect(status().isBadRequest());
    assertThat(users.findByEmail(email).orElseThrow().getVerificationAttempts()).isEqualTo(5);
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", current, "password", "test-password-123"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void missingOrFailedDeliveryNeverCreatesAnActiveAccount() throws Exception {
    String email = "no-mail-" + UUID.randomUUID() + "@example.test";
    MockHttpSession session = new MockHttpSession();
    doThrow(
            new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Email is not configured."))
        .when(mail)
        .requireConfigured();
    send(
            session,
            "POST",
            "/api/auth/register",
            Map.of("name", "No mail", "email", email, "password", "test-password-123"))
        .andExpect(status().isServiceUnavailable());
    assertThat(users.findByEmail(email)).isEmpty();
    doNothing().when(mail).requireConfigured();
    doThrow(
            new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Delivery failed."))
        .when(mail)
        .sendCode(anyString(), anyString());
    send(
            session,
            "POST",
            "/api/auth/register",
            Map.of("name", "No mail", "email", email, "password", "test-password-123"))
        .andExpect(status().isServiceUnavailable());
    assertThat(users.findByEmail(email)).isEmpty();
  }

  @Test
  void legacyUnverifiedUsersCanVerifyWithoutLosingTheirExistingData() throws Exception {
    String email = "legacy-" + UUID.randomUUID() + "@example.test";
    AppUser legacy = new AppUser();
    legacy.setName("Legacy");
    legacy.setEmail(email);
    legacy.setPasswordHash(passwords.encode("old-password-123"));
    users.save(legacy);
    MockHttpSession session = new MockHttpSession();
    send(session, "POST", "/api/auth/login", Map.of("email", email, "password", "old-password-123"))
        .andExpect(status().isOk());
    send(session, "POST", "/api/auth/resend-verification", Map.of("email", email))
        .andExpect(status().isAccepted());
    send(
            session,
            "POST",
            "/api/auth/verify",
            Map.of("email", email, "code", sentCodes.get(email), "password", "owner-password-123"))
        .andExpect(status().isOk());
    MvcResult signedIn =
        send(
                session,
                "POST",
                "/api/auth/login",
                Map.of("email", email, "password", "owner-password-123"))
            .andExpect(status().isOk())
            .andReturn();
    session = (MockHttpSession) signedIn.getRequest().getSession(false);
    assertThat(users.findByEmail(email).orElseThrow().getId()).isEqualTo(legacy.getId());
    JsonNode workspace =
        body(mvc.perform(get("/api/workspace").session(session)).andExpect(status().isOk()));
    assertThat(workspace.path("categories").size()).isEqualTo(9);
    send(session, "POST", "/api/auth/resend-verification", Map.of("email", "unknown@example.test"))
        .andExpect(status().isAccepted());
    verify(mail, never()).sendCode(eq("unknown@example.test"), anyString());
  }

  @Test
  void csrfAuthenticationAndInputValidation() throws Exception {
    mvc.perform(get("/api/workspace")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
    MockHttpSession session = register("Validation");
    send(session, "POST", "/api/transactions", Map.of("amount", -1))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/analytics?month=2026-99").session(session))
        .andExpect(status().isBadRequest());
    send(session, "POST", "/api/auth/logout", null).andExpect(status().isNoContent());
  }
}
