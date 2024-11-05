package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.jindo.minipay.pending.type.PendingStatus.PENDING;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.pending.entity.PendingTransfer;
import com.jindo.minipay.pending.repository.PendingTransferRepository;
import org.springframework.stereotype.Service;

@Service
public class PendingRemitService extends RemitService {

  private final MemberRepository memberRepository;
  private final PendingTransferRepository pendingTransferRepository;

  public PendingRemitService(ChargeService chargeService, MemberRepository memberRepository,
      CheckingAccountRepository checkingAccountRepository,
      PendingTransferRepository pendingTransferRepository) {
    super(chargeService, checkingAccountRepository);
    this.memberRepository = memberRepository;
    this.pendingTransferRepository = pendingTransferRepository;
  }

  @Override
  public boolean isImmediateTransfer() {
    return false;
  }

  @Override
  public RemitResponse remit(CheckingAccountRemitRequest request) {
    Long senderId = request.getSenderId();
    Long receiverId = request.getReceiverId();
    long amount = request.getAmount();

    Member sender = getMember(senderId);
    Member receiver = getMember(receiverId);

    CheckingAccount senderAccount = getCheckingAccountForUpdate(senderId);

    if (senderAccount.getBalance() < amount) {
      autoCharge(senderAccount, senderAccount.getBalance(), amount, senderId);
    }

    senderAccount.withdraw(amount);

    PendingTransfer pendingTransfer = PendingTransfer.builder()
        .sender(sender)
        .receiver(receiver)
        .amount(request.getAmount())
        .status(PENDING)
        .build();

    pendingTransferRepository.save(pendingTransfer);

    return RemitResponse.of(senderAccount);
  }

  private Member getMember(Long senderId) {
    return memberRepository.findById(senderId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }
}
