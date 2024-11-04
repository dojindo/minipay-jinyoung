package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.member.entity.Member;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImmediateRemitServiceTest {

  @InjectMocks
  ImmediateRemitService immediateRemitService;

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Mock
  ChargeService chargeService;

  @Test
  @DisplayName("즉시 송금 설정은 true 이다.")
  void isImmediateTransfer() {
    // given
    // when
    boolean immediateTransfer = immediateRemitService.isImmediateTransfer();
    // then
    assertThat(immediateTransfer).isTrue();
  }

  @Nested
  @DisplayName("즉시 송금")
  class ImmediateRemit {

    Member sender = Member.builder()
        .id(1L)
        .build();

    Member receiver = Member.builder()
        .id(2L)
        .build();

    CheckingAccountRemitRequest request = CheckingAccountRemitRequest.builder()
        .senderId(sender.getId())
        .receiverId(receiver.getId())
        .amount(10000L)
        .build();

    @Test
    @DisplayName("실패 - 송신자의 계좌가 없으면 실패한다.")
    void remit_fail_sender_account_not_found() {
      // given
      given(checkingAccountRepository.findByOwnerIdForUpdate(sender.getId()))
          .willThrow(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> immediateRemitService.remit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 수신자의 계좌가 없으면 실패한다.")
    void remit_fail_receiver_account_not_found() {
      // given
      CheckingAccount senderAccount = CheckingAccount.builder()
          .owner(sender)
          .balance(10000L)
          .build();

      given(checkingAccountRepository.findByOwnerIdForUpdate(sender.getId())).willReturn(
          Optional.of(senderAccount));

      given(checkingAccountRepository.findByOwnerIdForUpdate(receiver.getId()))
          .willThrow(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> immediateRemitService.remit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("즉시 송금 시, 송금액만큼 송신자의 계좌에서 수신자의 계좌로 빠져나간다.")
    void remit() {
      // given
      CheckingAccount senderAccount = CheckingAccount.builder()
          .owner(sender)
          .balance(10000L)
          .build();

      CheckingAccount receiverAccount = CheckingAccount.builder()
          .owner(receiver)
          .balance(0L)
          .build();

      given(checkingAccountRepository.findByOwnerIdForUpdate(sender.getId())).willReturn(
          Optional.of(senderAccount));

      given(checkingAccountRepository.findByOwnerIdForUpdate(receiver.getId())).willReturn(
          Optional.of(receiverAccount));

      // when
      RemitResponse response = immediateRemitService.remit(request);

      // then
      assertThat(response.getBalance()).isZero();
    }
  }
}