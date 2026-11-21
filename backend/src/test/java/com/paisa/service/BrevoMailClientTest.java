package com.paisa.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

class BrevoMailClientTest {
  final ObjectMapper json = new ObjectMapper();

  @Test
  void sendsCorrectHttpsApiPayloadAndCredentialHeader() throws Exception {
    var payload = new AtomicReference<String>();
    var header = new AtomicReference<String>();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v3/smtp/email", request -> {
      payload.set(new String(request.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      header.set(request.getRequestHeaders().getFirst("api-key"));
      request.sendResponseHeaders(201, -1);
      request.close();
    });
    server.start();
    try {
      var client = new BrevoMailClient(json, "test-only-key", "sender@example.test",
          URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email"));
      client.send("recipient@example.test", "Verify Paisa", "Your code: 123456");
      var body = json.readTree(payload.get());
      assertThat(header.get()).isEqualTo("test-only-key");
      assertThat(body.path("sender").path("email").asText()).isEqualTo("sender@example.test");
      assertThat(body.path("to").get(0).path("email").asText()).isEqualTo("recipient@example.test");
      assertThat(body.path("textContent").asText()).contains("123456");
      assertThat(payload.get()).doesNotContain("test-only-key");
    } finally { server.stop(0); }
  }

  @Test
  void rejectedProviderResponseIsSafeAndNotRetried() throws Exception {
    var calls = new java.util.concurrent.atomic.AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/email", request -> {
      calls.incrementAndGet();
      byte[] detail = "private-provider-detail".getBytes(StandardCharsets.UTF_8);
      request.sendResponseHeaders(403, detail.length);
      request.getResponseBody().write(detail);
      request.close();
    });
    server.start();
    try {
      var client = new BrevoMailClient(json, "test-only-key", "sender@example.test",
          URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/email"));
      assertThatThrownBy(() -> client.send("recipient@example.test", "Reset", "123456"))
          .isInstanceOf(ResponseStatusException.class).hasMessageContaining("could not send")
          .hasMessageNotContaining("private-provider-detail")
          .hasMessageNotContaining("123456").hasMessageNotContaining("test-only-key");
      assertThat(calls.get()).isEqualTo(1);
    } finally { server.stop(0); }
  }

  @Test
  void verificationAndRecoveryPreferHttpsOverSmtp() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> smtp = mock(ObjectProvider.class);
    BrevoMailClient brevo = mock(BrevoMailClient.class);
    when(brevo.configured()).thenReturn(true);
    var sender = new VerificationMailSender(smtp, "", "sender@example.test", brevo);
    assertThat(sender.configured()).isTrue();
    sender.sendCode("recipient@example.test", "123456");
    sender.sendResetCode("recipient@example.test", "654321");
    verify(brevo).send(eq("recipient@example.test"), eq("Verify your Paisa email address"), contains("123456"));
    verify(brevo).send(eq("recipient@example.test"), eq("Reset your Paisa password"), contains("654321"));
    verifyNoInteractions(smtp);
    assertThat(new BrevoMailClient(json, "", "sender@example.test").configured()).isFalse();
    assertThat(new BrevoMailClient(json, "test-only-key", "").configured()).isFalse();
  }
}
