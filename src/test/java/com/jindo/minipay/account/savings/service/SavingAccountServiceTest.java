package com.jindo.minipay.account.savings.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingAccountServiceTest {

  @Mock
  SavingAccountRepository savingAccountRepository;

  @Mock
  AccountNumberCreator accountNumberCreator;

  @Mock
  MemberRepository memberRepository;

  @InjectMocks
  SavingAccountService savingAccountService;

  @Nested
  @DisplayName("적금 계좌 등록 메서드")
  class SavingAccountCreateMethod {

    SavingAccountCreateRequest request = new SavingAccountCreateRequest(1L);

    @Test
    @DisplayName("실패 - 존재히지 않는 회원일 경우")
    void create_not_found_member() {
      // given
      given(memberRepository.findById(request.getMemberId())).willThrow(
          new CustomException(ErrorCode.MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.create(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void create() {
      // given
      Member owner = Member.builder()
          .id(1L)
          .build();

      given(memberRepository.findById(request.getMemberId())).willReturn(Optional.ofNullable(owner));

      // when
      savingAccountService.create(request);
      // then
      verify(savingAccountRepository, times(1)).save(any(SavingAccount.class));
    }
  }
}