package com.jindo.minipay.account.repository;

import com.jindo.minipay.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

  boolean existsByAccountNumber(String accountNumber);
}
