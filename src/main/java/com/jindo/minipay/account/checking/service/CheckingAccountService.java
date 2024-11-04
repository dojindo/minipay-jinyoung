package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckingAccountService {

  private final CheckingAccountRepository checkingAccountRepository;
  private final MemberRepository memberRepository;
  private final AccountNumberCreator accountNumberCreator;
  private final ChargeService chargeService;
  private final RemitServiceFinder remitServiceFinder;

  public void create(Long memberId) {
    Member owner = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    CheckingAccount checkingAccount = CheckingAccount.builder()
        .owner(owner)
        .accountNumber(accountNumberCreator.create(CHECKING))
        .build();

    checkingAccountRepository.save(checkingAccount);
  }

  @Transactional
  public ChargeResponse charge(CheckingAccountChargeRequest request) {
    CheckingAccount checkingAccount = getCheckingAccountForUpdate(request.getMemberId());
    return new ChargeResponse(
        chargeService.charge(checkingAccount, request.getMemberId(), request.getAmount()));
  }

  @Transactional
  public RemitResponse remit(CheckingAccountRemitRequest request) {
    return remitServiceFinder.find(request.getSenderId()).remit(request);
  }

  private CheckingAccount getCheckingAccountForUpdate(Long memberId) {
    return checkingAccountRepository
        .findByOwnerIdForUpdate(memberId)
        .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
  }
}
