package com.jindo.minipay.account.checking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckingAccountWireRequest {

  @NotNull
  private Long senderId;

  @NotNull
  private Long receiverId;

  @Min(1L)
  private long amount;
}
