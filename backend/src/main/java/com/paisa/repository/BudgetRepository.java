package com.paisa.repository;

import com.paisa.model.Budget;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
  List<Budget> findByOwnerIdOrderByIdAsc(Long owner);

  Optional<Budget> findByOwnerIdAndCategoryIdAndMonth(Long owner, Long category, String month);

  Optional<Budget> findByIdAndOwnerId(Long id, Long owner);
}
