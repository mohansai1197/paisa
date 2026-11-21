package com.paisa.service;

import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificationRateLimiter {
  private final Map<String, List<Instant>> sends = new HashMap<>();
  private final int maxSends;

  public VerificationRateLimiter(
      @Value("${paisa.verification.max-sends-per-hour:10}") int maxSends) {
    this.maxSends = maxSends;
  }

  public synchronized void consume(String address) {
    Instant now = Instant.now(), cutoff = now.minusSeconds(3600);
    sends.values().forEach(times -> times.removeIf(time -> !time.isAfter(cutoff)));
    sends.values().removeIf(List::isEmpty);
    if (sends.size() >= 10000 && !sends.containsKey(address)) limited();
    List<Instant> times = sends.computeIfAbsent(address, key -> new ArrayList<>());
    if (times.size() >= maxSends) limited();
    times.add(now);
  }

  private void limited() {
    throw new ResponseStatusException(
        HttpStatus.TOO_MANY_REQUESTS, "Too many verification requests. Please try again later.");
  }
}
