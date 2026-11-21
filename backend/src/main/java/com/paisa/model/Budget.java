package com.paisa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "budgets",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"owner_id", "category_id", "budget_month"}))
public class Budget {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private AppUser owner;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Category category;

  @Column(name = "budget_month", nullable = false, length = 7)
  private String month;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal limitAmount;

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

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category value) {
    this.category = value;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String value) {
    this.month = value;
  }

  public BigDecimal getLimitAmount() {
    return limitAmount;
  }

  public void setLimitAmount(BigDecimal value) {
    this.limitAmount = value;
  }
}
