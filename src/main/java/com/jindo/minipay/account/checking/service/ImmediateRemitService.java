package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import org.springframework.stereotype.Service;

@Service
public class ImmediateRemitService extends RemitService {
  private final CheckingAccountRepository checkingAccountRepository;

  public ImmediateRemitService(ChargeService chargeService,
      CheckingAccountRepository checkingAccountRepository) {
    super(chargeService);
    this.checkingAccountRepository = checkingAccountRepository;
  }

  @Override
  public boolean isImmediateTransfer() {
    return true;
  }

  @Override
  public RemitResponse remit(CheckingAccountRemitRequest request) {
    Long senderId = request.getSenderId();
    Long receiverId = request.getReceiverId();
    long amount = request.getAmount();

    CheckingAccount senderAccount;
    CheckingAccount receiverAccount;

    if (senderId < receiverId) {
      senderAccount = getCheckingAccountForUpdate(senderId);
      receiverAccount = getCheckingAccountForUpdate(receiverId);
    } else {
      receiverAccount = getCheckingAccountForUpdate(receiverId);
      senderAccount = getCheckingAccountForUpdate(senderId);
    }

    if (senderAccount.getBalance() < amount) {
      autoCharge(senderAccount.getBalance(), amount, senderId);
    }

    senderAccount.withdraw(amount);
    receiverAccount.deposit(amount);

    return RemitResponse.of(senderAccount);
  }

  private CheckingAccount getCheckingAccountForUpdate(Long memberId) {
    return checkingAccountRepository
        .findByOwnerIdForUpdate(memberId)
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
  }
}
