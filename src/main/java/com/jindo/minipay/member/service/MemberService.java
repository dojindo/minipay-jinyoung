package com.jindo.minipay.member.service;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.global.exception.ErrorCode.ALREADY_EXISTS_USERNAME;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final CheckingAccountRepository checkingAccountRepository;
  private final AccountNumberCreator accountNumberCreator;

  @Transactional
  public Long signup(MemberSignupRequest memberSignupRequest) {
    if (memberRepository.existsByUsername(memberSignupRequest.getUsername())) {
      throw new CustomException(ALREADY_EXISTS_USERNAME);
    }

    Member member = memberSignupRequest.toEntity();

    Member owner = memberRepository.save(member);

    // TODO : 계좌 생성 분리하기
    CheckingAccount checkingAccount = CheckingAccount.builder()
        .owner(owner)
        .accountNumber(accountNumberCreator.create(CHECKING))
        .build();

    checkingAccountRepository.save(checkingAccount);
    return owner.getId();
  }
}
