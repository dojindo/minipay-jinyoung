package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountChargeResponse;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireRequest;
import com.jindo.minipay.account.checking.dto.CheckingAccountWireResponse;
import com.jindo.minipay.account.checking.entity.ChargeAmount;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.repository.redis.ChargeAmountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import java.time.Duration;
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
  ChargeAmountRepository chargeAmountRepository;

  @Mock
  MemberRepository memberRepository;

  @Mock
  AccountNumberCreator accountNumberCreator;

  @Nested
  @DisplayName("메인 계좌 생성")
  class CheckingAccountCreateMethod {
    Long memberId = 1L;

    @Test
    @DisplayName("싪패 - 존재하지 않는 회원알 때")
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
    @DisplayName("성공")
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

    Member owner = Member.builder()
        .id(request.getMemberId())
        .build();

    CheckingAccount checkingAccount = CheckingAccount.builder()
        .id(1L)
        .owner(owner)
        .accountNumber("111112345678")
        .balance(request.getAmount())
        .build();

    @Test
    @DisplayName("실패 - 없는 계좌일 때")
    void charge_not_found_checking_account() {
      // given
      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getMemberId()))
          .willThrow(new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.charge(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 일일 충전 한도를 초과했을 때")
    void charge_limit_exceeded() {
      // given
      ChargeAmount limitMaximumAmount = new ChargeAmount(owner.getId(), ACCOUNT_CHARGE_LIMIT);

      when(checkingAccountRepository.findByOwnerIdForUpdate(request.getMemberId())).thenReturn(
          Optional.of(checkingAccount));

      when(chargeAmountRepository.findByMemberId(request.getMemberId())).thenReturn(
          Optional.of(limitMaximumAmount));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.charge(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(CHARGE_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("성공")
    void charge_already_exists_redis() {
      // given
      ChargeAmount chargeAmount = new ChargeAmount(owner.getId(), 10_000L);

      when(checkingAccountRepository.findByOwnerIdForUpdate(request.getMemberId())).thenReturn(
          Optional.of(checkingAccount));

      when(chargeAmountRepository.findByMemberId(request.getMemberId())).thenReturn(
          Optional.of(chargeAmount));

      // when
      CheckingAccountChargeResponse response = checkingAccountService.charge(request);

      // then
      verify(chargeAmountRepository, times(1)).save(any(Long.class), any(Long.class),
          any(Duration.class));
      assertThat(response.getBalance()).isEqualTo(20_000L);
    }
  }

  @Nested
  @DisplayName("계좌 송금")
  class CheckingAccountWireMethod {

    long amount = 15_000L;

    CheckingAccountWireRequest request = new CheckingAccountWireRequest(1L, 2L, amount);

    @Test
    @DisplayName("실패 - 송신자의 메인계좌가 없는 경우")
    void wire_not_found_sender_checking_account() {
      // given
      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getSenderId()))
          .willThrow(new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.wire(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 수신자의 메인계좌가 없는 경우")
    void wire_not_found_receiver_checking_account() {
      // given
      Member sender = Member.builder()
          .id(1L)
          .build();

      CheckingAccount senderAccount = CheckingAccount.builder()
          .id(1L)
          .owner(sender)
          .accountNumber("111112345678")
          .build();

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getSenderId()))
          .willReturn(Optional.of(senderAccount));

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getReceiverId()))
          .willThrow(new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.wire(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Nested
    @DisplayName("송신자의 잔액이 송금 금액보다 적은 경우 - 자동 충전")
    class isBalanceLessThanWireAmount {

      Member sender = Member.builder()
          .id(1L)
          .build();

      CheckingAccount senderAccount = CheckingAccount.builder()
          .id(1L)
          .owner(sender)
          .accountNumber("111112345678")
          .balance(0L)
          .build();

      Member receiver = Member.builder()
          .id(2L)
          .build();

      CheckingAccount receiverAccount = CheckingAccount.builder()
          .id(2L)
          .owner(receiver)
          .accountNumber("111198765432")
          .balance(0L)
          .build();

      @Test
      @DisplayName("실패 - 송신자의 일일 한도가 초과한 경우")
      void wire_limit_exceeded() {
        // given
        ChargeAmount limitMaximumAmount = new ChargeAmount(sender.getId(), ACCOUNT_CHARGE_LIMIT);

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getSenderId())).thenReturn(
            Optional.of(senderAccount));

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getReceiverId())).thenReturn(
            Optional.of(receiverAccount));

        when(chargeAmountRepository.findByMemberId(request.getSenderId())).thenReturn(
            Optional.of(limitMaximumAmount));
        // when
        // then
        assertThatThrownBy(() -> checkingAccountService.wire(request))
            .isInstanceOf(CustomException.class)
            .hasMessage(CHARGE_LIMIT_EXCEEDED.getMessage());
      }

      @Test
      @DisplayName("성공 - 송신자의 계좌에 자동 충전 단위로 충전된 후 수신자의 계좌에 송금한다.")
      void wire_autoCharge() {
        // given
        ChargeAmount chargeAmount = new ChargeAmount(sender.getId(), 0L);

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getSenderId())).thenReturn(
            Optional.of(senderAccount));

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getReceiverId())).thenReturn(
            Optional.of(receiverAccount));

        when(chargeAmountRepository.findByMemberId(request.getSenderId())).thenReturn(
            Optional.of(chargeAmount));

        // when
        CheckingAccountWireResponse response = checkingAccountService.wire(request);

        // then
        verify(chargeAmountRepository, times(1)).save(any(Long.class), anyLong(),
            any(Duration.class));

        assertThat(receiverAccount.getBalance()).isEqualTo(15_000L);

        assertThat(response.getBalance()).isEqualTo(5_000L);
      }

      @Test
      @DisplayName("성공 - 송신자의 계좌에 자동 충전 단위로 충전된 후 수신자의 계좌에 송금한다.")
      void wire_autoCharge_zero_mod() {
        // given
        long zeroModAmount = 20000L;
        CheckingAccountWireRequest request = new CheckingAccountWireRequest(1L, 2L, zeroModAmount);

        ChargeAmount chargeAmount = new ChargeAmount(sender.getId(), 0L);

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getSenderId())).thenReturn(
            Optional.of(senderAccount));

        when(checkingAccountRepository.findByOwnerIdForUpdate(request.getReceiverId())).thenReturn(
            Optional.of(receiverAccount));

        when(chargeAmountRepository.findByMemberId(request.getSenderId())).thenReturn(
            Optional.of(chargeAmount));

        // when
        CheckingAccountWireResponse response = checkingAccountService.wire(request);

        // then
        verify(chargeAmountRepository, times(1)).save(any(Long.class), anyLong(),
            any(Duration.class));

        assertThat(receiverAccount.getBalance()).isEqualTo(20000L);

        assertThat(response.getBalance()).isEqualTo(10_000L);
      }
    }
  }
}