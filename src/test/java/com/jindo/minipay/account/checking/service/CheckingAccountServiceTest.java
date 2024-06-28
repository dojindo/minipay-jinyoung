package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.entity.ChargeAmount;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.ChargeAmountRepository;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
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

  @Nested
  @DisplayName("메인 계좌 충전")
  class CheckingAccountChargeMethod {

    CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(1L, 10_000L);

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
          .willThrow(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.charge(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ErrorCode.ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 일일 충전 한도를 초과했을 때")
    void charge_limit_exceeded() {
      // given
      ChargeAmount exceedAmount = new ChargeAmount(owner.getId(), ACCOUNT_CHARGE_LIMIT);

      when(checkingAccountRepository.findByOwnerIdForUpdate(request.getMemberId())).thenReturn(
          Optional.of(checkingAccount));

      when(chargeAmountRepository.findByMemberId(request.getMemberId())).thenReturn(
          Optional.of(exceedAmount));

      // when
      // then
      assertThatThrownBy(() -> checkingAccountService.charge(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ErrorCode.CHARGE_LIMIT_EXCEEDED.getMessage());
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
      checkingAccountService.charge(request);

      // then
      verify(chargeAmountRepository, times(1)).save(any(Long.class), any(Long.class),
          any(Duration.class));
      assertThat(checkingAccount.getBalance()).isEqualTo(20_000L);
    }
  }
}