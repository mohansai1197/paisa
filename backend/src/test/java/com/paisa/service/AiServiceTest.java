package com.paisa.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AiServiceTest {
  final ObjectMapper json = new ObjectMapper();
  FinanceService finance;
  GeminiClient gemini;
  AiService service;
  final String email = "synthetic@example.test";

  @BeforeEach
  void prepare() {
    finance = mock(FinanceService.class);
    gemini = mock(GeminiClient.class);
    service = new AiService(finance, gemini);
    when(finance.snapshot(email))
        .thenReturn(
            Map.of(
                "categories",
                List.of(
                    Map.of("id", 7L, "type", "EXPENSE", "name", "Food"),
                    Map.of("id", 8L, "type", "INCOME", "name", "Salary"))));
    when(finance.analytics(email, "2026-11"))
        .thenReturn(
            Map.of(
                "transactionCount",
                3,
                "categories",
                List.of(
                    Map.of(
                        "categoryId",
                        7L,
                        "name",
                        "Food",
                        "suggestedLimit",
                        new BigDecimal("900.00")))));
  }

  @Test
  void rejectsUnknownIncomeAndNonIntegerCategoryIds() throws Exception {
    for (String id : List.of("999", "8", "7.5", "\"7\"")) {
      when(gemini.generate(anyString(), any(), eq("category")))
          .thenReturn(json.readTree("{\"categoryId\":" + id + ",\"explanation\":\"Synthetic\"}"));
      assertThatThrownBy(() -> service.categorize(email, "Coffee"))
          .isInstanceOfSatisfying(
              ResponseStatusException.class,
              e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
    }
  }

  @Test
  void acceptsAllowedCategoryAndValidatedMonthlyOutput() throws Exception {
    when(gemini.generate(anyString(), any(), eq("category")))
        .thenReturn(
            json.readTree("{\"categoryId\":7,\"explanation\":\"Synthetic category fixture\"}"));
    assertThat(service.categorize(email, "Coffee")).containsEntry("categoryId", 7L);
    when(gemini.generate(anyString(), any(), eq("monthly")))
        .thenReturn(json.readTree(monthly("900", "7")));
    Map<String, Object> result = service.monthly(email, "2026-11");
    assertThat(result).containsKeys("summary", "patterns", "recommendations", "metrics");
    assertThat(result.get("recommendations").toString()).contains("900.00", "Food");
  }

  @Test
  void rejectsInventedBudgetAmountsAndForeignCategoryIds() throws Exception {
    for (String output :
        List.of(monthly("901", "7"), monthly("900", "999"), monthly("\"900\"", "7"))) {
      when(gemini.generate(anyString(), any(), eq("monthly"))).thenReturn(json.readTree(output));
      assertThatThrownBy(() -> service.monthly(email, "2026-11"))
          .isInstanceOfSatisfying(
              ResponseStatusException.class,
              e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
    }
  }

  @Test
  void emptyMonthNeverCallsProvider() {
    when(finance.analytics(email, "2026-11")).thenReturn(Map.of("transactionCount", 0));
    assertThatThrownBy(() -> service.monthly(email, "2026-11"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(400));
    verifyNoInteractions(gemini);
  }

  String monthly(String amount, String category) {
    return "{\"summary\":\"Synthetic monthly fixture\",\"patterns\":[{\"title\":\"Food\",\"detail\":\"Synthetic pattern\"}],"
        + "\"recommendations\":[{\"categoryId\":"
        + category
        + ",\"suggestedLimit\":"
        + amount
        + ",\"reason\":\"Illustrative target\"}]}";
  }
}
