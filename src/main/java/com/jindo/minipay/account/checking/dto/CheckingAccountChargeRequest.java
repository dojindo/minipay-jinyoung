package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckingAccountChargeRequest {

  @NotNull
  private Long memberId;

  @PositiveOrZero
  private long amount;

}
