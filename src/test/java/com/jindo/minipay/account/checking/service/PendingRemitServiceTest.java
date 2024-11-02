package com.jindo.minipay.account.checking.service;

import static com.jindo.minipay.global.exception.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.jindo.minipay.account.checking.dto.CheckingAccountRemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.pending.entity.PendingTransfer;
import com.jindo.minipay.pending.repository.PendingTransferRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingRemitServiceTest {

  @InjectMocks
  PendingRemitService pendingRemitService;

  @Mock
  MemberRepository memberRepository;

  @Mock
  CheckingAccountRepository checkingAccountRepository;

  @Mock
  PendingTransferRepository pendingTransferRepository;

  @Test
  @DisplayName("즉시 송금 설정은 false 이다.")
  void isImmediateTransfer() {
    // given
    // when
    boolean immediateTransfer = pendingRemitService.isImmediateTransfer();
    // then
    assertThat(immediateTransfer).isFalse();
  }

  @Nested
  @DisplayName("보류 송금")
  class PendingRemit {

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
    @DisplayName("실패 - 송신자가 없으면 실패한다.")
    void remit_fail_sender_not_found() {
      // given
      given(memberRepository.findById(sender.getId()))
          .willThrow(new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> pendingRemitService.remit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 수신자가 없으면 실패한다.")
    void remit_fail_receiver_not_found() {
      // given
      given(memberRepository.findById(sender.getId())).willReturn(Optional.of(sender));
      given(memberRepository.findById(receiver.getId()))
          .willThrow(new CustomException(MEMBER_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> pendingRemitService.remit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 송신자의 계좌가 없으면 실패한다.")
    void remit_fail_sender_account_not_found() {
      // given
      given(memberRepository.findById(sender.getId())).willReturn(Optional.of(sender));
      given(memberRepository.findById(receiver.getId())).willReturn(Optional.of(receiver));
      given(checkingAccountRepository.findByOwnerIdForUpdate(sender.getId()))
          .willThrow(new CustomException(ACCOUNT_NOT_FOUND));

      // when
      // then
      assertThatThrownBy(() -> pendingRemitService.remit(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("보류 송금 시, 송금액만큼 송신자의 계좌에서 빠져나가고, 보류 내역을 생성한다.")
    void remit() {
      // given
      CheckingAccount senderAccount = CheckingAccount.builder()
          .owner(sender)
          .balance(10000L)
          .build();

      given(memberRepository.findById(sender.getId())).willReturn(Optional.of(sender));
      given(memberRepository.findById(receiver.getId())).willReturn(Optional.of(receiver));

      given(checkingAccountRepository.findByOwnerIdForUpdate(sender.getId())).willReturn(
          Optional.of(senderAccount));

      // when
      RemitResponse response = pendingRemitService.remit(request);

      // then
      verify(pendingTransferRepository).save(any(PendingTransfer.class));
      assertThat(response.getBalance()).isEqualTo(0L);
    }
  }
}