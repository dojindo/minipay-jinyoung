package com.jindo.minipay.account.common.util;

import static com.jindo.minipay.account.common.type.AccountType.CHECKING;
import static com.jindo.minipay.account.common.type.AccountType.SAVING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.savings.repository.SavingAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountNumberCreatorTest {

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Mock
  SavingAccountRepository savingAccountRepository;

  @InjectMocks
  AccountNumberCreator accountNumberCreator;

  @Test
  @DisplayName("성공 - 메인 계좌 번호 생성")
  void create_checking_account() {
    // given
    when(checkingAccountRepository.existsByAccountNumber(any())).thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(CHECKING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(CHECKING.getCode());
  }

  @Test
  @DisplayName("성공 - 메인 계좌 번호 생성 시 한 번 중복됐을 떄, 번호를 재생성한다.")
  void create_checking_account_retry() {
    // given
    when(checkingAccountRepository.existsByAccountNumber(any()))
        .thenReturn(true)
        .thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(CHECKING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(CHECKING.getCode());
    verify(checkingAccountRepository, times(2)).existsByAccountNumber(any());
  }

  @Test
  @DisplayName("성공 - 적금 계좌 번호 생성")
  void create_saving_account() {
    // given
    when(savingAccountRepository.existsByAccountNumber(any())).thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(SAVING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(SAVING.getCode());
  }

  @Test
  @DisplayName("성공 - 적금 계좌 번호 생성 시 한 번 중복됐을 떄, 번호를 재생성한다.")
  void create_saving_account_retry() {
    // given
    when(savingAccountRepository.existsByAccountNumber(any()))
        .thenReturn(true)
        .thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(SAVING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(SAVING.getCode());
    verify(savingAccountRepository, times(2)).existsByAccountNumber(any());
  }

}