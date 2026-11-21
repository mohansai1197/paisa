package com.paisa.repository;

import com.paisa.model.Account;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
  List<Account> findByOwnerIdOrderByIdAsc(Long owner);

  Optional<Account> findByIdAndOwnerId(Long id, Long owner);
}
