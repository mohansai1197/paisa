package com.paisa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "transactions",
    indexes = @Index(name = "idx_transaction_owner_date", columnList = "owner_id,transaction_date"))
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private AppUser owner;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Account account;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Category category;

  @Column(nullable = false, length = 10)
  private String type;

  @Column(nullable = false, length = 180)
  private String description;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Column(name = "transaction_date", nullable = false)
  private LocalDate date;

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public AppUser getOwner() {
    return owner;
  }

  public void setOwner(AppUser value) {
    this.owner = value;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account value) {
    this.account = value;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category value) {
    this.category = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    this.type = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate value) {
    this.date = value;
  }
}
