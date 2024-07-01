package com.jindo.minipay.account.checking.entity;

import static com.jindo.minipay.account.common.constant.AccountConstants.CHARGE_AMOUNT_KEY;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = CHARGE_AMOUNT_KEY)
public class ChargeAmount {

  @Id
  private Long memberId;

  private long amount;
}
