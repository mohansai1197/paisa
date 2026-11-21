package com.paisa.api;

import com.paisa.service.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class FinanceController {
  final FinanceService finance;
  final AiService ai;
  final com.paisa.service.VerificationMailSender mail;

  @Value("${spring.profiles.active:mysql}")
  String profile;

  public FinanceController(
      FinanceService f, AiService a, com.paisa.service.VerificationMailSender m) {
    finance = f;
    ai = a;
    mail = m;
  }

  @GetMapping("/api/status")
  public Map<String, Object> status() {
    return Map.of(
        "service",
        "Paisa",
        "aiConfigured",
        ai.configured(),
        "emailVerificationConfigured",
        mail.configured(),
        "storage",
        profile.contains("demo") ? "H2 local demo" : "MySQL");
  }

  @GetMapping("/api/workspace")
  public Map<String, Object> workspace(Authentication a) {
    return finance.snapshot(a.getName());
  }

  @PostMapping("/api/accounts")
  public Map<String, Object> account(
      Authentication a, @Valid @RequestBody Requests.AccountInput i) {
    return finance.saveAccount(a.getName(), null, i);
  }

  @PutMapping("/api/accounts/{id}")
  public Map<String, Object> updateAccount(
      Authentication a, @PathVariable Long id, @Valid @RequestBody Requests.AccountInput i) {
    return finance.saveAccount(a.getName(), id, i);
  }

  @DeleteMapping("/api/accounts/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(Authentication a, @PathVariable Long id) {
    finance.deleteAccount(a.getName(), id);
  }

  @PostMapping("/api/categories")
  public Map<String, Object> category(
      Authentication a, @Valid @RequestBody Requests.CategoryInput i) {
    return finance.addCategory(a.getName(), i);
  }

  @PostMapping("/api/transactions")
  public Map<String, Object> transaction(
      Authentication a, @Valid @RequestBody Requests.TransactionInput i) {
    return finance.saveTransaction(a.getName(), null, i);
  }

  @PutMapping("/api/transactions/{id}")
  public Map<String, Object> update(
      Authentication a, @PathVariable Long id, @Valid @RequestBody Requests.TransactionInput i) {
    return finance.saveTransaction(a.getName(), id, i);
  }

  @DeleteMapping("/api/transactions/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication a, @PathVariable Long id) {
    finance.deleteTransaction(a.getName(), id);
  }

  @PostMapping("/api/budgets")
  public Map<String, Object> budget(Authentication a, @Valid @RequestBody Requests.BudgetInput i) {
    return finance.saveBudget(a.getName(), i);
  }

  @DeleteMapping("/api/budgets/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBudget(Authentication a, @PathVariable Long id) {
    finance.deleteBudget(a.getName(), id);
  }

  @GetMapping("/api/analytics")
  public Map<String, Object> analytics(Authentication a, @RequestParam String month) {
    return finance.analytics(a.getName(), month);
  }

  @GetMapping("/api/reports")
  public ResponseEntity<String> report(Authentication a, @RequestParam String month) {
    String csv = finance.report(a.getName(), month);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=paisa-" + month + ".csv")
        .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
        .body(csv);
  }

  @PostMapping("/api/demo/seed")
  public Map<String, Object> seed(Authentication a) {
    finance.seedDemo(a.getName());
    return finance.snapshot(a.getName());
  }

  @PostMapping("/api/ai/categorize")
  public Map<String, Object> categorize(
      Authentication a, @Valid @RequestBody Requests.Categorize input) {
    return ai.categorize(a.getName(), input.description());
  }

  @PostMapping("/api/ai/monthly")
  public Map<String, Object> insights(Authentication a, @RequestParam String month) {
    return ai.monthly(a.getName(), month);
  }
}
