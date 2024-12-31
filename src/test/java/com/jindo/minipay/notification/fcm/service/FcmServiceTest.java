package com.jindo.minipay.notification.fcm.service;

import static com.jindo.minipay.global.exception.ErrorCode.FCM_TOKEN_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.notification.dto.NotificationCommonDto;
import com.jindo.minipay.notification.fcm.dto.FcmTokenRegisterRequest;
import com.jindo.minipay.notification.fcm.entity.FcmToken;
import com.jindo.minipay.notification.fcm.repository.FcmTokenRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

  @InjectMocks
  FcmService fcmService;

  @Mock
  FcmTokenRepository fcmTokenRepository;

  @Mock
  MemberRepository memberRepository;

  @Mock
  FireMessageSender fireMessageSender;

  @Nested
  @DisplayName("FCM 토큰 등록")
  class RegisterMethod {

    String tokenValue = "request token value";
    FcmTokenRegisterRequest request = new FcmTokenRegisterRequest(1L, tokenValue);

    @Test
    @DisplayName("실패 - 존재하지 않는 회원일 때")
    void fail_fcm_token_register_member_not_found() {
      // given
      // when
      when(memberRepository.findById(request.getMemberId())).thenThrow(
          new CustomException(MEMBER_NOT_FOUND));
      // then
      Assertions.assertThatThrownBy(() -> fcmService.registerToken(request))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void success_fcm_token_register() {
      // given
      Member member = Member.builder()
          .id(1L)
          .build();

      // when
      when(memberRepository.findById(request.getMemberId())).thenReturn(Optional.of(member));
      fcmService.registerToken(request);

      // then
      ArgumentCaptor<FcmToken> captor = ArgumentCaptor.forClass(FcmToken.class);
      verify(fcmTokenRepository).save(captor.capture());

      FcmToken savedToken = captor.getValue();
      Assertions.assertThat(savedToken.getMember()).isEqualTo(member);
      Assertions.assertThat(savedToken.getToken()).isEqualTo(request.getToken());
    }
  }

  @Nested
  @DisplayName("FCM 알림 전송")
  class SendNotificationMethod {

    String title = "notification test title";
    String content = "notification test content";

    NotificationCommonDto dto = NotificationCommonDto.builder()
        .memberId(1L)
        .title(title)
        .content(content)
        .build();

    @Test
    @DisplayName("실패 - 존재하지 않는 회원일 때")
    void fail_fcm_send_notification_member_not_found() {
      // given
      // when
      when(memberRepository.findById(dto.getMemberId())).thenThrow(
          new CustomException(MEMBER_NOT_FOUND));
      // then
      Assertions.assertThatThrownBy(() -> fcmService.sendNotification(dto))
          .isInstanceOf(CustomException.class)
          .hasMessage(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 토큰일 때")
    void fail_fcm_send_notification_fcm_token_not_found() {
      // given
      Member member = Member.builder()
          .id(1L)
          .build();

      // when
      when(memberRepository.findById(dto.getMemberId())).thenReturn(Optional.of(member));
      when(fcmTokenRepository.findByMember(member)).thenThrow(
          new CustomException(FCM_TOKEN_NOT_FOUND));

      // then
      Assertions.assertThatThrownBy(() -> fcmService.sendNotification(dto))
          .isInstanceOf(CustomException.class)
          .hasMessage(FCM_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("성공")
    void success_fcm_send_notification() throws FirebaseMessagingException {
      // given
      Member member = Member.builder()
          .id(1L)
          .build();

      FcmToken fcmToken = FcmToken.builder()
          .id(1L)
          .token("dummy token")
          .member(member)
          .build();

      // when
      when(memberRepository.findById(dto.getMemberId())).thenReturn(Optional.of(member));
      when(fcmTokenRepository.findByMember(member)).thenReturn(Optional.of(fcmToken));
      when(fireMessageSender.send(any(Message.class))).thenReturn("success-response");

      fcmService.sendNotification(dto);

      // then
      verify(fireMessageSender).send(any(Message.class));
    }
  }
}