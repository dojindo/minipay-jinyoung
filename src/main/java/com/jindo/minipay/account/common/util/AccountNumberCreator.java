package com.jindo.minipay.account.common.util;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVING;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNumberCreator {

  private final CheckingAccountRepository checkingAccountRepository;
  private final SavingAccountRepository savingAccountRepository;

  public String create(AccountType accountType) {
    StringBuilder sb = new StringBuilder();

    sb.append(accountType.getCode());

    for (int i = 0; i < 8; i++) {
      sb.append((int) (Math.random() * 10));
    }

    boolean exists = false;
    if (accountType == CHECKING) {
      exists = checkingAccountRepository.existsByAccountNumber(sb.toString());
    } else if (accountType == SAVING) {
      exists = savingAccountRepository.existsByAccountNumber(sb.toString());
    }

    if (exists) {
      return create(accountType);
    }

    return sb.toString();
  }
}
