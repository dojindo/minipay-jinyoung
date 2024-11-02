package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;

import com.jindo.minipay.account.checking.entity.ChargeAmount;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.repository.redis.ChargeAmountRepository;
import com.jindo.minipay.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargeService {

  private final CheckingAccountRepository checkingAccountRepository;
  private final ChargeAmountRepository chargeAmountRepository;

  @Transactional
  public long charge(Long ownerId, long amount) {
    ChargeAmount chargeAmount = getChargeAmountOfMember(ownerId);

    long afterChargeAmount = chargeAmount.getAmount() + amount;
    validateChargeLimit(afterChargeAmount);

    CheckingAccount mainAccount = checkingAccountRepository.findByOwnerIdForUpdate(ownerId)
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

    mainAccount.deposit(amount);
    updateChargeAmountOfMember(ownerId, afterChargeAmount);

    return mainAccount.getBalance();
  }

  private ChargeAmount getChargeAmountOfMember(Long memberId) {
    return chargeAmountRepository.findByMemberId(memberId)
        .orElseGet(() -> new ChargeAmount(memberId, 0L));
  }

  private void validateChargeLimit(long afterChargeAmount) {
    if (afterChargeAmount > ACCOUNT_CHARGE_LIMIT) {
      throw new CustomException(CHARGE_LIMIT_EXCEEDED);
    }
  }

  private void updateChargeAmountOfMember(Long memberId, long amount) {
    chargeAmountRepository.save(memberId, amount, timeToMidnight());
  }

  private Duration timeToMidnight() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
    return Duration.between(now, midnight);
  }
}
