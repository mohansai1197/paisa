package com.paisa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Sends transactional codes over HTTPS; credentials and provider bodies never enter logs. */
@Component
public class BrevoMailClient {
  private final ObjectMapper json;
  private final String key, from;
  private final URI endpoint;
  private final HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();

  @Autowired
  public BrevoMailClient(ObjectMapper json,
      @Value("${paisa.brevo-key:}") String key,
      @Value("${paisa.mail-from:}") String from) {
    this(json, key, from, URI.create("https://api.brevo.com/v3/smtp/email"));
  }

  BrevoMailClient(ObjectMapper json, String key, String from, URI endpoint) {
    this.json = json;
    this.key = key;
    this.from = from;
    this.endpoint = endpoint;
  }

  public boolean configured() {
    return !key.isBlank() && !from.isBlank();
  }

  public void send(String email, String subject, String text) {
    if (!configured()) throw unavailable();
    try {
      String body = json.writeValueAsString(Map.of(
          "sender", Map.of("name", "Paisa", "email", from),
          "to", List.of(Map.of("email", email)), "subject", subject, "textContent", text));
      HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(15))
          .header("api-key", key).header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body)).build();
      // Do not retry automatically: a timeout may occur after an email was accepted.
      if (client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() != 201)
        throw unavailable();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw unavailable();
    } catch (Exception e) {
      throw unavailable();
    }
  }

  private ResponseStatusException unavailable() {
    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "We could not send the email. Please try again later.");
  }
}
