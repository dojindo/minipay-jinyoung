package com.jindo.minipay.account.savings.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccountCreateRequest {

  @NotNull
  private Long memberId;

}
