package com.jindo.minipay.account.checking.repository;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long> {

  boolean existsByAccountNumber(String accountNumber);

  Optional<CheckingAccount> findByOwnerId(Long ownerId);

  @Lock(PESSIMISTIC_WRITE)
  @Query("SELECT c FROM CheckingAccount c WHERE c.owner.id = :owner_id")
  Optional<CheckingAccount> findByOwnerIdForUpdate(@Param("owner_id") Long ownerId);
}
