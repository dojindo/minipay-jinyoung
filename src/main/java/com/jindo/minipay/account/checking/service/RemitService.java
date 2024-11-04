package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.AUTO_CHARGE_UNIT;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RemitService {

  private final ChargeService chargeService;
  private final CheckingAccountRepository checkingAccountRepository;

  abstract boolean isImmediateTransfer();

  abstract RemitResponse remit(CheckingAccountRemitRequest request);

  protected final void autoCharge(
      CheckingAccount checkingAccount, long balance, long amount, Long senderId) {
    long autoChargeAmount = calculateAutoChargeAmount(balance, amount);

    chargeService.charge(checkingAccount, senderId, autoChargeAmount);
  }

  protected final CheckingAccount getCheckingAccountForUpdate(Long memberId) {
    return checkingAccountRepository
        .findByOwnerIdForUpdate(memberId)
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
  }

  private long calculateAutoChargeAmount(long balance, long amount) {
    long gap = amount - balance;
    long result = (gap / AUTO_CHARGE_UNIT) * AUTO_CHARGE_UNIT;

    return gap % AUTO_CHARGE_UNIT > 0 ? result + AUTO_CHARGE_UNIT : result;
  }
}
