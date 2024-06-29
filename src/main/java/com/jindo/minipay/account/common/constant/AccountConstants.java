package com.jindo.minipay.account.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountConstants {

  public static final String CHECKING_ACCOUNT_PREFIX = "1111";

  public static final String SAVING_ACCOUNT_PREFIX = "1122";

  public static final int ACCOUNT_CHARGE_LIMIT = 3_000_000;

  public static final String CHARGE_AMOUNT_KEY = "chargeAmount:";
}
