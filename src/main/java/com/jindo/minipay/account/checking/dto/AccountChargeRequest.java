package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountChargeRequest {

  @NotNull
  private Long memberId;

  @PositiveOrZero
  private long amount;

}
