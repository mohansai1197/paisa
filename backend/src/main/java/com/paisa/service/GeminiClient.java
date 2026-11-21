package com.paisa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Backend-only provider transport. The request key and provider error bodies are never logged. */
@Component
public class GeminiClient {
  private final ObjectMapper json;
  private final HttpClient http;
  private final String key;
  private final String model;
  private final URI modelsUri;

  @Autowired
  public GeminiClient(
      ObjectMapper json,
      @Value("${paisa.gemini-key:}") String key,
      @Value("${paisa.gemini-model:gemini-3.5-flash-lite}") String model) {
    this(json, key, model, URI.create("https://generativelanguage.googleapis.com/v1beta/models/"));
  }

  // The alternate endpoint is package-private and used only by synthetic transport tests.
  GeminiClient(ObjectMapper json, String key, String model, URI modelsUri) {
    this.json = json;
    this.key = key;
    this.model = model;
    this.modelsUri = modelsUri;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public boolean configured() {
    return key != null && !key.isBlank();
  }

  public JsonNode generate(String instruction, Object context, String schemaName) {
    if (!configured())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "AI is not connected yet. Configure GEMINI_API_KEY on the backend. Manual finance features remain available.");
    if (!model.matches("[a-zA-Z0-9._-]+"))
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Check the backend model configuration.");
    if (!List.of("category", "monthly").contains(schemaName))
      throw new IllegalArgumentException("Unknown AI response schema");
    try {
      JsonNode schema;
      try (var input =
          new ClassPathResource("ai/" + schemaName + ".schema.json").getInputStream()) {
        schema = json.readTree(input);
      }
      String body =
          json.writeValueAsString(
              Map.of(
                  "systemInstruction", Map.of("parts", List.of(Map.of("text", instruction))),
                  "contents",
                      List.of(
                          Map.of(
                              "role",
                              "user",
                              "parts",
                              List.of(
                                  Map.of(
                                      "text",
                                      "CONTEXT_JSON:\n" + json.writeValueAsString(context))))),
                  "generationConfig",
                      Map.of(
                          "responseMimeType",
                          "application/json",
                          "responseJsonSchema",
                          schema,
                          "temperature",
                          0.2,
                          "maxOutputTokens",
                          2400)));
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(modelsUri.toString() + model + ":generateContent"))
              .timeout(Duration.ofSeconds(35))
              .header("Content-Type", "application/json")
              .header("x-goog-api-key", key)
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 429)
        throw new ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS,
            "The AI provider's rate limit or quota has been reached. Wait before trying again; your records are safe.");
      if (response.statusCode() == 401 || response.statusCode() == 403)
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "The AI key is not authorized. Check the backend key and Google project permissions.");
      if (response.statusCode() == 404)
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "This Gemini model is not available to the configured project. Check the backend model setting.");
      if (response.statusCode() < 200 || response.statusCode() > 299)
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "The AI provider is unavailable or its configuration needs attention. Try again later.");
      if (response.body().length() > 131072) throw invalid();
      JsonNode candidate = json.readTree(response.body()).path("candidates").path(0);
      if (candidate.has("finishReason") && !candidate.path("finishReason").asText().equals("STOP"))
        throw invalid();
      StringBuilder output = new StringBuilder();
      for (JsonNode part : candidate.path("content").path("parts")) {
        if (!part.path("thought").asBoolean(false) && part.path("text").isTextual())
          output.append(part.path("text").asText());
      }
      if (output.isEmpty()) throw invalid();
      JsonNode result = json.readTree(output.toString());
      if (result == null || !result.isObject()) throw invalid();
      return result;
    } catch (ResponseStatusException e) {
      throw e;
    } catch (HttpTimeoutException e) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "The AI provider took too long. Please try again later.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "AI request interrupted. Please try again.");
    } catch (IOException | IllegalArgumentException e) {
      throw invalid();
    }
  }

  private static ResponseStatusException invalid() {
    return new ResponseStatusException(
        HttpStatus.BAD_GATEWAY,
        "The AI response could not be validated. Your records have not been changed.");
  }
}
