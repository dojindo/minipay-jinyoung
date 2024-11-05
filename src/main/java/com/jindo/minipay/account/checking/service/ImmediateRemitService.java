package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class ImmediateRemitService extends RemitService {
  public ImmediateRemitService(ChargeService chargeService,
      CheckingAccountRepository checkingAccountRepository) {
    super(chargeService, checkingAccountRepository);
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
      autoCharge(senderAccount, senderAccount.getBalance(), amount, senderId);
    }

    senderAccount.withdraw(amount);
    receiverAccount.deposit(amount);

    return RemitResponse.of(senderAccount);
  }
}
