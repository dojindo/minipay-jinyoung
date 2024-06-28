package com.jindo.minipay.member.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.CHECKING_ACCOUNT_PREFIX;
import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.dto.MemberSignupRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Mock
  AccountNumberCreator accountNumberCreator;

  @InjectMocks
  MemberService memberService;

  @Nested
  @DisplayName("회원 등록 메서드")
  class MemberSignupMethod {

    MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
        .username("username1")
        .password("1q2w3e4r!")
        .build();

    @Test
    @DisplayName("실패 - 유저네임이 중복된 경우")
    void signup_conflict_username() {
      // given

      // when
      given(memberRepository.existsByUsername(memberSignupRequest.getUsername())).willReturn(true);

      // then
      assertThatThrownBy(() -> memberService.signup(memberSignupRequest))
          .isInstanceOf(CustomException.class)
          .hasMessage(ErrorCode.ALREADY_EXISTS_USERNAME.getMessage());
    }

    @Test
    @DisplayName("성공 - 회원과 메인계좌가 생성된다.")
    void signup() {
      // given
      given(memberRepository.existsByUsername(memberSignupRequest.getUsername())).willReturn(false);

      Member owner = Member.builder()
          .id(1L)
          .username(memberSignupRequest.getUsername())
          .password(memberSignupRequest.getPassword())
          .build();

      // when
      when(memberRepository.save(any())).thenReturn(owner);

      String accountNumber = CHECKING_ACCOUNT_PREFIX + "12345678";
      when(accountNumberCreator.create(CHECKING)).thenReturn(accountNumber);

      memberService.signup(memberSignupRequest);

      // then
      verify(checkingAccountRepository).save(any());
    }
  }
}