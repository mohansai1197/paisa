package com.paisa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private AppUser owner;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, length = 20)
  private String type;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal openingBalance;

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

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    this.type = value;
  }

  public BigDecimal getOpeningBalance() {
    return openingBalance;
  }

  public void setOpeningBalance(BigDecimal value) {
    this.openingBalance = value;
  }
}
