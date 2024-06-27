package com.jindo.minipay.account.savings.repository;

import com.jindo.minipay.account.savings.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {

  boolean existsByAccountNumber(String string);

}
