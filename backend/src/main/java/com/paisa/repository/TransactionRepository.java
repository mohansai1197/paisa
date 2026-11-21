package com.paisa.repository;

import com.paisa.model.Transaction;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByOwnerIdOrderByDateDescIdDesc(Long owner);

  Optional<Transaction> findByIdAndOwnerId(Long id, Long owner);

  boolean existsByAccountId(Long account);
}
