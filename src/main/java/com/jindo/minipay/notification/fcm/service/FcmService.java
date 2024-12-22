package com.jindo.minipay.notification.fcm.service;

import static com.jindo.minipay.global.exception.ErrorCode.FCM_MESSAGE_SEND_ERROR;
import static com.jindo.minipay.global.exception.ErrorCode.FCM_TOKEN_NOT_FOUND;
import static com.jindo.minipay.global.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jindo.minipay.global.exception.CustomException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.notification.dto.NotificationCommonDto;
import com.jindo.minipay.notification.fcm.dto.FcmTokenRegisterRequest;
import com.jindo.minipay.notification.fcm.entity.FcmToken;
import com.jindo.minipay.notification.fcm.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

  private final FcmTokenRepository fcmTokenRepository;
  private final MemberRepository memberRepository;

  public void registerToken(FcmTokenRegisterRequest request) {
    Member member = getMember(request.getMemberId());

    fcmTokenRepository.save(request.toEntity(member));
    // TODO: 토큰 만료 로직(fcm token 레코드 삭제) - 로그아웃, 앱(삭제 시), 웹(브라우저 종료 시)
  }

  public void sendNotification(NotificationCommonDto dto) {
    Member member = getMember(dto.getMemberId());

    FcmToken fcmToken = fcmTokenRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(FCM_TOKEN_NOT_FOUND));

    // TODO: FCM 은 토큰 or 토픽 방식 -> 토픽으로 전송이 추가될 가능성을 고려하여 확장성 있게 설계
    String token = fcmToken.getToken();
    sendByFcmToken(token, dto.getTitle(), dto.getContent());
  }

  private void sendByFcmToken(String token, String title, String body) {
    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();

    Message message = Message.builder()
        .setToken(token)
        .setNotification(notification)
        .build();

    String response;
    try {
      response = FirebaseMessaging.getInstance().send(message);
    } catch (FirebaseMessagingException e) {
      log.error("FCM message send Failed " + e);
      throw new CustomException(FCM_MESSAGE_SEND_ERROR, e);
    }
    log.info("FCM message send Success " + response);
  }

  private Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }
}
