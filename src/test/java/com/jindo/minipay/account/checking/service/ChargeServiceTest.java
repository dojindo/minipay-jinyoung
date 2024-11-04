package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.account.common.constant.AccountConstants.ACCOUNT_CHARGE_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.CHARGE_LIMIT_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.dto.CheckingAccountChargeRequest;
import com.jindo.minipay.account.checking.entity.ChargeLimit;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.repository.redis.ChargeLimitRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import java.time.Duration;
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
class ChargeServiceTest {

  @InjectMocks
  ChargeService chargeService;

  @Mock
  ChargeLimitRepository chargeLimitRepository;

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Nested
  @DisplayName("메인 계좌 충전")
  class ChargeMethod {

    long amount = 10_000L;

    Member owner = Member.builder()
        .id(1L)
        .build();

    CheckingAccountChargeRequest request = new CheckingAccountChargeRequest(owner.getId(), amount);

    CheckingAccount checkingAccount = CheckingAccount.builder()
        .id(owner.getId())
        .owner(owner)
        .accountNumber("111112345678")
        .balance(request.getAmount())
        .build();

    @Test
    @DisplayName("실패 - 일일 충전 한도를 초과했을 때")
    void charge_limit_exceeded() {
      // given
      ChargeLimit limitMaximumAmount = new ChargeLimit(request.getMemberId(),
          ACCOUNT_CHARGE_LIMIT);

      when(chargeLimitRepository.findByMemberId(request.getMemberId())).thenReturn(
          Optional.of(limitMaximumAmount));

      // when
      // then
      assertThatThrownBy(() -> chargeService.charge(checkingAccount, owner.getId(), amount))
          .isInstanceOf(CustomException.class)
          .hasMessage(CHARGE_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("성공")
    void charge() {
      // given
      long balance = checkingAccount.getBalance();
      ChargeLimit chargeLimit = new ChargeLimit(owner.getId(), 10_000L);

      when(chargeLimitRepository.findByMemberId(request.getMemberId())).thenReturn(
          Optional.of(chargeLimit));

      // when
      chargeService.charge(checkingAccount, owner.getId(), amount);

      // then
      verify(chargeLimitRepository, times(1)).save(any(Long.class), any(Long.class),
          any(Duration.class));
      Assertions.assertThat(checkingAccount.getBalance()).isEqualTo(balance + amount);
    }
  }
}