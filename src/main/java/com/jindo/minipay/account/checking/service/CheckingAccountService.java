package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.account.common.constant.AccountConstants.AUTO_CHARGE_UNIT;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountChargeResponse;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireResponse;
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
public class CheckingAccountService {

  private final CheckingAccountRepository checkingAccountRepository;

  private final ChargeAmountRepository chargeAmountRepository;

  @Transactional
  public CheckingAccountChargeResponse charge(CheckingAccountChargeRequest request) {
    CheckingAccount checkingAccount = getCheckingAccountForUpdate(request.getMemberId());

    actualCharge(checkingAccount, request.getAmount());

    return new CheckingAccountChargeResponse(checkingAccount.getBalance());
  }

  @Transactional
  public CheckingAccountWireResponse wire(CheckingAccountWireRequest request) {

    // TODO: 데드락 해결하기
    Long senderId = request.getSenderId();
    Long receiverId = request.getReceiverId();

    CheckingAccount senderAccount;
    CheckingAccount receiverAccount;

    if (senderId < receiverId) {
      senderAccount = getCheckingAccountForUpdate(senderId);
      receiverAccount = getCheckingAccountForUpdate(receiverId);
    } else {
      receiverAccount = getCheckingAccountForUpdate(receiverId);
      senderAccount = getCheckingAccountForUpdate(senderId);
    }

    checkBalanceAndAutoCharge(senderAccount, request.getAmount());

    senderAccount.withdraw(request.getAmount());

    receiverAccount.deposit(request.getAmount());

    return new CheckingAccountWireResponse(senderAccount.getBalance());
  }

  private CheckingAccount getCheckingAccountForUpdate(Long memberId) {
    return checkingAccountRepository
        .findByOwnerIdForUpdate(memberId)
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
  }

  private void actualCharge(CheckingAccount checkingAccount, long amount) {
    updateChargeAmountOfMember(checkingAccount.getOwner().getId(), amount);
    checkingAccount.deposit(amount);
  }

  private void updateChargeAmountOfMember(Long memberId, long amount) {
    long chargeAmount = getChargeAmountOfMember(memberId).getAmount();

    long afterChargeAmount = chargeAmount + amount;
    validateChargeLimit(afterChargeAmount);

    chargeAmountRepository.save(memberId, afterChargeAmount, timeToMidnight());
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

  private void checkBalanceAndAutoCharge(CheckingAccount checkingAccount, long amount) {
    long balance = checkingAccount.getBalance();
    if (balance < amount) {
      long diff = amount - balance;
      long chargeAmount = (diff / AUTO_CHARGE_UNIT) * AUTO_CHARGE_UNIT;

      if (diff % ACCOUNT_CHARGE_LIMIT > 0) {
        chargeAmount += AUTO_CHARGE_UNIT;
      }
      actualCharge(checkingAccount, chargeAmount);
    }
  }

  private Duration timeToMidnight() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
    return Duration.between(now, midnight);
  }
}
