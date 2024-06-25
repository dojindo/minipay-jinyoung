package com.jindo.minipay.member.service;

import static com.jindo.minipay.global.exception.ErrorCode.ALREADY_EXISTS_USERNAME;

import com.jindo.minipay.account.entity.Account;
import com.jindo.minipay.account.entity.type.AccountType;
import com.jindo.minipay.account.repository.AccountRepository;
import com.jindo.minipay.account.util.AccountNumberCreator;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.dto.MemberSignupDto;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final AccountRepository accountRepository;
  private final AccountNumberCreator accountNumberCreator;

  @Transactional
  public void signup(MemberSignupDto memberSignupDto) {
    if (memberRepository.existsByUsername(memberSignupDto.getUsername())) {
      throw new CustomException(ALREADY_EXISTS_USERNAME);
    }

    Member member = Member.builder()
        .username(memberSignupDto.getUsername())
        .password(memberSignupDto.getPassword())
        .build();

    Member owner = memberRepository.save(member);

    Account account = Account.builder()
        .owner(owner)
        .accountNumber(accountNumberCreator.create(AccountType.MAIN))
        .type(AccountType.MAIN)
        .build();

    accountRepository.save(account);
  }
}
