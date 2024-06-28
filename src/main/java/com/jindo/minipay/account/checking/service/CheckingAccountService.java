package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.entity.ChargeAmount;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.ChargeAmountRepository;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckingAccountService {

  private final CheckingAccountRepository checkingAccountRepository;

  private final ChargeAmountRepository chargeAmountRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void charge(CheckingAccountChargeRequest request) {
    CheckingAccount checkingAccount = checkingAccountRepository
        .findByOwnerIdForUpdate(request.getMemberId())
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

    long chargeAmount = chargeAmountRepository.findByMemberId(request.getMemberId())
        .orElseGet(() -> new ChargeAmount(request.getMemberId(), 0L)).getAmount();

    long afterChargeAmount = chargeAmount + request.getAmount();

    if (afterChargeAmount > ACCOUNT_CHARGE_LIMIT) {
      throw new CustomException(CHARGE_LIMIT_EXCEEDED);
    }

    chargeAmountRepository.save(request.getMemberId(), afterChargeAmount, timeToMidnight());

    checkingAccount.charge(request.getAmount());
  }

  private Duration timeToMidnight() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
    return Duration.between(now, midnight);
  }
}
