package com.paisa.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
  public record Register(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @Email @Size(max = 180) String email,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank String password) {}

  public record VerificationEmail(@NotBlank @Email @Size(max = 180) String email) {}

  public record VerifyEmail(
      @NotBlank @Email @Size(max = 180) String email,
      @NotNull @Pattern(regexp = "[0-9]{6}") String code,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record ResetPassword(
      @NotBlank @Email @Size(max = 180) String email,
      @NotNull @Pattern(regexp = "[0-9]{6}") String code,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record AccountInput(
      @NotBlank @Size(max = 80) String name,
      @Pattern(regexp = "BANK|CASH|WALLET") @NotNull String type,
      @NotNull
          @DecimalMin(value = "0.00", message = "Opening balance cannot be negative.")
          @Digits(integer = 11, fraction = 2)
          BigDecimal openingBalance) {}

  public record CategoryInput(
      @NotBlank @Size(max = 60) String name,
      @Pattern(regexp = "INCOME|EXPENSE") @NotNull String type) {}

  public record TransactionInput(
      @NotNull Long accountId,
      @NotNull Long categoryId,
      @NotNull @Pattern(regexp = "INCOME|EXPENSE") String type,
      @NotBlank @Size(max = 180) String description,
      @NotNull @DecimalMin("0.01") @Digits(integer = 11, fraction = 2) BigDecimal amount,
      @NotNull LocalDate date) {}

  public record BudgetInput(
      @NotNull Long categoryId,
      @NotNull @Pattern(regexp = "\\d{4}-\\d{2}") String month,
      @NotNull @DecimalMin("0.01") @Digits(integer = 11, fraction = 2) BigDecimal limitAmount) {}

  public record Categorize(@NotBlank @Size(max = 180) String description) {}
}
