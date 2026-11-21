package com.paisa.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name", "type"}))
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private AppUser owner;

  @Column(nullable = false, length = 60)
  private String name;

  @Column(nullable = false, length = 10)
  private String type;

  @Column(nullable = false, length = 20)
  private String color;

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

  public String getColor() {
    return color;
  }

  public void setColor(String value) {
    this.color = value;
  }
}
