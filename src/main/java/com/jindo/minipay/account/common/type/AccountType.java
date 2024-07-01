package com.jindo.minipay.account.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
  CHECKING("1111"),
  SAVING("1112");

  private final String code;

}
