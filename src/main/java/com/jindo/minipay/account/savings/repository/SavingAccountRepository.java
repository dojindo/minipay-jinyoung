package com.jindo.minipay.account.savings.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.jindo.minipay.account.savings.entity.SavingAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

  boolean existsByAccountNumber(String string);

  @Lock(PESSIMISTIC_WRITE)
  @Query("SELECT s FROM SavingAccount s WHERE s.id = :id")
  Optional<SavingAccount> findByIdForUpdate(@Param("id") Long id);
}
