package com.paisa.service;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.server.ResponseStatusException;

/** Synthetic provider tests. These do not claim that Google generation has been verified. */
class GeminiClientTest {
  final ObjectMapper json = new ObjectMapper();
  HttpServer server;
  URI endpoint;
  AtomicInteger status = new AtomicInteger(200), calls = new AtomicInteger();
  AtomicReference<String> response = new AtomicReference<>();
  AtomicReference<JsonNode> requestBody = new AtomicReference<>();
  AtomicReference<String> requestKey = new AtomicReference<>(),
      requestPath = new AtomicReference<>();

  @BeforeEach
  void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          calls.incrementAndGet();
          requestKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
          requestPath.set(exchange.getRequestURI().toString());
          requestBody.set(json.readTree(exchange.getRequestBody()));
          byte[] bytes = response.get().getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(status.get(), bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
    server.start();
    endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta/models/");
    respond("{\"categoryId\":7,\"explanation\":\"Synthetic fixture\"}", "STOP");
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  void respond(String output, String finishReason) throws Exception {
    response.set(
        json.writeValueAsString(
            Map.of(
                "candidates",
                List.of(
                    Map.of(
                        "finishReason",
                        finishReason,
                        "content",
                        Map.of(
                            "parts",
                            List.of(
                                Map.of("thought", true, "text", "Hidden synthetic reasoning"),
                                Map.of("text", output))))))));
  }

  GeminiClient client(String key) {
    return new GeminiClient(json, key, "test-model", endpoint);
  }

  @Test
  void sendsSecretInHeaderAndSuppliesResponseSchema() {
    JsonNode result =
        client("synthetic-test-key")
            .generate("Classify safely", Map.of("description", "Coffee"), "category");
    assertThat(result.path("categoryId").asInt()).isEqualTo(7);
    assertThat(requestKey.get()).isEqualTo("synthetic-test-key");
    assertThat(requestPath.get()).isEqualTo("/v1beta/models/test-model:generateContent");
    assertThat(requestPath.get()).doesNotContain("synthetic-test-key");
    assertThat(requestBody.get().toString()).doesNotContain("synthetic-test-key");
    assertThat(
            requestBody
                .get()
                .path("generationConfig")
                .path("responseJsonSchema")
                .path("required")
                .toString())
        .contains("categoryId", "explanation");
    assertThat(
            requestBody.get().path("systemInstruction").path("parts").get(0).path("text").asText())
        .isEqualTo("Classify safely");
  }

  @Test
  void missingKeyDoesNotCallProvider() {
    assertThatThrownBy(() -> client("").generate("Test", Map.of(), "category"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
    assertThat(calls.get()).isZero();
  }

  @ParameterizedTest
  @CsvSource({"429,429", "401,503", "403,503", "404,503", "500,503"})
  void mapsProviderErrorsWithoutExposingResponseBody(int upstream, int expected) {
    status.set(upstream);
    response.set("{\"sensitive-provider-detail\":\"not-public\"}");
    assertThatThrownBy(() -> client("synthetic-test-key").generate("Test", Map.of(), "monthly"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> {
              assertThat(e.getStatusCode().value()).isEqualTo(expected);
              assertThat(e.getReason())
                  .doesNotContain("sensitive-provider-detail", "not-public", "synthetic-test-key");
            });
    assertThat(calls.get())
        .isEqualTo(1); // No automatic retry or extra potentially billable generation.
  }

  @Test
  void rejectsTruncatedAndNonObjectResults() throws Exception {
    respond("{\"categoryId\":7}", "MAX_TOKENS");
    assertThatThrownBy(() -> client("synthetic-test-key").generate("Test", Map.of(), "category"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
    respond("[]", "STOP");
    assertThatThrownBy(() -> client("synthetic-test-key").generate("Test", Map.of(), "category"))
        .isInstanceOfSatisfying(
            ResponseStatusException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
  }
}
