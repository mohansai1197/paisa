package com.paisa.repository;

import com.paisa.model.Category;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByOwnerIdOrderByIdAsc(Long owner);

  Optional<Category> findByIdAndOwnerId(Long id, Long owner);
}
