package com.jindo.minipay.account.checking.dto;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemitResponse {

  private long balance;

  public static RemitResponse of(CheckingAccount account) {
    return new RemitResponse(account.getBalance());
  }
}
