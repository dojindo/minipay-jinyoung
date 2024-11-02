package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.AUTO_CHARGE_UNIT;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RemitService {

  private final ChargeService chargeService;

  abstract boolean isImmediateTransfer();

  abstract RemitResponse remit(CheckingAccountRemitRequest request);

  public void autoCharge(long balance, long amount, Long senderId) {
    long autoChargeAmount = calculateAutoChargeAmount(balance, amount);

    chargeService.charge(senderId, autoChargeAmount);
  }

  private long calculateAutoChargeAmount(long balance, long amount) {
    long gap = amount - balance;
    long result = (gap / AUTO_CHARGE_UNIT) * AUTO_CHARGE_UNIT;

    return gap % AUTO_CHARGE_UNIT > 0 ? result + AUTO_CHARGE_UNIT : result;
  }
}
