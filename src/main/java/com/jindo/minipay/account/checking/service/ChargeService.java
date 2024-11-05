package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;

import com.jindo.minipay.account.checking.entity.ChargeLimit;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.redis.ChargeLimitRepository;
import com.jindo.minipay.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChargeService {
  private final ChargeLimitRepository chargeLimitRepository;

  public long charge(CheckingAccount checkingAccount, Long ownerId, long amount) {
    ChargeLimit chargeLimit = getChargeLimitOfMember(ownerId);

    long afterChargeAmount = chargeLimit.getAmount() + amount;
    validateChargeLimit(afterChargeAmount);

    checkingAccount.deposit(amount);
    updateChargeAmountOfMember(ownerId, afterChargeAmount);

    return checkingAccount.getBalance();
  }

  private ChargeLimit getChargeLimitOfMember(Long memberId) {
    return chargeLimitRepository.findByMemberId(memberId)
        .orElseGet(() -> new ChargeLimit(memberId, 0L));
  }

  private void validateChargeLimit(long afterChargeAmount) {
    if (afterChargeAmount > ACCOUNT_CHARGE_LIMIT) {
      throw new CustomException(CHARGE_LIMIT_EXCEEDED);
    }
  }

  private void updateChargeAmountOfMember(Long memberId, long amount) {
    chargeLimitRepository.save(memberId, amount, timeToMidnight());
  }

  private Duration timeToMidnight() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
    return Duration.between(now, midnight);
  }
}
