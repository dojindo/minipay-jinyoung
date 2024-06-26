package com.jindo.minipay.account.common.util;

import static com.jindo.minipay.account.common.constant.AccountConstants.CHECKING_ACCOUNT_PREFIX;
import static com.jindo.minipay.account.common.constant.AccountConstants.SAVING_ACCOUNT_PREFIX;
import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVING;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.type.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountNumberCreator {

  private final CheckingAccountRepository checkingAccountRepository;

  public String create(AccountType accountType) {
    StringBuilder sb = new StringBuilder();

    if (accountType == CHECKING) {
      sb.append(CHECKING_ACCOUNT_PREFIX);
    } else if (accountType == SAVING) {
      sb.append(SAVING_ACCOUNT_PREFIX);
    }

    for (int i = 0; i < 8; i++) {
      sb.append((int) (Math.random() * 10));
    }

    boolean exists = checkingAccountRepository.existsByAccountNumber(sb.toString());

    if (exists) {
      return create(accountType);
    }

    return sb.toString();
  }
}
