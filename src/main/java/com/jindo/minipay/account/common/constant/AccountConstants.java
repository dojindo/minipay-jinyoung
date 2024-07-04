package com.jindo.minipay.account.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountConstants {

  public static final long ACCOUNT_CHARGE_LIMIT = 3_000_000L;

  public static final long AUTO_CHARGE_UNIT = 10_000L;

  public static final String CHARGE_AMOUNT_KEY = "chargeAmount:";
}
