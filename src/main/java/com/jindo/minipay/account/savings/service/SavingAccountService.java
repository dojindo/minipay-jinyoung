package com.jindo.minipay.account.savings.service;

import static com.jindo.minipay.account.common.type.AccountType.SAVING;

import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingAccountService {

  private final SavingAccountRepository savingAccountRepository;
  private final MemberRepository memberRepository;
  private final AccountNumberCreator accountNumberCreator;


  public void create(SavingAccountCreateRequest request) {
    Member owner = memberRepository.findById(request.getMemberId()).orElseThrow(
        () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
    );

    SavingAccount savingAccount = SavingAccount.builder()
        .owner(owner)
        .accountNumber(accountNumberCreator.create(SAVING))
        .build();

    savingAccountRepository.save(savingAccount);
  }
}
