package com.jindo.minipay.account.savings.service;

import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.BALANCE_NOT_ENOUGH;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.util.AccountNumberCreator;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateRequest;
import com.jindo.minipay.account.savings.dto.SavingAccountCreateResponse;
import com.jindo.minipay.account.savings.dto.SavingAccountDepositRequest;
import com.jindo.minipay.account.savings.entity.SavingAccount;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
  CheckingAccountRepository checkingAccountRepository;

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
      given(memberRepository.findById(request.getMemberId())).willReturn(
          Optional.empty());

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.create(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void create() {
      // given
      Member owner = Member.builder()
          .id(1L)
          .build();

      SavingAccount savingAccount = SavingAccount.builder()
          .id(1L)
          .owner(owner)
          .build();

      given(memberRepository.findById(request.getMemberId())).willReturn(
          Optional.ofNullable(owner));

      given(savingAccountRepository.save(any())).willReturn(
          savingAccount);

      // when
      SavingAccountCreateResponse response = savingAccountService.create(request);

      // then
      verify(savingAccountRepository, times(1)).save(any(SavingAccount.class));
      Assertions.assertThat(response.getSavingAccountId()).isEqualTo(1L);
    }
  }

  @Nested
  @DisplayName("적금 계좌 입금 메서드")
  class SavingAccountDepositMethod {

    SavingAccountDepositRequest request = new SavingAccountDepositRequest(1L, 1L, 10_000L);

    Member owner = Member.builder()
        .id(1L)
        .build();

    SavingAccount savingAccount = SavingAccount.builder()
        .id(1L)
        .amount(10_000L)
        .owner(owner)
        .build();

    CheckingAccount checkingAccount = CheckingAccount.builder()
        .id(1L)
        .balance(50_000L)
        .owner(owner)
        .build();

    CheckingAccount notEnoughCheckingAccount = CheckingAccount.builder()
        .id(1L)
        .balance(0L)
        .owner(owner)
        .build();

    @Test
    @DisplayName("실패 - 존재히지 않는 회원인 경우")
    void deposit_not_found_member() {
      // given
      given(memberRepository.existsById(request.getOwnerId())).willReturn(false);

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.deposit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재히지 않는 메인 계좌인 경우")
    void deposit_not_found_checking_account() {
      // given
      given(memberRepository.existsById(request.getOwnerId())).willReturn(true);

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getOwnerId())).willThrow(
          new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.deposit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재히지 않는 적금 계좌인 경우")
    void deposit_not_found_saving_account() {
      // given
      given(memberRepository.existsById(request.getOwnerId())).willReturn(true);

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getOwnerId())).willReturn(
          Optional.of(checkingAccount));

      given(savingAccountRepository.findByIdForUpdate(request.getSavingAccountId())).willThrow(
          new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.deposit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 메인 계좌에 잔액이 부족한 경우")
    void deposit_not_found_balance_not_enough() {

      // given
      given(savingAccountRepository.findByIdForUpdate(request.getSavingAccountId())).willReturn(
          Optional.of(savingAccount));

      given(memberRepository.existsById(request.getOwnerId())).willReturn(true);

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getOwnerId())).willReturn(
          Optional.of(notEnoughCheckingAccount));

      // when
      // then
      assertThatThrownBy(() -> savingAccountService.deposit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(BALANCE_NOT_ENOUGH.getMessage());
    }

    @Test
    @DisplayName("성공")
    void deposit() {

      // given
      given(savingAccountRepository.findByIdForUpdate(request.getSavingAccountId())).willReturn(
          Optional.of(savingAccount));

      given(memberRepository.existsById(request.getOwnerId())).willReturn(true);

      given(checkingAccountRepository.findByOwnerIdForUpdate(request.getOwnerId())).willReturn(
          Optional.of(checkingAccount));

      // when
      savingAccountService.deposit(request);

      // then
      assertThat(savingAccount.getAmount()).isEqualTo(20_000L);
      assertThat(checkingAccount.getBalance()).isEqualTo(40_000L);
    }
  }
}