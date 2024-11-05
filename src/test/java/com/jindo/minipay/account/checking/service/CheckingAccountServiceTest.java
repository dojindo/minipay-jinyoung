package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckingAccountServiceTest {

  @InjectMocks
  CheckingAccountService checkingAccountService;

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Mock
  MemberRepository memberRepository;

  @Mock
  AccountNumberCreator accountNumberCreator;

  @Mock
  ChargeService chargeService;

  @Mock
  RemitServiceFinder remitServiceFinder;


  @Nested
  @DisplayName("메인 계좌 생성")
  class CheckingAccountCreateMethod {

    Long memberId = 1L;

    @Test
    @DisplayName("메인 계좌 생성 실패 - 존재하지 않는 회원알 때")
    void create_member_not_found() {
      // given
      given(memberRepository.findById(memberId)).willThrow(new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.create(memberId))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메인 계좌 생성 성공")
    void create_success() {
      // given
      Member member = Member.builder()
          .id(1L)
          .build();

      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

      // when
      checkingAccountService.create(memberId);

      // then
      verify(checkingAccountRepository).save(any(CheckingAccount.class));
    }
  }

  @Nested
  @DisplayName("메인 계좌 충전")
  class CheckingAccountChargeMethod {

    long amount = 10_000L;

    CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(1L, amount);

    @Test
    @DisplayName("메인 계좌 충전 성공")
    void charge_success() {
      // given
      CheckingAccount checkingAccount = CheckingAccount.builder()
          .balance(0L)
          .build();

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getMemberId()))
          .willReturn(Optional.of(checkingAccount));

      given(chargeService.charge(checkingAccount, request.getMemberId(), amount))
          .willReturn(10_000L);

      // when
      ChargeResponse response = checkingAccountService.charge(request);

      // then
      assertThat(response.getBalance()).isEqualTo(10_000L);
    }
  }

  @Nested
  @DisplayName("계좌 송금")
  class CheckingAccountRemitMethod {

    @Mock
    ImmediateRemitService immediateRemitService;

    @Mock
    PendingRemitService pendingRemitService;

    Member sender = Member.builder()
        .id(1L)
        .build();

    long amount = 10_000L;

    CheckingAccountRemitRequest request = new CheckingAccountRemitRequest(sender.getId(), 2L,
        amount);

    @Test
    @DisplayName("즉시 송금")
    void immediateRemit() {
      // given
      given(remitServiceFinder.find(sender.getId())).willReturn(immediateRemitService);

      RemitResponse expectedResponse = new RemitResponse(amount);
      given(immediateRemitService.remit(request)).willReturn(
          new RemitResponse(amount));

      // when
      RemitResponse response = checkingAccountService.remit(request);

      // then
      assertThat(response.getBalance()).isEqualTo(expectedResponse.getBalance());
    }

    @Test
    @DisplayName("보류 송금")
    void pendingRemit() {
      // given
      given(remitServiceFinder.find(sender.getId())).willReturn(pendingRemitService);

      RemitResponse expectedResponse = new RemitResponse(amount);
      given(pendingRemitService.remit(request)).willReturn(
          new RemitResponse(amount));

      // when
      RemitResponse response = checkingAccountService.remit(request);

      // then
      assertThat(response.getBalance()).isEqualTo(expectedResponse.getBalance());
    }
  }
}