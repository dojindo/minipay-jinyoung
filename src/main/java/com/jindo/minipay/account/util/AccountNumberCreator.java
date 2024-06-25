package com.jindo.minipay.account.util;

import static com.jindo.minipay.account.constant.AccountConstants.MAIN_ACCOUNT_PREFIX;
import static com.jindo.minipay.account.constant.AccountConstants.SAVINGS_ACCOUNT_PREFIX;
import static com.jindo.minipay.account.entity.type.AccountType.MAIN;
import static com.jindo.minipay.account.entity.type.AccountType.SAVINGS;

import com.jindo.minipay.account.entity.type.AccountType;
import com.jindo.minipay.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNumberCreator {

  private final AccountRepository accountRepository;

  public String create(AccountType accountType) {
    StringBuilder sb = new StringBuilder();

    if (accountType == MAIN) {
      sb.append(MAIN_ACCOUNT_PREFIX);
    } else if (accountType == SAVINGS) {
      sb.append(SAVINGS_ACCOUNT_PREFIX);
    }

    for (int i = 0; i < 8; i++) {
      sb.append((int) (Math.random() * 10));
    }

    boolean exists = accountRepository.existsByAccountNumber(sb.toString());

    if (exists) {
      return create(accountType);
    }

    return sb.toString();
  }
}
