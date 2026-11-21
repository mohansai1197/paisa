package com.paisa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, unique = true, length = 180)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean emailVerified;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "boolean default false")
  private boolean verificationRequired;

  public boolean isVerificationRequired() {
    return verificationRequired;
  }

  public void setVerificationRequired(boolean value) {
    verificationRequired = value;
  }

  public boolean canAccessWorkspace() {
    return emailVerified || !verificationRequired;
  }

  @JsonIgnore private String verificationHash;
  @JsonIgnore private Instant verificationExpiresAt;
  @JsonIgnore private Instant verificationSentAt;
  @JsonIgnore private Instant verificationWindowAt;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "integer default 0")
  private int verificationAttempts;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "integer default 0")
  private int verificationSends;

  @JsonIgnore private String passwordResetHash;
  @JsonIgnore private Instant passwordResetExpiresAt;
  @JsonIgnore private Instant passwordResetSentAt;
  @JsonIgnore private Instant passwordResetWindowAt;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "integer default 0")
  private int passwordResetAttempts;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "integer default 0")
  private int passwordResetSends;

  @JsonIgnore
  @Column(nullable = false, columnDefinition = "bigint default 0")
  private long authVersion;

  public String getPasswordResetHash() {
    return passwordResetHash;
  }

  public void setPasswordResetHash(String value) {
    passwordResetHash = value;
  }

  public Instant getPasswordResetExpiresAt() {
    return passwordResetExpiresAt;
  }

  public void setPasswordResetExpiresAt(Instant value) {
    passwordResetExpiresAt = value;
  }

  public Instant getPasswordResetSentAt() {
    return passwordResetSentAt;
  }

  public void setPasswordResetSentAt(Instant value) {
    passwordResetSentAt = value;
  }

  public Instant getPasswordResetWindowAt() {
    return passwordResetWindowAt;
  }

  public void setPasswordResetWindowAt(Instant value) {
    passwordResetWindowAt = value;
  }

  public int getPasswordResetAttempts() {
    return passwordResetAttempts;
  }

  public void setPasswordResetAttempts(int value) {
    passwordResetAttempts = value;
  }

  public int getPasswordResetSends() {
    return passwordResetSends;
  }

  public void setPasswordResetSends(int value) {
    passwordResetSends = value;
  }

  public long getAuthVersion() {
    return authVersion;
  }

  public void setAuthVersion(long value) {
    authVersion = value;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(boolean value) {
    emailVerified = value;
  }

  public String getVerificationHash() {
    return verificationHash;
  }

  public void setVerificationHash(String value) {
    verificationHash = value;
  }

  public Instant getVerificationExpiresAt() {
    return verificationExpiresAt;
  }

  public void setVerificationExpiresAt(Instant value) {
    verificationExpiresAt = value;
  }

  public Instant getVerificationSentAt() {
    return verificationSentAt;
  }

  public void setVerificationSentAt(Instant value) {
    verificationSentAt = value;
  }

  public Instant getVerificationWindowAt() {
    return verificationWindowAt;
  }

  public void setVerificationWindowAt(Instant value) {
    verificationWindowAt = value;
  }

  public int getVerificationAttempts() {
    return verificationAttempts;
  }

  public void setVerificationAttempts(int value) {
    verificationAttempts = value;
  }

  public int getVerificationSends() {
    return verificationSends;
  }

  public void setVerificationSends(int value) {
    verificationSends = value;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long value) {
    this.id = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String value) {
    this.passwordHash = value;
  }
}
