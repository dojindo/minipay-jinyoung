package com.jindo.minipay.account.savings.dto;

import com.jindo.minipay.account.savings.entity.SavingAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccountDepositResponse {

  private long amount;

  public static SavingAccountDepositResponse fromEntity(SavingAccount savingAccount) {
    return new SavingAccountDepositResponse(savingAccount.getAmount());
  }
}
