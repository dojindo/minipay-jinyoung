package com.jindo.minipay.account.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.constant.AccountConstants;
import com.jindo.minipay.account.common.type.AccountType;
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
    String accountNumber = accountNumberCreator.create(AccountType.CHECKING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(AccountConstants.CHECKING_ACCOUNT_PREFIX);
  }

  @Test
  @DisplayName("성공 - 메인 계좌 번호 생성 시 한 번 중복됐을 떄, 번호를 재생성한다.")
  void create_checking_account_retry() {
    // given
    when(checkingAccountRepository.existsByAccountNumber(any()))
        .thenReturn(true)
        .thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(AccountType.CHECKING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(AccountConstants.CHECKING_ACCOUNT_PREFIX);
    verify(checkingAccountRepository, times(2)).existsByAccountNumber(any());
  }

  @Test
  @DisplayName("성공 - 적금 계좌 번호 생성")
  void create_saving_account() {
    // given
    when(savingAccountRepository.existsByAccountNumber(any())).thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(AccountType.SAVING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(AccountConstants.SAVING_ACCOUNT_PREFIX);
  }

  @Test
  @DisplayName("성공 - 적금 계좌 번호 생성 시 한 번 중복됐을 떄, 번호를 재생성한다.")
  void create_saving_account_retry() {
    // given
    when(savingAccountRepository.existsByAccountNumber(any()))
        .thenReturn(true)
        .thenReturn(false);

    // when
    String accountNumber = accountNumberCreator.create(AccountType.SAVING);

    // then
    assertThat(accountNumber.substring(0, 4)).isEqualTo(AccountConstants.SAVING_ACCOUNT_PREFIX);
    verify(savingAccountRepository, times(2)).existsByAccountNumber(any());
  }

}