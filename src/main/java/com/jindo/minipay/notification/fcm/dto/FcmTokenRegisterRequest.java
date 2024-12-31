package com.jindo.minipay.notification.fcm.dto;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.notification.fcm.entity.FcmToken;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRegisterRequest {

  @NotNull
  private Long memberId;

  @NotNull
  private String token;

  public FcmToken toEntity(Member member) {
    return FcmToken.builder()
        .member(member)
        .token(token)
        .build();
  }
}
