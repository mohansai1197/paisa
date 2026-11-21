-- Paisa / MySQL 8+. Reference schema; Hibernate currently creates/updates it.
-- Run as a user authorized to create this application database.
CREATE DATABASE IF NOT EXISTS paisa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE paisa;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verification_required BOOLEAN NOT NULL DEFAULT FALSE,
  verification_hash VARCHAR(255),
  verification_expires_at TIMESTAMP(6) NULL,
  verification_sent_at TIMESTAMP(6) NULL,
  verification_window_at TIMESTAMP(6) NULL,
  verification_attempts INT NOT NULL DEFAULT 0,
  verification_sends INT NOT NULL DEFAULT 0,
  password_reset_hash VARCHAR(255),
  password_reset_expires_at TIMESTAMP(6) NULL,
  password_reset_sent_at TIMESTAMP(6) NULL,
  password_reset_window_at TIMESTAMP(6) NULL,
  password_reset_attempts INT NOT NULL DEFAULT 0,
  password_reset_sends INT NOT NULL DEFAULT 0,
  auth_version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS accounts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  type VARCHAR(20) NOT NULL,
  opening_balance DECIMAL(14,2) NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  name VARCHAR(60) NOT NULL,
  type VARCHAR(10) NOT NULL,
  color VARCHAR(20) NOT NULL,
  UNIQUE KEY uk_category_owner_name_type (owner_id, name, type),
  FOREIGN KEY (owner_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS transactions (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  type VARCHAR(10) NOT NULL,
  description VARCHAR(180) NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  transaction_date DATE NOT NULL,
  KEY idx_transaction_owner_date (owner_id, transaction_date),
  FOREIGN KEY (owner_id) REFERENCES users(id),
  FOREIGN KEY (account_id) REFERENCES accounts(id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);
CREATE TABLE IF NOT EXISTS budgets (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  budget_month VARCHAR(7) NOT NULL,
  limit_amount DECIMAL(14,2) NOT NULL,
  UNIQUE KEY uk_budget_owner_category_month (owner_id, category_id, budget_month),
  FOREIGN KEY (owner_id) REFERENCES users(id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);
