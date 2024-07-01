package com.jindo.minipay.account.savings.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccountDepositRequest {

  @NotNull
  private Long ownerId; // TODO : @AuthenticationPrincipal or @Login 으로 수정

  @NotNull
  private Long savingAccountId;

  @PositiveOrZero
  private long amount;
}
